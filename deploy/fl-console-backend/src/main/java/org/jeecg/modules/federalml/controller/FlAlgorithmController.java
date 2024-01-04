package org.jeecg.modules.federalml.controller;

import static org.jeecg.common.constant.CommonConstant.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.HttpPost;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.federalml.entity.Algorithm;
import org.jeecg.modules.federalml.entity.AlgorithmModel;
import org.jeecg.modules.federalml.entity.FlActivity;
import org.jeecg.modules.federalml.service.IAlgorithmService;
import org.jeecg.modules.federalml.service.IFlActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 联邦学习算法库
 * @Author: jeecg-boot
 * @Date: 2021-05-31
 * @Version: V1.0
 */
@Api(tags = "4.2 联邦学习算法库", hidden = true)
@RestController
@RequestMapping("/federalml/algorithm")
@Slf4j
public class FlAlgorithmController extends JeecgController<Algorithm, IAlgorithmService> {
    @Autowired
    private IAlgorithmService algorithmService;
    @Autowired
    private IFlActivityService flActivityService;

    private static final int HOT_ALG_NUM = 4;

    @AutoLog(value = "算法库-通用算法调用")
    @ApiOperation(value = "通用算法调用", notes = "通用算法调用")
    @PostMapping(value = "/call")
    public Object call(@RequestParam @ApiParam(name = "id", value = "算法id", required = true) String id,
        @RequestBody JSONObject conf, HttpServletRequest req) {
        // 算法id，根据算法id查询到调用地址
        Algorithm algorithm = algorithmService.getById(id);
        String url = algorithm.getUrl();

        // 调用方式，post、get
        String requestMethod = algorithm.getRequestMethod();

        RestTemplate restTemplate = new RestTemplate();
        if (HttpPost.METHOD_NAME.equals(requestMethod)) {
            return restTemplate.postForObject(url, conf, Object.class);
        } else {
            return restTemplate.getForObject(url, Object.class, conf);
        }

    }

    /**
     * 分页列表查询
     *
     * @param algorithm
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "联邦学习算法库-分页列表查询", notes = "联邦学习算法库-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<Algorithm>> queryPageList(Algorithm algorithm,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<Algorithm> queryWrapper = QueryGenerator.initQueryWrapper(algorithm, req.getParameterMap());
        Page<Algorithm> page = new Page<Algorithm>(pageNo, pageSize);
        IPage<Algorithm> pageList = algorithmService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 热门算法
     */
    @ApiOperation(value = "热门算法", notes = "热门算法")
    @GetMapping(value = "/hotAlgorithm")
    public Result<List<Algorithm>> hotAlgorithm() {
        List<Algorithm> returnAlgList = new ArrayList<>();
        List<FlActivity> flActivityList = flActivityService.selectHotAlg();
        if (flActivityList != null || flActivityList.size() >= HOT_ALG_NUM) {
            for (int i = 0; i < HOT_ALG_NUM; i++) {
                FlActivity flActivity = flActivityList.get(i);
                Algorithm algorithm = algorithmService.getById(flActivity.getTypeId());
                returnAlgList.add(algorithm);
            }
        } else {
            returnAlgList = algorithmService.list();
        }
        return Result.OK(returnAlgList);
    }

    @ApiOperation(value = "算法池", notes = "联邦学习算法库-分页列表查询")
    @GetMapping(value = "/listAllAlgorithm")
    public Result<IPage<AlgorithmModel>> listAllAlgorithm(
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) @ApiParam(name = "name", value = "数据名称", required = false) String name) {
        Page<AlgorithmModel> page = new Page<>(pageNo, pageSize);
        IPage<AlgorithmModel> pageList = algorithmService.listAllAlgorithmPage(page, name);
        return Result.OK(pageList);
    }

    /**
     * 查询指定类型的算法
     *
     * @param evalType
     * @return
     */
    @ApiOperation(value = "查询指定类型的算法", notes = "根据数据评估类型查询算法")
    @GetMapping(value = "/queryAlgorithm")
    public Result<List<Algorithm>> queryAlgorithm(@ApiParam(name = "evalType", value = "评估类型",
        required = true) @RequestParam(name = "evalType", required = true) String evalType) {
        List<Algorithm> algorithm =
            algorithmService.list(new QueryWrapper<Algorithm>().lambda().like(Algorithm::getEvalTypes, evalType));
        return Result.OK(algorithm);
    }

    /**
     * 添加
     *
     * @param algorithm
     * @return
     */
    @AutoLog(value = "算法库-添加", operateType = OPERATE_TYPE_2)
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody Algorithm algorithm) {
        algorithmService.save(algorithm);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param algorithm
     * @return
     */
    @AutoLog(value = "算法库-编辑", operateType = OPERATE_TYPE_3)
    @ApiOperation(value = "联邦学习算法库-编辑", notes = "联邦学习算法库-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody Algorithm algorithm) {
        algorithmService.updateById(algorithm);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "算法库-通过id删除", operateType = OPERATE_TYPE_4)
    @ApiOperation(value = "联邦学习算法库-通过id删除", notes = "联邦学习算法库-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        algorithmService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "算法库-批量删除", operateType = OPERATE_TYPE_4)
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.algorithmService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "联邦学习算法库-通过id查询", notes = "联邦学习算法库-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        Algorithm algorithm = algorithmService.getById(id);
        if (algorithm == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(algorithm);
    }

    @ApiOperation(value = "算法使用统计信息", notes = "算法使用统计信息")
    @GetMapping(value = "/algUseInfo")
    public Result<?> algUseInfo(@RequestParam(name = "id", required = true) String id) {
        List<HashMap<String, Object>> list = algorithmService.algorithmUseInfo(id);
        JSONObject result = new JSONObject();
        if (list != null && list.size() > 0) {
            int usedNums = list.size();
            HashSet<HashMap<String, String>> usedUserList = new HashSet<>();
            HashSet<String> partyIdSet = new HashSet<String>();
            LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
            for (HashMap<String, Object> map : list) {
                String userName = map.get("realname") == null ? "" : map.get("realname").toString();
                partyIdSet.add(map.get("party_id") == null ? "" : map.get("party_id").toString());
                if (!userName.equals(sysUser.getRealname())) {
                    HashMap<String, String> userMap = new HashMap<>(2);
                    userMap.put("name", userName);
                    userMap.put("avatar", map.get("avatar") == null ? "" : map.get("avatar").toString());
                    usedUserList.add(userMap);
                }
            }
            int partyNums = partyIdSet.size();
            result.put("usedNums", usedNums);
            result.put("partyNums", partyNums);
            result.put("userList", usedUserList);
        }
        return Result.OK(result);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param algorithm
     */
    @AutoLog(value = "算法库-导出算法库", operateType = OPERATE_TYPE_6)
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Algorithm algorithm) {
        return super.exportXls(request, algorithm, Algorithm.class, "联邦学习算法库");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @AutoLog(value = "算法库-导出算法库", operateType = OPERATE_TYPE_5)
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Algorithm.class);
    }

}
