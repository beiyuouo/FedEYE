package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.federalml.entity.*;
import org.jeecg.modules.federalml.service.ICollectFileMetaService;
import org.jeecg.modules.federalml.service.IFlJobFeatureService;
import org.jeecg.modules.federalml.service.IFlJobRecruitService;
import org.jeecg.modules.federalml.service.IFlPartyInfoService;
import org.jeecg.modules.federalml.utils.CheckPermissionUtil;
import org.jeecg.modules.federalml.vo.CollectFileMetaVo;
import org.jeecg.modules.federalml.vo.FlJobFeatureVo;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import io.dapr.client.domain.CloudEvent;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 边缘端所采集数据文件的元信息
 * @Author: jeecg-boot
 * @Date: 2021-03-05
 * @Version: V1.0
 */
@Api(tags = "4.3 数据文件元信息", hidden = true)
@RestController
@RequestMapping("/federalml/collectFileMeta")
@Slf4j
public class FlCollectFileMetaController extends JeecgController<CollectFileMeta, ICollectFileMetaService> {
    @Autowired
    private ICollectFileMetaService collectFileMetaService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private CheckPermissionUtil checkPermissionUtil;
    @Value("${features.permissonGradeEnabled}")
    private boolean permissonGradeEnabled;
    @Autowired
    private IFlJobFeatureService flJobFeatureService;
    @Autowired
    private IFlPartyInfoService flPartyInfoService;
    @Autowired
    private IFlJobRecruitService flJobRecruitService;

