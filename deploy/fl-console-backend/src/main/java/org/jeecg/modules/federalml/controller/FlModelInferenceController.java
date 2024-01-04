package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.federalml.entity.*;
import org.jeecg.modules.federalml.enums.*;
import org.jeecg.modules.federalml.global.Dict;
import org.jeecg.modules.federalml.model.FlInference;
import org.jeecg.modules.federalml.model.FlInference4Create;
import org.jeecg.modules.federalml.model.FlInferenceNlp;
import org.jeecg.modules.federalml.service.*;
import org.jeecg.modules.federalml.service.impl.AsyncJobService;
import org.jeecg.modules.federalml.utils.CheckPermissionUtil;
import org.jeecg.modules.federalml.utils.HttpClientPool;
import org.jeecg.modules.federalml.vo.FlInferenceImageInfoVo;
import org.jeecg.modules.federalml.vo.FlInferenceImageResultVo;
import org.jeecg.modules.federalml.vo.FlInferenceInfoVo;
import org.jeecg.modules.federalml.vo.FlInferenceResultVo;
import org.jeecg.modules.system.util.SourceIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xkcoding.http.util.StringUtil;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.utils.StringUtils;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @Description: 模型推理服务
 * @Author: jeecg-boot
 * @Date: 2021-06-08
 * @Version: V1.0
 */
@Api(tags = "4.5 模型推理服务", hidden = true)
@RestController
@RequestMapping("/federalml/flModelInference")
@Slf4j
public class FlModelInferenceController extends JeecgController<FlJobInference, IFlJobInferenceService> {
    @Autowired
    private IFlJobInferenceService flJobInferenceService;
    @Autowired
    private IFlInferenceInfoService flInferenceInfoService;
    @Autowired
    private HttpClientPool httpClientPool;
    @Autowired
    private IFlPartyInfoService flPartyInfoService;
    @Autowired
    private IFlModelInfoService flModelInfoService;
    @Autowired
    private IFlOpLogService flOpLogService;
    @Autowired
    private AsyncJobService asyncJobService;
    @Autowired
    private IFlModelPrivateService flModelPrivateService;
    @Autowired
    private CheckPermissionUtil checkPermissionUtil;
    @Autowired
    private SourceIdUtil sourceIdUtil;
    @Autowired
    private IFlJobRecruitService flJobRecruitService;
    @Autowired
    private IFlJobRegistService flJobRegistService;
    @Autowired
    private IFlHeteroInferenceInfoService flHeteroInferenceInfoService;
    @Autowired
    private ICollectFileMetaService collectFileMetaService;

    public static final String QUICK_PUB_URL = "http://fl-serving.%s:8000/v1/model/quick_publish";
    public static final String INFERENCE_URL = "http://fl-serving.%s:8000/v1/federation/";
    public static final String INFERENCEZ_EXIST_URL = "http://fl-serving.%s:8000/v1/model/query";

    // public static final String QUICK_PUB_URL = "http://192.168.66.1:30008/v1/model/quick_publish";
    // public static final String INFERENCE_URL = "http://192.168.66.1:30008/v1/federation/";
    // public static final String QUICK_PUB_URL = "http://192.168.66.11:31547/v1/model/quick_publish";
    // public static final String INFERENCE_URL = "http://192.168.66.11:31547/v1/federation/";

