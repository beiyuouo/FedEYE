package org.jeecg.modules.quartz.job;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeCondition;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.federalml.entity.DeviceMeta;
import org.jeecg.modules.federalml.service.IDeviceMetaService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static io.fabric8.kubernetes.api.model.Quantity.getAmountInBytes;

/**
 * 定期获取各个边缘节点设备信息
 *
 * @author: MaXM
 * @date: 2021年03月05日
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Slf4j
public class GetNodeStatusJob implements Job {
    @Autowired
    private IDeviceMetaService deviceMetaService;

    private KubernetesClient client = new DefaultKubernetesClient();


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info(" --- 定期获取各个边缘节点设备信息任务调度开始 --- ");
        Map<String, DeviceMeta> deviceMetaMap = new HashMap<>();
        List<Node> nodeList = client.nodes().list().getItems();
        for (Node node : nodeList) {
            DeviceMeta deviceMeta = new DeviceMeta();

            deviceMeta.setUpdateTime(new Date());

            deviceMeta.setName(node.getMetadata().getName());
            // TODO 后续抽取 magic string 为变量
            deviceMeta.setPartyName(node.getMetadata().getLabels().getOrDefault("partyName", "None"));
            deviceMeta.setInternalIp(node.getMetadata().getLabels().getOrDefault("k3s.io/internal-ip", "None"));

            List<NodeCondition> nodeConditionList = node.getStatus().getConditions();
            Optional<NodeCondition> nodeCondition = nodeConditionList.stream().filter(condition -> "Ready".equals(condition.getType())).findFirst();
            String status = nodeCondition.isPresent() && "True".equals(nodeCondition.get().getStatus()) ? "Ready" : "Not Ready";

            deviceMeta.setStatus(status);
            deviceMeta.setOs(node.getStatus().getNodeInfo().getOsImage());

            deviceMeta.setCpuCapacity(getAmountInBytes(node.getStatus().getCapacity().get("cpu")).doubleValue());
            deviceMeta.setMemCapacity(getAmountInBytes(node.getStatus().getCapacity().get("memory")).doubleValue());

            deviceMeta.setCpuAllocatable(getAmountInBytes(node.getStatus().getAllocatable().get("cpu")).doubleValue());
            deviceMeta.setMemAllocatable(getAmountInBytes(node.getStatus().getAllocatable().get("memory")).doubleValue());

            // 赋默认值 0
            deviceMeta.setCpuUsage(0.0);
            deviceMeta.setMemUsage(0.0);

            deviceMetaMap.put(deviceMeta.getName(), deviceMeta);
        }

        NodeMetricsList nodeMetricsList = client.top().nodes().metrics();
        for (NodeMetrics nodeMetrics : nodeMetricsList.getItems()) {
            DeviceMeta deviceMeta = deviceMetaMap.get(nodeMetrics.getMetadata().getName());
            deviceMeta.setCpuUsage(getAmountInBytes(nodeMetrics.getUsage().get("cpu")).doubleValue());
            deviceMeta.setMemUsage(getAmountInBytes(nodeMetrics.getUsage().get("memory")).doubleValue());
        }

        List<DeviceMeta> saveDeviceMetaList = new ArrayList<>();

        List<DeviceMeta> oldDeviceMetaList = deviceMetaService.list();
        Map<String, DeviceMeta> oldDeviceMetaMap = new HashMap<>(oldDeviceMetaList.size());
        oldDeviceMetaList.forEach(deviceMeta -> oldDeviceMetaMap.put(deviceMeta.getName(), deviceMeta));

        deviceMetaMap.values().forEach(deviceMeta -> {
            DeviceMeta oldDeviceMeta = oldDeviceMetaMap.getOrDefault(deviceMeta.getName(), null);
            String id = oldDeviceMeta != null ? oldDeviceMeta.getId() : null;
            deviceMeta.setId(id);
            saveDeviceMetaList.add(deviceMeta);
        });

        deviceMetaService.saveOrUpdateBatch(saveDeviceMetaList);

        log.info(" --- 定期获取各个边缘节点设备信息执行完毕，时间：" + DateUtils.now() + " ---");
    }

}
