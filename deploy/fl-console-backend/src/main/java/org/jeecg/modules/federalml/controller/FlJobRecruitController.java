package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.federalml.entity.*;
import org.jeecg.modules.federalml.enums.CheckStatusEnum;
import org.jeecg.modules.federalml.enums.JobTypeEnum;
import org.jeecg.modules.federalml.enums.RecruitStatusEnum;
import org.jeecg.modules.federalml.enums.TrainRoleEnum;
import org.jeecg.modules.federalml.global.Dict;
import org.jeecg.modules.federalml.model.FlJob4Update;
import org.jeecg.modules.federalml.model.FlJobPreproccessForCreate;
import org.jeecg.modules.federalml.model.SelectCollectModel;
import org.jeecg.modules.federalml.service.*;
import org.jeecg.modules.federalml.service.impl.AsyncJobService;
import org.jeecg.modules.federalml.utils.CheckPermissionUtil;
import org.jeecg.modules.federalml.utils.HttpClientPool;
import org.jeecg.modules.federalml.utils.JobConfUtil;
import org.jeecg.modules.federalml.utils.MessageSendUtil;
import org.jeecg.modules.federalml.vo.FlJobRecruitPage;
import org.jeecg.modules.federalml.vo.FlJobRecruitVo;
import org.jeecg.modules.federalml.vo.FlRecruitJobInfo;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.service.ISysAnnouncementService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicParameters;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.utils.StringUtils;

/**
 * @Description: 联邦招募任务表
 * @Author: jeecg-boot
 * @Date: 2021-07-20
 * @Version: V1.0
 */
@Api(tags = "4.1 联邦招募任务")
@RestController
@RequestMapping("/platform/flJobRecruit")
@Slf4j
public class FlJobRecruitController {
    @Autowired
    private IFlJobRecruitService flJobRecruitService;
    @Autowired
    private IFlJobRegistService flJobRegistService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IFlPartyInfoService flPartyInfoService;
    @Autowired
    private ISysUserRoleService sysUserRoleService;
    @Autowired
    private IFlReadyStatusChkService readyStatusChkService;
    @Autowired
    private ICollectFileMetaService collectFileMetaService;

    @Autowired
    private ItJobService tJobService;

    @Autowired
    private MessageSendUtil messageSendUtil;

    @Autowired
    private ISysAnnouncementService sysAnnouncementService;

    @Autowired
    private AsyncJobService asyncJobService;

    @Autowired
    private IAlgorithmService algorithmService;

    @Autowired
    private IFlOpLogService flOpLogService;

    @Resource
    private WebSocket webSocket;

    @Value("${fateflow.url}")
    private String fateFlowUrl;

    @Value("${requestMode}")
    private String requestMode;

    @Resource
    private JobConfUtil jobConfUtil;

    @Autowired
    private CheckPermissionUtil checkPermissionUtil;

    @Autowired
    private IFlModelInfoService flModelInfoService;

    @Autowired
    private IFlModelPrivateService flModelPrivateService;

    @Autowired
    private IFlJobPreproccessService flJobPreproccessService;

    @Value("${features.specialEdgeEnabled}")
    private boolean specialEdgeEnabled;

    @Autowired
    private IFlJobFeatureService flJobFeatureService;

    @Autowired
    private HttpClientPool httpClientPool;

    final int checkCodeStrSplitLength = 3;

    final String specialEdge = "1008";

    final String federalml = "federalml";

    public static final String SELECTED_META_URL = "http://fl-uploader.%s:8080/selectedMeta";

