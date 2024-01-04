package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.federalml.entity.FlPartyInfo;
import org.jeecg.modules.federalml.service.IFlPartyInfoService;
import org.jeecg.modules.federalml.vo.FlPartyInfoVo;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;

import cn.hutool.core.util.RandomUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.utils.StringUtils;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @Description: 联邦方基础信息
 * @Author: jeecg-boot
 * @Date: 2021-07-09
 * @Version: V1.0
 */
@Api(tags = "1.4 联邦方管理")
@RestController
@RequestMapping("/platform/flPartyInfo")
@Slf4j
public class FlPartyInfoController extends JeecgController<FlPartyInfo, IFlPartyInfoService> {
    @Autowired
    private IFlPartyInfoService flPartyInfoService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ISysUserRoleService sysUserRoleService;
    @Autowired
    private ISysUserService sysUserService;

    final String comma = ",";

    /**
     * 分页列表查询
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<?> queryPageList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        FlPartyInfo flPartyInfo = new FlPartyInfo();
        QueryWrapper<FlPartyInfo> queryWrapper = QueryGenerator.initQueryWrapper(flPartyInfo, req.getParameterMap());
        Page<FlPartyInfo> page = new Page<FlPartyInfo>(pageNo, pageSize);
        IPage<FlPartyInfo> pageList = flPartyInfoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    // 联邦方数目

    @ApiOperation(value = "查询用户所属的所有联邦方", notes = "查询用户所属的所有联邦方")
    @GetMapping(value = "/queryPartyInfo")
    public Result<List<FlPartyInfoVo>> queryPartyInfo() {

        LoginUser user = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        String relTenantIds = user.getRelTenantIds();
        if (oConvertUtils.isEmpty(relTenantIds)) {
            return Result.OK();
        }
        List<FlPartyInfoVo> flPartyInfoVoList =
            flPartyInfoService.getPartyInfoByUser(Arrays.asList(relTenantIds.split(",")));
        return Result.OK(flPartyInfoVoList);
    }

    /**
     * 添加
     *
     * @param flPartyInfo
     * @return
     */
    @AutoLog(value = "联邦方基础信息-添加", operateType = OPERATE_TYPE_2)
    @ApiOperation(value = "联邦方基础信息-添加", notes = "联邦方基础信息-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FlPartyInfo flPartyInfo) {

        flPartyInfoService.save(flPartyInfo);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param flPartyInfo
     * @return
     */
    @AutoLog(value = "联邦方基础信息-编辑", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "联邦方基础信息-编辑", notes = "联邦方基础信息-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FlPartyInfo flPartyInfo) {
        flPartyInfoService.updateById(flPartyInfo);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦方基础信息-删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦方基础信息-通过id删除", notes = "联邦方基础信息-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        flPartyInfoService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "联邦方基础信息-批量删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦方基础信息-批量删除", notes = "联邦方基础信息-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flPartyInfoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "联邦方基础信息-通过id查询", notes = "联邦方基础信息-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FlPartyInfo> queryById(@RequestParam(name = "id", required = true) String id) {
        FlPartyInfo flPartyInfo = flPartyInfoService.getById(id);
        return Result.OK(flPartyInfo);
    }

    /**
     * 校验联邦方名称是否唯一<br> 可以校验其他 需要检验什么就传什么。。。
     *
     * @param partyInfo
     * @return
     */
    @ApiOperation(value = "检查联邦方是否已存在", notes = "检查联邦方是否已存在，需要检验什么就传什么")
    @DynamicResponseParameters(
        properties = {@DynamicParameter(name = "code", value = "代码", required = true, dataTypeClass = Integer.class),
            @DynamicParameter(name = "message", value = "消息", required = true, dataTypeClass = String.class),
            @DynamicParameter(name = "result", value = "数据", required = true, dataTypeClass = String.class),
            @DynamicParameter(name = "success", value = "是否唯一标识", required = true, dataTypeClass = Boolean.class),
            @DynamicParameter(name = "timestamp", value = "时间戳", required = true, dataTypeClass = long.class)})
    @RequestMapping(value = "/checkOnlyParty", method = RequestMethod.POST)
    public Result<Boolean> checkOnlyParty(@RequestBody FlPartyInfo partyInfo) {
        Result<Boolean> result = new Result<>();
        // 如果此参数为false则程序发生异常
        result.setResult(true);
        try {
            if (partyInfo.getId() == null || StringUtils.isEmpty(partyInfo.getNameEn()) || StringUtils
                .isEmpty(partyInfo.getName())) {
                result.setSuccess(false);
                result.setMessage("参数错误！");
                return result;
            }
            // 通过传入信息查询新的用户信息
            if (flPartyInfoService.check(partyInfo)) {
                result.setSuccess(false);
                result.setMessage("联邦方已存在");
                return result;
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            return result;
        }
        result.setSuccess(true);
        return result;
    }

    /**
     * 联邦方注册接口
     *
     * @param partyInfo
     * @return
     */
    @AutoLog(value = "联邦方基础信息-联邦方注册")
    @ApiOperation(value = "联邦方注册", notes = "联邦方注册")
    @PostMapping("/register")
    public Result<JSONObject> register(@RequestBody FlPartyInfo partyInfo) {
        LoginUser user = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        Result<JSONObject> result = new Result<>();
        if (partyInfo.getId() == null || StringUtils.isEmpty(partyInfo.getNameEn()) || StringUtils
            .isEmpty(partyInfo.getName())) {
            result.setSuccess(false);
            result.setMessage("参数错误！");
            return result;
        }
        // 通过传入信息查询新的用户信息
        if (flPartyInfoService.check(partyInfo)) {
            result.setSuccess(false);
            result.setMessage("联邦方已存在");
            return result;
        }
        try {
            // 设置创建时间
            partyInfo.setCreateTime(new Date());
            // 创建联邦方，更新用户联邦方字段，添加用户联邦方角色
            // 默认管理员角色
            flPartyInfoService.addPartyWithUpdateUserRole(user, partyInfo, "1417211262740447233");
            result.success("联邦方注册成功");
        } catch (Exception e) {
            result.error500("联邦方注册失败");
        }
        return result;
    }

    /**
     * 生成加入联邦方的邀请码
     */
    @ApiOperation(value = "生成加入联邦方的邀请码", notes = "生成加入联邦方的邀请码")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @DynamicResponseParameters(
        properties = {@DynamicParameter(name = "code", value = "代码", required = true, dataTypeClass = Integer.class),
            @DynamicParameter(name = "message", value = "消息", required = true, dataTypeClass = String.class),
            @DynamicParameter(name = "result", value = "8位邀请码", required = true, dataTypeClass = String.class),
            @DynamicParameter(name = "success", value = "成功标识", required = true, dataTypeClass = Boolean.class),
            @DynamicParameter(name = "timestamp", value = "时间戳", required = true, dataTypeClass = long.class)})
    @RequestMapping(value = "/inviteCode", method = RequestMethod.GET)
    public Result<String> inviteCode(HttpServletRequest req) {
        Result<String> result = new Result<String>();
        String tenantId = req.getHeader("tenant_id");
        // 随机数
        String inviteCode = RandomUtil.randomNumbers(8);

        // 邀请码10天内有效
        redisUtil.set("partyInviteCode:" + inviteCode, tenantId, 86400 * 10);

        result.setResult(inviteCode);
        result.setSuccess(true);

        return result;
    }

    /**
     * 通过邀请码加入联邦方，并赋予默认角色
     */
    @AutoLog(value = "联邦方基础信息-通过邀请码加入联邦方")
    @ApiOperation(value = "通过邀请码加入联邦方", notes = "通过邀请码加入联邦方")
    @DynamicResponseParameters(
        properties = {@DynamicParameter(name = "code", value = "代码", required = true, dataTypeClass = Integer.class),
            @DynamicParameter(name = "message", value = "加入'partyName'成功!", required = true,
                dataTypeClass = String.class),
            @DynamicParameter(name = "result", value = "partyName", required = true, dataTypeClass = String.class),
            @DynamicParameter(name = "success", value = "成功标识", required = true, dataTypeClass = Boolean.class),
            @DynamicParameter(name = "timestamp", value = "时间戳", required = true, dataTypeClass = long.class)})
    @RequestMapping(value = "/joinParty", method = RequestMethod.POST)
    public Result<?> joinParty(
        @RequestParam @ApiParam(name = "inviteCode", value = "邀请码", required = true) String inviteCode) {
        LoginUser user = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        // key为邀请码，value为联邦方id
        Object checkCode = redisUtil.get("partyInviteCode:" + inviteCode);
        if (checkCode == null) {
            return Result.error("邀请码无效！");
        }
        int partyId = Integer.parseInt(checkCode.toString());
        String partyName = flPartyInfoService.getById(partyId).getName();
        // 判断是否已经加入
        String partyIds = user.getRelTenantIds();
        String newPartyIds = "";
        if (!oConvertUtils.isEmpty(partyIds)) {
            for (String id : partyIds.split(comma)) {
                if (partyId == Integer.parseInt(id)) {
                    return Result.error("您已经加入'" + partyName + "'，无需重复加入");
                }
            }
            newPartyIds = partyIds + "," + partyId;
        } else {
            newPartyIds = String.valueOf(partyId);
        }
        // 默认用户角色
        SysUserRole sysFlUserRole = new SysUserRole(user.getId(), "1415192088216326146", partyId, 1);

        user.setRelTenantIds(newPartyIds);
        // 更新用户表租户字段，保存用户角色
        sysUserRoleService.saveFlUserRoleWithUpdateUser(sysFlUserRole, user);
        redisUtil.del("partyInviteCode:" + inviteCode);
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("partyId", partyId);
        resultMap.put("partyName", partyName);
        return Result.OK("加入'" + partyName + "'成功!", resultMap);
    }

    @ApiIgnore
    @ApiOperation(value = "通过联邦方id和用户id加入联邦方", notes = "")
    @DynamicResponseParameters(
        properties = {@DynamicParameter(name = "code", value = "代码", required = true, dataTypeClass = Integer.class),
            @DynamicParameter(name = "message", value = "加入'partyName'成功!", required = true,
                dataTypeClass = String.class),
            @DynamicParameter(name = "result", value = "partyName", required = true, dataTypeClass = String.class),
            @DynamicParameter(name = "success", value = "成功标识", required = true, dataTypeClass = Boolean.class),
            @DynamicParameter(name = "timestamp", value = "时间戳", required = true, dataTypeClass = long.class)})
    @RequestMapping(value = "/joinPartyByPartyId", method = RequestMethod.POST)
    public boolean joinPartyByPartyId(
        @RequestParam @ApiParam(name = "partyId", value = "联邦方id", required = true) Integer partyId,
        @RequestParam @ApiParam(name = "userId", value = "用户id", required = true) String userId) {
        try {
            SysUser user = sysUserService.getById(userId);
            String partyName = flPartyInfoService.getById(partyId).getName();
            // 判断是否已经加入
            String partyIds = user.getRelTenantIds();
            String newPartyIds = "";
            if (!oConvertUtils.isEmpty(partyIds)) {
                for (String id : partyIds.split(comma)) {
                    if (partyId.equals(Integer.parseInt(id))) {
                        return false;
                    }
                }
                newPartyIds = partyIds + "," + partyId;
            } else {
                newPartyIds = String.valueOf(partyId);
            }
            // 默认用户角色
            SysUserRole sysFlUserRole = new SysUserRole(user.getId(), "1415192088216326146", partyId, 1);

            user.setRelTenantIds(newPartyIds);
            sysUserRoleService.saveFlUserRoleWithUpdateUser1(sysFlUserRole, user);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

}