    /**
     * 分页列表查询
     *
     * @param collectFileMeta
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "数据池分页查询", notes = "边缘端所采集数据文件的元信息-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<CollectFileMeta>> queryPageList(CollectFileMeta collectFileMeta,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<CollectFileMeta> queryWrapper =
            QueryGenerator.initQueryWrapper(collectFileMeta, req.getParameterMap());
        queryWrapper.lambda().eq(CollectFileMeta::isHidden, false);
        queryWrapper.lambda().eq(CollectFileMeta::isDeleted, false);
        Page<CollectFileMeta> page = new Page<>(pageNo, pageSize);
        IPage<CollectFileMeta> pageList = collectFileMetaService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @ApiOperation(value = "数据池", notes = "边缘端所采集数据文件的元信息-分页列表查询")
    @GetMapping(value = "/listAllData")
    public Result<IPage<CollectFileMetaModel>> listAllData(
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) @ApiParam(name = "name", value = "数据名称", required = false) String name,
        HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        Page<CollectFileMetaModel> page = new Page<>(pageNo, pageSize);
        IPage<CollectFileMetaModel> pageList = collectFileMetaService.listAllDataPage(page, name, tenantId);
        return Result.OK(pageList);
    }

    @ApiOperation(value = "我的数据", notes = "分页查询当前联邦方的数据信息列表")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @GetMapping(value = "/listCurrent")
    public Result<IPage<CollectFileMeta>> listCurrent(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) @ApiParam(name = "name", value = "数据名称", required = false) String name,
        HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        String userName = JwtUtil.getUserNameByToken(req);
        SysUser sysUser = sysUserService.getUserByName(userName);
        String orgCode = sysUser.getOrgCode();
        QueryWrapper<CollectFileMeta> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CollectFileMeta::getPartyId, tenantId);
        queryWrapper.lambda().eq(CollectFileMeta::isHidden, false);
        queryWrapper.lambda().eq(CollectFileMeta::isDeleted, false);
        if (permissonGradeEnabled) {
            queryWrapper.lambda()
                .nested(i -> i.eq(CollectFileMeta::getPermission, "PERSON").eq(CollectFileMeta::getCreateBy, userName)
                    .or(j -> j.eq(CollectFileMeta::getPermission, "OFFICE").eq(CollectFileMeta::getOrgCode, orgCode))
                    .or(k -> k.eq(CollectFileMeta::getPermission, "ORG").likeRight(CollectFileMeta::getOrgCode,
                        orgCode.substring(0, 2))));
        }
        if (oConvertUtils.isNotEmpty(name)) {
            queryWrapper.lambda().and(wrapper -> wrapper.like(CollectFileMeta::getName, name).or()
                .like(CollectFileMeta::getCreateParty, name));
        }
        queryWrapper.lambda().orderByDesc(CollectFileMeta::getCreateTime);
        Page<CollectFileMeta> page = new Page<CollectFileMeta>(pageNo, pageSize);
        IPage<CollectFileMeta> pageList = collectFileMetaService.page(page, queryWrapper);
        List<CollectFileMeta> list = pageList.getRecords();
        list.stream().forEach(collectFileMeta -> {
            collectFileMeta.setSize(FileUtil.readableFileSize(Long.parseLong(collectFileMeta.getSize())));
        });
        return Result.OK(pageList);
    }

    @ApiOperation(value = "推理任务选择数据", notes = "推理任务选择数据")
    @GetMapping(value = "/selectList")
    public Result<?> selectList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) @ApiParam(name = "contentType", value = "数据类型", required = false,
            defaultValue = "") String contentType,
        @RequestParam(required = false) @ApiParam(name = "selectedId", value = "已选数据id", required = false,
            defaultValue = "") String selectedId,
        HttpServletRequest req) {
        if (req == null) {
            return Result.error("请求头为空");
        }
        String tenantId = req.getHeader("tenant_id");
        Page<CollectFileMetaVo> page = new Page<>(pageNo, pageSize);
        return Result.OK(collectFileMetaService.selectList(page, selectedId, contentType, tenantId));
    }

    @ApiOperation(value = "通过数据集名称查询数据", notes = "通过数据集名称查询数据")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @GetMapping(value = "/searchByName")
    public Result<List<CollectFileMeta>> searchByName(
        @RequestParam(required = true) @ApiParam(name = "name", value = "数据名称", required = true) String name,
        HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        String userName = JwtUtil.getUserNameByToken(req);
        QueryWrapper<CollectFileMeta> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CollectFileMeta::getPartyId, tenantId).eq(CollectFileMeta::getName, name)
            .eq(CollectFileMeta::isDeleted, false).eq(CollectFileMeta::isHidden, false)
            .eq(CollectFileMeta::getCreateBy, userName);
        List<CollectFileMeta> collectFileMeta = collectFileMetaService.list(queryWrapper);

        return Result.OK(collectFileMeta);
    }

    @ApiOperation(value = "查询指定类型的数据", notes = "根据算法评估类型查询数据")
    @ApiImplicitParam(paramType = "header", name = "tenant_id", value = "联邦方id", dataType = "String", required = true)
    @GetMapping(value = "/queryData")
    public Result<List<CollectFileMeta>> queryData(@ApiParam(name = "evalTypes", value = "评估类型，逗号分隔",
        required = true) @RequestParam(name = "evalTypes", required = true) String evalTypes, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        List<CollectFileMeta> collectFileMeta = collectFileMetaService
            .list(new QueryWrapper<CollectFileMeta>().lambda().eq(CollectFileMeta::getPartyId, tenantId)
                .eq(CollectFileMeta::isHidden, false).eq(CollectFileMeta::isDeleted, false)
                .in(CollectFileMeta::getEvalType, Arrays.asList(evalTypes.split(","))));
        return Result.OK(collectFileMeta);
    }

    @ApiOperation(value = "新建任务时双向筛选数据和算法", notes = "fileMetaId和algorithmId都可为空，返回全部，任何一个不为空或都不为空时，筛选查询")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "query", name = "fileMetaId", value = "数据id", dataType = "String",
            required = false),
        @ApiImplicitParam(paramType = "query", name = "algorithmId", value = "算法id", dataType = "String",
            required = false)})

    @GetMapping(value = "/queryFileMetaAndAlgorithm")
    public Result<Map<String, Object>> queryFileMetaAndAlgorithm(@RequestParam(required = false) String fileMetaId,
        @RequestParam(required = false) String algorithmId, HttpServletRequest req) {
        return Result.OK(collectFileMetaService.queryFileMetaAndAlgorithm(fileMetaId, algorithmId, req));
    }

    /**
     * 更新边缘端数据文件统计信息
     *
     * @param body
     * @return
     */
    @AutoLog(value = "边缘端数据文件元信息-更新", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "边缘端数据文件元信息-边缘端数据文件统计信息更新", notes = "边缘端数据文件元信息-边缘端数据文件统计信息更新")
    @PostMapping(value = "/upinsert")
    public Result<?> upinsert(@RequestBody(required = false) byte[] body) {
        CollectFileMeta model;
        try {
            CloudEvent event = CloudEvent.deserialize(body);
            String jsonStr = (String)event.getData();
            model = JSONUtil.toBean(jsonStr, CollectFileMeta.class);
            UpdateWrapper<CollectFileMeta> updateWrapper = new UpdateWrapper<CollectFileMeta>()
                .eq("create_party", model.getCreateParty()).eq("path", model.getPath());
            collectFileMetaService.saveOrUpdate(model, updateWrapper);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.OK(model);
    }

    /**
     * 边缘端数据文件统计信息比对
     *
     * @param metas
     * @return
     */
    @ApiOperation(value = "边缘端数据文件元信息-边缘端数据文件统计信息比对", notes = "边缘端数据文件元信息-边缘端数据文件统计信息比对")
    @PostMapping(value = "/summaryCompare")
    public Result<?> summaryCompare(@RequestBody(required = false) Map<String, CollectFileMeta> metas) {

        // self 参与方，target 发起方
        CollectFileMeta self = metas.get("self");
        CollectFileMeta target = metas.get("target");
        if (self == null || "".equals(self.getCreateParty()) || "".equals(self.getId())) {
            return Result.error("请等待发起方上传数据!");
        }

        self = collectFileMetaService.query().eq("create_party", self.getCreateParty()).eq("id", self.getId())
            .last("limit 1").one();

        target = collectFileMetaService.query().eq("create_party", target.getCreateParty()).eq("id", target.getId())
            .last("limit 1").one();

        if (!self.getContentType().equals(target.getContentType())) {
            return Result.error("文件类型不匹配!");
        }
        // 根据数据集的是否隐藏属性判断是否需要比对字段
        if (target.isHidden()) {
            // 如果发起方是隐藏的数据集，证明是子数据集，需要对比字段是否在父数据集中
            String targetColumns = target.getColumns();
            String selfColumns = self.getColumns();
            String commaStr = ",";
            for (String column : targetColumns.split(commaStr)) {
                if (!selfColumns.contains(column)) {
                    return Result.error("文件字段不匹配!");
                }
            }
        } else {
            // 如果发起方是非隐藏的数据集，证明是父数据集，需要比对字段
            if (self.getColumns() != null && target.getColumns() != null) {
                if (!self.getColumns().equals(target.getColumns())) {
                    return Result.error("文件字段不匹配!");
                }
            }
        }
        return Result.OK();
    }

    /**
     * 添加
     *
     * @param collectFileMeta
     * @return
     */
    @AutoLog(value = "边缘端所采集数据文件的元信息-添加", operateType = OPERATE_TYPE_2)
    @ApiOperation(value = "边缘端所采集数据文件的元信息-添加", notes = "边缘端所采集数据文件的元信息-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody CollectFileMeta collectFileMeta) {
        collectFileMetaService.save(collectFileMeta);
        return Result.OK("添加成功！");
    }

    /**
     * 添加
     *
     * @param flJobFeatureList
     * @return
     */
    @AutoLog(value = "单标签多分类数据集联邦任务分类选择-添加", operateType = OPERATE_TYPE_2)
    @ApiOperation(value = "单标签多分类数据集联邦任务分类选择-添加", notes = "单标签多分类数据集联邦任务分类选择-添加")
    @PostMapping(value = "/add_selected_feature")
    public Result<?> addSelectedFeature(@RequestBody List<FlJobFeature> flJobFeatureList) {
        if (flJobFeatureList == null || flJobFeatureList.size() == 0) {
            return Result.error("参数为空！");
        }
        String jobId = flJobFeatureList.get(0).getJobId();
        int partyId = flJobFeatureList.get(0).getPartyId();
        flJobFeatureService.remove(new QueryWrapper<FlJobFeature>().lambda().eq(FlJobFeature::getJobId, jobId)
            .eq(FlJobFeature::getPartyId, partyId));
        for (FlJobFeature flJobFeature : flJobFeatureList) {
            flJobFeatureService.save(flJobFeature);
        }
        return Result.OK("添加成功！");
    }

    /**
     * 通过任务id查询分类选择
     *
     * @param jobId
     * @return
     */
    @ApiOperation(value = "通过任务id查询分类选择", notes = "通过任务id查询分类选择")
    @GetMapping(value = "/query_selected_feature")
    public Result<?> querySelectedFeature(@RequestParam(name = "jobId", required = true) String jobId) {
        FlJobRecruit flJobRecruit = flJobRecruitService.getById(jobId);
        if (flJobRecruit == null) {
            return Result.error("任务不存在！");
        }
        // 如果是独立联邦，直接返回空
        Integer independentFederation = 2;
        if (flJobRecruit.getJobType() == independentFederation) {
            return Result.OK(new ArrayList<>());
        }
        List<FlJobFeature> list =
            flJobFeatureService.list(new QueryWrapper<FlJobFeature>().lambda().eq(FlJobFeature::getJobId, jobId));
        List<FlPartyInfo> flPartyInfoList =
            flPartyInfoService.list(new QueryWrapper<FlPartyInfo>().lambda().eq(FlPartyInfo::getStatus, 1));
        Map<Integer, String> partyInfoMap = new HashMap<>(list.size());
        for (FlPartyInfo flPartyInfo : flPartyInfoList) {
            partyInfoMap.put(flPartyInfo.getId(), flPartyInfo.getName());
        }
        List<FlJobFeatureVo> returnList = new ArrayList<>();
        for (FlJobFeature flJobFeature : list) {
            FlJobFeatureVo flJobFeatureVo = new FlJobFeatureVo();
            BeanUtils.copyProperties(flJobFeature, flJobFeatureVo);
            flJobFeatureVo.setPartyName(partyInfoMap.get(flJobFeature.getPartyId()));
            returnList.add(flJobFeatureVo);
        }
        return Result.OK(returnList);
    }

    /**
     *
     *
     * @param flJobFeatureList
     * @return
     */
    @AutoLog(value = "修改任务分类选择", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "修改任务分类选择", notes = "修改任务分类选择")
    @PutMapping(value = "/update_selected_feature")
    public Result<?> updateSelectedFeature(@RequestBody List<FlJobFeature> flJobFeatureList) {
        if (flJobFeatureList == null || flJobFeatureList.size() == 0) {
            return Result.error("参数为空！");
        }
        List<Integer> ruleList = new ArrayList<>();
        for (FlJobFeature flJobFeature : flJobFeatureList) {
            ruleList.add(Integer.parseInt(flJobFeature.getRule()));
        }
        // 判断rule是否是连续数字
        if (!isContinuousNumber(ruleList)) {
            return Result.error("规则必须从0开始，并且是连续的数字！");
        }
        for (FlJobFeature flJobFeature : flJobFeatureList) {
            FlJobFeature flJobFeature1 = flJobFeatureService.getById(flJobFeature.getId());
            if (flJobFeature1 != null) {
                flJobFeature1.setRule(flJobFeature.getRule());
                flJobFeatureService.updateById(flJobFeature1);
            }
        }
        return Result.OK("编辑成功!");
    }

    private boolean isContinuousNumber(List<Integer> list) {
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            if (i != list.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 编辑
     *
     * @param collectFileMeta
     * @return
     */
    @AutoLog(value = "边缘端所采集数据文件的元信息-编辑", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "边缘端所采集数据文件的元信息-编辑", notes = "边缘端所采集数据文件的元信息-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody CollectFileMeta collectFileMeta) {
        collectFileMetaService.updateById(collectFileMeta);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "通过id删除", notes = "通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id, HttpServletRequest req) {
        String tenantId = req.getHeader("tenant_id");
        CollectFileMeta collectFileMeta = collectFileMetaService.getById(id);
        boolean flag = checkPermissionUtil.checkPermission(req, tenantId, collectFileMeta.getCreateBy(),
            collectFileMeta.getPartyId());
        if (!flag) {
            return Result.error("没有权限！");
        }
        if (collectFileMeta == null) {
            return Result.error("该数据集已被删除！");
        }
        collectFileMeta.setDeleted(true);
        collectFileMetaService.updateById(collectFileMeta);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "边缘端所采集数据文件的元信息-批量删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "边缘端所采集数据文件的元信息-批量删除", notes = "边缘端所采集数据文件的元信息-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.collectFileMetaService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "边缘端所采集数据文件的元信息-通过id查询", notes = "边缘端所采集数据文件的元信息-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<CollectFileMeta> queryById(@RequestParam(name = "id", required = true) String id) {
        CollectFileMeta collectFileMeta = collectFileMetaService.getById(id);
        collectFileMeta.setSize(FileUtil.readableFileSize(Long.parseLong(collectFileMeta.getSize())));
        if (collectFileMeta == null) {
            Result<CollectFileMeta> result = new Result();
            result.error500("未找到对应数据");
            return result;
        }
        return Result.OK(collectFileMeta);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param collectFileMeta
     */
    @AutoLog(value = "边缘端所采集数据文件的元信息-导出", operateType = OPERATE_TYPE_6)
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CollectFileMeta collectFileMeta) {
        return super.exportXls(request, collectFileMeta, CollectFileMeta.class, "边缘端所采集数据文件的元信息");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @AutoLog(value = "边缘端所采集数据文件的元信息-导入", operateType = OPERATE_TYPE_5)
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CollectFileMeta.class);
    }

}
