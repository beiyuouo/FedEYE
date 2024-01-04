package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.FmlParty;
import org.jeecg.modules.federalml.service.IFmlPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 联邦方管理
 * @Author: jeecg-boot
 * @Date: 2021-06-02
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/fmlParty")
@Slf4j
public class FlPartyController extends JeecgController<FmlParty, IFmlPartyService> {
    @Autowired
    private IFmlPartyService fmlPartyService;

    /**
     * 分页列表查询
     *
     * @param fmlParty
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "联邦方管理-分页列表查询", notes = "联邦方管理-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FmlParty fmlParty, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<FmlParty> queryWrapper = QueryGenerator.initQueryWrapper(fmlParty, req.getParameterMap());
        Page<FmlParty> page = new Page<FmlParty>(pageNo, pageSize);
        IPage<FmlParty> pageList = fmlPartyService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 接收并更新边缘端发送的联邦方信息
     *
     * @param body
     * @return
     */
    //    @AutoLog(value = "联邦方管理-边缘端更新", operateType = OPERATE_TYPE_3)
    //    @ApiOperation(value = "联邦方管理-边缘端更新", notes = "联邦方管理-边缘端更新")
    //    @PostMapping(value = "/upinsert")
    //    public Result<?> upinsert(@RequestBody(required = false) byte[] body) {
    //        try {
    //            CloudEvent event = CloudEvent.deserialize(body);
    //            String jsonStr = (String) event.getData();
    //
    //            FmlParty party = JSONUtil.toBean(jsonStr, FmlParty.class);
    //            // convert seconds to milliseconds
    //            // party.setUpdateTime(new Date(party.getHeartbeatTs()*1000));
    //
    //            UpdateWrapper<FmlParty> updateWrapper = new UpdateWrapper<FmlParty>()
    //                    .eq("party_id", party.getPartyId());
    //
    //            fmlPartyService.saveOrUpdate(party, updateWrapper);
    //        } catch (IOException e) {
    //            throw new RuntimeException(e);
    //        }
    //        return Result.OK("添加成功！");
    //    }

    /**
     * 添加
     *
     * @param fmlParty
     * @return
     */
    @AutoLog(value = "联邦方管理-添加", operateType = OPERATE_TYPE_2)
    @ApiOperation(value = "联邦方管理-添加", notes = "联邦方管理-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FmlParty fmlParty) {
        fmlPartyService.save(fmlParty);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param fmlParty
     * @return
     */
    @AutoLog(value = "联邦方管理-编辑", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "联邦方管理-编辑", notes = "联邦方管理-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FmlParty fmlParty) {
        fmlPartyService.updateById(fmlParty);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "联邦方管理-删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦方管理-通过id删除", notes = "联邦方管理-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        fmlPartyService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "联邦方管理-批量删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦方管理-批量删除", notes = "联邦方管理-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.fmlPartyService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "联邦方管理-通过id查询", notes = "联邦方管理-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        FmlParty fmlParty = fmlPartyService.getById(id);
        if (fmlParty == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(fmlParty);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param fmlParty
     */
    @AutoLog(value = "联邦方管理-导出", operateType = OPERATE_TYPE_6)
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FmlParty fmlParty) {
        return super.exportXls(request, fmlParty, FmlParty.class, "联邦方管理");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @AutoLog(value = "联邦方管理-导入", operateType = OPERATE_TYPE_5)
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FmlParty.class);
    }

}
