package org.jeecg.modules.federalml.controller;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.FlConf;
import org.jeecg.modules.federalml.entity.FlSysVersion;
import org.jeecg.modules.federalml.service.IFlConfService;
import org.jeecg.modules.federalml.service.IFlSysVersionService;
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
 * @Description: 联邦系统配置表
 * @Author: jeecg-boot
 * @Date: 2021-06-28
 * @Version: V1.0
 */
@Api(tags = "联邦系统配置表", hidden = true)
@RestController
@RequestMapping("/federalml/flConf")
@Slf4j
public class FlConfController extends JeecgController<FlConf, IFlConfService> {
    @Autowired
    private IFlConfService flConfService;
    @Autowired
    private IFlSysVersionService flSysVersionService;

    /**
     * 分页列表查询
     *
     * @param flConf
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "联邦系统配置表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FlConf flConf, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FlConf> queryWrapper = QueryGenerator.initQueryWrapper(flConf, req.getParameterMap());
        Page<FlConf> page = new Page<FlConf>(pageNo, pageSize);
        IPage<FlConf> pageList = flConfService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "系统更新日志")
    @ApiOperation(value = "系统更新日志", notes = "系统更新日志")
    @GetMapping(value = "/version/list")
    public Result<?> queryVersion() {
        List<FlSysVersion> list = flSysVersionService.list();
        return Result.OK(list);
    }

    /**
     * 添加
     *
     * @param flConf
     * @return
     */
    @AutoLog(value = "联邦系统配置表-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FlConf flConf) {
        flConfService.save(flConf);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param flConf
     * @return
     */
    @AutoLog(value = "联邦系统配置表-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FlConf flConf) {
        flConfService.updateById(flConf);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦系统配置表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        flConfService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "联邦系统配置表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flConfService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦系统配置表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        FlConf flConf = flConfService.getById(id);
        if (flConf == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(flConf);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param flConf
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FlConf flConf) {
        return super.exportXls(request, flConf, FlConf.class, "联邦系统配置表");
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
        return super.importExcel(request, response, FlConf.class);
    }

}
