package org.jeecg.modules.kafka;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.federalml.entity.CollectFileMeta;
import org.jeecg.modules.federalml.entity.CollectFileMetaModel;
import org.jeecg.modules.federalml.entity.FlJobRecruit;
import org.jeecg.modules.federalml.entity.FlJobRegist;
import org.jeecg.modules.federalml.enums.ResourceIdPrefixEnum;
import org.jeecg.modules.federalml.enums.TrainRoleEnum;
import org.jeecg.modules.federalml.service.ICollectFileMetaService;
import org.jeecg.modules.federalml.service.IFlJobRecruitService;
import org.jeecg.modules.federalml.service.IFlJobRegistService;
import org.jeecg.modules.federalml.utils.HttpClientPool;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.util.SourceIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.utils.StringUtils;

/**
 * @author zhaoxh
 */
@Slf4j
@Service
public class FileMetaConsumer {
    @Autowired
    private ICollectFileMetaService collectFileMetaService;
    @Autowired
    private IFlJobRecruitService flJobRecruitService;
    @Autowired
    private IFlJobRegistService flJobRegistService;
    @Autowired
    private HttpClientPool httpClientPool;
    @Autowired
    private ISysUserService sysUserService;
    @Value("${fateflow.url}")
    private String fateFlowUrl;
    @Autowired
    private SourceIdUtil sourceIdUtil;

    @KafkaListener(topics = "${kafkaTopic.fileMeta}", groupId = "fileMeta")
    public void consume(ConsumerRecord consumerRecord) {
        log.error(String.format("#### -> start consumed message -> %s", consumerRecord));
        // noinspection AlibabaRemoveCommentedCode
        Optional.ofNullable(consumerRecord.value()).ifPresent(record -> {
            String recordMsg = record.toString();
            CollectFileMeta currentCollectFileMeta = JSONObject.parseObject(recordMsg, CollectFileMeta.class);
            // 保留一个不覆盖的版本
            CollectFileMetaModel currentCollectFileMetaNotCover =
                JSONObject.parseObject(recordMsg, CollectFileMetaModel.class);
            if (currentCollectFileMeta.isHidden()) {
                currentCollectFileMeta.setId(null);
                FlJobRecruit flJobRecruit = flJobRecruitService.getById(currentCollectFileMetaNotCover.getJobId());
                currentCollectFileMeta.setName(currentCollectFileMeta.getName() + "-" + flJobRecruit.getName());
            }
            UpdateWrapper<CollectFileMeta> updateWrapper =
                new UpdateWrapper<CollectFileMeta>().eq("create_party", currentCollectFileMeta.getCreateParty())
                    .eq("name", currentCollectFileMeta.getName()).eq("create_by", currentCollectFileMeta.getCreateBy());
            if (currentCollectFileMeta.getVersion() != null) {
                updateWrapper.eq("version", currentCollectFileMeta.getVersion());
            } else {
                updateWrapper.isNull("version");
            }
            if (StringUtils.isNotEmpty(currentCollectFileMetaNotCover.getToken())) {
                String userName = JwtUtil.getUsername(currentCollectFileMetaNotCover.getToken());
                SysUser sysUser = sysUserService.getUserByName(userName);
                log.error(String.format("#### -> 创建人 -> %s", userName));
                currentCollectFileMeta.setCreateBy(userName);
                currentCollectFileMeta.setCreateParty(sysUser.getOrgCode());
                currentCollectFileMeta.setOrgCode(sysUser.getOrgCode());
                log.error(String.format("#### -> 权限permission -> %s", currentCollectFileMeta.getPermission()));
                log.error(String.format("#### -> 组织id orgCode -> %s", currentCollectFileMeta.getOrgCode()));
                currentCollectFileMeta.setCreateBy(userName);
                currentCollectFileMeta.setCreateTime(new Date());
            }
            currentCollectFileMeta.setResourceId(sourceIdUtil.generateSourceId(ResourceIdPrefixEnum.DATA.name()));
            collectFileMetaService.saveOrUpdate(currentCollectFileMeta, updateWrapper);
            if (currentCollectFileMeta.isHidden() && currentCollectFileMetaNotCover.getJobId() != null) {
                QueryWrapper<FlJobRegist> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(FlJobRegist::getRecruitId, currentCollectFileMetaNotCover.getJobId());
                queryWrapper.lambda().eq(FlJobRegist::getPartyId, currentCollectFileMeta.getPartyId());
                FlJobRegist flJobRegist = flJobRegistService.getOne(queryWrapper);
                QueryWrapper<CollectFileMeta> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().eq(CollectFileMeta::getPartyId, currentCollectFileMeta.getPartyId());
                queryWrapper1.lambda().eq(CollectFileMeta::getTableName, currentCollectFileMeta.getTableName());
                CollectFileMeta collectFileMeta = collectFileMetaService.getOne(queryWrapper1);
                flJobRegist.setDataId(collectFileMeta.getId());
                flJobRegistService.updateById(flJobRegist);
            }
            // 根据数据版本的更新自动执行联邦任务
            // autoRunJob(currentCollectFileMeta);
        });

        log.info(String.format("#### <- end consumed message -> %s", consumerRecord));
    }

