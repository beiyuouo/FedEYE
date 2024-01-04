package org.jeecg.modules.federalml.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.FlJobManage;
import org.jeecg.modules.federalml.global.Dict;
import org.jeecg.modules.federalml.global.ErrorCode;
import org.jeecg.modules.federalml.service.IFlJobManageService;
import org.jeecg.modules.federalml.utils.HttpClientPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 任务管理
 * @Author: jeecg-boot
 * @Date: 2021-06-24
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/flJobManage")
@Slf4j
public class FlJobManageController extends JeecgController<FlJobManage, IFlJobManageService> {
    @Autowired
    private IFlJobManageService flJobManageService;
    @Autowired
    private HttpClientPool httpClientPool;

    /**
     * 分页列表查询
     *
     * @param flJobManage
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "任务管理-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FlJobManage flJobManage,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FlJobManage> queryWrapper = QueryGenerator.initQueryWrapper(flJobManage, req.getParameterMap());
        Page<FlJobManage> page = new Page<FlJobManage>(pageNo, pageSize);
        IPage<FlJobManage> pageList = flJobManageService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param flJobManage
     * @return
     */
    @AutoLog(value = "任务管理-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FlJobManage flJobManage, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        String conf = flJobManage.getConf();
        String dsl = flJobManage.getDsl();
        String guestDataset = flJobManage.getGuestDataset();
        JSONObject initiatorJsonObject = new JSONObject();
        initiatorJsonObject.put("role", "guest");
        initiatorJsonObject.put("party_id", tenantId);
        JSONObject roleIdJsonObject = new JSONObject();
        JSONArray guestJsonArray = new JSONArray();
        guestJsonArray.add(tenantId);
        JSONArray arbiterJsonArray = new JSONArray();
        arbiterJsonArray.add(tenantId);
        JSONArray hostJsonArray = new JSONArray();
        JSONObject confJsonObject = JSONObject.parseObject(conf);
        JSONObject cpJsonObject = confJsonObject.getJSONObject("component_parameters");
        String guestNamespace = guestDataset.split(":")[0];
        String guestName = guestDataset.split(":")[1];
        JSONObject guestBaseJsonObject = new JSONObject();
        guestBaseJsonObject.put("name", guestName);
        guestBaseJsonObject.put("namespace", guestNamespace);
        JSONObject guestTableJsonObject = new JSONObject();
        guestTableJsonObject.put("table", guestBaseJsonObject);
        JSONObject guestReaderJsonObject = new JSONObject();
        guestReaderJsonObject.put("reader_0", guestTableJsonObject);
        JSONObject guestNumJsonObject = new JSONObject();
        guestNumJsonObject.put("0", guestReaderJsonObject);
        JSONObject roleJsonObject = new JSONObject();
        roleJsonObject.put("guest", guestNumJsonObject);
        JSONObject numJsonObject = new JSONObject();
        hostJsonArray.add(flJobManage.getHosts());
        String dataset = flJobManage.getHostDataset();
        String namespace = dataset.split(":")[0];
        String name = dataset.split(":")[1];
        JSONObject baseJsonObject = new JSONObject();
        baseJsonObject.put("name", name);
        baseJsonObject.put("namespace", namespace);
        JSONObject tableJsonObject = new JSONObject();
        tableJsonObject.put("table", baseJsonObject);
        JSONObject readerJsonObject = new JSONObject();
        readerJsonObject.put("reader_0", tableJsonObject);
        numJsonObject.put(String.valueOf(0), readerJsonObject);
        roleJsonObject.put("host", numJsonObject);
        cpJsonObject.put("role", roleJsonObject);
        roleIdJsonObject.put("guest", guestJsonArray);
        roleIdJsonObject.put("host", hostJsonArray);
        roleIdJsonObject.put("arbiter", arbiterJsonArray);
        confJsonObject.put("role", roleIdJsonObject);
        confJsonObject.put("initiator", initiatorJsonObject);
        confJsonObject.put("dsl_version", 2);
        confJsonObject
            .put("job_parameters", JSON.parseObject("{'common':{'job_type':'train', 'backend':0, 'work_mode':1}}"));
        JSONObject jobSubmissionJson = new JSONObject();
        jobSubmissionJson.put("job_dsl", JSON.parseObject(dsl));
        jobSubmissionJson.put("job_runtime_conf", confJsonObject);
        String fateUrl = "http://192.168.182.121:9380";
        String result;
        try {
            result = httpClientPool.post(fateUrl + Dict.URL_JOB_SUBMIT, jobSubmissionJson);
            log.info(result);
            JSONObject resultJson = JSONObject.parseObject(result);
            int retcode = resultJson.getIntValue("retcode");
            if (retcode == 0) {
                String jobId = resultJson.getString("jobId");
                // 取party id
                flJobManage.setDsl(dsl);
                flJobManage.setConf(confJsonObject.toJSONString());
                flJobManage.setJobId(jobId);
                flJobManage.setBoardUrl(resultJson.getJSONObject("data").getString("board_url"));
                flJobManageService.save(flJobManage);
            } else {
                log.error(resultJson.getString("data"));
                return Result.error(resultJson.getString("data"));
            }
        } catch (Exception e) {
            log.error("connect fateflow error:", e);
            return Result.ok(ErrorCode.FATEFLOW_ERROR_CONNECTION);
        }
        return Result.OK("提交成功!");
    }

    /**
     * 编辑
     *
     * @param flJobManage
     * @return
     */
    @AutoLog(value = "任务管理-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FlJobManage flJobManage) {
        flJobManageService.updateById(flJobManage);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "任务管理-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        flJobManageService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "任务管理-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flJobManageService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "任务管理-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        FlJobManage flJobManage = flJobManageService.getById(id);
        if (flJobManage == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(flJobManage);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param flJobManage
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FlJobManage flJobManage) {
        return super.exportXls(request, flJobManage, FlJobManage.class, "任务管理");
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
        return super.importExcel(request, response, FlJobManage.class);
    }

}
