package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.federalml.entity.*;
import org.jeecg.modules.federalml.enums.ModelDownloadStatusEnum;
import org.jeecg.modules.federalml.enums.TrainRoleEnum;
import org.jeecg.modules.federalml.service.*;
import org.jeecg.modules.federalml.service.impl.AsyncJobService;
import org.jeecg.modules.federalml.utils.CheckPermissionUtil;
import org.jeecg.modules.federalml.utils.HttpClientPool;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 模型记录表
 * @Author: jeecg-boot
 * @Date: 2021-11-15
 * @Version: V1.0
 */
@Api(tags = "4.4 模型记录表")
@RestController
@RequestMapping("/federalml/flModelInfo")
@Slf4j
public class FlModelInfoController extends JeecgController<FlModelInfo, IFlModelInfoService> {
    @Autowired
    private IFlModelInfoService flModelInfoService;
    @Autowired
    private IFlPartyInfoService flPartyInfoService;
    @Autowired
    private ItJobService tJobService;
    @Autowired
    private IFlJobRecruitService flJobRecruitService;
    @Autowired
    private IFlJobRegistService flJobRegistService;
    @Autowired
    private ItMachineLearningModelInfoService tMachineLearningModelInfoService;
    @Autowired
    private ICollectFileMetaService collectFileMetaService;
    @Autowired
    private AsyncJobService asyncJobService;
    @Autowired
    private IFlJobInferenceService inferenceService;
    @Autowired
    private IFlModelPrivateService flModelPrivateService;
    @Autowired
    private IFlModelPublicService flModelPublicService;
    @Autowired
    private ISysUserRoleService sysUserRoleService;
    @Autowired
    private IFlModelParameterService modelParameterService;
    @Autowired
    private CheckPermissionUtil checkPermissionUtil;
    @Autowired
    private HttpClientPool httpClientPool;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IAlgorithmService algorithmService;

    public static final String STORE_URL = "http://fateflow-client.%s:9380/v1/model/store";
    public static final String RESTORE_URL = "http://fateflow-client.%s:9380/v1/model/restore";
    public static final String MIGRATE_URL = "http://fateflow-client.%s:9380/v1/model/migrate";
    public static final String PUSH_URL = "http://fateflow-client.%s:9380/v1/model/homo/save_model_for_label_pl";
    public static final String MODEL_DOWNLOAD_URL = "http://fateflow-client.%s:9380/v1/model/homo/model_download";

