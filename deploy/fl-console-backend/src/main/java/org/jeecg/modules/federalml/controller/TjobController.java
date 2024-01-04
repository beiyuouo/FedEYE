package org.jeecg.modules.federalml.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.federalml.entity.FlJobRecruit;
import org.jeecg.modules.federalml.entity.FlModelInfo;
import org.jeecg.modules.federalml.entity.Tjob;
import org.jeecg.modules.federalml.enums.TrainRoleEnum;
import org.jeecg.modules.federalml.service.IFlJobRecruitService;
import org.jeecg.modules.federalml.service.IFlModelInfoService;
import org.jeecg.modules.federalml.service.ItJobService;
import org.jeecg.modules.federalml.utils.JobStatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import io.dapr.client.domain.CloudEvent;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: t_job
 * @Author: jeecg-boot
 * @Date: 2021-05-27
 * @Version: V1.0
 */
@RestController
@RequestMapping("/federalml/tJob")
@Slf4j
public class TjobController extends JeecgController<Tjob, ItJobService> {
    @Autowired
    private ItJobService tJobService;
    @Autowired
    private IFlModelInfoService flModelInfoService;
    @Autowired
    private IFlJobRecruitService flJobRecruitService;

    /**
     * 分页列表查询
     *
     * @param tJob
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<?> queryPageList(Tjob tJob, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {

        Map<String, String[]> params = new HashMap<>(req.getParameterMap());
        params.replace("column", new String[] {"f_create_time"});

        QueryWrapper<Tjob> queryWrapper = QueryGenerator.initQueryWrapper(tJob, params);

        Page<Tjob> page = new Page<Tjob>(pageNo, pageSize);
        IPage<Tjob> pageList = tJobService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param tJob
     * @return
     */
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody Tjob tJob) {
        tJobService.save(tJob);
        return Result.OK("添加成功！");
    }

    /**
     * 接收并更新边缘端发送的训练任务信息
     *
     * @param body
     * @return
     */
    @AutoLog(value = "训练任务管理-训练模型任务更新")
    @ApiOperation(value = "训练任务管理-训练模型任务更新", notes = "训练任务管理-训练模型任务更新")
    @PostMapping(value = "/upinsert")
    public Result<?> upinsert(@RequestBody(required = false) byte[] body) {
        try {
            CloudEvent event = CloudEvent.deserialize(body);
            String jsonStr = (String)event.getData();

            JSONArray array = JSONUtil.parseArray(jsonStr);
            List<Tjob> jobs = JSONUtil.toList(array, Tjob.class);

            for (Tjob job : jobs) {
                UpdateWrapper<Tjob> updateWrapper =
                    new UpdateWrapper<Tjob>().eq("f_job_id", job.getFJobId()).eq("f_role", job.getFRole())
                        .eq("f_party_id", job.getFPartyId());

                // 更新任务表
                tJobService.saveOrUpdate(job, updateWrapper);

                int statusCode = JobStatusUtil.statusCode(job.getFStatus());
                // 更新模型任务状态，上传下载的角色为local
                if ("local".equals(job.getFRole())) {
                    QueryWrapper<FlModelInfo> wrapperFlModelInfo = new QueryWrapper<FlModelInfo>();
                    wrapperFlModelInfo.eq("job_id", job.getFJobId());
                    FlModelInfo flModelInfo = flModelInfoService.getOne(wrapperFlModelInfo);
                    // 成功时执行模型迁移
                    if ("success".equals(job.getFStatus())) {
                        flModelInfoService.modelMigrate(flModelInfo);
                    } else {
                        flModelInfo.setJobStatus(statusCode);
                    }
                    flModelInfoService.updateById(flModelInfo);
                }

                // 更新训练任务状态，角色为guest
                if (TrainRoleEnum.guest.toString().equals(job.getFRole())) {
                    UpdateWrapper<FlJobRecruit> updateWrapperFlJobRecruit = new UpdateWrapper<FlJobRecruit>();
                    updateWrapperFlJobRecruit.eq("job_id", job.getFJobId()).set("recruit_status", statusCode);
                    flJobRecruitService.update(null, updateWrapperFlJobRecruit);
                }

                log.info("upinsert job success: " + job);
            }
        } catch (IOException e) {
            log.error("upinsert job faild: " + e.toString());
            throw new RuntimeException(e);
        }
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param tJob
     * @return
     */
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody Tjob tJob) {
        tJobService.updateById(tJob);
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
        tJobService.removeById(id);
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
        this.tJobService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        Tjob tJob = tJobService.getById(id);
        if (tJob == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(tJob);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param tJob
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Tjob tJob) {
        return super.exportXls(request, tJob, Tjob.class, "t_job");
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
        return super.importExcel(request, response, Tjob.class);
    }

}
