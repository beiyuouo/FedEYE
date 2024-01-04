package org.jeecg.modules.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.federalml.entity.TmachineLearningModelInfo;
import org.jeecg.modules.federalml.service.IFlModelInfoService;
import org.jeecg.modules.federalml.service.ItMachineLearningModelInfoService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Description: 模型同步
 * @Author: jeecg-boot
 * @Date:
 * @Version: V1.0
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Slf4j
public class ModelSyncJob implements Job {
    @Autowired
    private ItMachineLearningModelInfoService tMachineLearningModelInfoService;
    @Autowired
    private IFlModelInfoService flModelInfoService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 取待同步的模型
        List<TmachineLearningModelInfo> list = tMachineLearningModelInfoService.successModel();

        if(list.size()>0){
            for (TmachineLearningModelInfo modelInfo: list) {
                flModelInfoService.modelInfoSync(modelInfo);
            }
            log.info(" --- 模型同步完毕，个数："+list.size()+"---");
        }
    }

}
