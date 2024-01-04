package cn.sdaict.fldatauploader.utils;

import org.apache.ibatis.annotations.*;

import java.util.LinkedHashMap;
import java.util.List;

@Mapper
public interface PublicSqlMapper {
    /**
     * 通用查询
     *
     * @return
     */
    @Select("${sql}")
    List<LinkedHashMap<String, Object>> select(String sql);

    /**
     * 新增
     *
     * @param sql
     * @return
     */
    @Insert("${sql}")
    int insert(String sql);

    /**
     * 修改
     *
     * @param sql
     * @return
     */
    @Update("${sql}")
    int update(String sql);

    /**
     * 删除
     *
     * @param sql
     * @return
     */
    @Delete("${sql}")
    int delete(String sql);
}


