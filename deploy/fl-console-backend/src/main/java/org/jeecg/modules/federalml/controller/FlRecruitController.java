package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.federalml.entity.FlRecruit;
import org.jeecg.modules.federalml.entity.FlRegistration;
import org.jeecg.modules.federalml.entity.Jobsubmission;
import org.jeecg.modules.federalml.global.Dict;
import org.jeecg.modules.federalml.global.ErrorCode;
import org.jeecg.modules.federalml.service.IFlRecruitService;
import org.jeecg.modules.federalml.service.IFlRegistrationService;
import org.jeecg.modules.federalml.service.IJobsubmissionService;
import org.jeecg.modules.federalml.utils.CheckPermissionUtil;
import org.jeecg.modules.federalml.utils.HttpClientPool;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 联邦学习招募表
 * @Author: jeecg-boot
 * @Date: 2021-06-23
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/flRecruit")
@Slf4j
public class FlRecruitController extends JeecgController<FlRecruit, IFlRecruitService> {

    @Autowired
    private IFlRecruitService flRecruitService;

    @Autowired
    private IFlRegistrationService flRegistrationService;

    @Autowired
    private IJobsubmissionService jobsubmissionService;

    @Autowired
    private HttpClientPool httpClientPool;

    @Autowired
    private CheckPermissionUtil checkPermissionUtil;
    /*---------------------------------主表处理-begin-------------------------------------*/

    final String comma = ",";

