package org.jeecg.modules.federalml.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.federalml.entity.FlModelManage;
import org.jeecg.modules.federalml.service.IFlModelManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 模型管理表
 * @Author: jeecg-boot
 * @Date: 2021-06-25
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/flModelManage")
@Slf4j
public class FlModelManageController extends JeecgController<FlModelManage, IFlModelManageService> {
    @Autowired
    private IFlModelManageService flModelManageService;

    /**
     * 分页列表查询
     *
     * @param flModelManage
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "模型管理表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FlModelManage flModelManage,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        Page<FlModelManage> page = new Page<FlModelManage>(pageNo, pageSize);
        IPage<FlModelManage> pageList = flModelManageService.pageNew(page);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param flModelManage
     * @return
     */
    @AutoLog(value = "模型管理表-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FlModelManage flModelManage) {
        flModelManageService.save(flModelManage);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param flModelManage
     * @return
     */
    @AutoLog(value = "模型管理表-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FlModelManage flModelManage) {
        flModelManageService.updateById(flModelManage);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "模型管理表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        flModelManageService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "模型管理表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flModelManageService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "模型管理表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        FlModelManage flModelManage = flModelManageService.getById(id);
        if (flModelManage == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(flModelManage);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param flModelManage
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FlModelManage flModelManage) {
        return super.exportXls(request, flModelManage, FlModelManage.class, "模型管理表");
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
        return super.importExcel(request, response, FlModelManage.class);
    }

}