    /**
     * 分页列表查询
     *
     * @param flJobRecruit
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "联邦招募任务表-分页列表查询", notes = "联邦招募任务表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FlJobRecruit>> queryPageList(FlJobRecruit flJobRecruit,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FlJobRecruit> queryWrapper = QueryGenerator.initQueryWrapper(flJobRecruit, req.getParameterMap());
        Page<FlJobRecruit> page = new Page<FlJobRecruit>(pageNo, pageSize);
        IPage<FlJobRecruit> pageList = flJobRecruitService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "联邦招募任务-报名")
    @ApiOperation(value = "联邦招募任务报名", notes = "联邦招募任务报名，报名时不检测状态")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @PostMapping(value = "/regist")
    @SuppressWarnings("PMD")
    public Result<FlJobRegist> regist(
        @RequestParam @ApiParam(name = "recruitId", value = "招募任务的id", required = true) String recruitId,
        @ApiParam(name = "messsage", value = "留言", required = false) @RequestParam(required = false) String messsage,
        HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        FlJobRegist existFlJobRegist = flJobRegistService.getOne(new QueryWrapper<FlJobRegist>().lambda()
            .eq(FlJobRegist::getPartyId, Integer.parseInt(tenantId)).eq(FlJobRegist::getRecruitId, recruitId));
        if (existFlJobRegist != null
            && (existFlJobRegist.getRegistStatus() == 0 || existFlJobRegist.getRegistStatus() == 1)) {
            Result<FlJobRegist> result = new Result<>();
            result.setSuccess(false);
            result.setMessage("请不要重复报名");
            return result;
        }
        int permission = flJobRecruitService.getById(recruitId).getPermission();
        FlJobRegist flJobRegist = new FlJobRegist();
        flJobRegist.setRecruitId(recruitId);
        flJobRegist.setMessage(messsage);
        flJobRegist.setPartyId(Integer.parseInt(tenantId));
        flJobRegist.setRole("host");
        if (permission == 0) {
            flJobRegist.setRegistStatus(1);
            if (existFlJobRegist != null) {
                flJobRegist.setId(existFlJobRegist.getId());
                flJobRegist.setDataId(null);
                flJobRegistService.updateById(flJobRegist);
            } else {
                flJobRegistService.save(flJobRegist);
            }
            FlPartyInfo flPartyInfo = flPartyInfoService.getById(tenantId);
            LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
            FlOpLog flOpLog = new FlOpLog();
            JSONObject content = new JSONObject();
            content.put("partyName", flPartyInfo.getName());
            content.put("userName", sysUser.getUsername());
            flOpLog.setContent(content.toJSONString());
            flOpLog.setPartyId(Integer.parseInt(tenantId));
            flOpLog.setUrl("/platform/flJobRecruit/regist");
            flOpLog.setParam(JSONUtil.toJsonStr(req.getParameterMap()));
            List<FlActivity> flActivityList = new ArrayList<>();
            FlActivity missionActivity = new FlActivity();
            missionActivity.setTypeId(recruitId);
            missionActivity.setType(Dict.MISSION_ACTICITY_TYPE);
            missionActivity.setOpType(Dict.MISSION_JOIN_TYPE);
            flActivityList.add(missionActivity);
            flOpLogService.saveMain(flOpLog, flActivityList);
            return Result.OK("报名成功！", flJobRegist);
        } else {
            flJobRegist.setRegistStatus(0);
            if (existFlJobRegist != null) {
                flJobRegist.setId(existFlJobRegist.getId());
                flJobRegist.setDataId(null);
                flJobRegistService.updateById(flJobRegist);
            } else {
                flJobRegistService.save(flJobRegist);
            }
            JSONObject message =
                messageSendUtil.jobMessage(Integer.parseInt(tenantId), flJobRegist.getId(), "regist", "jrrwshtx", "3");
            sysAnnouncementService.sendJobAnnouncement(message);
            JSONObject obj = new JSONObject();
            obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
            webSocket.pushMessage(message.getString("toUserId"), obj.toJSONString());
            FlPartyInfo flPartyInfo = flPartyInfoService.getById(tenantId);
            LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
            FlOpLog flOpLog = new FlOpLog();
            JSONObject content = new JSONObject();
            content.put("partyName", flPartyInfo.getName());
            content.put("userName", sysUser.getUsername());
            flOpLog.setContent(content.toJSONString());
            flOpLog.setPartyId(Integer.parseInt(tenantId));
            flOpLog.setUrl("/platform/flJobRecruit/regist");
            flOpLog.setParam(JSONUtil.toJsonStr(req.getParameterMap()));
            List<FlActivity> flActivityList = new ArrayList<>();
            FlActivity missionActivity = new FlActivity();
            missionActivity.setTypeId(recruitId);
            missionActivity.setType(Dict.MISSION_ACTICITY_TYPE);
            missionActivity.setOpType(Dict.MISSION_APPLY_JOIN_TYPE);
            flActivityList.add(missionActivity);
            flOpLogService.saveMain(flOpLog, flActivityList);
            return Result.OK("报名成功，请等待审核！", flJobRegist);
        }
    }

    /**
     * 新手任务 环境部署状态 从party info中获取
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "新手任务", notes = "新手任务")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @DynamicResponseParameters(
        properties = {@DynamicParameter(name = "code", value = "代码", required = true, dataTypeClass = Integer.class),
            @DynamicParameter(name = "message", value = "消息", required = true, dataTypeClass = String.class),
            @DynamicParameter(name = "result",
                value = "{\"avatarStatus\": 1,\"inviteStatus\": 0,\"deployStatus\": 0}，1完成，0未完成", required = true,
                dataTypeClass = String.class),
            @DynamicParameter(name = "success", value = "成功标识", required = true, dataTypeClass = Boolean.class),
            @DynamicParameter(name = "timestamp", value = "时间戳", required = true, dataTypeClass = long.class)})
    @PostMapping(value = "/beginnerJob")
    public Result<?> beginnerJob(HttpServletRequest req) {
        String partyId = req.getHeader("tenant_id");
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(Integer.parseInt(partyId));
        JSONObject result = new JSONObject();
        // 部署状态
        Integer deployStatus = flPartyInfo.getStatus();
        if (deployStatus != null && deployStatus == 1) {
            result.put("deployStatus", 1);
        } else {
            result.put("deployStatus", 0);
        }
        // 头像状态
        String avatar = flPartyInfo.getAvatar();
        if (oConvertUtils.isNotEmpty(avatar) && avatar.contains(federalml)) {
            result.put("avatarStatus", 1);
        } else {
            result.put("avatarStatus", 0);
        }
        // 是否邀请过他人，通过人数判断
        int nums = sysUserRoleService.count(new QueryWrapper<SysUserRole>().select("distinct user_id").lambda()
            .eq(SysUserRole::getPartyId, Integer.parseInt(partyId)));
        if (nums > 1) {
            result.put("inviteStatus", 1);
        } else {
            result.put("inviteStatus", 0);
        }

        return Result.OK(result);
    }

    @AutoLog(value = "联邦招募任务-检查硬件状态")
    @ApiOperation(value = "检查硬件状态", notes = "检查硬件状态")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @PostMapping(value = "/hardWareStatus")
    public Result<?> hardWareStatus(HttpServletRequest req) {
        String partyId = req.getHeader("tenant_id");
        if (specialEdgeEnabled) {
            if (specialEdge.equals(partyId)) {
                return Result.OK(new HashSet<>());
            }
        }
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(Integer.parseInt(partyId));
        String partyName = flPartyInfo.getNameEn();
        Set<String> notReady = readyStatusChkService.notReadyNodeComponents(partyName);
        return Result.OK(notReady);
    }

    /**
     * 检查软件状态
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "检查软件状态", notes = "检查软件状态")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @PostMapping(value = "/softWareStatus")
    public Result<?> softWareStatus(HttpServletRequest req) {
        String partyId = req.getHeader("tenant_id");
        if (specialEdgeEnabled) {
            if (specialEdge.equals(partyId)) {
                return Result.OK(new HashSet<>());
            }
        }
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(Integer.parseInt(partyId));
        Integer deployStatus = flPartyInfo.getStatus();
        String partyName = flPartyInfo.getNameEn();
        Set<String> notReady = readyStatusChkService.notReadyAppComponents(partyName);
        if (notReady.size() == 0) {
            if (deployStatus != null && deployStatus == 1) {
                return Result.OK(notReady);
            }
            // 部署完成，只更新一次
            flPartyInfo.setStatus(1);
            flPartyInfoService.updateById(flPartyInfo);
            return Result.OK(notReady);
        } else {
            return Result.OK(notReady);
        }
    }

    @AutoLog(value = "联邦招募任务-退出报名")
    @ApiOperation(value = "联邦招募任务退出报名", notes = "联邦招募任务退出报名")
    @PostMapping(value = "/exitRegist")
    public Result<?> exitRegist(
        @RequestParam @ApiParam(name = "registId", value = "报名id", required = true) String registId,
        HttpServletRequest req) {
        FlJobRegist flJobRegist = flJobRegistService.getById(registId);
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(flJobRegist.getRecruitId());
        if (flJobRecruit.getRecruitStatus() != 0) {
            Result<?> result = new Result<>();
            result.setSuccess(false);
            result.setMessage("当前状态，无法退出任务！");
        }

        if (TrainRoleEnum.guest.toString().equals(flJobRegist.getRole())) {
            // 任务取消
            flJobRecruit.setRecruitStatus(9);
            flJobRecruitService.updateById(flJobRecruit);
        } else {
            flJobRegistService.removeById(flJobRegist.getId());
        }
        // ${partyName}的${userName}退出了任务
        // --- log start ---
        String partyId = req.getHeader("tenant_id");
        if (partyId == null) {
            partyId = flJobRegist.getPartyId().toString();
        }
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(partyId);
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

        FlOpLog flOpLog = new FlOpLog();

        JSONObject content = new JSONObject();
        content.put("partyName", flPartyInfo.getName());
        content.put("userName", sysUser.getUsername());

        flOpLog.setContent(content.toJSONString());
        flOpLog.setPartyId(Integer.parseInt(partyId));
        flOpLog.setUrl("/platform/flJobRecruit/exitRegist");
        flOpLog.setParam(JSONUtil.toJsonStr(req.getParameterMap()));

        List<FlActivity> flActivityList = new ArrayList<>();

        FlActivity missionActivity = new FlActivity();
        missionActivity.setTypeId(flJobRegist.getRecruitId());
        missionActivity.setType(Dict.MISSION_ACTICITY_TYPE);
        missionActivity.setOpType(Dict.MISSION_EXIT_TYPE);

        flActivityList.add(missionActivity);
        flOpLogService.saveMain(flOpLog, flActivityList);
        // --- log end ---

        return Result.OK("退出成功！");
    }

    @AutoLog(value = "联邦招募任务-报名审核")
    @ApiOperation(value = "联邦招募任务报名审核", notes = "联邦招募任务报名审核")
    @PostMapping(value = "/checkRegist")
    public Result<?> checkRegist(
        @RequestParam @ApiParam(name = "registId", value = "报名id", required = true) String registId,
        @RequestParam @ApiParam(name = "checkStatus", value = "审核状态，1通过，2拒绝", required = true) Integer checkStatus,
        HttpServletRequest req) {
        FlJobRegist flJobRegist = flJobRegistService.getById(registId);
        flJobRegist.setRegistStatus(checkStatus);
        flJobRegistService.updateById(flJobRegist);

        // 审核完成发送通知
        JSONObject message = messageSendUtil.jobAuditResultMessage(registId, "regist", "jrrwjgtx");
        if (checkStatus.equals(CheckStatusEnum.PASS.getCode())) {
            message.put("registStatus", "通过");
        } else if (checkStatus.equals(CheckStatusEnum.REFUSE.getCode())) {
            message.put("registStatus", "拒绝");
        }

        sysAnnouncementService.sendJobAnnouncement(message);

        JSONObject obj = new JSONObject();
        obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
        webSocket.pushMessage(message.getString("toUserId"), obj.toJSONString());

        // ${partyName}的${userName}通过了/拒绝了${partyName}的${userName}的申请
        // --- log start ---
        String partyId = req.getHeader("tenant_id");
        if (partyId == null) {
            FlJobRecruit jobRecruit = flJobRecruitService.getById(flJobRegist.getRecruitId());
            partyId = jobRecruit.getPartyId().toString();
        }
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(partyId);
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

        FlOpLog flOpLog = new FlOpLog();

        JSONObject content = new JSONObject();
        content.put("guestPartyName", flPartyInfo.getName());
        content.put("guestUserName", sysUser.getUsername());
        content.put("hostPartyName", flPartyInfoService.getById(flJobRegist.getPartyId()).getName());
        content.put("hostUserName", flJobRegist.getCreateBy());

        flOpLog.setContent(content.toJSONString());
        flOpLog.setPartyId(Integer.parseInt(partyId));
        flOpLog.setUrl("/platform/flJobRecruit/checkRegist");
        flOpLog.setParam(JSONUtil.toJsonStr(req.getParameterMap()));

        List<FlActivity> flActivityList = new ArrayList<>();

        FlActivity missionActivity = new FlActivity();
        missionActivity.setTypeId(flJobRegist.getRecruitId());
        missionActivity.setType(Dict.MISSION_ACTICITY_TYPE);

        if (CheckStatusEnum.PASS.getCode().toString().equals(checkStatus)) {
            missionActivity.setOpType(Dict.MISSION_AGREE_JOIN_TYPE);
        } else if (CheckStatusEnum.REFUSE.getCode().equals(checkStatus)) {
            missionActivity.setOpType(Dict.MISSION_REFUSE_JOIN_TYPE);
        }

        flActivityList.add(missionActivity);
        flOpLogService.saveMain(flOpLog, flActivityList);
        // --- log end ---

        return Result.OK("审核成功！");
    }

    /**
     * 保存训练数据
     *
     * @param
     * @return
     */
    @AutoLog(value = "联邦招募任务-保存训练数据")
    @ApiOperation(value = "更新参与方数据集id", notes = "")
    @PostMapping(value = "/updateData")
    public Result<?> registUpdateWithStatus(
        @RequestParam @ApiParam(name = "registId", value = "报名id", required = true) String registId,
        @RequestParam @ApiParam(name = "dataId", value = "数据id", required = true) String dataId,
        HttpServletRequest req) {
        FlJobRegist flJobRegist = flJobRegistService.getById(registId);
        // flJobRegist.getDataId()如果有值代表是隐藏的子数据集已经选中
        if (flJobRegist.getDataId() != null) {
            CollectFileMeta collectFileMeta = collectFileMetaService.getById(flJobRegist.getDataId());
            if (!collectFileMeta.isHidden()) {
                flJobRegist.setDataId(dataId);
            }
        } else {
            flJobRegist.setDataId(dataId);
        }
        // 等待训练开始
        flJobRegist.setEnvStatus(1);
        flJobRegistService.updateById(flJobRegist);

        // ${partyName}的${userName}上传了数据集${dataName}
        // --- log start ---
        String partyId = req.getHeader("tenant_id");
        if (partyId == null) {
            FlJobRecruit jobRecruit = flJobRecruitService.getById(flJobRegist.getRecruitId());
            partyId = jobRecruit.getPartyId().toString();
        }
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(partyId);
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

        FlOpLog flOpLog = new FlOpLog();

        JSONObject content = new JSONObject();
        content.put("partyName", flPartyInfo.getName());
        content.put("userName", sysUser.getUsername());
        CollectFileMeta data = collectFileMetaService.getById(dataId);
        content.put("dataId", dataId);
        content.put("dataName", data.getName());
        content.put("dataSize", data.getSize());

        flOpLog.setContent(content.toJSONString());
        flOpLog.setPartyId(Integer.parseInt(partyId));
        flOpLog.setUrl("/platform/flJobRecruit/updateData");
        flOpLog.setParam(JSONUtil.toJsonStr(req.getParameterMap()));

        List<FlActivity> flActivityList = new ArrayList<>();

        FlActivity missionActivity = new FlActivity();
        missionActivity.setTypeId(flJobRegist.getRecruitId());
        missionActivity.setType(Dict.MISSION_ACTICITY_TYPE);
        missionActivity.setOpType(Dict.MISSION_UPLOAD_DATA_TYPE);

        FlActivity dataActivity = new FlActivity();
        dataActivity.setTypeId(dataId);
        dataActivity.setType(Dict.DATA_ACTICITY_TYPE);
        dataActivity.setOpType(Dict.DATA_USE_TYPE);

        flActivityList.add(missionActivity);
        flActivityList.add(dataActivity);
        flOpLogService.saveMain(flOpLog, flActivityList);
        // --- log end ---
        return Result.OK(flJobRegist);
    }

