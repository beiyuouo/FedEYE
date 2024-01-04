package org.jeecg.modules.federalml.controller;

import java.util.Set;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.federalml.service.IFlReadyStatusChkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 软硬件环境状态检查 @Author: Maxm @Date: 2021-08-03 @Version: V1.0
 */
@Api(tags = "软硬件环境状态检查", hidden = true)
@RestController
@RequestMapping("/federalml/readyStatus")
@Slf4j
public class FlReadyStatusChkController {

    @Autowired
    private IFlReadyStatusChkService readyStatusChkService;
    @Value("${features.specialEdgeEnabled}")
    private boolean specialEdgeEnabled;
    final String specailEdgeName = "edge1008";

    /**
     * 硬件状态检查
     *
     * @param partyName
     * @return
     */
    @ApiOperation(value = "软硬件环境状态检查-硬件状态检查", notes = "软硬件环境状态检查-硬件状态检查")
    @GetMapping(value = "/nodeStatus")
    public Result<?> nodeStatus(@RequestParam(name = "partyName", required = true) String partyName) {
        if (specialEdgeEnabled) {
            if (specailEdgeName.equals(partyName)) {
                return Result.OK("node status is ready");
            }
        }
        if (readyStatusChkService.nodeStatus(partyName)) {
            return Result.OK("node status is ready");
        }
        return Result.error(400, "node status is notReady");
    }

    /**
     * 软件状态检查
     *
     * @param partyName
     * @return
     */
    @ApiOperation(value = "软硬件环境状态检查-软件状态检查", notes = "软硬件环境状态检查-软件状态检查")
    @GetMapping(value = "/appStatus")
    public Result<?> appStatus(@RequestParam(name = "partyName", required = true) String partyName) {
        if (specialEdgeEnabled) {
            if (specailEdgeName.equals(partyName)) {
                return Result.OK();
            }
        }
        Set<String> notReady = readyStatusChkService.notReadyAppComponents(partyName);

        if (notReady.size() == 0) {
            return Result.OK();
        } else {
            return Result.error(400, "app status is notReady: " + notReady);
        }
    }
}
