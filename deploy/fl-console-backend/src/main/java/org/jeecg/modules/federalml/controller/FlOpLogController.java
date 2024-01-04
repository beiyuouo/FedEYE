package org.jeecg.modules.federalml.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.federalml.entity.FlActivity;
import org.jeecg.modules.federalml.entity.FlOpLog;
import org.jeecg.modules.federalml.service.IFlActivityService;
import org.jeecg.modules.federalml.service.IFlOpLogService;
import org.jeecg.modules.federalml.vo.FlOpLogPage;
import org.jeecg.modules.federalml.vo.FlOpLogView;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: fl_op_log
 * @Author: jeecg-boot
 * @Date: 2022-01-18
 * @Version: V1.0
 */
@Api(tags = "操作日志记录")
@RestController
@RequestMapping("/federalml/flOpLog")
@Slf4j
public class FlOpLogController {
    @Autowired
    private IFlOpLogService flOpLogService;
    @Autowired
    private IFlActivityService flActivityService;

    /**
     * 任务操作记录查询
     *
     * @param pageNo
     * @param pageSize
     * @param id
     * @param opType
     * @return
     */
    @ApiOperation(value = "任务操作记录查询", notes = "任务操作记录查询")
    @PostMapping(value = "/missionOpLog")
    public Result<IPage<FlOpLogView>> missionOpLog(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = true) @ApiParam(name = "id", value = "任务id", required = true) String id,
        @RequestParam(required = false) @ApiParam(name = "opType", value = "操作类型", required = false) String opType) {
        Page<FlOpLogView> page = new Page<FlOpLogView>(pageNo, pageSize);
        IPage<FlOpLogView> pageList = flOpLogService.selectByTypeId(page, id, "1", opType);
        return Result.OK(pageList);
    }

    /**
     * 数据最近使用记录查询
     *
     * @param pageNo
     * @param pageSize
     * @param id
     * @param opType
     * @return
     */
    @ApiOperation(value = "数据最近使用记录查询", notes = "数据最近使用记录查询")
    @PostMapping(value = "/dataOpLog")
    public Result<IPage<FlOpLogView>> dataOpLog(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = true) @ApiParam(name = "id", value = "数据id", required = true) String id,
        @RequestParam(required = false, defaultValue = "1") @ApiParam(name = "opType", value = "操作类型，默认1使用了",
            required = false) String opType) {
        Page<FlOpLogView> page = new Page<FlOpLogView>(pageNo, pageSize);
        IPage<FlOpLogView> pageList = flOpLogService.selectByTypeId(page, id, "2", opType);
        return Result.OK(pageList);
    }

    /**
     * 算法最近使用记录查询
     *
     * @param pageNo
     * @param pageSize
     * @param id
     * @param opType
     * @return
     */
    @ApiOperation(value = "算法最近使用记录查询", notes = "算法最近使用记录查询")
    @PostMapping(value = "/algOpLog")
    public Result<IPage<FlOpLogView>> algOpLog(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = true) @ApiParam(name = "id", value = "算法id", required = true) String id,
        @RequestParam(required = false, defaultValue = "1") @ApiParam(name = "opType", value = "操作类型，默认1使用了",
            required = false) String opType) {
        Page<FlOpLogView> page = new Page<FlOpLogView>(pageNo, pageSize);
        IPage<FlOpLogView> pageList = flOpLogService.selectByTypeId(page, id, "3", opType);
        return Result.OK(pageList);
    }

    /**
     * 模型最近使用记录查询
     *
     * @param pageNo
     * @param pageSize
     * @param id
     * @param opType
     * @return
     */
    @ApiOperation(value = "模型最近使用记录查询", notes = "模型最近使用记录查询")
    @PostMapping(value = "/modelOpLog")
    public Result<IPage<FlOpLogView>> modelOpLog(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = true) @ApiParam(name = "id", value = "模型id", required = true) String id,
        @RequestParam(required = false, defaultValue = "1") @ApiParam(name = "opType", value = "操作类型，默认1使用了",
            required = false) String opType) {
        Page<FlOpLogView> page = new Page<FlOpLogView>(pageNo, pageSize);
        IPage<FlOpLogView> pageList = flOpLogService.selectByTypeId(page, id, "4", opType);
        return Result.OK(pageList);
    }

    /**
     * 分页列表查询
     *
     * @param flOpLog
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FlOpLog flOpLog, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FlOpLog> queryWrapper = QueryGenerator.initQueryWrapper(flOpLog, req.getParameterMap());
        Page<FlOpLog> page = new Page<FlOpLog>(pageNo, pageSize);
        IPage<FlOpLog> pageList = flOpLogService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param flOpLogPage
     * @return
     */
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FlOpLogPage flOpLogPage) {
        FlOpLog flOpLog = new FlOpLog();
        BeanUtils.copyProperties(flOpLogPage, flOpLog);
        flOpLogService.saveMain(flOpLog, flOpLogPage.getFlActivityList());
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param flOpLogPage
     * @return
     */
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FlOpLogPage flOpLogPage) {
        FlOpLog flOpLog = new FlOpLog();
        BeanUtils.copyProperties(flOpLogPage, flOpLog);
        FlOpLog flOpLogEntity = flOpLogService.getById(flOpLog.getId());
        if (flOpLogEntity == null) {
            return Result.error("未找到对应数据");
        }
        flOpLogService.updateMain(flOpLog, flOpLogPage.getFlActivityList());
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        flOpLogService.delMain(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.flOpLogService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        FlOpLog flOpLog = flOpLogService.getById(id);
        if (flOpLog == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(flOpLog);

    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryFlActivityByMainId")
    public Result<?> queryFlActivityListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<FlActivity> flActivityList = flActivityService.selectByMainId(id);
        return Result.OK(flActivityList);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param flOpLog
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FlOpLog flOpLog) {
        // Step.1 组装查询条件查询数据
        QueryWrapper<FlOpLog> queryWrapper = QueryGenerator.initQueryWrapper(flOpLog, request.getParameterMap());
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

        //Step.2 获取导出数据
        List<FlOpLog> queryList = flOpLogService.list(queryWrapper);
        // 过滤选中数据
        String selections = request.getParameter("selections");
        List<FlOpLog> flOpLogList = new ArrayList<FlOpLog>();
        if (oConvertUtils.isEmpty(selections)) {
            flOpLogList = queryList;
        } else {
            List<String> selectionList = Arrays.asList(selections.split(","));
            flOpLogList =
                queryList.stream().filter(item -> selectionList.contains(item.getId())).collect(Collectors.toList());
        }

        // Step.3 组装pageList
        List<FlOpLogPage> pageList = new ArrayList<FlOpLogPage>();
        for (FlOpLog main : flOpLogList) {
            FlOpLogPage vo = new FlOpLogPage();
            BeanUtils.copyProperties(main, vo);
            List<FlActivity> flActivityList = flActivityService.selectByMainId(main.getId());
            vo.setFlActivityList(flActivityList);
            pageList.add(vo);
        }

        // Step.4 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "fl_op_log列表");
        mv.addObject(NormalExcelConstants.CLASS, FlOpLogPage.class);
        mv.addObject(NormalExcelConstants.PARAMS,
            new ExportParams("fl_op_log数据", "导出人:" + sysUser.getRealname(), "fl_op_log"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
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
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<FlOpLogPage> list = ExcelImportUtil.importExcel(file.getInputStream(), FlOpLogPage.class, params);
                for (FlOpLogPage page : list) {
                    FlOpLog po = new FlOpLog();
                    BeanUtils.copyProperties(page, po);
                    flOpLogService.saveMain(po, page.getFlActivityList());
                }
                return Result.OK("文件导入成功！数据行数:" + list.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("文件导入失败:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.OK("文件导入失败！");
    }

}
