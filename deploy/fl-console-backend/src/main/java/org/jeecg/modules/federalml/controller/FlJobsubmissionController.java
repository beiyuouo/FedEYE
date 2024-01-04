package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.FmlParty;
import org.jeecg.modules.federalml.entity.Jobsubmission;
import org.jeecg.modules.federalml.global.Dict;
import org.jeecg.modules.federalml.global.ErrorCode;
import org.jeecg.modules.federalml.service.IFmlPartyService;
import org.jeecg.modules.federalml.service.IJobsubmissionService;
import org.jeecg.modules.federalml.utils.HttpClientPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.json.JSONUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 联邦任务发起
 * @Author: jeecg-boot
 * @Date: 2020-12-30
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/jobsubmission")
@Slf4j
public class FlJobsubmissionController extends JeecgController<Jobsubmission, IJobsubmissionService> {
    @Autowired
    private IJobsubmissionService jobsubmissionService;
    @Autowired
    private IFmlPartyService fmlPartyService;
    @Autowired
    private HttpClientPool httpClientPool;

    /**
     * 分页列表查询
     *
     * @param jobsubmission
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "联邦任务发起-分页列表查询", notes = "联邦任务发起-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(Jobsubmission jobsubmission,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<Jobsubmission> queryWrapper =
            QueryGenerator.initQueryWrapper(jobsubmission, req.getParameterMap());
        Page<Jobsubmission> page = new Page<Jobsubmission>(pageNo, pageSize);
        IPage<Jobsubmission> pageList = jobsubmissionService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param jobsubmission
     * @return
     */
    @AutoLog(value = "联邦任务发起-添加", operateType = OPERATE_TYPE_2)
    @ApiOperation(value = "联邦任务发起-添加", notes = "联邦任务发起-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody Jobsubmission jobsubmission) {
        cn.hutool.json.JSONObject jobSubmissionJson = JSONUtil.createObj();
        jobSubmissionJson.putOnce("job_dsl", JSON.parseObject(jobsubmission.getDsl()));
        JSONObject conf = JSON.parseObject(jobsubmission.getConf());
        jobSubmissionJson.putOnce("job_runtime_conf", conf);

        // fetch guest party id, and based on it fetch guest party name i.e. namespace
        Integer partyId = conf.getJSONObject("initiator").getInteger("party_id");
        jobsubmission.setPartyId(partyId.toString());
        FmlParty party = fmlPartyService.getOne(new QueryWrapper<FmlParty>().eq("party_id", partyId));

        String fateUrl = String.format("http://%s.%s:%d", "fateflow-client", party.getPartyName().trim(), 9380);
        String result;
        try {
            // TODO 将联邦学习任务下发改为异步调用
            result = httpClientPool.post(fateUrl + Dict.URL_JOB_SUBMIT, jobSubmissionJson);
            cn.hutool.json.JSONObject resultJson = JSONUtil.parseObj(result);
            int retcode = resultJson.getInt("retcode");
            if (retcode == 0) {
                String jobId = resultJson.getStr("jobId");
                jobsubmission.setJobId(jobId);
                jobsubmissionService.save(jobsubmission);
            } else {
                log.error(resultJson.getStr("data"));
                return Result.ok(ErrorCode.FATEFLOW_ERROR_CONNECTION);
            }

        } catch (Exception e) {
            log.error("connect fateflow error:", e);
            return Result.ok(ErrorCode.FATEFLOW_ERROR_CONNECTION);
        }
        return Result.ok("添加成功！" + result);
    }

    /**
     * 编辑
     *
     * @param jobsubmission
     * @return
     */
    @AutoLog(value = "联邦任务发起-编辑", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "联邦任务发起-编辑", notes = "联邦任务发起-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody Jobsubmission jobsubmission) {
        jobsubmissionService.updateById(jobsubmission);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦任务发起-删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦任务发起-通过id删除", notes = "联邦任务发起-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        jobsubmissionService.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "联邦任务发起-批量删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦任务发起-批量删除", notes = "联邦任务发起-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.jobsubmissionService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "联邦任务发起-通过id查询", notes = "联邦任务发起-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        Jobsubmission jobsubmission = jobsubmissionService.getById(id);
        if (jobsubmission == null) {
            return Result.error("未找到对应数据");
        }
        return Result.ok(jobsubmission);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param jobsubmission
     */
    @AutoLog(value = "联邦任务发起-导出", operateType = OPERATE_TYPE_6)
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Jobsubmission jobsubmission) {
        return super.exportXls(request, jobsubmission, Jobsubmission.class, "联邦任务发起");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @AutoLog(value = "联邦任务发起-导入", operateType = OPERATE_TYPE_5)
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Jobsubmission.class);
    }

}
