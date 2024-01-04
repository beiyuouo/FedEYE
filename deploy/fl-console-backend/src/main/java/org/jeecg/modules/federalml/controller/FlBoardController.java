package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.FmlBoard;
import org.jeecg.modules.federalml.service.IFmlBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 联邦学习仪表盘
 * @Author: jeecg-boot
 * @Date: 2021-05-27
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/fmlBoard")
@Slf4j
public class FlBoardController extends JeecgController<FmlBoard, IFmlBoardService> {
    @Autowired
    private IFmlBoardService fmlBoardService;

    /**
     * 分页列表查询
     *
     * @param fmlBoard
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "联邦学习仪表盘-分页列表查询", notes = "联邦学习仪表盘-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FmlBoard fmlBoard, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FmlBoard> queryWrapper = QueryGenerator.initQueryWrapper(fmlBoard, req.getParameterMap());
        Page<FmlBoard> page = new Page<FmlBoard>(pageNo, pageSize);
        IPage<FmlBoard> pageList = fmlBoardService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param fmlBoard
     * @return
     */
    @AutoLog(value = "联邦学习仪表盘-添加", operateType = OPERATE_TYPE_2)
    @ApiOperation(value = "联邦学习仪表盘-添加", notes = "联邦学习仪表盘-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FmlBoard fmlBoard) {
        fmlBoardService.save(fmlBoard);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param fmlBoard
     * @return
     */
    @AutoLog(value = "联邦学习仪表盘-编辑", operateType = OPERATE_TYPE_3)
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FmlBoard fmlBoard) {
        fmlBoardService.updateById(fmlBoard);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦学习仪表盘-通过id删除", operateType = OPERATE_TYPE_4)
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        fmlBoardService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "联邦学习仪表盘-批量删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦学习仪表盘-批量删除", notes = "联邦学习仪表盘-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.fmlBoardService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦学习仪表盘-通过id查询")
    @ApiOperation(value = "联邦学习仪表盘-通过id查询", notes = "联邦学习仪表盘-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        FmlBoard fmlBoard = fmlBoardService.getById(id);
        if (fmlBoard == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(fmlBoard);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param fmlBoard
     */
    @AutoLog(value = "联邦学习仪表盘-导出学习仪表盘", operateType = OPERATE_TYPE_6)
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FmlBoard fmlBoard) {
        return super.exportXls(request, fmlBoard, FmlBoard.class, "联邦学习仪表盘");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @AutoLog(value = "联邦学习仪表盘-导入学习仪表盘", operateType = OPERATE_TYPE_5)
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FmlBoard.class);
    }

}
