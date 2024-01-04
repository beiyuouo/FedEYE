package org.jeecg.modules.federalml.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.FlJobInference;
import org.jeecg.modules.federalml.entity.FmlParty;
import org.jeecg.modules.federalml.entity.TmachineLearningModelInfo;
import org.jeecg.modules.federalml.global.ErrorCode;
import org.jeecg.modules.federalml.service.IFlJobInferenceService;
import org.jeecg.modules.federalml.service.IFmlPartyService;
import org.jeecg.modules.federalml.service.ItMachineLearningModelInfoService;
import org.jeecg.modules.federalml.utils.HttpClientPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import io.dapr.client.domain.CloudEvent;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: t_machine_learning_model_info
 * @Author: jeecg-boot
 * @Date: 2021-06-02
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/tMachineLearningModelInfo")
@Slf4j
public class TmachineLearningModelInfoController
    extends JeecgController<TmachineLearningModelInfo, ItMachineLearningModelInfoService> {
    @Autowired
    private ItMachineLearningModelInfoService tMachineLearningModelInfoService;
    @Autowired
    private HttpClientPool httpClientPool;
    @Autowired
    private IFmlPartyService fmlPartyService;
    @Autowired
    private IFlJobInferenceService flJobInferenceService;

    public static final String QUICK_PUB_URL = "http://fl-serving.%s:8000/v1/model/quick_publish";
    public static final String INFERENCE_URL = "http://fl-serving.%s:8000/v1/federation/inference";

    /**
     * 分页列表查询
     *
     * @param tMachineLearningModelInfo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<?> queryPageList(TmachineLearningModelInfo tMachineLearningModelInfo,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {

        Map<String, String[]> params = new HashMap<>(req.getParameterMap());
        params.replace("column", new String[] {"f_create_time"});
        QueryWrapper<TmachineLearningModelInfo> queryWrapper =
            QueryGenerator.initQueryWrapper(tMachineLearningModelInfo, params);
        // 仅返回 f_parent 为 1，即父级模型
        queryWrapper = queryWrapper.eq("f_parent", 1);
        Page<TmachineLearningModelInfo> page = new Page<TmachineLearningModelInfo>(pageNo, pageSize);
        IPage<TmachineLearningModelInfo> pageList = tMachineLearningModelInfoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @ApiOperation(value = "我的模型", notes = "我训练的模型，不包括下载的共享模型")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @GetMapping(value = "/listCurrent")
    public Result<IPage<TmachineLearningModelInfo>> listCurrent(
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");

        QueryWrapper<TmachineLearningModelInfo> queryWrapper = new QueryWrapper<>();
        // 仅返回 f_parent 为 1，即父级模型
        queryWrapper = queryWrapper.eq("f_parent", 1).eq("f_party_id", tenantId).eq("f_role", "guest");

        Page<TmachineLearningModelInfo> page = new Page<TmachineLearningModelInfo>(pageNo, pageSize);
        IPage<TmachineLearningModelInfo> pageList = tMachineLearningModelInfoService.page(page, queryWrapper);

        return Result.OK(pageList);
    }

    /**
     * 接收并更新边缘端发送的已训练模型信息
     *
     * @param body
     * @return
     */
    @ApiOperation(value = "已训练模型管理-已训练模型信息更新", notes = "已训练模型管理-已训练模型信息更新")
    @PostMapping(value = "/upinsert")
    public Result<?> upinsert(@RequestBody(required = false) byte[] body) {
        try {
            CloudEvent event = CloudEvent.deserialize(body);
            String jsonStr = (String)event.getData();

            JSONArray array = JSONUtil.parseArray(jsonStr);
            List<TmachineLearningModelInfo> models = JSONUtil.toList(array, TmachineLearningModelInfo.class);

            for (TmachineLearningModelInfo model : models) {
                UpdateWrapper<TmachineLearningModelInfo> updateWrapper = new UpdateWrapper<TmachineLearningModelInfo>()
                    .eq("f_party_id", model.getFPartyId()).eq("f_role", model.getFRole())
                    .eq("f_model_id", model.getFModelId()).eq("f_model_version", model.getFModelVersion());

                tMachineLearningModelInfoService.saveOrUpdate(model, updateWrapper);

                log.info("upinsert model success: " + model);
            }
        } catch (IOException e) {
            log.error("upinsert model faild: " + e.toString());
            throw new RuntimeException(e);
        }
        return Result.OK("添加成功！");
    }

    /**
     * 模型快速发布
     *
     * @param tMachineLearningModelInfo
     * @return
     */
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody TmachineLearningModelInfo tMachineLearningModelInfo) {
        cn.hutool.json.JSONObject modelPubJson = JSONUtil.createObj();
        // TODO 改为用户手动输入
        String serviceId = tMachineLearningModelInfo.getFJobId() + "-" + RandomUtil.randomString(10);
        modelPubJson.putOnce("service_id", serviceId);

        cn.hutool.json.JSONObject jobParamsJson = JSONUtil.createObj();
        jobParamsJson.putOnce("model_id", tMachineLearningModelInfo.getFModelId());
        jobParamsJson.putOnce("model_version", tMachineLearningModelInfo.getFModelVersion());

        modelPubJson.putOnce("job_parameters", jobParamsJson);

        String partyId = tMachineLearningModelInfo.getFPartyId();
        FmlParty party = fmlPartyService.getOne(new QueryWrapper<FmlParty>().eq("party_id", partyId));

        String result;

        try {
            // TODO 改为异步调用
            result = httpClientPool.post(String.format(QUICK_PUB_URL, party.getPartyName()), modelPubJson);
            // 获取结果，保存到 FmlModelInference 表中
            FlJobInference flJobInference = JSONUtil.parseObj(result).get("data", FlJobInference.class);

            String inferenceUrl = String.format(INFERENCE_URL, party.getPartyName());
            flJobInference.setInferenceUrl(inferenceUrl);

            flJobInferenceService.save(flJobInference);

        } catch (Exception e) {
            log.error("connect fl-serving error:", e);
            return Result.ok(ErrorCode.FATEFLOW_ERROR_CONNECTION);
        }
        return Result.ok("发布成功！" + result);

    }

    /**
     * 编辑
     *
     * @param tMachineLearningModelInfo
     * @return
     */
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody TmachineLearningModelInfo tMachineLearningModelInfo) {

        cn.hutool.json.JSONObject modelPubJson = JSONUtil.createObj();

        // TODO 改为用户手动输入
        String serviceId = tMachineLearningModelInfo.getFJobId() + "-" + RandomUtil.randomString(10);
        modelPubJson.putOnce("service_id", serviceId);

        cn.hutool.json.JSONObject jobParamsJson = JSONUtil.createObj();
        jobParamsJson.putOnce("model_id", tMachineLearningModelInfo.getFModelId());
        jobParamsJson.putOnce("model_version", tMachineLearningModelInfo.getFModelVersion());

        modelPubJson.putOnce("job_parameters", jobParamsJson);

        String partyId = tMachineLearningModelInfo.getFPartyId();
        FmlParty party = fmlPartyService.getOne(new QueryWrapper<FmlParty>().eq("party_id", partyId));

        String result;

        try {
            // TODO 改为异步调用
            result = httpClientPool.post(String.format(QUICK_PUB_URL, party.getPartyName()), modelPubJson);
            // 获取结果，保存到 FmlModelInference 表中
            FlJobInference flJobInference = JSONUtil.parseObj(result).get("data", FlJobInference.class);

            String inferenceUrl =
                String.format("http://fl-serving.%s:8000/v1/federation/inference", party.getPartyName());
            flJobInference.setInferenceUrl(inferenceUrl);

            flJobInferenceService.save(flJobInference);

        } catch (Exception e) {
            log.error("connect fl-serving error:", e);
            return Result.ok(ErrorCode.FATEFLOW_ERROR_CONNECTION);
        }
        return Result.ok("发布成功！" + result);
    }

    /**
     * 模型快速发布
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {

        TmachineLearningModelInfo tMachineLearningModelInfo = tMachineLearningModelInfoService.getById(id);

        cn.hutool.json.JSONObject modelPubJson = JSONUtil.createObj();

        // TODO 改为用户手动输入
        String serviceId = tMachineLearningModelInfo.getFJobId() + "-" + RandomUtil.randomString(10);
        modelPubJson.putOnce("service_id", serviceId);

        cn.hutool.json.JSONObject jobParamsJson = JSONUtil.createObj();
        jobParamsJson.putOnce("model_id", tMachineLearningModelInfo.getFModelId());
        jobParamsJson.putOnce("model_version", tMachineLearningModelInfo.getFModelVersion());

        modelPubJson.putOnce("job_parameters", jobParamsJson);

        String partyId = tMachineLearningModelInfo.getFPartyId();
        FmlParty party = fmlPartyService.getOne(new QueryWrapper<FmlParty>().eq("party_id", partyId));

        String result;

        try {
            // TODO 改为异步调用
            result = httpClientPool.post(String.format(QUICK_PUB_URL, party.getPartyName()), modelPubJson);
        } catch (Exception e) {
            log.error("connect fl-serving error:", e);
            return Result.ok(ErrorCode.FATEFLOW_ERROR_CONNECTION);
        }
        return Result.ok("发布成功！" + result);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.tMachineLearningModelInfoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        TmachineLearningModelInfo tMachineLearningModelInfo = tMachineLearningModelInfoService.getById(id);
        if (tMachineLearningModelInfo == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(tMachineLearningModelInfo);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param tMachineLearningModelInfo
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, TmachineLearningModelInfo tMachineLearningModelInfo) {
        return super.exportXls(request, tMachineLearningModelInfo, TmachineLearningModelInfo.class,
            "t_machine_learning_model_info");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, TmachineLearningModelInfo.class);
    }

}