    /**
     * 不需要校验校验环境及数据状态的更新，报名申请审核，退出报名
     *
     * @param flJobRegist
     * @return
     */
    @AutoLog(value = "联邦招募任务-报名信息审核")
    @PostMapping(value = "/registUpdate")
    public Result<?> registUpdate(@RequestBody FlJobRegist flJobRegist) {
        flJobRegistService.updateById(flJobRegist);
        return Result.OK(flJobRegist);
    }

    /**
     * 当前联邦方参与的招募任务
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "我的任务", notes = "当前联邦方参与的招募任务")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @GetMapping(value = "/listMyJob")
    public Result<IPage<FlRecruitJobInfo>> listMyJob(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) @ApiParam(name = "name", value = "任务名称", required = false) String name,
        @RequestParam(name = "type", defaultValue = "ALL") @ApiParam(name = "type",
            value = "类型（ALL、TRAIN、INFERENCE）") String type,
        HttpServletRequest req) {
        String userName = JwtUtil.getUserNameByToken(req);
        IPage<FlRecruitJobInfo> result = null;
        try {
            String partyId = req.getHeader("tenant_id");
            Page<FlRecruitJobInfo> page = new Page<>(pageNo, pageSize);
            // 当该用户在此联邦方是普通用户时，数据权限才具体到用户
            boolean isParyUser = sysUserRoleService.isPartyUser(userName, partyId);
            if (!isParyUser) {
                userName = null;
            }
            if (JobTypeEnum.ALL.toString().equals(type)) {
                result = flJobRecruitService.listMyJobPage(page, Integer.parseInt(partyId), name, userName);
            } else if (JobTypeEnum.TRAIN.toString().equals(type)) {
                result = flJobRecruitService.listMyTrainJobPage(page, Integer.parseInt(partyId), name, userName);
            } else if (JobTypeEnum.INFERENCE.toString().equals(type)) {
                result = flJobRecruitService.listMyInferenceJobPage(page, Integer.parseInt(partyId), name, userName);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Result.OK(result);
    }

    /**
     * 任务池，所有处于非私密状态的任务
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "任务池", notes = "所有处于非私密状态的任务")
    @GetMapping(value = "/listAllJob")
    @SuppressWarnings("checkstyle:parameternumber")
    public Result<IPage<FlRecruitJobInfo>> listAllJob(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) @ApiParam(name = "name", value = "任务名称", required = false) String name,
        @RequestParam(required = false) @ApiParam(name = "type", value = "任务类型", required = false) String type,
        @RequestParam(required = false) @ApiParam(name = "status", value = "任务状态", required = false) String status,
        HttpServletRequest req) {
        String partyId = req.getHeader("tenant_id");
        Page<FlRecruitJobInfo> page = new Page<>(pageNo, pageSize);
        IPage<FlRecruitJobInfo> pageList = flJobRecruitService.listAllJobPage(page, name, type, status, partyId);
        return Result.OK(pageList);
    }

    /**
     * 生成加入任务的邀请码
     */
    @AutoLog(value = "联邦招募任务-生成加入联邦任务的邀请码")
    @ApiOperation(value = "生成加入联邦任务的邀请码", notes = "生成加入联邦任务的邀请码，result为邀请码")
    @DynamicResponseParameters(
        properties = {@DynamicParameter(name = "code", value = "代码", required = true, dataTypeClass = Integer.class),
            @DynamicParameter(name = "message", value = "消息", required = true, dataTypeClass = String.class),
            @DynamicParameter(name = "result", value = "8位邀请码", required = true, dataTypeClass = String.class),
            @DynamicParameter(name = "success", value = "成功标识", required = true, dataTypeClass = Boolean.class),
            @DynamicParameter(name = "timestamp", value = "时间戳", required = true, dataTypeClass = long.class)})
    @RequestMapping(value = "/inviteCode", method = RequestMethod.GET)
    public Result<?> inviteCode(
        @ApiParam(name = "id", value = "招募任务id", required = true) @RequestParam(name = "id") String id,
        HttpServletRequest req) {
        // 随机数
        String inviteCode = RandomUtil.randomNumbers(8);

        String partyId = req.getHeader("tenant_id");
        if (partyId == null) {
            FlJobRecruit jobRecruit = flJobRecruitService.getById(id);
            partyId = jobRecruit.getPartyId().toString();
        }
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(partyId);
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

        // 邀请码7天内有效
        redisUtil.set("jobInviteCode:" + inviteCode, id + ":" + flPartyInfo.getName() + ":" + sysUser.getUsername(),
            86400 * 7);
        return Result.OK(inviteCode);
    }