    /**
     * 构建新的招募任务
     *
     * @param oldFlJobRegistList
     * @param newFlJobRecruit
     * @param currentCollectFileMeta
     * @param version
     * @return
     */
    private List<FlJobRegist> genNewFlJobRegist(List<FlJobRegist> oldFlJobRegistList, FlJobRecruit newFlJobRecruit,
        CollectFileMeta currentCollectFileMeta, String version) {
        List<FlJobRegist> newFlJobRegistList = new ArrayList<>();

        for (FlJobRegist oldFlJobRegist : oldFlJobRegistList) {
            FlJobRegist newFlJobRegist = new FlJobRegist();
            newFlJobRegist.setRecruitId(newFlJobRecruit.getId());
            newFlJobRegist.setId(oldFlJobRegist.getId() + "-" + version);
            newFlJobRegist.setPartyId(oldFlJobRegist.getPartyId());
            if (TrainRoleEnum.guest.toString().equals(oldFlJobRegist.getRole())) {
                newFlJobRegist.setDataId(currentCollectFileMeta.getId());
                newFlJobRegist.setNamespace(currentCollectFileMeta.getEggrollNamespace());
                newFlJobRegist.setName(currentCollectFileMeta.getEggrollName());
                newFlJobRegist.setEvalType(currentCollectFileMeta.getEvalType());
                newFlJobRegist.setLabelName(currentCollectFileMeta.getLabelName());
            } else {
                newFlJobRegist.setDataId(oldFlJobRegist.getDataId());
                //
                CollectFileMeta collectFileMeta = collectFileMetaService.getById(oldFlJobRegist.getDataId());
                newFlJobRegist.setNamespace(collectFileMeta.getEggrollNamespace());
                newFlJobRegist.setName(collectFileMeta.getEggrollName());
                newFlJobRegist.setEvalType(collectFileMeta.getEvalType());
                newFlJobRegist.setLabelName(collectFileMeta.getLabelName());
            }
            newFlJobRegist.setRole(oldFlJobRegist.getRole());
            newFlJobRegist.setMessage(oldFlJobRegist.getMessage());
            newFlJobRegist.setRegistStatus(oldFlJobRegist.getRegistStatus());
            newFlJobRegist.setEnvStatus(oldFlJobRegist.getEnvStatus());

            newFlJobRegistList.add(newFlJobRegist);
        }
        return newFlJobRegistList;
    }

    /**
     * 生成新的招募任务
     *
     * @param oldFlJobRecruit
     * @param version
     * @return
     */
    private FlJobRecruit genNewFlJobRecruit(FlJobRecruit oldFlJobRecruit, String version) {
        FlJobRecruit newFlJobRecruit = new FlJobRecruit();
        newFlJobRecruit.setId(oldFlJobRecruit.getId() + "-" + version);
        newFlJobRecruit.setPartyId(oldFlJobRecruit.getPartyId());
        newFlJobRecruit.setCompoments(oldFlJobRecruit.getCompoments());
        newFlJobRecruit.setParameters(oldFlJobRecruit.getParameters());
        newFlJobRecruit.setDsl(oldFlJobRecruit.getDsl());
        newFlJobRecruit.setContent(oldFlJobRecruit.getContent());
        newFlJobRecruit.setPermission(oldFlJobRecruit.getPermission());
        newFlJobRecruit.setRecruitStatus(oldFlJobRecruit.getRecruitStatus());
        newFlJobRecruit.setName(oldFlJobRecruit.getName());
        newFlJobRecruit.setAvatar(oldFlJobRecruit.getAvatar());

        newFlJobRecruit.setParentId(oldFlJobRecruit.getId());
        // 不自动执行
        newFlJobRecruit.setAutoRun("0");

        newFlJobRecruit.setVersionId(version);

        return newFlJobRecruit;
    }
}
