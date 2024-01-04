package org.jeecg.modules.federalml.controller;

import static java.lang.Math.min;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.federalml.entity.Algorithm;
import org.jeecg.modules.federalml.entity.AlgorithmModel;
import org.jeecg.modules.federalml.entity.FlJobRecruit;
import org.jeecg.modules.federalml.global.Dict;
import org.jeecg.modules.federalml.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: TODO
 * @author: zhaoxh
 * @date: 2022年02月11日 8:43
 */

@Api(tags = "大屏显示")
@RestController
@RequestMapping("/federalml/bigScreen")
@Slf4j
public class BigScreenController {
    @Autowired
    private IFlPartyInfoService flPartyInfoService;
    @Autowired
    private IFlJobRecruitService flJobRecruitService;
    @Autowired
    private IAlgorithmService algorithmService;
    @Autowired
    private ICollectFileMetaService collectFileMetaService;
    @Autowired
    private IFlOpLogService flOpLogService;

    @Autowired
    private IFlClusterervice flClusterService;

    @ApiOperation(value = "首页个数展示", notes = "首页个数展示")
    @GetMapping(value = "/num")
    public Result<?> num(HttpServletRequest req) {
        Map<String, Integer> map = new HashMap<>(3);
        map.put("partyNum", flPartyInfoService.count());
        map.put("jobNum", flJobRecruitService.count());
        map.put("metaNum", collectFileMetaService.count());
        return Result.OK(map);
    }
    // 联邦方个数、设备个数、任务个数、算法个数、数据集个数

    @ApiOperation(value = "联邦方个数", notes = "联邦方个数")
    @GetMapping(value = "/partyNum")
    public Result<?> partyNum(HttpServletRequest req) {
        return Result.OK(flPartyInfoService.count());
    }

    @ApiOperation(value = "设备个数", notes = "设备个数")
    @GetMapping(value = "/deviceNum")
    public Result<?> deviceNum(HttpServletRequest req) {
        KubernetesClient client = new DefaultKubernetesClient();
        return Result.OK(client.nodes().list().getItems().size());
    }

    @ApiOperation(value = "任务个数", notes = "任务个数")
    @GetMapping(value = "/jobNum")
    public Result<?> jobNum(HttpServletRequest req) {
        return Result.OK(flJobRecruitService.count());
    }

    @ApiOperation(value = "算法个数", notes = "算法个数")
    @GetMapping(value = "/algorithmNum")
    public Result<?> algorithmNum(HttpServletRequest req) {
        return Result
            .OK(algorithmService.count((new QueryWrapper<Algorithm>().lambda().like(Algorithm::getStatus, 1))));
    }

    @ApiOperation(value = "数据集个数", notes = "数据集个数")
    @GetMapping(value = "/fileNum")
    public Result<?> fileNum(HttpServletRequest req) {
        return Result.OK(collectFileMetaService.count());
    }

    @ApiOperation(value = "热门任务查询", notes = "热门任务查询，显示任务名称、创建时间、联邦方名称")
    @GetMapping(value = "/hotJob")
    public Result<?> hotJob(@RequestParam(name = "nums", defaultValue = "10") Integer nums, HttpServletRequest req) {
        List<String> recruitIdList =
            flOpLogService.selectHotTypeId(nums, Dict.MISSION_ACTICITY_TYPE, Dict.MISSION_VIEW_TYPE);

        JSONArray resultArray = new JSONArray();
        for (String recruitId : recruitIdList) {
            FlJobRecruit flJobRecruit = flJobRecruitService.getById(recruitId);
            JSONObject job = new JSONObject();
            job.put("name", flJobRecruit.getName());
            job.put("time", flJobRecruit.getCreateTime());
            job.put("partyName", flPartyInfoService.getById(flJobRecruit.getPartyId()).getName());
            resultArray.add(job);
        }

        return Result.OK(resultArray);
    }

    @ApiOperation(value = "热门算法查询", notes = "热门算法查询，显示算法名称、使用次数")
    @GetMapping(value = "/hotAlg")
    public Result<?> hotAlg(@RequestParam(name = "nums", defaultValue = "10") Integer nums, HttpServletRequest req) {
        Page<AlgorithmModel> page = new Page<>(1, 100);
        IPage<AlgorithmModel> pageList = algorithmService.listAllAlgorithmPage(page, null);
        List<AlgorithmModel> list = pageList.getRecords();
        list.sort((o1, o2) -> {
            if (o2.getCounts() > o1.getCounts()) {
                return 1;
            } else if (o2.getCounts().equals(o1.getCounts())) {
                return 0;
            } else {
                return -1;
            }
        });

        return Result.OK(list.subList(0, min(nums - 1, list.size() - 1)));
    }

    /**
     * 每天发起的任务数
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "每天发起的任务数", notes = "每天发起的任务数，最近七天")
    @GetMapping(value = "/jobNumsByDay")
    public Result<?> jobNumsByDay(HttpServletRequest req) {
        // 最近七天
        return Result.OK(flJobRecruitService.jobNumsByDay());
    }

    /**
     * 任务状态统计：招募中、训练中、训练完成、训练失败
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "任务状态统计", notes = "任务状态统计：招募中、训练中、训练完成、训练失败")
    @GetMapping(value = "/jobNumsByStatus")
    public Result<?> jobNumsByStatus(HttpServletRequest req) {
        // 最近七天
        return Result.OK(flJobRecruitService.jobNumsByStatus());
    }

    /**
     * 资源使用量
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "资源使用量", notes = "资源使用量")
    @GetMapping(value = "/clusterSourceInfo")
    public Result<?> getClusterSourceInfo(HttpServletRequest req) {
        return Result.OK(flClusterService.getClusterSourceInfo());
    }

}
