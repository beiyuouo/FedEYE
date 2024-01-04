package org.jeecg.modules.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 用户角色表 Mapper 接口
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select("select role_code from sys_role where id in (select role_id from sys_user_role where user_id = (select id from sys_user where username=#{username}))")
    List<String> getRoleByUserName(@Param("username") String username);

    @Select("select id from sys_role where id in (select role_id from sys_user_role where user_id = (select id from sys_user where username=#{username}))")
    List<String> getRoleIdByUserName(@Param("username") String username);

    @Select("SELECT\n" +
            "\tid\n" +
            "FROM\n" +
            "\tsys_user_role t1 \n" +
            "WHERE\n" +
            "\tt1.user_id = ( SELECT id FROM sys_user WHERE username = #{username} )  \n" +
            "\tAND t1.party_id = #{partyId} \n" +
            "\tAND t1.role_id =(\n" +
            "\tSELECT\n" +
            "\t\tid \n" +
            "\tFROM\n" +
            "\t\tsys_fl_role \n" +
            "WHERE\n" +
            "\trole_code = 'fluser')")
    List<String> getCountByUserNameAndPartyId(@Param("username") String username, @Param("partyId") String partyId);

}
