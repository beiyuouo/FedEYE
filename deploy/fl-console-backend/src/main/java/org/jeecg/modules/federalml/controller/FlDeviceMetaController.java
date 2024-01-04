package org.jeecg.modules.federalml.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.DeviceMeta;
import org.jeecg.modules.federalml.service.IDeviceMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 联邦设备元信息表
 * @Author: jeecg-boot
 * @Date: 2021-03-09
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/deviceMeta")
@Slf4j
public class FlDeviceMetaController extends JeecgController<DeviceMeta, IDeviceMetaService> {
    @Autowired
    private IDeviceMetaService deviceMetaService;

    /**
     * 分页列表查询
     *
     * @param deviceMeta
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "联邦设备元信息表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(DeviceMeta deviceMeta,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<DeviceMeta> queryWrapper = QueryGenerator.initQueryWrapper(deviceMeta, req.getParameterMap());
        Page<DeviceMeta> page = new Page<DeviceMeta>(pageNo, pageSize);
        IPage<DeviceMeta> pageList = deviceMetaService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param deviceMeta
     * @return
     */
    @AutoLog(value = "联邦设备元信息表-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody DeviceMeta deviceMeta) {
        deviceMetaService.save(deviceMeta);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param deviceMeta
     * @return
     */
    @AutoLog(value = "联邦设备元信息表-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody DeviceMeta deviceMeta) {
        deviceMetaService.updateById(deviceMeta);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦设备元信息表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        deviceMetaService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "联邦设备元信息表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.deviceMetaService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦设备元信息表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        DeviceMeta deviceMeta = deviceMetaService.getById(id);
        if (deviceMeta == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(deviceMeta);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param deviceMeta
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, DeviceMeta deviceMeta) {
        return super.exportXls(request, deviceMeta, DeviceMeta.class, "联邦设备元信息表");
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
        return super.importExcel(request, response, DeviceMeta.class);
    }

}