    @ApiOperation(value = "我的模型", notes = "我的模型，包括我训练的和我下载的")
    @GetMapping(value = "/listMyModel")
    public Result<IPage<FlModelInfoModel>> listMyModel(
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) @ApiParam(name = "name", value = "模型名称", required = false) String name,
        HttpServletRequest req) {
        String partyId = req.getHeader("tenant_id");
        String userName = JwtUtil.getUserNameByToken(req);
        boolean isParyUser = sysUserRoleService.isPartyUser(userName, partyId);
        if (!isParyUser) {
            userName = null;
        }
        Page<FlModelInfoModel> page = new Page<>(pageNo, pageSize);
        IPage<FlModelInfoModel> pageList = flModelInfoService.listMyModel(page, name, partyId, userName);
        return Result.OK(pageList);
    }

    /**
     * 模型池，权限为2的
     *
     * @param pageNo
     * @param pageSize
     * @param name
     * @param req
     * @return
     */
    @ApiOperation(value = "模型池", notes = "模型池，公开的模型")
    @GetMapping(value = "/listPoolModel")
    public Result<IPage<FlModelInfoModel>> listPoolModel(
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) @ApiParam(name = "name", value = "模型名称", required = false) String name,
        HttpServletRequest req) {
        String partyId = req.getHeader("tenant_id");
        Page<FlModelInfoModel> page = new Page<>(pageNo, pageSize);
        IPage<FlModelInfoModel> pageList = flModelInfoService.listPoolModel(page, name, partyId);
        return Result.OK(pageList);
    }

    @ApiOperation(value = "模型详情", notes = "模型详情")
    @GetMapping(value = "/modelDetail")
    @SuppressWarnings({"PMD.MethodTooLongRule", "checkstyle:methodlength"})
    public Result<?> modelDetail(@RequestParam @ApiParam(name = "id", value = "模型id", required = true) String id,
        HttpServletRequest req) {
        String partyId = req.getHeader("tenant_id");
        FlModelPrivate flModelPrivate = flModelPrivateService.getById(id);
        FlModelPublic flModelPublic = flModelPublicService.getById(id);
        JSONObject result = new JSONObject();
        String modelInfoId = "";
        int downloadCount = 0;
        boolean canDelete = false;
        if (flModelPrivate != null) {
            modelInfoId = flModelPrivate.getModelInfoId();
            if (flModelPrivate.getPartyId().equals(partyId)) {
                canDelete = true;
            }
        }
        if (flModelPublic != null) {
            modelInfoId = flModelPublic.getModelInfoId();
            if (flModelPublic.getPartyId().equals(partyId)) {
                canDelete = true;
            }
        }
        if (StringUtils.isEmpty(modelInfoId)) {
            return Result.error("模型不存在");
        }
        // 模型信息
        FlModelInfo flModelInfo = flModelInfoService.getById(modelInfoId);
        if (flModelInfo == null) {
            return Result.error("模型信息不存在");
        }
        // 招募任务
        FlJobRecruit flJobRecruit = flJobRecruitService.getOne(
            new QueryWrapper<FlJobRecruit>().lambda().eq(FlJobRecruit::getJobId, flModelInfo.getModelVersion()));
        cn.hutool.json.JSONArray compoments = JSONUtil.parseArray(flJobRecruit.getCompoments());
        if (compoments != null && compoments.size() > 0) {
            cn.hutool.json.JSONObject compomentsJson = compoments.get(0, cn.hutool.json.JSONObject.class);
            String algId = compomentsJson.getStr("id");
            Algorithm algorithm = algorithmService.getById(algId);
            result.put("canConvert", algorithm.isCanConvert());
        }
        // 报名任务
        List<FlJobRegist> flJobRegistList = flJobRegistService.list(new QueryWrapper<FlJobRegist>().lambda()
            .eq(FlJobRegist::getRecruitId, flJobRecruit.getId()).eq(FlJobRegist::getRegistStatus, 1));
        List<Map<String, Object>> partyNameList = new ArrayList<>();
        int rowNums = 0;
        int rowNum = 0;
        for (FlJobRegist flJobRegist : flJobRegistList) {
            if (flJobRegist.getRowsNum() != null) {
                rowNums = rowNums + flJobRegist.getRowsNum();
                rowNum = flJobRegist.getRowsNum();
            } else {
                String dataId = flJobRegist.getDataId();
                if (oConvertUtils.isNotEmpty(dataId)) {
                    CollectFileMeta collectFileMeta = collectFileMetaService.getById(dataId);
                    if (collectFileMeta != null) {
                        rowNums = rowNums + (collectFileMeta.getRowsNum() == null ? 0 : collectFileMeta.getRowsNum());
                        rowNum = collectFileMeta.getRowsNum() == null ? 0 : collectFileMeta.getRowsNum();
                    }
                }
            }

            FlPartyInfo flPartyInfo = flPartyInfoService.getById(flJobRegist.getPartyId());
            Map<String, Object> map = new HashMap<>(2);
            map.put("name", flPartyInfo.getName());
            map.put("rowNum", rowNum);
            partyNameList.add(map);
        }
        if (StringUtils.isEmpty(flModelInfo.getModelName())) {
            flModelInfo.setModelName(flJobRecruit.getName());
        }

        if (flModelPrivate != null) {
            flModelInfo.setModelId(flModelPrivate.getId());
            flModelInfo.setModelName(flModelPrivate.getModelName());
            flModelInfo.setModelDescribe(flModelPrivate.getModelDescribe());
            flModelInfo.setInstruction(flModelPrivate.getInstruction());
            flModelInfo.setApplication(flModelPrivate.getApplication());
        }
        if (flModelPublic != null) {
            flModelInfo.setModelId(flModelPublic.getId());
            flModelInfo.setModelName(flModelPublic.getModelName());
            flModelInfo.setModelDescribe(flModelPublic.getModelDescribe());
            flModelInfo.setInstruction(flModelPublic.getInstruction());
            flModelInfo.setApplication(flModelPublic.getApplication());
            downloadCount = flModelPublic.getDownloadCount() == null ? 0 : flModelPublic.getDownloadCount();
        }
        result.put("flModelInfo", flModelInfo);
        result.put("partyNameList", partyNameList);
        result.put("rowNums", rowNums);

        List<HashMap<String, Object>> list = flModelInfoService.modelUseInfo(id);
        if (list != null) {
            HashSet<HashMap<String, String>> usedUserList = new HashSet<>();
            HashSet<String> partyIdSet = new HashSet<String>();

            LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

            for (HashMap<String, Object> map : list) {
                String userName = map.get("realname") == null ? "" : map.get("realname").toString();
                partyIdSet.add(map.get("party_id") == null ? "" : map.get("party_id").toString());
                if (!userName.equals(sysUser.getRealname())) {
                    HashMap<String, String> userMap = new HashMap<>(2);
                    userMap.put("name", userName);
                    userMap.put("avatar", map.get("avatar") == null ? "" : map.get("avatar").toString());
                    usedUserList.add(userMap);
                }
            }
            int partyNums = partyIdSet.size();
            // 下载次数
            result.put("downloadNums", downloadCount);
            // 使用医院数目
            result.put("partyNums", partyNums);
            // 其他使用医生的头像
            result.put("usedUserList", usedUserList);
        }
        QueryWrapper<FlJobInference> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlJobInference::getModelInfoId, id);
        int count = inferenceService.count(queryWrapper);
        result.put("usedNums", count);
        result.put("canDelete", canDelete);
        return Result.OK(result);
    }

    /**
     * 我的模型中模型状态
     *
     * @param id
     * @param req
     * @return
     */
    @ApiOperation(value = "我的模型中模型状态", notes = "我的模型中模型上传下载状态")
    @GetMapping(value = "/opStatus")
    public Result<?> opStatus(@RequestParam @ApiParam(name = "id", value = "模型id", required = true) String id,
        HttpServletRequest req) {
        // 下载人员的联邦方id
        String userPartyId = req.getHeader("tenant_id");
        String modelInfoId = "";
        FlModelPrivate flModelPrivate = flModelPrivateService.getById(id);
        FlModelPublic flModelPublic = flModelPublicService.getById(id);
        if (flModelPrivate != null) {
            modelInfoId = flModelPrivate.getModelInfoId();
        }
        if (flModelPublic != null) {
            modelInfoId = flModelPublic.getModelInfoId();
        }
        FlModelInfo flModelInfo =
            flModelInfoService.getOne(new QueryWrapper<FlModelInfo>().lambda().eq(FlModelInfo::getId, modelInfoId));
        String jobTypeFlag = "1";
        if (jobTypeFlag.equals(flModelInfo.getJobType())) {
            return Result.OK(flModelInfoService.storeStatus(flModelInfo));
        } else {
            return Result.OK(flModelInfoService.reStoreStatus(flModelInfo));
        }
    }

    /**
     * 模型池中模型状态，已上传、已下载、上传中、下载中、上传失败、下载失败
     *
     * @param id
     * @param req
     * @return
     */
    @ApiOperation(value = "模型池中模型状态", notes = "我的模型中模型上传下载状态")
    @GetMapping(value = "/poolOpStatus")
    public Result<?> poolOpStatus(@RequestParam @ApiParam(name = "id", value = "模型id", required = true) String id,
        HttpServletRequest req) {
        // 下载人员的联邦方id
        String userPartyId = req.getHeader("tenant_id");

        FlModelInfo flModelInfo =
            flModelInfoService.getOne(new QueryWrapper<FlModelInfo>().lambda().eq(FlModelInfo::getId, id));
        // 模型池中的模型都为上传的模型
        // 如果此模型是我上传的。返回上传状态
        JSONArray result = new JSONArray();
        if (flModelInfo.getUserPartyId().equals(userPartyId)) {
            JSONObject json = new JSONObject();
            json.put("id", flModelInfo.getId());
            json.put("name", flModelInfo.getModelName());
            json.put("status", flModelInfoService.storeStatus(flModelInfo));
            result.add(json);
        } else {
            // 查询是否有子模型
            List<FlModelInfo> childFlModelInfoList = flModelInfoService.list(new QueryWrapper<FlModelInfo>().lambda()
                .eq(FlModelInfo::getParentId, id).eq(FlModelInfo::getUserPartyId, userPartyId));
            // 返回每个子模型的下载状态，为空则显示未下载
            for (FlModelInfo childFlModelInfo : childFlModelInfoList) {
                JSONObject json = new JSONObject();
                json.put("id", childFlModelInfo.getId());
                json.put("name", childFlModelInfo.getModelName());
                json.put("status", flModelInfoService.reStoreStatus(childFlModelInfo));
                result.add(json);
            }
        }
        return Result.OK(result);
    }

    /**
     * 根据模型名称判断模型是否存在
     *
     * @param name
     * @param req
     * @return
     */
    @ApiOperation(value = "根据模型名称查询模型", notes = "根据模型名称判断模型是否存在")
    @PostMapping(value = "/queryByName")
    public Result<?> queryByName(@RequestParam @ApiParam(name = "name", value = "模型名称", required = true) String name,
        HttpServletRequest req) {
        // 下载人员的联邦方id
        String userPartyId = req.getHeader("tenant_id");

        // 先查询是否存在
        List<FlModelInfo> flModelInfoList = flModelInfoService.list(new QueryWrapper<FlModelInfo>().lambda()
            .eq(FlModelInfo::getModelName, name).eq(FlModelInfo::getUserPartyId, userPartyId));
        return Result.OK(flModelInfoList);
    }

    /**
     * 模型共享，restore完成的模型信息不会写t_machine_learning_model_info，deploy时会从文件中读取信息 共享后的模型显示到模型池
     *
     * @param id
     * @param permission
     * @param name
     * @param describe
     * @param instruction
     * @param application
     * @param retry
     * @param req
     * @return
     */
    @AutoLog(value = "模型池-上传", operateType = OPERATE_TYPE_5)
    @ApiOperation(value = "模型上传", notes = "提交模型共享任务")
    @PostMapping(value = "/upload")
    @SuppressWarnings("checkstyle:parameternumber")
    public Result<?> store(@RequestParam @ApiParam(name = "id", value = "模型id", required = true) String id,
        @RequestParam @ApiParam(name = "permission", value = "模型权限", required = false) String permission,
        @RequestParam @ApiParam(name = "name", value = "模型名称", required = true) String name,
        @RequestParam @ApiParam(name = "describe", value = "模型描述", required = true) String describe,
        @RequestParam @ApiParam(name = "instruction", value = "使用说明", required = true) String instruction,
        @RequestParam @ApiParam(name = "application", value = "适用情况", required = true) String application,
        @RequestParam(defaultValue = "0", required = false) @ApiParam(name = "retry", value = "0默认，1重新上传",
            required = false) String retry,
        HttpServletRequest req) {
        String userPartyId = req.getHeader("tenant_id");
        // 先查询是否存在
        FlModelPrivate flModelPrivate = flModelPrivateService.getById(id);
        if (flModelPrivate == null) {
            return Result.error("找不到该模型！");
        }
        FlModelInfo exitFlModelInfo = flModelInfoService.getById(flModelPrivate.getModelInfoId());
        // 保存名称、权限等
        exitFlModelInfo.setPermission(permission);
        // 任务类型 1 store
        exitFlModelInfo.setJobType("1");
        // 任务状态设置为保存成功
        exitFlModelInfo.setJobStatus(7);
        exitFlModelInfo.setMessage("");

        // 异步提交
        String conf = flModelInfoService.modelStoreConf(exitFlModelInfo);

        FlPartyInfo flPartyInfo = flPartyInfoService.getById(userPartyId);
        String url = String.format(STORE_URL, flPartyInfo.getNameEn());

        JSONObject storeParams = new JSONObject();
        storeParams.put("url", url);
        storeParams.put("conf", conf);

        exitFlModelInfo.setStoreParams(storeParams.toJSONString());
        flModelInfoService.saveOrUpdate(exitFlModelInfo);

        try {
            asyncJobService.modelStore(url, conf, exitFlModelInfo);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        // 保存到模型池
        FlModelPublic flModelPublic = new FlModelPublic();
        flModelPublic.setPartyId(userPartyId);
        flModelPublic.setModelInfoId(exitFlModelInfo.getId());
        flModelPublic.setModelName(name);
        flModelPublic.setModelDescribe(describe);
        flModelPublic.setInstruction(instruction);
        flModelPublic.setApplication(application);
        flModelPublicService.save(flModelPublic);
        return Result.OK("横向模型上传任务提交成功");
    }

    /**
     * 横向模型下载，根据父模型id
     *
     * @param id
     * @param retry
     * @param req
     * @return
     */
    @AutoLog(value = "模型池-下载", operateType = OPERATE_TYPE_6)
    @ApiOperation(value = "模型下载", notes = "提交模型下载任务")
    @PostMapping(value = "/download")
    public Result<?> restore(@RequestParam @ApiParam(name = "id", value = "父模型id", required = true) String id,
        @RequestParam(defaultValue = "0", required = false) @ApiParam(name = "retry", value = "0默认，1重新下载",
            required = false) String retry,
        HttpServletRequest req) {
        // 下载人员的联邦方id
        String userPartyId = req.getHeader("tenant_id");

        // 先查询模型主信息是否存在
        FlModelPublic flModelPublic = flModelPublicService.getById(id);
        if (flModelPublic == null) {
            return Result.error("找不到该id的模型！");
        }
        flModelPublic
            .setDownloadCount(flModelPublic.getDownloadCount() == null ? 0 : flModelPublic.getDownloadCount() + 1);
        flModelPublicService.updateById(flModelPublic);
        FlModelInfo parentFlModelInfo = flModelInfoService.getById(flModelPublic.getModelInfoId());
        FlModelInfo childFlModelInfo = new FlModelInfo();
        // 保存到模型记录表
        flModelInfoService.copyParentModelInfo(childFlModelInfo, parentFlModelInfo);
        childFlModelInfo.setUserPartyId(userPartyId);
        // 任务类型 2 restore
        childFlModelInfo.setJobType("2");
        // 下载的模型权限为私有，不再展示到模型池，并且不能再次上传
        childFlModelInfo.setPermission("1");
        // 任务状态设置为保存成功
        childFlModelInfo.setJobStatus(7);
        // 异步提交
        String conf = flModelInfoService.modelStoreConf(parentFlModelInfo);
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(userPartyId);
        String url = String.format(RESTORE_URL, flPartyInfo.getNameEn());
        JSONObject storeParams = new JSONObject();
        storeParams.put("url", url);
        storeParams.put("conf", conf);
        childFlModelInfo.setStoreParams(storeParams.toJSONString());
        try {
            asyncJobService.modelStore(url, conf, childFlModelInfo);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        // 保存到我的模型
        FlModelPrivate flModelPrivate = new FlModelPrivate();
        flModelPrivate.setPartyId(userPartyId);
        flModelPrivate.setModelInfoId(flModelPublic.getModelInfoId());
        flModelPrivate.setModelName(flModelPublic.getModelName());
        flModelPrivate.setModelDescribe(flModelPublic.getModelDescribe());
        flModelPrivate.setInstruction(flModelPublic.getInstruction());
        flModelPrivate.setApplication(flModelPublic.getApplication());
        flModelPrivate.setUsedCount(0);
        flModelPrivate.setDownload(true);
        flModelPrivate.setSourcePublicId(flModelPublic.getId());
        flModelPrivateService.save(flModelPrivate);
        return Result.OK("横向模型下载任务提交成功");
    }

    @AutoLog(value = "模型下载到本地", operateType = OPERATE_TYPE_6)
    @ApiOperation(value = "模型下载到本地", notes = "模型下载到本地")
    @GetMapping(value = "/model_download")
    public ResponseEntity<byte[]>
        modelDownload(@RequestParam @ApiParam(name = "shareCode", value = "模型提取码", required = true) String shareCode) {
        String id = redisUtil.get(shareCode).toString();
        if (id == null) {
            log.error("模型提取码错误");
            return null;
        }
        return modelDownloadById(id);
    }

    @AutoLog(value = "转换并下载标准模型", operateType = OPERATE_TYPE_6)
    @ApiOperation(value = "转换并下载标准模型", notes = "转换并下载标准模型")
    @GetMapping(value = "/model_download_by_id")
    public ResponseEntity<byte[]>
        modelDownloadById(@RequestParam @ApiParam(name = "id", value = "我的模型id", required = true) String id) {
        // 先查询主模型信息是否存在
        FlModelPrivate flModelPrivate = flModelPrivateService.getById(id);
        FlModelInfo flModelInfo = flModelInfoService.getById(flModelPrivate.getModelInfoId());
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(flModelInfo.getModelPartyId());
        String url = String.format(MODEL_DOWNLOAD_URL, flPartyInfo.getNameEn());

        JSONObject params = new JSONObject();
        params.put("model_id", flModelInfo.getModelId());
        params.put("model_version", flModelInfo.getModelVersion());
        params.put("party_id", flModelInfo.getModelPartyId());
        params.put("role", flModelInfo.getModelRole());
        ResponseEntity<byte[]> response = restTemplate.postForEntity(url, params, byte[].class);
        return response;
    }

    @AutoLog(value = "获取模型提取码", operateType = OPERATE_TYPE_6)
    @ApiOperation(value = "获取模型提取码", notes = "获取模型提取码")
    @GetMapping(value = "/share_code")
    public Result<?> getShareCode(@RequestParam @ApiParam(name = "id", value = "我的模型id", required = true) String id) {
        FlModelPrivate flModelPrivate = flModelPrivateService.getById(id);
        if (flModelPrivate == null) {
            return Result.error("找不到该id的模型！");
        }
        // 通过id生成四位随机码
        String shareCode = RandomUtil.randomString(4);
        if (redisUtil.get(shareCode) != null) {
            shareCode = RandomUtil.randomString(4);
        }
        redisUtil.set(shareCode, id, 60 * 60 * 24);
        return Result.OK(shareCode);
    }

    /**
     * 横向模型下载，根据父模型id
     *
     * @param id
     * @param req
     * @return
     */
    @AutoLog(value = "我的模型-推送到标注系统", operateType = OPERATE_TYPE_6)
    @ApiOperation(value = "推送模型到标注系统", notes = "我的模型-推送到标注系统")
    @PostMapping(value = "/push")
    public Result<?> push(@RequestParam @ApiParam(name = "id", value = "模型id", required = true) String id,
        HttpServletRequest req) {
        // 下载人员的联邦方id
        String userPartyId = req.getHeader("tenant_id");
        // 先查询主模型信息是否存在
        FlModelPrivate flModelPrivate = flModelPrivateService.getById(id);
        if (flModelPrivate == null) {
            return Result.error("找不到该id的模型！");
        }
        FlModelInfo flModelInfo = flModelInfoService.getById(flModelPrivate.getModelInfoId());
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(userPartyId);
        String url = String.format(PUSH_URL, flPartyInfo.getNameEn());

        JSONObject pushParams = new JSONObject();
        pushParams.put("model_id", flModelInfo.getModelId());
        pushParams.put("model_version", flModelInfo.getModelVersion());
        pushParams.put("party_id", flModelInfo.getModelPartyId());
        pushParams.put("role", flModelInfo.getModelRole());
        pushParams.put("name", flModelInfo.getModelName());
        String result = httpClientPool.postWithoutTimeout(url, pushParams.toJSONString());
        log.info("推送到标注系统的结果：" + result);
        cn.hutool.json.JSONObject resultJson = JSONUtil.parseObj(result);
        String retcode = "retcode";
        if (resultJson.getInt(retcode) == 0) {
            log.info("resultJson.getInt(\"retcode\") == 0过了");
            return Result.OK("推送成功");
        } else {
            return Result.error("推送失败");
        }
    }

    /**
     * 模型同步，t_machine_learning_model_info => fl_model_info
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "模型同步", notes = "模型同步")
    @GetMapping(value = "/modelSync")
    public Result<?> modelSync(HttpServletRequest req) {
        // 取前十条未处理的
        List<TmachineLearningModelInfo> list = tMachineLearningModelInfoService.successModel();

        for (TmachineLearningModelInfo modelInfo : list) {
            flModelInfoService.modelInfoSync(modelInfo);
        }

        return Result.OK();
    }

    /**
     * 查询模型共享任务运行状态
     *
     * @param id
     * @param req
     * @return
     */
    @ApiOperation(value = "查询模型共享任务运行状态", notes = "查询任务状态")
    @GetMapping(value = "/search")
    public Result<Tjob> search(@RequestParam @ApiParam(name = "id", value = "模型id", required = true) String id,
        HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");

        FlModelInfo flModelInfo = flModelInfoService.getById(id);

        if (flModelInfo != null) {
            String jobId = flModelInfo.getJobId();
            Tjob job = tJobService.getOne(new QueryWrapper<Tjob>().lambda().eq(Tjob::getFJobId, jobId));
            return Result.OK(job);
        } else {
            return Result.OK();
        }
    }

    /**
     * 分页列表查询
     *
     * @param flModelInfo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "模型共享记录表-分页列表查询", notes = "模型共享记录表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FlModelInfo flModelInfo,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FlModelInfo> queryWrapper = QueryGenerator.initQueryWrapper(flModelInfo, req.getParameterMap());
        Page<FlModelInfo> page = new Page<FlModelInfo>(pageNo, pageSize);
        IPage<FlModelInfo> pageList = flModelInfoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param flModelInfo
     * @return
     */
    @AutoLog(value = "模型池-添加", operateType = OPERATE_TYPE_2)
    @ApiOperation(value = "模型共享记录表-添加", notes = "模型共享记录表-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FlModelInfo flModelInfo) {
        flModelInfoService.save(flModelInfo);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param flModelInfo
     * @return
     */
    @AutoLog(value = "模型池-添加", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "模型共享记录表-编辑", notes = "模型共享记录表-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FlModelInfo flModelInfo) {
        String id = flModelInfo.getId();
        if (oConvertUtils.isEmpty(id)) {
            return Result.error("id不能为空");
        }
        FlModelPrivate flModelPrivate = flModelPrivateService.getById(id);
        flModelPrivate.setModelName(flModelInfo.getModelName());
        flModelPrivate.setModelDescribe(flModelInfo.getModelDescribe());
        flModelPrivate.setInstruction(flModelInfo.getInstruction());
        flModelPrivate.setApplication(flModelInfo.getApplication());
        flModelPrivateService.updateById(flModelPrivate);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "模型池-删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "我的模型-通过id删除", notes = "我的模型-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        FlModelPrivate flModelPrivate = flModelPrivateService.getById(id);
        boolean flag = checkPermissionUtil.checkPermission(req, tenantId, flModelPrivate.getCreateBy(),
            flModelPrivate.getPartyId());
        if (!flag) {
            return Result.error("没有权限");
        }
        flModelPrivateService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "模型池-删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "模型池-通过id删除", notes = "模型池-通过id删除")
    @DeleteMapping(value = "/delete_public")
    public Result<?> deletePublic(@RequestParam(name = "id", required = true) String id, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        FlModelPublic flModelPublic = flModelPublicService.getById(id);
        boolean flag =
            checkPermissionUtil.checkPermission(req, tenantId, flModelPublic.getCreateBy(), flModelPublic.getPartyId());
        if (!flag) {
            return Result.error("没有权限");
        }
        flModelPublicService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "模型池-批量删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "模型共享记录表-批量删除", notes = "模型共享记录表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flModelInfoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "模型共享记录表-通过id查询", notes = "模型共享记录表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        FlModelInfo flModelInfo = flModelInfoService.getById(id);
        if (flModelInfo == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(flModelInfo);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param flModelInfo
     */
    @AutoLog(value = "模型池-导出", operateType = OPERATE_TYPE_6)
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FlModelInfo flModelInfo) {
        return super.exportXls(request, flModelInfo, FlModelInfo.class, "模型共享记录表");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @AutoLog(value = "模型池-导入", operateType = OPERATE_TYPE_5)
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FlModelInfo.class);
    }

    /**
     * 上传各个联邦方的模型，只能是发起方调用，发起方上传时会把参与方的一起上传
     *
     * @param id
     * @param permission
     * @param name
     * @param describe
     * @param instruction
     * @param application
     * @param retry
     * @param req
     * @return
     */
    @AutoLog(value = "模型池-纵向模型上传", operateType = OPERATE_TYPE_5)
    @ApiOperation(value = "纵向模型上传", notes = "提交模型共享任务")
    @PostMapping(value = "/heteroUpload")
    @SuppressWarnings("checkstyle:parameternumber")
    public Result<?> heteroStore(@RequestParam @ApiParam(name = "id", value = "模型表id", required = true) String id,
        @RequestParam @ApiParam(name = "permission", value = "模型权限", required = false) String permission,
        @RequestParam @ApiParam(name = "name", value = "模型名称", required = true) String name,
        @RequestParam @ApiParam(name = "describe", value = "模型描述", required = true) String describe,
        @RequestParam @ApiParam(name = "instruction", value = "使用说明", required = true) String instruction,
        @RequestParam @ApiParam(name = "application", value = "适用情况", required = true) String application,
        @RequestParam(defaultValue = "0", required = false) @ApiParam(name = "retry", value = "0默认，1重新上传",
            required = false) String retry,
        HttpServletRequest req) {
        String userPartyId = req.getHeader("tenant_id");
        // 判断模型名称是否存在
        List<FlModelInfo> flModelInfoList = flModelInfoService.list(new QueryWrapper<FlModelInfo>().lambda()
            .eq(FlModelInfo::getModelName, name).eq(FlModelInfo::getUserPartyId, userPartyId));
        if (flModelInfoList != null && flModelInfoList.size() > 0) {
            return Result.error("模型名称重复");
        }
        // 先查询是否存在
        FlModelInfo exitFlModelInfo = flModelInfoService.getById(id);
        if (!exitFlModelInfo.getModelPartyId().equals(userPartyId)) {
            return Result.error("不是自己的模型");
        }
        String jobTypeFlag = "2";
        if (exitFlModelInfo.getJobType() != null && jobTypeFlag.equals(exitFlModelInfo.getJobType())) {
            return Result.error("下载的模型不能再次上传");
        }
        // 参与方不能进行模型上传
        if (TrainRoleEnum.guest.toString().equals(exitFlModelInfo.getModelRole())) {
            return Result.error("参与方不能进行模型上传");
        }
        // 查询纵向模型的所有需上传的模型
        List<FlModelInfo> allModelInfoList = flModelInfoService
            .list(new QueryWrapper<FlModelInfo>().lambda().eq(FlModelInfo::getModelId, exitFlModelInfo.getModelId())
                .eq(FlModelInfo::getModelVersion, exitFlModelInfo.getModelVersion()));

        // 遍历模型，提交上传任务
        for (FlModelInfo modelInfo : allModelInfoList) {
            // 保存名称、权限等
            modelInfo.setModelName(name);
            modelInfo.setPermission(permission);
            modelInfo.setModelDescribe(describe);
            modelInfo.setInstruction(instruction);
            modelInfo.setApplication(application);
            // 任务类型 1 store
            modelInfo.setJobType("1");
            // 任务状态设置为保存成功
            modelInfo.setJobStatus(7);

            String conf = flModelInfoService.modelStoreConf(modelInfo);

            FlPartyInfo flPartyInfo = flPartyInfoService.getById(modelInfo.getModelPartyId());
            String url = String.format(STORE_URL, flPartyInfo.getNameEn());

            JSONObject storeParams = new JSONObject();
            storeParams.put("url", url);
            storeParams.put("conf", conf);

            modelInfo.setStoreParams(storeParams.toJSONString());
            flModelInfoService.saveOrUpdate(modelInfo);

            asyncJobService.modelStore(url, conf, modelInfo);
        }
        return Result.OK("纵向模型上传任务提交成功");

    }

    /**
     * 纵向模型下载，调用方为guest，需指定host方的partyid，partyid可采用加密方式进行限制，防止随便调用
     *
     * @param param
     * @param req
     * @return
     */
    @AutoLog(value = "模型池-纵向模型下载", operateType = OPERATE_TYPE_6)
    @ApiOperation(value = "纵向模型下载", notes = "提交模型下载任务")
    @PostMapping(value = "/heteroDownload")
    @SuppressWarnings({"PMD.MethodTooLongRule", "checkstyle:methodlength", "checkstyle:returncount"})
    public Result<?> heteroRestore(
        @RequestBody @ApiParam(name = "param", value = "参数", required = true) JSONObject param,
        HttpServletRequest req) {
        String id = param.getString("id");
        String retry = param.getString("retry");
        JSONObject partyIdsMap = param.getJSONObject("partyIdsMap");
        // 下载人员的联邦方id
        String userPartyId = req.getHeader("tenant_id");
        // 先查询是否存在
        FlModelInfo parentFlModelInfo = flModelInfoService.getById(id);
        if (parentFlModelInfo == null) {
            return Result.error("模型不存在");
        }
        if (!TrainRoleEnum.host.toString().equals(parentFlModelInfo.getModelRole())) {
            return Result.error("参与方模型不能下载");
        }
        String oldModelId = parentFlModelInfo.getModelId();
        String oldModelVersion = parentFlModelInfo.getModelVersion();
        // 新模型id与version
        String newModelId = oldModelId;
        for (String oldPartyId : partyIdsMap.keySet()) {
            newModelId = newModelId.replace(oldPartyId, partyIdsMap.getString(oldPartyId));
        }
        String newModelVersion = oldModelVersion;
        // 查询所有需下载的模型，只能是父模型
        List<FlModelInfo> oldAllModelInfoList =
            flModelInfoService.list(new QueryWrapper<FlModelInfo>().lambda().eq(FlModelInfo::getModelId, oldModelId)
                .eq(FlModelInfo::getModelVersion, oldModelVersion).isNull(FlModelInfo::getParentId));
        // 父模型id列表
        List<String> parentIdList = new ArrayList<>();
        for (FlModelInfo model : oldAllModelInfoList) {
            parentIdList.add(model.getId());
        }
        // 判断所有子任务的状态，ParentId都用guest方的还是分开
        List<FlModelInfo> newFlModelInfoList = flModelInfoService.list(new QueryWrapper<FlModelInfo>().lambda()
            .eq(FlModelInfo::getModelId, newModelId).eq(FlModelInfo::getModelVersion, newModelVersion)
            .eq(FlModelInfo::getUserPartyId, userPartyId).in(FlModelInfo::getParentId, parentIdList));
        // 所有子任务成功才成功，只要有一个失败就失败
        if (newFlModelInfoList != null && newFlModelInfoList.size() > 0) {
            String retryFlag = "0";
            if (retryFlag.equals(retry)) {
                List<Integer> statusList = new ArrayList<>();
                for (FlModelInfo childFlModelInfo : newFlModelInfoList) {
                    statusList.add(childFlModelInfo.getJobStatus());
                }
                if (statusList.contains(ModelDownloadStatusEnum.SUBMIT_FAIL.getCode())
                    || statusList.contains(ModelDownloadStatusEnum.TRAIN_FAIL.getCode())
                    || statusList.contains(ModelDownloadStatusEnum.CANCEL.getCode())) {
                    return Result.error("模型下载失败，请重新下载");
                } else if (statusList.contains(ModelDownloadStatusEnum.RECRUITING.getCode())
                    || statusList.contains(ModelDownloadStatusEnum.RUNNING.getCode())
                    || statusList.contains(ModelDownloadStatusEnum.SAVE_SUCCESS.getCode())
                    || statusList.contains(ModelDownloadStatusEnum.WAITING.getCode())) {
                    return Result.error("模型正在下载中，请等待");
                } else if (statusList.contains(ModelDownloadStatusEnum.TRAIN_SUCCESS.getCode())) {
                    return Result.error("模型下载成功，请不要重复下载");
                } else {
                    return Result.error("未知错误");
                }
            }
        }
        // 模型迁移参数
        JSONObject params = flModelInfoService.getRoleJosn(oldAllModelInfoList, partyIdsMap);
        JSONObject oldRoleJson = params.getJSONObject("oldRoleJson");
        JSONObject newRoleJson = params.getJSONObject("newRoleJson");
        // 遍历模型，提交下载任务
        for (FlModelInfo oldModelInfo : oldAllModelInfoList) {
            // 查询新参与方id
            String newPartyId = partyIdsMap.getString(oldModelInfo.getModelPartyId());
            // 查询新模型是否存在
            FlModelInfo newModelInfo = flModelInfoService.getOne(new QueryWrapper<FlModelInfo>().lambda()
                .eq(FlModelInfo::getModelId, newModelId).eq(FlModelInfo::getModelVersion, newModelVersion)
                .eq(FlModelInfo::getModelPartyId, newPartyId).eq(FlModelInfo::getModelRole, oldModelInfo.getModelRole())
                .eq(FlModelInfo::getParentId, oldModelInfo.getId()));
            if (newModelInfo == null) {
                newModelInfo = new FlModelInfo();
                // 保存，生成新的模型id、模型版本
                newModelInfo.setUserPartyId(userPartyId);
                newModelInfo.setParentId(oldModelInfo.getId());
                // 设置为新的id与version
                newModelInfo.setModelId(newModelId);
                newModelInfo.setModelVersion(newModelVersion);
                newModelInfo.setModelPartyId(newPartyId);
                newModelInfo.setModelRole(oldModelInfo.getModelRole());
                newModelInfo.setMetric(oldModelInfo.getMetric());
                newModelInfo.setModelName(oldModelInfo.getModelName());
                newModelInfo.setModelDescribe(oldModelInfo.getModelDescribe());
                newModelInfo.setInstruction(oldModelInfo.getInstruction());
                newModelInfo.setApplication(oldModelInfo.getApplication());
                // 任务类型 2 restore
                newModelInfo.setJobType("2");
                // 下载的模型权限为私有，不再展示到模型池，并且不能再次上传
                newModelInfo.setPermission("1");
            }
            // 模型下载参数
            String storeConf = flModelInfoService.modelStoreConf(oldModelInfo);
            // 模型迁移参数
            String migrateConf = flModelInfoService.modelMigrateConf(oldRoleJson, newRoleJson, oldModelId,
                oldModelVersion, newModelInfo);
            // 提交
            FlPartyInfo newPartyInfo = flPartyInfoService.getById(Integer.parseInt(newModelInfo.getModelPartyId()));
            String migrateUrl = String.format(MIGRATE_URL, newPartyInfo.getNameEn());
            String restoreUrl = String.format(RESTORE_URL, newPartyInfo.getNameEn());
            // 保存参数
            JSONObject storeParams = new JSONObject();
            storeParams.put("url", restoreUrl);
            storeParams.put("conf", storeConf);
            JSONObject migrateParams = new JSONObject();
            migrateParams.put("url", migrateUrl);
            migrateParams.put("conf", migrateConf);
            newModelInfo.setStoreParams(storeParams.toJSONString());
            newModelInfo.setMigrateParams(migrateParams.toJSONString());
            flModelInfoService.saveOrUpdate(newModelInfo);
            asyncJobService.modelStore(restoreUrl, storeConf, newModelInfo);
        }
        return Result.OK("纵向模型下载任务提交成功");
    }

    @ApiOperation(value = "模型参数描述查询", notes = "模型参数描述查询")
    @GetMapping(value = "/parameter")
    public Result<?> parameter() {
        List<FlModelParameter> list = modelParameterService.list();
        return Result.OK(list);
    }

}
