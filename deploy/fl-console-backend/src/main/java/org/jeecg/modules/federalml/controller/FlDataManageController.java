package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.FlDataManage;
import org.jeecg.modules.federalml.service.IFlDataManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 联邦数据管理
 * @Author: jeecg-boot
 * @Date: 2021-06-28
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/flDataManage")
@Slf4j
public class FlDataManageController extends JeecgController<FlDataManage, IFlDataManageService> {
    @Autowired
    private IFlDataManageService flDataManageService;

    /**
     * 分页列表查询
     *
     * @param flDataManage
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "联邦数据管理-分页列表查询", notes = "联邦数据管理-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FlDataManage flDataManage,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FlDataManage> queryWrapper = QueryGenerator.initQueryWrapper(flDataManage, req.getParameterMap());
        Page<FlDataManage> page = new Page<FlDataManage>(pageNo, pageSize);
        IPage<FlDataManage> pageList = flDataManageService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param flDataManage
     * @return
     */
    @AutoLog(value = "联邦数据管理-添加", operateType = OPERATE_TYPE_2)
    @ApiOperation(value = "联邦数据管理-添加", notes = "联邦数据管理-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FlDataManage flDataManage) {
        flDataManageService.save(flDataManage);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param flDataManage
     * @return
     */
    @AutoLog(value = "联邦数据管理-编辑", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "联邦数据管理-编辑", notes = "联邦数据管理-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FlDataManage flDataManage) {
        flDataManageService.updateById(flDataManage);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦数据管理-通过id删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦数据管理-通过id删除", notes = "联邦数据管理-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        flDataManageService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "联邦数据管理-批量删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦数据管理-批量删除", notes = "联邦数据管理-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flDataManageService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦数据管理-通过id查询")
    @ApiOperation(value = "联邦数据管理-通过id查询", notes = "联邦数据管理-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        FlDataManage flDataManage = flDataManageService.getById(id);
        if (flDataManage == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(flDataManage);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param flDataManage
     */
    @AutoLog(value = "联邦数据管理-导出", operateType = OPERATE_TYPE_6)
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FlDataManage flDataManage) {
        return super.exportXls(request, flDataManage, FlDataManage.class, "联邦数据管理");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @AutoLog(value = "联邦数据管理-导入", operateType = OPERATE_TYPE_5)
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FlDataManage.class);
    }

}