    /**
     * 创建推理任务
     *
     * @param flInference4Create
     * @param req
     * @return
     */
    @AutoLog(value = "模型推理服务-创建推理任务", operateType = OPERATE_TYPE_2)
    @ApiOperation(value = "创建推理任务", notes = "创建推理任务")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @PostMapping(value = "/createJob")
    @SuppressWarnings({"PMD.MethodTooLongRule", "checkstyle:methodlength"})
    public Result<?> createJob(@RequestBody FlInference4Create flInference4Create, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        String id = flInference4Create.getId();
        String name = flInference4Create.getName();
        FlModelPrivate flModelPrivate = flModelPrivateService.getById(id);
        if (flModelPrivate == null) {
            return Result.error("模型不存在");
        }
        FlModelInfo flModelInfo = flModelInfoService.getById(flModelPrivate.getModelInfoId());
        if (flModelInfo == null) {
            return Result.error("模型不存在");
        }
        FlJobRecruit flJobRecruit = flJobRecruitService.getOne(
            new QueryWrapper<FlJobRecruit>().lambda().eq(FlJobRecruit::getJobId, flModelInfo.getModelVersion()));
        List<FlJobRegist> flJobRegistList = flJobRegistService
            .list(new QueryWrapper<FlJobRegist>().lambda().eq(FlJobRegist::getRecruitId, flJobRecruit.getId()));
        if (flJobRegistList == null || flJobRegistList.size() == 0) {
            return Result.error("未找到该模型的参与方信息");
        }
        if (!tenantId.equals(flModelPrivate.getPartyId())) {
            return Result.error("所属联邦方不一致");
        }
        String serviceId = id + "-" + RandomUtil.randomString(10);
        // 查询服务是否已存在
        List<FlJobInference> flJobInferenceList = flJobInferenceService.list(new QueryWrapper<FlJobInference>().lambda()
            .eq(FlJobInference::getPartyId, tenantId).eq(FlJobInference::getName, name));
        if (flJobInferenceList != null && flJobInferenceList.size() > 0) {
            return Result.error("服务名称已存在");
        }
        cn.hutool.json.JSONObject modelPubJson = JSONUtil.createObj();
        modelPubJson.putOnce("service_id", serviceId);
        modelPubJson.putOnce("role", flModelInfo.getModelRole());
        cn.hutool.json.JSONObject jobParamsJson = JSONUtil.createObj();
        jobParamsJson.putOnce("model_id", flModelInfo.getModelId());
        jobParamsJson.putOnce("model_version", flModelInfo.getModelVersion());
        modelPubJson.putOnce("job_parameters", jobParamsJson);
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(flModelInfo.getUserPartyId());
        String url = String.format(QUICK_PUB_URL, flPartyInfo.getNameEn());
        // 获取结果，保存到 FlJobInference 表中
        FlJobInference flJobInference = new FlJobInference();
        String inferenceUrl = String.format(INFERENCE_URL, flPartyInfo.getNameEn());
        flJobInference.setInferenceUrl(inferenceUrl);
        flJobInference.setModelInfoId(id);
        flJobInference.setPartyId(tenantId);
        flJobInference.setName(name);
        boolean serviceExist = false;
        // 模型表中是否已经存在推理服务id
        if (StringUtils.isNotEmpty(flModelInfo.getServiceId())) {
            flJobInference.setStatus("CREATING");
            flJobInference.setResourceId(sourceIdUtil.generateSourceId(ResourceIdPrefixEnum.JOB.name()));
            flJobInferenceService.save(flJobInference);
            // 调用python接口判断模型表中的推理服务是否还存在
            Map<String, Object> empMap = new HashMap<>(1);
            empMap.put("service_id", flModelInfo.getServiceId());
            String result = httpClientPool.postWithoutTimeout(
                String.format(INFERENCEZ_EXIST_URL, flPartyInfo.getNameEn()), JSON.toJSONString(empMap));
            cn.hutool.json.JSONObject resultJson = JSONUtil.parseObj(result);
            log.info("resultJson:==>" + result);
            // todo 推理任务失败的处理，非200且retcode非0的情况下 是失败，建议添加字段，保存失败原因
            String retCode = "retcode";
            if (resultJson.getInt(retCode) == 0) {
                cn.hutool.json.JSONObject resultJson1 = JSONUtil.parseObj(resultJson.get("data"));
                int total = resultJson1.getInt("total");
                if (total == 1) {
                    serviceExist = true;
                }
            }
        }
        if (serviceExist) {
            FlJobInference flJobInference1 = flJobInferenceService
                .getOne(new QueryWrapper<FlJobInference>().lambda().eq(FlJobInference::getPartyId, tenantId)
                    .eq(FlJobInference::getServiceId, flModelInfo.getServiceId()).last("limit 1"));
            if (flJobInference1 != null) {
                flJobInference.setModelVersion(flJobInference1.getModelVersion());
                flJobInference.setModelSpace(flJobInference1.getModelSpace());
                flJobInference.setRole(flJobInference1.getRole());
                flJobInference.setHeader(flJobInference1.getHeader());
                flJobInference.setPubTime(flJobInference1.getPubTime());
                flJobInference.setModelType(flJobInference1.getModelType());
                flJobInference.setStatus("CREATED");
                flJobInference.setServiceId(flModelInfo.getServiceId());
                flJobInference.setContent(flInference4Create.getContent());
                flJobInferenceService.updateById(flJobInference);
            }
        } else {
            flJobInference.setStatus("CREATING");
            flJobInferenceService.save(flJobInference);
            asyncJobService.createInference(url, modelPubJson, flJobInference.getId(), flModelInfo);
        }
        // 如果模型是纵向模型时，在fl_hetero_inference_info表中添加一条参与方记录
        if (flModelInfo.getFlType() != null && flModelInfo.getFlType() == FlTypeEnum.LONGITUDINAL.getCode()) {
            for (FlJobRegist flJobRegist : flJobRegistList) {
                if (tenantId.equals(String.valueOf(flJobRegist.getPartyId()))) {
                    continue;
                }
                FlHeteroInferenceInfo flHeteroInferenceInfo = new FlHeteroInferenceInfo();
                flHeteroInferenceInfo.setJobId(flJobInference.getId());
                flHeteroInferenceInfo.setUserName(flJobRegist.getCreateBy());
                flHeteroInferenceInfo.setPartyId(flJobRegist.getPartyId());
                flHeteroInferenceInfoService.save(flHeteroInferenceInfo);
            }
        }

        // --- log start ---
        // ${partyName}的${userName}使用xx模型创建了${FlModelInferenceControllerjobName}
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        FlOpLog flOpLog = new FlOpLog();
        JSONObject content1 = new JSONObject();
        content1.put("partyName", flPartyInfo.getName());
        content1.put("userName", sysUser.getUsername());
        content1.put("modelName", flModelInfo.getModelName());
        content1.put("jobName", name);
        flOpLog.setContent(content1.toJSONString());
        flOpLog.setPartyId(Integer.parseInt(tenantId));
        flOpLog.setUrl("/federalml/flJobInference/createJob");
        flOpLog.setParam(JSONUtil.toJsonStr(req.getParameterMap()));

        List<FlActivity> flActivityList = new ArrayList<>();

        // 任务
        FlActivity missionActivity = new FlActivity();
        missionActivity.setTypeId(flJobInference.getId());
        missionActivity.setType(Dict.MISSION_ACTICITY_TYPE);
        missionActivity.setOpType(Dict.MISSION_CREATE_TYPE);
        // 模型
        FlActivity modelActivity = new FlActivity();
        modelActivity.setTypeId(id);
        modelActivity.setType(Dict.MODEL_ACTICITY_TYPE);
        modelActivity.setOpType(Dict.MODEL_USE_TYPE);

        flActivityList.add(missionActivity);
        flActivityList.add(modelActivity);
        flOpLogService.saveMain(flOpLog, flActivityList);

        // --- log end ---

        return Result.OK("创建成功！", flJobInference.getId());
    }