    /**
     * 通过邀请码加入任务，报名状态为1已通过
     */
    @AutoLog(value = "联邦招募任务-通过邀请码加入联邦任务")
    @ApiOperation(value = "通过邀请码加入联邦任务", notes = "通过邀请码加入联邦任务")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @RequestMapping(value = "/joinJob", method = RequestMethod.POST)
    @SuppressWarnings({"PMD.MethodTooLongRule", "checkstyle:methodlength"})
    public Result<FlJobRegist> joinParty(
        @RequestParam @ApiParam(name = "inviteCode", value = "邀请码", required = true) String inviteCode,
        HttpServletRequest req) {
        Result<FlJobRegist> result = new Result<>();
        String tenantId = req.getHeader("tenant_id");
        // key为邀请码，value为联邦方id
        Object checkCode = redisUtil.get("jobInviteCode:" + inviteCode);
        if (checkCode == null) {
            result.setSuccess(false);
            result.setMessage("验证码错误");
            return result;
        }

        String checkCodeStr = checkCode.toString();
        String[] checkCodeStrSplit = checkCodeStr.split(":");
        String recruitId = checkCodeStrSplit[0];
        String jobName = flJobRecruitService.getById(recruitId).getName();
        // 判断是否已经加入？，一个联邦方不能加入多次
        FlJobRegist existFlJobRegist = flJobRegistService.getOne(new QueryWrapper<FlJobRegist>().lambda()
            .eq(FlJobRegist::getPartyId, Integer.parseInt(tenantId)).eq(FlJobRegist::getRecruitId, recruitId));
        if (existFlJobRegist != null// NOPMD
            && (existFlJobRegist.getRegistStatus() == 0 || existFlJobRegist.getRegistStatus() == 1)) {
            result.setSuccess(false);
            result.setMessage("已经加入'" + jobName + "'，无需重复加入");
            return result;
        }

        FlJobRegist flJobRegist = new FlJobRegist();
        flJobRegist.setRecruitId(recruitId);
        flJobRegist.setPartyId(Integer.parseInt(tenantId));
        flJobRegist.setRole("host");
        flJobRegist.setRegistStatus(1);

        if (existFlJobRegist != null) {
            flJobRegist.setId(existFlJobRegist.getId());
            flJobRegist.setDataId(null);
            flJobRegistService.updateById(flJobRegist);
        } else {
            flJobRegistService.save(flJobRegist);
        }

        redisUtil.del("jobInviteCode:" + inviteCode);

        // ${partyName}的${userName}邀请了${partyName}的${userName}加入任务
        // --- log start ---
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(tenantId);
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

        FlOpLog flOpLog = new FlOpLog();

        JSONObject content = new JSONObject();
        // 邀请人
        if (checkCodeStrSplit.length == checkCodeStrSplitLength) {
            String inviterPartyName = checkCodeStr.split(":")[1];
            String inviterUserName = checkCodeStr.split(":")[2];
            content.put("guestPartyName", inviterPartyName);
            content.put("guestUserName", inviterUserName);
        }

        // 被邀请人
        content.put("hostPartyName", flPartyInfo.getName());
        content.put("hostUserName", sysUser.getUsername());

        flOpLog.setContent(content.toJSONString());
        flOpLog.setPartyId(Integer.parseInt(tenantId));
        flOpLog.setUrl("/platform/flJobRecruit/joinJob");
        flOpLog.setParam(JSONUtil.toJsonStr(req.getParameterMap()));

        List<FlActivity> flActivityList = new ArrayList<>();

        FlActivity missionActivity = new FlActivity();
        missionActivity.setTypeId(recruitId);
        missionActivity.setType(Dict.MISSION_ACTICITY_TYPE);
        missionActivity.setOpType(Dict.MISSION_INVITE_CODE_JOIN_TYPE);

        flActivityList.add(missionActivity);
        flOpLogService.saveMain(flOpLog, flActivityList);
        // --- log end ---

        result.setSuccess(true);
        result.setMessage("加入'" + jobName + "'成功!");
        result.setResult(flJobRegist);
        return result;
    }

