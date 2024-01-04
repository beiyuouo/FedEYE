package org.jeecg.modules.federalml.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.FlDataPermission;
import org.jeecg.modules.federalml.service.IFlDataPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 数据权限表
 * @Author: jeecg-boot
 * @Date: 2021-07-06
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/flDataPermission")
@Slf4j
public class FlDataPermissionController extends JeecgController<FlDataPermission, IFlDataPermissionService> {
    @Autowired
    private IFlDataPermissionService flDataPermissionService;

    /**
     * 分页列表查询
     *
     * @param flDataPermission
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "数据权限表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FlDataPermission flDataPermission,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FlDataPermission> queryWrapper =
            QueryGenerator.initQueryWrapper(flDataPermission, req.getParameterMap());
        Page<FlDataPermission> page = new Page<FlDataPermission>(pageNo, pageSize);
        IPage<FlDataPermission> pageList = flDataPermissionService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param flDataPermission
     * @return
     */
    @AutoLog(value = "数据权限表-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FlDataPermission flDataPermission) {
        flDataPermissionService.save(flDataPermission);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param flDataPermission
     * @return
     */
    @AutoLog(value = "数据权限表-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FlDataPermission flDataPermission) {
        flDataPermissionService.updateById(flDataPermission);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "数据权限表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        flDataPermissionService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "数据权限表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flDataPermissionService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "数据权限表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        FlDataPermission flDataPermission = flDataPermissionService.getById(id);
        if (flDataPermission == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(flDataPermission);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param flDataPermission
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FlDataPermission flDataPermission) {
        return super.exportXls(request, flDataPermission, FlDataPermission.class, "数据权限表");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FlDataPermission.class);
    }

}