    /**
     * 纵向推理任务-选择数据集
     *
     * @param jobId
     * @param dataId
     * @return
     */
    @ApiOperation(value = "纵向推理任务-选择数据集", notes = "纵向推理任务-选择数据集")
    @PostMapping(value = "/hetero")
    public Result<?> hetero(@RequestParam("jobId") String jobId, @RequestParam("dataId") String dataId,
        @RequestParam("inferenceRole") String inferenceRole) {
        // 参与方角色选择数据集时的逻辑处理，更新fl_hetero_inference_info表中的collect_file_meta_id字段
        if (TrainRoleEnum.host.toString().toUpperCase().equals(inferenceRole)) {
            FlHeteroInferenceInfo flHeteroInferenceInfo = flHeteroInferenceInfoService
                .getOne(new QueryWrapper<FlHeteroInferenceInfo>().lambda().eq(FlHeteroInferenceInfo::getJobId, jobId));
            if (flHeteroInferenceInfo == null) {
                return Result.error("未找到该任务");
            }
            flHeteroInferenceInfo.setCollectFileMetaId(dataId);
            flHeteroInferenceInfoService.updateById(flHeteroInferenceInfo);
            // 发起方角色选择数据集时的逻辑处理，更新fl_job_inference表中的data字段
        } else if (TrainRoleEnum.guest.toString().toUpperCase().equals(inferenceRole)) {
            FlJobInference flJobInference = flJobInferenceService.getById(jobId);
            if (flJobInference == null) {
                return Result.error("未找到该任务");
            }
            FlInferenceInfo flInferenceInfo = flInferenceInfoService
                .getOne(new QueryWrapper<FlInferenceInfo>().lambda().eq(FlInferenceInfo::getJobId, jobId));
            if (flInferenceInfo != null) {
                flInferenceInfo.setData("collectFileMetaId:" + dataId);
                flInferenceInfoService.updateById(flInferenceInfo);
            } else {
                flInferenceInfo = new FlInferenceInfo();
                flInferenceInfo.setJobId(jobId);
                flInferenceInfo.setData("collectFileMetaId:" + dataId);
                flInferenceInfoService.save(flInferenceInfo);
            }
        }
        return Result.OK("选择成功！");
    }

