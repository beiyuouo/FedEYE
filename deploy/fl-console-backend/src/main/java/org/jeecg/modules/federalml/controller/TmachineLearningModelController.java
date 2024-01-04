package org.jeecg.modules.federalml.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.TmachineLearningModel;
import org.jeecg.modules.federalml.service.ItMachineLearningModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: t_machine_learning_model
 * @Author: jeecg-boot
 * @Date: 2021-11-15
 * @Version: V1.0
 */
@Api(tags = "共享模型存储表")
@RestController
@RequestMapping("/federalml/tMachineLearningModel")
@Slf4j
public class TmachineLearningModelController
    extends JeecgController<TmachineLearningModel, ItMachineLearningModelService> {
    @Autowired
    private ItMachineLearningModelService tMachineLearningModelService;

    /**
     * 分页列表查询
     *
     * @param tMachineLearningModel
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "t_machine_learning_model-分页列表查询", notes = "t_machine_learning_model-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(TmachineLearningModel tMachineLearningModel,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<TmachineLearningModel> queryWrapper =
            QueryGenerator.initQueryWrapper(tMachineLearningModel, req.getParameterMap());
        Page<TmachineLearningModel> page = new Page<TmachineLearningModel>(pageNo, pageSize);
        IPage<TmachineLearningModel> pageList = tMachineLearningModelService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param tMachineLearningModel
     * @return
     */
    @ApiOperation(value = "t_machine_learning_model-添加", notes = "t_machine_learning_model-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody TmachineLearningModel tMachineLearningModel) {
        tMachineLearningModelService.save(tMachineLearningModel);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param tMachineLearningModel
     * @return
     */
    @ApiOperation(value = "t_machine_learning_model-编辑", notes = "t_machine_learning_model-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody TmachineLearningModel tMachineLearningModel) {
        tMachineLearningModelService.updateById(tMachineLearningModel);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "t_machine_learning_model-通过id删除", notes = "t_machine_learning_model-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        tMachineLearningModelService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @ApiOperation(value = "t_machine_learning_model-批量删除", notes = "t_machine_learning_model-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.tMachineLearningModelService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "t_machine_learning_model-通过id查询", notes = "t_machine_learning_model-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        TmachineLearningModel tMachineLearningModel = tMachineLearningModelService.getById(id);
        if (tMachineLearningModel == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(tMachineLearningModel);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param tMachineLearningModel
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, TmachineLearningModel tMachineLearningModel) {
        return super.exportXls(request, tMachineLearningModel, TmachineLearningModel.class, "t_machine_learning_model");
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
        return super.importExcel(request, response, TmachineLearningModel.class);
    }

}