    /**
     * 添加 创建招募任务，JobRegistList为空 创建招募任务并添加发起方报名信息，JobRegistList为报名信息
     *
     * @param flJobRecruitPage
     * @return
     */
    @AutoLog(value = "联邦招募任务表-新增", operateType = OPERATE_TYPE_2)
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FlJobRecruitPage flJobRecruitPage) {
        FlJobRecruit flJobRecruit = new FlJobRecruit();
        BeanUtils.copyProperties(flJobRecruitPage, flJobRecruit);
        flJobRecruitService.saveMain(flJobRecruit, flJobRecruitPage.getFlJobRegistList());
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "联邦招募任务表-创建任务", operateType = OPERATE_TYPE_2)
    @ApiOperation(value = "创建任务", notes = "创建任务")
    @PostMapping(value = "/createJob")
    public Result<?> createJob(@RequestBody FlJobRecruitPage flJobRecruitPage, HttpServletRequest req) {
        FlJobRecruit flJobRecruit = new FlJobRecruit();
        BeanUtils.copyProperties(flJobRecruitPage, flJobRecruit);
        FlJobRegist flJobRegist = new FlJobRegist();
        boolean flag = false;
        // 是否是带数据和算法的添加
        if (flJobRecruitPage.getFlJobRegistList() != null && flJobRecruitPage.getFlJobRegistList().size() > 0
            && StringUtils.isNotEmpty(flJobRecruit.getCompoments())) {
            flag = true;
        }
        if (!flag) {
            flJobRegist.setPartyId(flJobRecruit.getPartyId());
        } else {
            flJobRegist = flJobRecruitPage.getFlJobRegistList().get(0);
        }
        String recruitId = flJobRecruitService.createJob(flJobRecruit, flJobRegist);

        // --- log start ---
        // ${partyName}的${userName}使用xx数据、xx工具创建了任务
        String partyId = req.getHeader("tenant_id");
        if (partyId == null) {
            partyId = flJobRecruit.getPartyId().toString();
        }
        String partyName = flPartyInfoService.getById(partyId).getName();
        String userName = flJobRecruit.getCreateBy();
        List<FlActivity> flActivityList = new ArrayList<>();
        String missionId = flJobRecruit.getId();
        String missionName = flJobRecruit.getName();
        FlOpLog flOpLog = new FlOpLog();
        JSONObject content = new JSONObject();
        content.put("partyName", partyName);
        content.put("userName", userName);
        content.put("missionName", missionName);
        if (flag) {
            String algName =
                JSONUtil.parseObj(JSONUtil.parseArray(flJobRecruit.getCompoments()).get(0).toString()).getStr("name");
            String algId =
                algorithmService.getOne(new QueryWrapper<Algorithm>().lambda().eq(Algorithm::getName, algName)).getId();
            String dataId = flJobRegist.getDataId();
            CollectFileMeta data = collectFileMetaService.getById(dataId);
            content.put("algId", algId);
            content.put("algName", algName);
            content.put("dataId", dataId);
            content.put("dataName", data.getName());
            content.put("dataSize", data.getSize());
            FlActivity dataActivity = new FlActivity();
            dataActivity.setTypeId(dataId);
            dataActivity.setType(Dict.DATA_ACTICITY_TYPE);
            dataActivity.setOpType(Dict.DATA_USE_TYPE);
            FlActivity algActivity = new FlActivity();
            algActivity.setTypeId(algId);
            algActivity.setType(Dict.ALG_ACTICITY_TYPE);
            algActivity.setOpType(Dict.ALG_USE_TYPE);
            flActivityList.add(dataActivity);
            flActivityList.add(algActivity);
        }
        flOpLog.setContent(content.toJSONString());
        flOpLog.setPartyId(Integer.parseInt(partyId));
        flOpLog.setUrl("/platform/flJobRecruit/createJob");
        flOpLog.setParam(JSONUtil.toJsonStr(flJobRecruitPage));
        FlActivity missionActivity = new FlActivity();
        missionActivity.setTypeId(missionId);
        missionActivity.setType(Dict.MISSION_ACTICITY_TYPE);
        missionActivity.setOpType(Dict.MISSION_CREATE_TYPE);
        flActivityList.add(missionActivity);
        flOpLogService.saveMain(flOpLog, flActivityList);
        // --- log end ---
        return Result.OK(recruitId);
    }

    @AutoLog(value = "联邦招募任务表-修改联邦任务的数据、算法和参数", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "修改联邦任务的数据、算法和参数", notes = "")
    @PutMapping(value = "/update")
    public Result<?> update(@RequestBody FlJob4Update flJob4Update, HttpServletRequest req) {
        if (StringUtils.isEmpty(flJob4Update.getId())) {
            return Result.error("参数错误！");
        }
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(flJob4Update.getId());
        if (flJobRecruit == null) {
            return Result.error("任务不存在！");
        }
        flJobRecruitService.update(flJob4Update, flJobRecruit, req);
        return Result.OK(flJobRecruit);
    }

    @AutoLog(value = "联邦招募任务表-修改选择的数据子集的列和特征值", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "联邦招募任务表-修改选择的数据子集的列和特征值", notes = "")
    @PutMapping(value = "/update_column_and_feature")
    public Result<?> updateColumnAndFeature(@RequestBody Map<String, Object> map, HttpServletRequest req) {
        String id = (String)map.get("id");
        if (StringUtils.isEmpty(id)) {
            return Result.error("参数错误！");
        }

        FlJobRecruit flJobRecruit = flJobRecruitService.getById(id);
        if (flJobRecruit == null) {
            return Result.error("任务不存在！");
        }

        String columeAndFeature = (String)map.get("columeAndFeature");
        flJobRecruit.setColumnAndFeature(columeAndFeature);
        flJobRecruitService.updateById(flJobRecruit);
        return Result.OK(flJobRecruit);
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "删除任务", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "通过id删除", notes = "通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id, HttpServletRequest req) {
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(id);
        String tenantId = req.getHeader("tenant_id");
        boolean flag =
            checkPermissionUtil.checkPermission(req, tenantId, flJobRecruit.getCreateBy(), flJobRecruit.getPartyId());
        if (!flag) {
            return Result.error("没有权限");
        }
        // 只能删除训练成功、训练失败的任务
        if (!(RecruitStatusEnum.TRAIN_SUCCESS.getCode().equals(flJobRecruit.getRecruitStatus())
            || RecruitStatusEnum.TRAIN_FAIL.getCode().equals(flJobRecruit.getRecruitStatus()))) {
            return Result.error("当前状态的训练任务不允许删除！");
        }
        String partyId = req.getHeader("tenant_id");
        flJobRecruitService.delMain(id, partyId, flJobRecruit);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "联邦招募任务表-批量删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦招募任务表-批量删除", notes = "联邦招募任务表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flJobRecruitService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "通过id查询联邦招募任务", notes = "联邦招募任务表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        FlJobRecruit flJobRecruit = getFlJobRecruitById(id);
        if (flJobRecruit == null) {
            Result<FlJobRecruitVo> result = new Result();
            result.error500("未找到对应数据");
            return result;
        }
        FlJobRecruitVo flJobRecruitVo = new FlJobRecruitVo();
        BeanUtil.copyProperties(flJobRecruit, flJobRecruitVo);
        FlJobRegist existFlJobRegist = flJobRegistService.getOne(new QueryWrapper<FlJobRegist>().lambda()
            .eq(FlJobRegist::getRecruitId, id).eq(FlJobRegist::getRole, "guest"));
        flJobRecruitVo.setCompleteFlag(true);
        if (flJobRecruit.getCompoments() == null || existFlJobRegist.getDataId() == null) {
            flJobRecruitVo.setCompleteFlag(false);
        }
        // 训练生成的模型id
        if (RecruitStatusEnum.TRAIN_SUCCESS.getCode().equals(flJobRecruit.getRecruitStatus())) {
            FlModelInfo flModelInfo = flModelInfoService.getOne(new QueryWrapper<FlModelInfo>().lambda()
                .eq(FlModelInfo::getModelVersion, flJobRecruit.getJobId()).eq(FlModelInfo::getUserPartyId, tenantId));
            if (flModelInfo != null) {
                FlModelPrivate flModelPrivate = flModelPrivateService.getOne(new QueryWrapper<FlModelPrivate>().lambda()
                    .eq(FlModelPrivate::getModelInfoId, flModelInfo.getId()).eq(FlModelPrivate::getPartyId, tenantId));
                if (flModelPrivate != null) {
                    flJobRecruitVo.setModelId(flModelPrivate.getId());
                }
            }
        }
        FlJobRegist existFlJobRegist1 = flJobRegistService.getOne(new QueryWrapper<FlJobRegist>().lambda()
            .eq(FlJobRegist::getRecruitId, id).eq(FlJobRegist::getPartyId, tenantId));

        // ${partyName}的${userName}浏览了任务
        // --- log start ---
        FlOpLog flOpLog = new FlOpLog();
        JSONObject content = new JSONObject();

        if (tenantId != null && existFlJobRegist1 != null) {
            FlPartyInfo flPartyInfo = flPartyInfoService.getById(tenantId);
            String fateboardUrl = flPartyInfo.getInfob() + "/#/details?job_id=" + flJobRecruit.getJobId() + "&role="
                + existFlJobRegist1.getRole() + "&party_id=" + tenantId;
            content.put("partyName", flPartyInfo.getName());
            flJobRecruitVo.setLogUrl(fateboardUrl);
            flOpLog.setPartyId(Integer.parseInt(tenantId));
        }

        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

        content.put("userName", sysUser.getUsername());

        flOpLog.setContent(content.toJSONString());

        flOpLog.setUrl("/platform/flJobRecruit/queryById");
        flOpLog.setParam(JSONUtil.toJsonStr(req.getParameterMap()));

        List<FlActivity> flActivityList = new ArrayList<>();

        FlActivity missionActivity = new FlActivity();
        missionActivity.setTypeId(id);
        missionActivity.setType(Dict.MISSION_ACTICITY_TYPE);
        missionActivity.setOpType(Dict.MISSION_VIEW_TYPE);

        flActivityList.add(missionActivity);
        flOpLogService.saveMain(flOpLog, flActivityList);
        // --- log end ---

        return Result.OK(flJobRecruitVo);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "发起方数据格式信息", notes = "发起方数据格式信息")
    @GetMapping(value = "/queryFlJobDataInfo")
    public Result<CollectFileMeta> queryFlJobDataInfo(@ApiParam(name = "id", value = "招募任务id",
        required = true) @RequestParam(name = "id", required = true) String id) {

        CollectFileMeta collectFileMeta = flJobRegistService.queryDataInfoByMainId(id);

        return Result.OK(collectFileMeta);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "联邦招募任务的所有报名信息", notes = "联邦招募任务的所有报名信息")
    @GetMapping(value = "/queryFlJobRegistByMainId")
    public Result<?> queryFlJobRegistListByMainId(
        @ApiParam(name = "id", value = "招募任务id", required = true) @RequestParam(name = "id", required = true) String id,
        HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        Map<String, Object> result = new HashMap<>(3);
        Map<String, Object> obj = getFlJobRegistByMainId(id, tenantId);
        FlJobRegist flJobRegist = queryMyFlJobRegistByMainId(id, tenantId);
        Tjob tJob = jobInfoByRecruitId(id);
        result.put("flJobRegistByMainId", obj);
        result.put("myFlJobRegistByMainId", flJobRegist);
        Map<String, Object> tJobMap = new HashMap<>(2);
        if (tJob != null) {
            tJobMap.put("fElapsed", tJob.getFElapsed());
            tJobMap.put("fProgress", tJob.getFProgress());
        }
        result.put("jobInfoByRecruitId", tJobMap);
        return Result.OK(result);
    }

    private FlJobRegist queryMyFlJobRegistByMainId(String id, String tenantId) {
        FlJobRegist flJobRegist = flJobRegistService.getOne(new QueryWrapper<FlJobRegist>().lambda()
            .eq(FlJobRegist::getPartyId, Integer.parseInt(tenantId)).eq(FlJobRegist::getRecruitId, id));
        if (flJobRegist == null) {
            return null;
        }
        // 查询阶段状态
        if (oConvertUtils.isEmpty(flJobRegist.getDataId())) {
            FlPartyInfo flPartyInfo = flPartyInfoService.getById(flJobRegist.getPartyId());
            if (flPartyInfo.getStatus() != null && flPartyInfo.getStatus() == 1) {
                flJobRegist.setEnvStatus(2);
            } else {
                flJobRegist.setEnvStatus(3);
            }
        } else {
            CollectFileMeta collectFileMeta = collectFileMetaService.getById(flJobRegist.getDataId());
            flJobRegist.setEnvStatus(1);
            flJobRegist.setEvalType(collectFileMeta.getEvalType());
            flJobRegist.setLabelName(collectFileMeta.getLabelName());
            flJobRegist.setName(collectFileMeta.getName());
            flJobRegist.setNamespace(collectFileMeta.getEggrollNamespace());
            flJobRegist.setRowsNum(collectFileMeta.getRowsNum());
        }
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(flJobRegist.getRecruitId());
        if (StringUtils.isEmpty(flJobRecruit.getCompoments())) {
            flJobRegist.setEnvStatus(2);
        }
        return flJobRegist;
    }

    /**
     * 模型指标 {"data": [["auc",0.985],["ks",0.872062]],"meta": {"metric_type": "EVALUATION_SUMMARY", "name":
     * "homo_lr_0"},"retcode": 0,"retmsg": "success"}
     *
     * @param jsonObject
     * @return
     */
    @ApiOperation(value = "模型指标", notes = "模型指标")
    @ApiOperationSupport(params = @DynamicParameters(name = "jsonObject",
        properties = {@DynamicParameter(name = "id", value = "任务id", required = true, dataTypeClass = String.class)}))
    @PostMapping("/metric")
    public Result<?> metric(@RequestBody JSONObject jsonObject) {
        String id = jsonObject.getString("id");
        // evaluation_0的上一个组件名称
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(id);
        String jobId = flJobRecruit.getJobId();
        if (oConvertUtils.isEmpty(jobId)) {
            return Result.error("任务id为空");
        }

        try {
            JSONObject result = flJobRecruitService.queryMetric(flJobRecruit);
            if (!result.isEmpty()) {
                return Result.OK(result);
            } else {
                return Result.error("查询失败");
            }
        } catch (Exception e) {
            log.error("connect fateflow error:", e);
            return Result.error(e.getMessage());
        }
    }

    @AutoLog(value = "联邦招募任务-提交任务")
    @ApiOperation(value = "任务提交", notes = "任务提交")
    @PostMapping("/submit")
    public Result<?> submit(
        @ApiParam(name = "id", value = "招募任务id", required = true) @RequestParam(name = "id", required = true) String id,
        HttpServletRequest req) {
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(id);
        if (StringUtils.isEmpty(flJobRecruit.getCompoments())) {
            return Result.error("未选择算法，不允许开启任务！");
        }
        boolean flag = false;
        Map<String, Object> map = getFlJobRegistByMainId(id, null);
        List<FlJobRegistModel> flJobRegistList = (List<FlJobRegistModel>)map.get("flJobRegistList");
        // 所有准备完成的参与方数据
        List<FlJobRegist> jobRegistList = flJobRegistService.selectAvailableByMainId(id, null);
        if (jobRegistList == null || jobRegistList.size() == 0) {
            return Result.error("没有有效的参与方！");
        }
        singleLableMultiFeatures(id, jobRegistList);
        cn.hutool.json.JSONObject dsl = null;
        cn.hutool.json.JSONObject conf = null;
        // 当任务类型是联邦任务，且没有其他联邦方加入时，提示无法开启训练
        if (flJobRecruit.getJobType() == 1) {
            if (flJobRegistList.size() == 1) {
                return Result.error("无其他联邦加入，联邦科研任务无法单独开启训练！");
            }
            int noReadyCount = 0;
            for (FlJobRegistModel flJobRegist : flJobRegistList) {
                if (flJobRegist.getEnvStatus() != 1) {
                    noReadyCount++;
                    flag = true;
                }
            }
            if (flag) {
                return Result.OK("未全部准备好！", (flJobRegistList.size() - noReadyCount) + "/" + flJobRegistList.size());
            }
            dsl = jobConfUtil.generateDsl(flJobRecruit, jobRegistList);
            conf = jobConfUtil.generateConf(flJobRecruit, jobRegistList);
        } else {
            // 任务类型是独立科研时，
            dsl = jobConfUtil.generateDsl(flJobRecruit, jobRegistList);
            conf = jobConfUtil.generateLocalConf(flJobRecruit, jobRegistList.get(0));
        }
        cn.hutool.json.JSONObject jobSubmissionJson = new cn.hutool.json.JSONObject();
        jobSubmissionJson.put("job_dsl", dsl);
        jobSubmissionJson.put("job_runtime_conf", conf);
        log.info(jobSubmissionJson.toString());
        // 提交任务
        // 根据party_id查询fate flow地址
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(flJobRecruit.getPartyId());
        String fateUrl = String.format(fateFlowUrl, flPartyInfo.getNameEn());
        asyncJobService.submit(fateUrl + Dict.URL_JOB_SUBMIT, jobSubmissionJson.toString(), id);
        // 更新conf、dsl、jobid、status
        flJobRecruit.setDsl(dsl.toString());
        flJobRecruit.setConf(conf.toString());
        setLog(id, req, flJobRecruit, flPartyInfo);
        return Result.OK();
    }

    private void singleLableMultiFeatures(String id, List<FlJobRegist> jobRegistList) {
        for (FlJobRegist flJobRegist : jobRegistList) {
            List<FlJobFeature> list = flJobFeatureService.lambdaQuery().eq(FlJobFeature::getJobId, id)
                .eq(FlJobFeature::getPartyId, flJobRegist.getPartyId()).list();
            if (list != null && list.size() > 0) {
                FlPartyInfo flPartyInfo = flPartyInfoService.getById(flJobRegist.getPartyId());
                SelectCollectModel selectCollectModel = new SelectCollectModel();
                selectCollectModel.setJobId(id);
                selectCollectModel.setTableName(list.get(0).getTableName());
                Map<String, String> selectedFeatures = new HashMap<>(list.size());
                for (FlJobFeature flJobFeature : list) {
                    selectedFeatures.put(flJobFeature.getSelectedFeature(), flJobFeature.getRule());
                }
                selectCollectModel.setSelectedFeatures(selectedFeatures);
                log.info("====================>selectCollectModel" + selectCollectModel);
                String result = httpClientPool.postWithoutTimeout(
                    String.format(SELECTED_META_URL, flPartyInfo.getNameEn()), JSON.toJSONString(selectCollectModel));
            }
        }
    }

    private void setLog(String id, HttpServletRequest req, FlJobRecruit flJobRecruit, FlPartyInfo flPartyInfo) {
        // 任务保存到数据库成功
        flJobRecruit.setRecruitStatus(7);
        flJobRecruitService.updateById(flJobRecruit);
        FlOpLog flOpLog = new FlOpLog();
        JSONObject content = new JSONObject();
        content.put("partyName", flPartyInfo.getName());
        content.put("userName", flJobRecruit.getCreateBy());
        flOpLog.setContent(content.toJSONString());
        flOpLog.setPartyId(flPartyInfo.getId());
        flOpLog.setUrl("/platform/flJobRecruit/submit");
        flOpLog.setParam(JSONUtil.toJsonStr(req.getParameterMap()));
        List<FlActivity> flActivityList = new ArrayList<>();
        FlActivity missionActivity = new FlActivity();
        missionActivity.setTypeId(id);
        missionActivity.setType(Dict.MISSION_ACTICITY_TYPE);
        missionActivity.setOpType(Dict.MISSION_SUBMIT_TYPE);
        flActivityList.add(missionActivity);
        flOpLogService.saveMain(flOpLog, flActivityList);
    }

    @AutoLog(value = "联邦招募任务-未全部准备时提交任务")
    @ApiOperation(value = "未全部准备时提交任务", notes = "未全部准备时提交任务")
    @PostMapping("/submitNotAllReady")
    public Result<?> submitNotAllReady(
        @ApiParam(name = "id", value = "招募任务id", required = true) @RequestParam(name = "id", required = true) String id,
        @ApiParam(name = "reserveFlag", value = "是否保留原任务", required = false) @RequestParam(name = "reserveFlag",
            required = false) boolean reserveFlag,
        HttpServletRequest req) {
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(id);
        Map<String, Object> map = getFlJobRegistByMainId(id, null);
        List<FlJobRegist> flJobRegistList = (List<FlJobRegist>)map.get("flJobRegistList");
        String newId = flJobRecruit.getId();
        // 保留原任务时，新建一个只有准备好的联邦方的任务
        if (reserveFlag) {
            FlJobRecruit newFlJobRecruit = new FlJobRecruit();
            BeanUtil.copyProperties(flJobRecruit, newFlJobRecruit);
            newFlJobRecruit.setId(null);
            newFlJobRecruit.setName(flJobRecruit.getName() + "_副本");
            flJobRecruitService.save(newFlJobRecruit);
            for (FlJobRegist flJobRegist : flJobRegistList) {
                if (flJobRegist.getEnvStatus() == 1) {
                    FlJobRegist newFlJobRegist = new FlJobRegist();
                    BeanUtil.copyProperties(flJobRegist, newFlJobRegist);
                    newFlJobRegist.setId(null);
                    newFlJobRegist.setRecruitId(newFlJobRecruit.getId());
                    flJobRegistService.save(newFlJobRegist);
                }
            }
            newId = newFlJobRecruit.getId();
            this.submit(newFlJobRecruit.getId(), req);
        } else {
            // 删除原任务时，直接将未准备好的参与方剔除掉就可以了
            for (FlJobRegist flJobRegist : flJobRegistList) {
                if (flJobRegist.getEnvStatus() != 1) {
                    flJobRegistService.removeById(flJobRegist.getId());
                }
            }
            this.submit(id, req);
        }
        return Result.OK(newId);
    }

    @AutoLog(value = "联邦招募任务-重新提交任务")
    @ApiOperation(value = "重新提交任务", notes = "重新提交任务")
    @PostMapping("/reSubmit")
    public Result<?> reSubmit(@ApiParam(name = "id", value = "招募任务id", required = true) @RequestParam(name = "id",
        required = true) String id) {
        // 查询任务params
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(id);

        cn.hutool.json.JSONObject dsl = cn.hutool.json.JSONUtil.parseObj(flJobRecruit.getDsl());
        cn.hutool.json.JSONObject conf = cn.hutool.json.JSONUtil.parseObj(flJobRecruit.getConf());

        cn.hutool.json.JSONObject jobSubmissionJson = new cn.hutool.json.JSONObject();
        jobSubmissionJson.put("job_dsl", dsl);
        jobSubmissionJson.put("job_runtime_conf", conf);
        log.info(jobSubmissionJson.toString());

        // 提交任务
        // 根据party_id查询fate flow地址
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(flJobRecruit.getPartyId());
        String fateUrl = String.format(fateFlowUrl, flPartyInfo.getNameEn());

        asyncJobService.submit(fateUrl + Dict.URL_JOB_SUBMIT, jobSubmissionJson.toString(), id);

        // 更新jobid、status
        flJobRecruit.setJobId("");
        flJobRecruit.setMessage("");
        // 任务保存到数据库成功
        flJobRecruit.setRecruitStatus(7);
        flJobRecruitService.updateById(flJobRecruit);
        return Result.OK();
    }

    @ApiOperation(value = "根据报名id查询任务执行信息", notes = "根据报名id查询任务执行信息")
    @PostMapping("/jobInfo")
    public Result<Tjob> jobInfo(@ApiParam(name = "registId", value = "报名id",
        required = true) @RequestParam(name = "registId", required = true) String registId, HttpServletRequest req) {
        FlJobRegist flJobRegist = flJobRegistService.getById(registId);

        // 查询任务params
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(flJobRegist.getRecruitId());

        String jobId = flJobRecruit.getJobId();
        String role = flJobRegist.getRole();
        Integer partyId = flJobRegist.getPartyId();

        // 查询数据库
        List<Tjob> jobList = tJobService.list(new QueryWrapper<Tjob>().lambda().eq(Tjob::getFJobId, jobId)
        // .eq(Tjob::getFRole, role)
        // .eq(Tjob::getFPartyId, partyId)
        );
        if (jobList.size() > 0) {
            return Result.OK(jobList.get(0));
        } else {
            return Result.OK();
        }
    }

    @ApiOperation(value = "根据招募id查询任务执行信息", notes = "根据招募id查询任务执行信息")
    @PostMapping("/jobInfoByRecruitId")
    public Result<Tjob>
        jobInfoByRecruitId(@ApiParam(name = "recruitId", value = "招募id",
            required = true) @RequestParam(name = "recruitId", required = true) String recruitId,
            HttpServletRequest req) {
        return Result.OK(jobInfoByRecruitId(recruitId));
    }

    private Tjob jobInfoByRecruitId(String recruitId) {
        // 查询任务params
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(recruitId);

        String jobId = flJobRecruit.getJobId();
        String role = "guest";
        Integer partyId = flJobRecruit.getPartyId();

        // 查询数据库
        List<Tjob> jobList = tJobService.list(new QueryWrapper<Tjob>().lambda().eq(Tjob::getFJobId, jobId)
            .eq(Tjob::getFRole, role).eq(Tjob::getFPartyId, partyId));
        if (jobList.size() > 0) {
            Tjob tJob = jobList.get(0);
            if (tJob.getFElapsed() != null && tJob.getFElapsed() != 0) {
                tJob.setFElapsed(tJob.getFElapsed() / 1000);
            } else {
                tJob.setFElapsed(Math.toIntExact(System.currentTimeMillis() - tJob.getFStartTime()) / 1000);
            }
            return jobList.get(0);
        } else {
            return null;
        }
    }

    /**
     * @param nums
     * @param req
     * @return
     */
    @ApiOperation(value = "热门任务查询", notes = "热门任务查询")
    @GetMapping(value = "/hotJob")
    public Result<?> hotJob(@RequestParam(name = "nums", defaultValue = "3") Integer nums, HttpServletRequest req) {
        String partyId = req.getHeader("tenant_id");
        List<Integer> partyIdList = flPartyInfoService.getExperienceListById(partyId);
        List<String> recruitIdList =
            flOpLogService.selectHotTypeId(nums, Dict.MISSION_ACTICITY_TYPE, Dict.MISSION_VIEW_TYPE);
        JSONArray resultArray = new JSONArray();
        for (String recruitId : recruitIdList) {
            // 根据recruitid查询任务基本信息
            FlJobRecruit flJobRecruit = getFlJobRecruitById(recruitId);
            // 根据recruitid查询参与方信息
            Map<String, Object> obj = getFlJobRegistByMainId(recruitId, null);
            obj.put("flJobRecruit", flJobRecruit);
            resultArray.add(obj);
        }

        return Result.OK(resultArray);
    }

    /**
     * 根据id查询招募任务详情
     *
     * @param id
     * @return
     */
    private FlJobRecruit getFlJobRecruitById(String id) {
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(id);
        if (flJobRecruit == null) {
            return null;
        }

        if (flJobRecruit.getJobId() != null) {
            // 查询任务表获取任务执行成功与否
            Tjob tjob =
                tJobService.getOne(new QueryWrapper<Tjob>().lambda().eq(Tjob::getFPartyId, flJobRecruit.getPartyId())
                    .eq(Tjob::getFRole, "guest").eq(Tjob::getFJobId, flJobRecruit.getJobId()));
            // waiting、ready、running、canceled、timeout、failed、success
            // 3任务训练成功，4任务训练失败，5任务取消，6任务正在运行中，7任务保存成功，8任务等待调度
            if (tjob != null) {
                switch (tjob.getFStatus()) {
                    case "success":
                        flJobRecruit.setRecruitStatus(3);
                        break;
                    case "failed":
                    case "timeout":
                        flJobRecruit.setRecruitStatus(4);
                        break;
                    case "canceled":
                        flJobRecruit.setRecruitStatus(5);
                        break;
                    case "running":
                        flJobRecruit.setRecruitStatus(6);
                        break;
                    case "waiting":
                    case "ready":
                        flJobRecruit.setRecruitStatus(8);
                        break;
                    default:
                        break;
                }
            }
        }
        return flJobRecruit;
    }

    /**
     * 根据招募任务id查询参与方详情
     *
     * @param recruitId
     * @param partyId
     * @return
     */
    private Map<String, Object> getFlJobRegistByMainId(String recruitId, String partyId) {
        try {
            List<FlJobRegist> flJobRegistList = flJobRegistService.selectByMainId(recruitId, 1, null);
            int partyNums = flJobRegistList.size();

            int rowNums = 0;
            List<FlJobRegistModel> returnList = new ArrayList<>();
            List<FlJobRegistModel> otherList = new ArrayList<>();
            for (FlJobRegist flJobRegist : flJobRegistList) {
                if (flJobRegist.getRowsNum() != null) {
                    rowNums = rowNums + flJobRegist.getRowsNum();
                }
                Set<String> notReady = null;
                // 准备状态
                FlPartyInfo flPartyInfo = flPartyInfoService.getById(flJobRegist.getPartyId());
                if (specialEdgeEnabled && specialEdge.equals(String.valueOf(flPartyInfo.getId()))) {
                    notReady = new HashSet<>();
                } else {
                    notReady = readyStatusChkService.notReadyAppComponents(flPartyInfo.getNameEn());
                }
                // 1都ok，2数据不ok环境ok，3都不ok，4数据ok环境不ok
                // 数据不ok
                if (oConvertUtils.isEmpty(flJobRegist.getDataId())) {
                    // 环境ok
                    if (notReady.size() == 0) {
                        flJobRegist.setEnvStatus(2);
                    } else {
                        flJobRegist.setEnvStatus(3);
                    }
                    // 数据ok
                } else {
                    // 环境ok
                    if (notReady.size() == 0) {
                        flJobRegist.setEnvStatus(1);
                    } else {
                        // 环境不ok
                        flJobRegist.setEnvStatus(4);
                    }
                }

                flJobRegist.setPartyAvatar(flPartyInfo.getAvatar());
                flJobRegist.setPartyName(flPartyInfo.getName());
                if (partyId != null && String.valueOf(flJobRegist.getPartyId()).equals(partyId)) {
                    FlJobRegistModel flJobRegistModel = new FlJobRegistModel();
                    BeanUtil.copyProperties(flJobRegist, flJobRegistModel);
                    flJobRegistModel.setCanShowDetail(true);
                    returnList.add(flJobRegistModel);
                } else {
                    FlJobRegistModel flJobRegistModel = new FlJobRegistModel();
                    flJobRegistModel.setCanShowDetail(false);
                    BeanUtil.copyProperties(flJobRegist, flJobRegistModel);
                    otherList.add(flJobRegistModel);
                }
            }
            returnList.addAll(otherList);
            returnList.sort(Comparator.comparing(FlJobRegistModel::getCreateTime));
            Map<String, Object> obj = new HashMap<>(3);
            obj.put("flJobRegistList", returnList);
            obj.put("partyNums", partyNums);
            obj.put("rowNums", rowNums);
            return obj;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 添加预处理配置
     * 
     * @param flJobPreproccessForCreate
     * @param req
     * @return
     */
    @ApiOperation(value = "添加预处理配置", notes = "添加预处理配置")
    @PostMapping(value = "/addPreproccess")
    public Result<?> addPreproccess(@RequestBody FlJobPreproccessForCreate flJobPreproccessForCreate,
        HttpServletRequest req) {

        try {
            if (StringUtils.isEmpty(flJobPreproccessForCreate.getJobId())) {
                return Result.error("任务id不能为空");
            }
            if (flJobPreproccessForCreate.getPreproccessAlgorithmList() == null
                || flJobPreproccessForCreate.getPreproccessAlgorithmList().size() == 0) {
                return Result.error("预处理算法不能为空");
            }

            // 删除原有预处理配置
            flJobPreproccessService.remove(new QueryWrapper<FlJobPreproccess>().lambda().eq(FlJobPreproccess::getJobId,
                flJobPreproccessForCreate.getJobId()));
            // TODO 检查算法是否存在
            for (FlJobPreproccessForCreate.PreproccessAlgorithm preproccessAlgorithm : flJobPreproccessForCreate
                .getPreproccessAlgorithmList()) {
                FlJobPreproccess flJobPreproccess = new FlJobPreproccess();

                BeanUtils.copyProperties(flJobPreproccessForCreate, flJobPreproccess);
                flJobPreproccess.setAlgorithmName(preproccessAlgorithm.getAlgorithmName());
                flJobPreproccess.setAlgorithmConf(preproccessAlgorithm.getAlgorithmConf());
                String userId = JwtUtil.getUserNameByToken(req);
                flJobPreproccess.setCreateBy(userId);
                flJobPreproccess.setCreateTime(new Date());

                flJobPreproccessService.save(flJobPreproccess);
            }
            return Result.OK("添加成功！");
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error("添加失败！");
        }
    }

    /**
     * 通过job_id查询预处理算法列表
     * 
     * @param jobId
     * @return
     */
    @ApiOperation(value = "通过job_id查询预处理算法列表", notes = "通过job_id查询预处理算法列表")
    @GetMapping(value = "/preproccessList")
    public Result<List<FlJobPreproccess>> preproccessList(@RequestParam String jobId) {
        Result<List<FlJobPreproccess>> result = new Result<>();
        try {
            List<FlJobPreproccess> flJobRecruitList = flJobPreproccessService
                .list(new QueryWrapper<FlJobPreproccess>().eq("job_id", jobId).orderByDesc("create_time"));
            result.setResult(flJobRecruitList);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }
}