    /**
     * 查询纵向推理任务数据集选择情况
     *
     * @param jobId
     * @return
     */
    @ApiOperation(value = "查询纵向推理任务数据集选择情况", notes = "查询纵向推理任务数据集选择情况")
    @GetMapping(value = "/heteroDataList")
    public Result<?> heteroDataList(@RequestParam("jobId") String jobId) {
        FlJobInference flJobInference = flJobInferenceService.getById(jobId);
        if (flJobInference == null) {
            return Result.error("未找到该任务");
        }
        FlInferenceInfo flInferenceInfo = flInferenceInfoService
            .getOne(new QueryWrapper<FlInferenceInfo>().lambda().eq(FlInferenceInfo::getJobId, flJobInference.getId()));
        boolean guestFlag = false;
        // 返回参与方选择的数据集信息
        Map<String, Object> guestMap = new HashMap<>(3);
        if (flInferenceInfo != null && StringUtils.isNotEmpty(flInferenceInfo.getData())) {
            guestFlag = flInferenceInfo.getData().contains("collectFileMetaId:");
            CollectFileMeta collectFileMeta = collectFileMetaService.getOne(new QueryWrapper<CollectFileMeta>().lambda()
                .eq(CollectFileMeta::getId, flInferenceInfo.getData().split(":")[1]));
            if (collectFileMeta != null) {
                guestMap.put("dataId", collectFileMeta.getId());
                guestMap.put("dataName", collectFileMeta.getName());
            }
        }
        // 参与方是否已经选择数据集
        guestMap.put("dataFlag", guestFlag);

        // 返回发起方选择的数据集信息
        boolean hostFlag = false;
        Map<String, Object> hostMap = new HashMap<>(3);
        FlHeteroInferenceInfo flHeteroInferenceInfo = flHeteroInferenceInfoService
            .getOne(new QueryWrapper<FlHeteroInferenceInfo>().lambda().eq(FlHeteroInferenceInfo::getJobId, jobId));
        if (flHeteroInferenceInfo == null) {
            hostFlag = false;
        } else {
            hostFlag = StringUtils.isNotEmpty(flHeteroInferenceInfo.getCollectFileMetaId());
            CollectFileMeta collectFileMeta = collectFileMetaService.getOne(new QueryWrapper<CollectFileMeta>().lambda()
                .eq(CollectFileMeta::getId, flHeteroInferenceInfo.getCollectFileMetaId()));
            if (collectFileMeta != null) {
                hostMap.put("dataId", collectFileMeta.getId());
                hostMap.put("dataName", collectFileMeta.getName());
                hostFlag = true;
            }
        }
        // 发起方是否已经选择数据集
        hostMap.put("dataFlag", hostFlag);
        // 返回最终结果
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("guest", guestMap);
        resultMap.put("host", hostMap);
        return Result.OK(resultMap);
    }

