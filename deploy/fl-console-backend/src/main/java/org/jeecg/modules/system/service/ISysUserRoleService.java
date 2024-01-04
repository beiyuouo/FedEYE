package org.jeecg.modules.system.service;

import java.util.Map;

import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户角色表 服务类
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface ISysUserRoleService extends IService<SysUserRole> {

    void saveFlUserRoleWithUpdateUser(SysUserRole sysFlUserRole, LoginUser user);

    void saveFlUserRoleWithUpdateUser1(SysUserRole sysFlUserRole, SysUser user);

    boolean isPartyUser(String userName, String partyId);

}
