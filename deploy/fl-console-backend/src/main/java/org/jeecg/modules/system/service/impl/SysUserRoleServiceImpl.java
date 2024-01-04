package org.jeecg.modules.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserRoleMapper;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 用户角色表 服务实现类
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

    @Resource
    SysUserRoleMapper sysUserRoleMapper;
    @Resource
    SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public void saveFlUserRoleWithUpdateUser(SysUserRole sysFlUserRole, LoginUser user) {
        //删除联邦方id为null的用户角色
        sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().lambda()
                .eq(SysUserRole::getUserId, sysFlUserRole.getUserId())
                .isNull(SysUserRole::getPartyId));
        //保存用户角色
        sysUserRoleMapper.insert(sysFlUserRole);
        //更新用户表租户字段
        sysUserMapper.updateTenantIds(user.getId(), user.getRelTenantIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public void saveFlUserRoleWithUpdateUser1(SysUserRole sysFlUserRole, SysUser user) {
        //删除联邦方id为null的用户角色
        sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().lambda()
                .eq(SysUserRole::getUserId, sysFlUserRole.getUserId())
                .isNull(SysUserRole::getPartyId));
        //保存用户角色
        sysUserRoleMapper.insert(sysFlUserRole);
        //更新用户表租户字段
        sysUserMapper.updateTenantIds(user.getId(), user.getRelTenantIds());
    }

    /**
     * 判断是否是联邦方普通用户
     * @param userName
     * @param partyId
     * @return
     */
    @Override
    public boolean isPartyUser(String userName, String partyId) {
        List<String> list = sysUserRoleMapper.getCountByUserNameAndPartyId(userName, partyId);
        return list.size() != 0;
    }
}