    /**
     * 推理任务-开启推理任务
     *
     * @param flInference
     * @param req
     * @return
     */
    @ApiOperation(value = "csv推理任务-开启推理任务", notes = "开启推理任务")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @PostMapping(value = "/inference")
    public Result<?> inference(
        @RequestBody @ApiParam(name = "FlInference", value = "特征数据", required = true) FlInference flInference,
        HttpServletRequest req) {
        List<Object> featureDataList = flInference.getFeatureDataList();
        FlJobInference inference = flJobInferenceService.getById(flInference.getId());
        inference.setStatus("INFERENCEING");
        flJobInferenceService.updateById(inference);
        String header = inference.getHeader();
        if (flInference.getFeatureDataList() == null || flInference.getFeatureDataList().size() == 0) {
            return Result.error("参数错误！");
        }
        // 判断字段是否一致
        String[] headers = header.substring(1, header.length() - 2).replace("\"", "").split(",");
        log.info("featureDataList.get(0)======>" + featureDataList.get(0));
        Set<String> inputDataHeaders = ((HashMap<String, String>)featureDataList.get(0)).keySet();
        for (String x : headers) {
            if (!inputDataHeaders.contains(x)) {
                return Result.error("字段不一致！");
            }
        }
        FlInferenceResultVo flInferenceResultVo = new FlInferenceResultVo();
        flInferenceResultVo.setHeader(inference.getHeader());
        try {
            if (flInference.getFeatureDataList() != null && flInference.getFeatureDataList().size() > 1) {
                List<FlInferenceInfoVo> flInferenceInfoVoList =
                    flJobInferenceService.batchInference(flInference.getFeatureDataList(), inference);
                flInferenceResultVo.setFlInferenceInfoVoList(flInferenceInfoVoList);
            } else {
                FlInferenceInfoVo flInferenceInfoVo = flJobInferenceService
                    .inference(flInference.getFeatureDataList().get(0), inference, "tabular", null);
                flInferenceResultVo.setFlInferenceInfoVoList(new ArrayList<FlInferenceInfoVo>() {
                    {
                        this.add(flInferenceInfoVo);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("逻辑错误！");
        }
        return Result.OK(flInferenceResultVo);
    }

    /**
     * nlp推理任务-开启推理任务
     *
     * @param flInferenceNlp
     * @param req
     * @return
     */
    @ApiOperation(value = "nlp推理任务-开启推理任务", notes = "开启推理任务")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @PostMapping(value = "/inference_nlp")
    public Result<?> inferenceNlp(
        @RequestBody @ApiParam(name = "FlInferenceNlp", value = "特征数据", required = true) FlInferenceNlp flInferenceNlp,
        HttpServletRequest req) {
        log.info("进入nlp推理方法");
        FlJobInference inference = flJobInferenceService.getById(flInferenceNlp.getId());
        inference.setStatus("INFERENCEING");
        flJobInferenceService.updateById(inference);
        String header = inference.getHeader();
        if (StringUtils.isEmpty(flInferenceNlp.getContent())) {
            return Result.error("参数错误！");
        }
        FlInferenceResultVo flInferenceResultVo = new FlInferenceResultVo();
        flInferenceResultVo.setHeader(inference.getHeader());
        try {
            Map<String, String> map = new HashMap<>(1);
            map.put("text", flInferenceNlp.getContent());
            FlInferenceInfoVo flInferenceInfoVo = flJobInferenceService.inference(map, inference, "nlp", null);
            flInferenceResultVo.setFlInferenceInfoVoList(new ArrayList<FlInferenceInfoVo>() {
                {
                    this.add(flInferenceInfoVo);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("逻辑错误！");
        }
        return Result.OK(flInferenceResultVo);
    }

    /**
     * 推理服务调用
     *
     * @param file
     * @param id
     * @param req
     * @return
     */
    @ApiOperation(value = "开启图片推理任务", notes = "开启图片推理任务")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @PostMapping(value = "/inferenceImage")
    public Result<?> inferenceImage(@RequestParam("file") MultipartFile file, @RequestParam("id") String id,
        HttpServletRequest req) {
        FlJobInference inference = flJobInferenceService.getById(id);
        inference.setStatus("INFERENCEING");
        flJobInferenceService.updateById(inference);
        String header = inference.getHeader();
        FlInferenceImageResultVo flInferenceImageResultVo = new FlInferenceImageResultVo();
        try {
            Map<String, Object> featureData = this.getFeatureData(file, header, 0);
            FlInferenceInfoVo flInferenceInfoVo =
                flJobInferenceService.inference(featureData, inference, "vision", file.getOriginalFilename());
            List<FlInferenceImageInfoVo> empList = new ArrayList<>();
            FlInferenceImageInfoVo flInferenceImageInfoVo = new FlInferenceImageInfoVo();
            flInferenceImageInfoVo.setResult(flInferenceInfoVo.getResult());
            flInferenceImageInfoVo.setName(file.getOriginalFilename());
            flInferenceImageInfoVo.setBase64(featureData.get("base64").toString());
            empList.add(flInferenceImageInfoVo);
            flInferenceImageResultVo.setFlInferenceInfoVoList(empList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("逻辑错误！");
        }
        return Result.OK(flInferenceImageResultVo);
    }

    /**
     * 开启多图片推理任务
     *
     * @param files
     * @param id
     * @param req
     * @return
     */
    @ApiOperation(value = "开启多图片推理任务", notes = "开启多图片推理任务")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @PostMapping(value = "/inferenceImages")
    public Result<?> inferenceImages(@RequestParam("files") MultipartFile[] files, @RequestParam("id") String id,
        HttpServletRequest req) {

        FlJobInference inference = flJobInferenceService.getById(id);
        inference.setStatus("INFERENCEING");
        flJobInferenceService.updateById(inference);
        String header = inference.getHeader();
        if (files == null) {
            return Result.error("参数错误！");
        }

        FlInferenceImageResultVo flInferenceImageResultVo = new FlInferenceImageResultVo();
        try {
            List<Object> list = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                Map<String, Object> featureData = this.getFeatureData(files[i], header, 0);
                list.add(featureData);
                names.add(files[i].getOriginalFilename());
            }
            List<FlInferenceInfoVo> flInferenceInfoVoList =
                flJobInferenceService.batchInferenceImge(list, inference, "vision", names);
            List<FlInferenceImageInfoVo> empList = new ArrayList<>();
            for (int i = 0; i < flInferenceInfoVoList.size(); i++) {
                FlInferenceInfoVo flInferenceInfoVo = flInferenceInfoVoList.get(i);
                FlInferenceImageInfoVo flInferenceImageInfoVo = new FlInferenceImageInfoVo();
                flInferenceImageInfoVo.setResult(flInferenceInfoVo.getResult());
                flInferenceImageInfoVo.setName(names.get(i));
                Map<String, Object> featureData = (Map<String, Object>)list.get(i);
                flInferenceImageInfoVo.setBase64(featureData.get("base64").toString());
                empList.add(flInferenceImageInfoVo);
                flInferenceImageResultVo.setFlInferenceInfoVoList(empList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("逻辑错误！");
        }
        return Result.OK(flInferenceImageResultVo);
    }

    @ApiOperation(value = "通过数据集推理", notes = "通过数据集推理")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @PostMapping(value = "/inferenceByDataCollection")
    public Result<?> inferenceByDataCollection(@RequestParam("dataId") String dataId, @RequestParam("id") String id,
        HttpServletRequest req) {
        try {
            if (StringUtil.isEmpty(dataId) || StringUtil.isEmpty(id)) {
                return Result.error("参数错误！");
            }
            return flJobInferenceService.inferenceByDataCollection(dataId, id);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("逻辑错误！");
        }
    }

    @ApiOperation(value = "纵向推理", notes = "纵向推理")
    @PostMapping(value = "/heteroInference")
    public Result<?> heteroInference(@RequestParam("id") String id) {
        try {
            if (StringUtil.isEmpty(id)) {
                return Result.error("参数错误！");
            }
            return flJobInferenceService.heretoInference(id);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("逻辑错误！");
        }
    }

    private Map<String, Object> getFeatureData(MultipartFile file, String header, int i) {
        Map<String, Object> featureData = new HashMap<>(3);
        try {
            featureData.put("base64", this.transformPhotoToBase64Data(file.getInputStream()));
            int[] headItem = this.haveHeadItem(header);
            if (headItem != null) {
                featureData.put("PIL_mode", "RGB");
                featureData.put("size", headItem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return featureData;
    }

    public String transformPhotoToBase64Data(InputStream file) {
        // 获取Base64编码器
        Base64.Encoder encoder = Base64.getEncoder();
        // 数据集缓存器
        byte[] imgContainer = null;
        // 到指定路径寻找文件
        FileInputStream fileInputStream = (FileInputStream)file;
        try {
            // 设置图片字节数据缓冲区大小
            imgContainer = new byte[fileInputStream.available()];
            // 将数据流中的图片数据读进缓冲区
            fileInputStream.read(imgContainer);
            // 将图片编码转换成Base64格式的数据集
            String base64ImgData = encoder.encodeToString(imgContainer);
            // 关闭数据流
            fileInputStream.close();
            // 将缓冲区数据转换成字符数据返回
            return base64ImgData;
        } catch (FileNotFoundException e) {
            return "找不到指定文件!";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "null";
    }

    /**
     * 分页列表查询
     *
     * @param flJobInference
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "分页查询推理任务", notes = "分页查询推理任务")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FlJobInference flJobInference,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FlJobInference> queryWrapper =
            QueryGenerator.initQueryWrapper(flJobInference, req.getParameterMap());
        queryWrapper.lambda().like(FlJobInference::getName, flJobInference.getName());
        Page<FlJobInference> page = new Page<FlJobInference>(pageNo, pageSize);
        IPage<FlJobInference> pageList = flJobInferenceService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @ApiIgnore
    @ApiOperation(value = "根据报名id查询推理服务详情", notes = "根据报名id查询推理服务详情")
    @PostMapping(value = "/getInferenceByRegistId")
    public Result<FlJobInference>
        getInferenceByRegistId(@RequestParam @ApiParam(name = "id", value = "报名id", required = true) String id) {
        List<FlJobInference> flJobInferenceList =
            flJobInferenceService.list(new QueryWrapper<FlJobInference>().lambda().eq(FlJobInference::getRegistId, id));
        if (flJobInferenceList.size() >= 1) {
            return Result.OK(flJobInferenceList.get(0));
        } else {
            return Result.OK();
        }
    }

    /**
     * 编辑
     *
     * @param flJobInference
     * @return
     */
    @AutoLog(value = "模型推理服务-编辑", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "编辑推理任务", notes = "编辑推理任务")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FlJobInference flJobInference) {
        flJobInferenceService.updateById(flJobInference);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "模型推理服务-删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "删除模型推理任务", notes = "删除推理任务")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        FlJobInference flJobInference = flJobInferenceService.getById(id);
        boolean flag = checkPermissionUtil.checkPermission(req, tenantId, flJobInference.getCreateBy(),
            flJobInference.getPartyId());
        if (!flag) {
            return Result.error("没有权限");
        }
        if (!InferenceStatusEnum.INFERENCEING.toString().equals(flJobInference.getStatus())) {
            return Result.error("当前状态下的推理任务不允许删除！");
        }
        flJobInferenceService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "模型推理服务-批量删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "批量删除推理任务", notes = "批量删除推理任务")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flJobInferenceService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "查询推理任务详情", notes = "查询推理任务详情")
    @GetMapping(value = "/queryById")
    @SuppressWarnings({"PMD.MethodTooLongRule", "checkstyle:methodlength", "checkstyle:returncount"})
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        Map<String, Object> returnMap = new HashMap<>(3);
        FlJobInference flJobInference = flJobInferenceService.getById(id);
        if (flJobInference == null) {
            return Result.error("未找到对应数据");
        }
        returnMap.put("flJobInference", flJobInference);
        FlModelPrivate flModelPrivate = flModelPrivateService.getById(flJobInference.getModelInfoId());
        FlModelInfo flModelInfo = flModelInfoService.getById(flModelPrivate.getModelInfoId());
        String type = "";
        if (InferenceTypeEnum.vision.toString().equals(flModelInfo.getContentType())) {
            FlInferenceImageResultVo flInferenceImageResultVo = new FlInferenceImageResultVo();
            try {
                List<FlInferenceImageInfoVo> empList =
                    flJobInferenceService.getInferenceImageResult(flJobInference.getId());
                flInferenceImageResultVo.setFlInferenceInfoVoList(empList);
                if (empList != null && empList.size() > 0) {
                    if (empList.get(0).getResult() != null
                        && empList.get(0).getResult().contains(InferenceTypeEnum.csv.toString())) {
                        type = "visionCollection";
                    } else {
                        type = "vision";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("未知错误");
            }
            returnMap.put("type", type);
            returnMap.put("flInferenceResultVo", flInferenceImageResultVo);
        } else if (InferenceTypeEnum.yolo.toString().equals(flModelInfo.getContentType())) {
            FlInferenceImageResultVo flInferenceImageResultVo = new FlInferenceImageResultVo();
            try {
                List<FlInferenceImageInfoVo> empList =
                    flJobInferenceService.getInferenceImageResult(flJobInference.getId());
                flInferenceImageResultVo.setFlInferenceInfoVoList(empList);
                if (empList != null && empList.size() > 0) {
                    if (empList.get(0).getResult() != null
                        && empList.get(0).getResult().contains(InferenceTypeEnum.csv.toString())) {
                        type = "yoloCollection";
                    } else {
                        type = "yolo";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("未知错误");
            }
            returnMap.put("type", type);
            returnMap.put("flInferenceResultVo", flInferenceImageResultVo);
        } else if (InferenceTypeEnum.nlp.toString().equals(flModelInfo.getContentType())) {
            FlInferenceResultVo flInferenceResultVo = new FlInferenceResultVo();
            try {
                flInferenceResultVo.setHeader(flJobInference.getHeader());
                List<FlInferenceInfoVo> empList = flJobInferenceService.getInferenceResult(flJobInference.getId());
                flInferenceResultVo.setFlInferenceInfoVoList(empList);
                if (empList != null && empList.size() > 0) {
                    if (empList.get(0).getResult() != null
                        && empList.get(0).getResult().contains(InferenceTypeEnum.csv.toString())) {
                        type = "nlpCollection";
                    } else {
                        type = "nlp";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("未知错误");
            }
            returnMap.put("flInferenceResultVo", flInferenceResultVo);
            returnMap.put("type", type);
        } else {
            FlInferenceResultVo flInferenceResultVo = new FlInferenceResultVo();
            try {
                flInferenceResultVo.setHeader(flJobInference.getHeader());
                List<FlInferenceInfoVo> empList = flJobInferenceService.getInferenceResult(flJobInference.getId());
                flInferenceResultVo.setFlInferenceInfoVoList(empList);
                if (empList != null && empList.size() > 0) {
                    if (empList.get(0).getResult() != null
                        && empList.get(0).getResult().contains(InferenceTypeEnum.csv.toString())) {
                        type = "tabularCollection";
                    } else {
                        type = "tabular";
                    }
                }
                if (flModelInfo.getFlType() == FlTypeEnum.LONGITUDINAL.getCode()) {
                    type = "tabularCollection";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("未知错误");
            }
            returnMap.put("flInferenceResultVo", flInferenceResultVo);
            returnMap.put("type", type);
        }
        return Result.OK(returnMap);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param flJobInference
     */
    @ApiIgnore
    @AutoLog(value = "模型推理服务-导出", operateType = OPERATE_TYPE_6)
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FlJobInference flJobInference) {
        return super.exportXls(request, flJobInference, FlJobInference.class, "模型推理服务");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @ApiIgnore
    @AutoLog(value = "模型推理服务-导入", operateType = OPERATE_TYPE_5)
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FlJobInference.class);
    }

    private int[] haveHeadItem(String header) {
        JSONArray jsonArray = JSON.parseArray(header);
        if (jsonArray.size() != 0) {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            JSONArray jsonArray1 = jsonObject.getJSONArray("header_item");
            int[] returnValue = new int[2];
            returnValue[0] = Integer.valueOf(jsonArray1.get(0).toString());
            returnValue[1] = Integer.valueOf(jsonArray1.get(1).toString());
            return returnValue;
        }
        return null;
    }

}