    /**
     * 分页列表查询
     *
     * @param flRecruit
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "联邦学习招募-分页列表查询", notes = "联邦学习招募表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FlRecruit flRecruit,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FlRecruit> queryWrapper = QueryGenerator.initQueryWrapper(flRecruit, req.getParameterMap());
        Page<FlRecruit> page = new Page<FlRecruit>(pageNo, pageSize);
        IPage<FlRecruit> pageList = flRecruitService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param flRecruit
     * @return
     */
    @AutoLog(value = "联邦学习招募-添加", operateType = OPERATE_TYPE_2)
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FlRecruit flRecruit, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        flRecruit.setTenantId(tenantId);
        flRecruitService.save(flRecruit);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param flRecruit
     * @return
     */
    @AutoLog(value = "联邦学习招募-编辑", operateType = OPERATE_TYPE_3)
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FlRecruit flRecruit) {
        flRecruitService.updateById(flRecruit);
        return Result.OK("编辑成功!");
    }

    /**
     * 任务提交
     *
     * @param flRecruit
     * @return
     */
    @AutoLog(value = "联邦学习招募-任务提交")
    @PutMapping(value = "/submit")
    @SuppressWarnings({"PMD.MethodTooLongRule", "checkstyle:methodlength"})
    public Result<?> submit(@RequestBody FlRecruit flRecruit) {
        String tenantId = flRecruit.getTenantId();
        String conf = flRecruit.getConf();
        String guestDataset = flRecruit.getDatasets();

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

        // 从报名表获取到host方数据与party id
        List<FlRegistration> flRegistrationList =
            flRegistrationService.list(new QueryWrapper<FlRegistration>().eq("recruit_id", flRecruit.getId()));

        JSONObject numJsonObject = new JSONObject();
        for (int i = 0; i < flRegistrationList.size(); i++) {
            hostJsonArray.add(flRegistrationList.get(i).getTenantId());

            String dataset = flRegistrationList.get(i).getDataSet();
            String namespace = dataset.split(":")[0];
            String name = dataset.split(":")[1];

            JSONObject baseJsonObject = new JSONObject();
            baseJsonObject.put("name", name);
            baseJsonObject.put("namespace", namespace);

            JSONObject tableJsonObject = new JSONObject();
            tableJsonObject.put("table", baseJsonObject);

            JSONObject readerJsonObject = new JSONObject();
            readerJsonObject.put("reader_0", tableJsonObject);

            numJsonObject.put(String.valueOf(i), readerJsonObject);
        }
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
        jobSubmissionJson.put("job_dsl", JSON.parseObject(flRecruit.getDsl()));
        jobSubmissionJson.put("job_runtime_conf", confJsonObject);

        String fateUrl = "http://192.168.182.121:9380";
        String result;
        try {
            // TODO 将联邦学习任务下发改为异步调用
            result = httpClientPool.post(fateUrl + Dict.URL_JOB_SUBMIT, jobSubmissionJson);
            log.info(result);
            JSONObject resultJson = JSONObject.parseObject(result);
            int retcode = resultJson.getIntValue("retcode");
            Jobsubmission jobsubmission = new Jobsubmission();
            if (retcode == 0) {
                String jobId = resultJson.getString("jobId");
                // 取party id
                jobsubmission.setDsl(flRecruit.getDsl());
                jobsubmission.setConf(confJsonObject.toJSONString());
                jobsubmission.setJobId(jobId);
                jobsubmissionService.save(jobsubmission);
            } else {
                log.error(resultJson.getString("data"));
                return Result.ok(ErrorCode.FATEFLOW_ERROR_CONNECTION);
            }

        } catch (Exception e) {
            log.error("connect fateflow error:", e);
            return Result.ok(ErrorCode.FATEFLOW_ERROR_CONNECTION);
        }

        return Result.OK("提交成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦学习招募-删除", operateType = OPERATE_TYPE_4)
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        FlRecruit flRecruit = flRecruitService.getById(id);
        boolean flag =
            checkPermissionUtil.checkPermission(req, tenantId, flRecruit.getCreateBy(), flRecruit.getTenantId());
        if (!flag) {
            return Result.error("没有权限");
        }
        flRecruitService.delMain(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "联邦学习招募-批量删除", operateType = OPERATE_TYPE_4)
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flRecruitService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 导出
     *
     * @return
     */
    @AutoLog(value = "联邦学习招募-导出", operateType = OPERATE_TYPE_6)
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FlRecruit flRecruit) {
        return super.exportXls(request, flRecruit, FlRecruit.class, "联邦学习招募表");
    }

    /**
     * 导入
     *
     * @return
     */
    @AutoLog(value = "联邦学习招募-导入", operateType = OPERATE_TYPE_5)
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FlRecruit.class);
    }
    /*---------------------------------主表处理-end-------------------------------------*/

    /*--------------------------------子表处理-联邦学习报名表-begin----------------------------------------------*/

    /**
     * 通过主表ID查询
     *
     * @return
     */
    @GetMapping(value = "/listFlRegistrationByMainId")
    public Result<?> listFlRegistrationByMainId(FlRegistration flRegistration,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FlRegistration> queryWrapper =
            QueryGenerator.initQueryWrapper(flRegistration, req.getParameterMap());
        Page<FlRegistration> page = new Page<FlRegistration>(pageNo, pageSize);
        IPage<FlRegistration> pageList = flRegistrationService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param flRegistration
     * @return
     */
    @AutoLog(value = "联邦学习报名-添加", operateType = OPERATE_TYPE_2)
    @PostMapping(value = "/addFlRegistration")
    public Result<?> addFlRegistration(@RequestBody FlRegistration flRegistration, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        flRegistration.setTenantId(tenantId);
        flRegistrationService.save(flRegistration);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param flRegistration
     * @return
     */
    @AutoLog(value = "联邦学习报名-编辑", operateType = OPERATE_TYPE_3)
    @PutMapping(value = "/editFlRegistration")
    public Result<?> editFlRegistration(@RequestBody FlRegistration flRegistration) {
        flRegistrationService.updateById(flRegistration);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦学习报名-删除", operateType = OPERATE_TYPE_3)
    @DeleteMapping(value = "/deleteFlRegistration")
    public Result<?> deleteFlRegistration(@RequestParam(name = "id", required = true) String id) {
        flRegistrationService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "联邦学习报名-批量删除", operateType = OPERATE_TYPE_3)
    @DeleteMapping(value = "/deleteBatchFlRegistration")
    public Result<?> deleteBatchFlRegistration(@RequestParam(name = "ids", required = true) String ids) {
        this.flRegistrationService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 导出
     *
     * @return
     */
    @AutoLog(value = "联邦学习报名-导出", operateType = OPERATE_TYPE_6)
    @RequestMapping(value = "/exportFlRegistration")
    public ModelAndView exportFlRegistration(HttpServletRequest request, FlRegistration flRegistration) {
        // Step.1 组装查询条件
        QueryWrapper<FlRegistration> queryWrapper =
            QueryGenerator.initQueryWrapper(flRegistration, request.getParameterMap());
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

        // Step.2 获取导出数据
        List<FlRegistration> pageList = flRegistrationService.list(queryWrapper);
        List<FlRegistration> exportList = null;

        // 过滤选中数据
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            exportList =
                pageList.stream().filter(item -> selectionList.contains(item.getId())).collect(Collectors.toList());
        } else {
            exportList = pageList;
        }

        // Step.3 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        // 此处设置的filename无效 ,前端会重更新设置一下
        mv.addObject(NormalExcelConstants.FILE_NAME, "联邦学习报名表");
        mv.addObject(NormalExcelConstants.CLASS, FlRegistration.class);
        mv.addObject(NormalExcelConstants.PARAMS,
            new ExportParams("联邦学习报名表报表", "导出人:" + sysUser.getRealname(), "联邦学习报名表"));
        mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
        return mv;
    }

    /**
     * 导入
     *
     * @return
     */
    @AutoLog(value = "联邦学习报名-导入", operateType = OPERATE_TYPE_5)
    @RequestMapping(value = "/importFlRegistration/{mainId}")
    public Result<?> importFlRegistration(HttpServletRequest request, HttpServletResponse response,
        @PathVariable("mainId") String mainId) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<FlRegistration> list =
                    ExcelImportUtil.importExcel(file.getInputStream(), FlRegistration.class, params);
                for (FlRegistration temp : list) {
                    temp.setRecruitId(mainId);
                }
                long start = System.currentTimeMillis();
                flRegistrationService.saveBatch(list);
                log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");
                return Result.OK("文件导入成功！数据行数：" + list.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("文件导入失败:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("文件导入失败！");
    }

    /*--------------------------------子表处理-联邦学习报名表-end----------------------------------------------*/

}
