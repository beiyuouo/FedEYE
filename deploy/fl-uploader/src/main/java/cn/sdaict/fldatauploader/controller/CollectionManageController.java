package cn.sdaict.fldatauploader.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.sdaict.fldatauploader.entity.*;
import cn.sdaict.fldatauploader.service.FileUploadService;
import cn.sdaict.fldatauploader.utils.*;
import cn.sdaict.fldatauploader.vo.CollectFileMeta;
import cn.sdaict.fldatauploader.vo.Result;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.csvreader.CsvReader;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.util.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;


/**
 * uploader
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class CollectionManageController {

    //获取上传的文件夹，具体路径参考application.properties中的配置
    @Value("${web.upload-path}")
    private String uploadPath;

    @Autowired
    private PublicSqlMapper publicSqlMapper;

    @Autowired
    private HttpClientPool httpClientPool;

    @Autowired
    private FileUploadService fileUploadService;

    @Value(value = "${file-upload.lakefs-url}")
    private String lakefsUrl;
    @Value(value = "${file-upload.lakefs-userName}")
    private String lakefsName;
    @Value(value = "${file-upload.lakefs-passWord}")
    private String lakefsPass;
    @Value(value = "${backend-path}")
    private String backendPath;

    private final int empCount = 20000;

    @PostMapping(value = "/inference")
    public Result<?> inference(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        log.info("====================>进入数据集推理方法" + params.toString());
        String inferenceUrl = params.get("inferenceUrl").toString();
        String serviceId = params.get("serviceId").toString();
        String name = params.get("name").toString();
        String tableName = params.get("tableName").toString();
        List<LinkedHashMap<String, Object>> featureDataList = publicSqlMapper.select("select * from fl_file_meta_detail_" + tableName);
//        String serviceId = inference.getServiceId();
        String url = inferenceUrl + "batchInference";
        JSONObject config = new JSONObject();
        JSONObject head = new JSONObject();
        head.put("serviceId", serviceId);
        config.put("head", head);
        JSONObject body = new JSONObject();
        List<LinkedHashMap<String, Object>> batchDataList = new ArrayList<>();
        // 组装csvHead
        List<Object> csvHead = new ArrayList<>();
        LinkedHashMap<String, Object> headMap = featureDataList.get(0);
        Iterator iterator1 = headMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator1.next();
            if (!"job_id".equals(entry.getKey()) && !"resource_id".equals(entry.getKey())) {
                csvHead.add(entry.getKey());
            }
        }
        csvHead.add("result");
        // 每五万条调一次批量推理的接口，防止oom
        int j = 0;
        int count = 0;
        String fileName = name + "_" + tableName;
        for (int i = 0; i < featureDataList.size(); i++) {
            LinkedHashMap<String, Object> batchDataMap = new LinkedHashMap<>();
            LinkedHashMap<String, Object> linkedHashMap = featureDataList.get(i);
            LinkedHashMap<String, Object> hashMap = new LinkedHashMap<>();
            Iterator iterator = linkedHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (!entry.getKey().toString().equals("job_id") && !entry.getKey().toString().equals("id")) {
                    if (entry.getKey().toString().equals("resource_id")) {
                        hashMap.put("id", entry.getValue().toString());
                    } else {
                        hashMap.put(entry.getKey().toString(), Double.valueOf(entry.getValue() == null ? "0" : entry.getValue().toString()));
                    }
                }
            }
            Object featureData = JSON.toJSON(hashMap);
            batchDataMap.put("index", i);
            batchDataMap.put("featureData", featureData);
            batchDataList.add(batchDataMap);
            j++;
            if (j >= empCount) {
                // count用来记录第几次进来
                count++;
                csvFile(featureDataList, count, batchDataList, body, config, url, j, fileName, csvHead, null);
                batchDataList = new ArrayList<>();
                j = 0;
            }
        }
        if (j > 0) {
            count++;
            csvFile(featureDataList, count, batchDataList, body, config, url, j, fileName, csvHead, null);
        }
        return Result.OK(uploadPath + fileName);
    }

    @PostMapping(value = "/inferenceHereto")
    public Result<?> inferenceHereto(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        log.info("====================>进入纵向数据集推理方法" + params.toString());
        String inferenceUrl = params.get("inferenceUrl").toString();
        String serviceId = params.get("serviceId").toString();
        String name = params.get("name").toString();
        String tableName = params.get("tableName").toString();
        List<LinkedHashMap<String, Object>> featureDataList = publicSqlMapper.select("select * from fl_file_meta_detail_" + tableName);
//        String serviceId = inference.getServiceId();
        String url = inferenceUrl + "batchInference";
        JSONObject config = new JSONObject();
        JSONObject head = new JSONObject();
        head.put("serviceId", serviceId);
        config.put("head", head);
        JSONObject body = new JSONObject();
        List<LinkedHashMap<String, Object>> batchDataList = new ArrayList<>();
        // 组装csvHead
        List<Object> csvHead = new ArrayList<>();
        LinkedHashMap<String, Object> headMap = featureDataList.get(0);
        Iterator iterator1 = headMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator1.next();
            if (!"job_id".equals(entry.getKey()) && !"resource_id".equals(entry.getKey())) {
                csvHead.add(entry.getKey());
            }
        }
        csvHead.add("result");
        // 每五万条调一次批量推理的接口，防止oom
        int j = 0;
        int count = 0;
        String fileName = name + "_" + tableName;
        for (int i = 0; i < featureDataList.size(); i++) {
            LinkedHashMap<String, Object> batchDataMap = new LinkedHashMap<>();
            LinkedHashMap<String, Object> linkedHashMap = featureDataList.get(i);
            LinkedHashMap<String, Object> hashMap = new LinkedHashMap<>();
            LinkedHashMap<String, Object> sendToRemoteFeatureDataMap = new LinkedHashMap<>();
            LinkedHashMap<String, Object> featureIds = new LinkedHashMap<>();
            Iterator iterator = linkedHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (!entry.getKey().toString().equals("job_id") && !entry.getKey().toString().equals("id")) {
                    if (entry.getKey().toString().equals("resource_id")) {
                        hashMap.put("id", entry.getValue().toString());
                        featureIds.put("id", entry.getValue().toString());
                    } else {
                        hashMap.put(entry.getKey().toString(), Double.valueOf(entry.getValue() == null ? "0" : entry.getValue().toString()));
                    }
                }
            }
            Object featureData = JSON.toJSON(hashMap);
            sendToRemoteFeatureDataMap.put("featureIds", featureIds);
            Object sendToRemoteFeatureData = JSON.toJSON(sendToRemoteFeatureDataMap);
            batchDataMap.put("index", i);
            batchDataMap.put("featureData", featureData);
            batchDataMap.put("sendToRemoteFeatureData", sendToRemoteFeatureData);
            batchDataList.add(batchDataMap);
            j++;
            if (j >= empCount) {
                // count用来记录第几次进来
                count++;
                csvFile(featureDataList, count, batchDataList, body, config, url, j, fileName, csvHead, params.get("path").toString());
                batchDataList = new ArrayList<>();
                j = 0;
            }
        }
        if (j > 0) {
            count++;
            csvFile(featureDataList, count, batchDataList, body, config, url, j, fileName, csvHead, params.get("path").toString());
        }
        return Result.OK(uploadPath + fileName);
    }


    private void csvFile(List<LinkedHashMap<String, Object>> featureDataList, int count, List<LinkedHashMap<String, Object>> batchDataList,
                         JSONObject body, JSONObject config, String url, int j, String fileName, List<Object> csvHead, String path) {
        List<LinkedHashMap<String, Object>> featureDataList1 = new ArrayList<>();
        // 纵向推理
        if (!StringUtils.isEmpty(path)) {
            LinkedHashMap<String, Object> remoteDataAdaptor = new LinkedHashMap<>();
            remoteDataAdaptor.put("name", "lakefs_adaptor");
            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("repository", path.split("/")[0]);
            params.put("ref", "main");
            remoteDataAdaptor.put("params", JSON.toJSON(params));
            body.put("remoteDataAdaptor", JSON.toJSON(remoteDataAdaptor));
        }
        body.put("batchDataList", JSON.toJSON(batchDataList));
        config.put("body", body);
        log.info("====================>config" + config);
        String result = httpClientPool.postWithoutTimeout(url, JSON.toJSONString(config));
        // 清空batchDataList
        batchDataList.clear();
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(result);
        log.info("====================>result" + jsonObject);
        cn.hutool.json.JSONObject dataObject = JSONUtil.parseObj(jsonObject.get("singleInferenceResultMap").toString());
        for (int k = 0; k < j; k++) {
            cn.hutool.json.JSONObject empObject = JSONUtil.parseObj(dataObject.get(String.valueOf((count - 1) * empCount + k)).toString());
            cn.hutool.json.JSONObject emp1Object = JSONUtil.parseObj(empObject.get("data"));
            // 生成到csv中
            LinkedHashMap<String, Object> dataMap = featureDataList.get((count - 1) * empCount + k);
//            log.info("=============>emp1Object.get(\"label\")=" + emp1Object.get("label"));
            if (emp1Object.get("label") == null) {
                Double score = Double.valueOf(emp1Object == null ? "0" : emp1Object.get("score").toString());
                dataMap.put("value", score > 0.5 ? "1" : "0");
            } else {
                dataMap.put("value", emp1Object.get("label").toString());
            }
//            featureDataList.set((count - 1) * empCount + k, dataMap);
            featureDataList1.add(dataMap);
        }

        log.info("==============>fileName:" + fileName);
        log.info("=============>uploadPath:" + uploadPath);
        File existPath = new File(uploadPath + "/" + fileName + ".csv");
        if (count == 1) {
            File path1 = new File(uploadPath);
            if (!path1.exists()) {
                path1.mkdirs();// 创建文件根目录
            }
            CsvFileUtil.createCSVFile(csvHead, featureDataList1, uploadPath, fileName);
        } else {
            CsvFileUtil.addCSVFile(featureDataList1, existPath);
        }
    }


    @PostMapping(value = "/inferenceGraph")
    public Result<?> inferenceGraph(@RequestParam Map<String, Object> params) {
        try {
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setContentType("application/json");
            response.setHeader("Cache-Control", "no-store");
            log.info("====================>进入数据集推理方法" + params.toString());
            String inferenceUrl = params.get("inferenceUrl").toString();
            String serviceId = params.get("serviceId").toString();
            String contentPath = params.get("path") + "/content.csv";
            String edgeIndexPath = params.get("path") + "/edge_index.csv";
            File contentFile = new File(contentPath);
            File edgeIndexFile = new File(edgeIndexPath);
            CsvReader reader = new CsvReader(new FileInputStream(contentFile), ',', Charset.forName("UTF-8"));
            List<Integer[]> list = new ArrayList<>();
            Integer[] integers = new Integer[list.size()];
            Map<Integer, Integer> empMap = new HashMap<>();
            int count = 0;
            while (reader.readRecord()) {
                List<Integer> readerList = new ArrayList<>();
                for (int i = 2; i < reader.getValues().length; i++) {
                    readerList.add(Integer.valueOf(reader.getValues()[i]));
                }
                Integer[] nsz = new Integer[readerList.size()];
                readerList.toArray(nsz);
                list.add(nsz);
                empMap.put(Integer.valueOf(reader.getValues()[0]), count);
                count++;
            }
            list.toArray(integers);
            // 处理edgeIndex文件
            List<Integer[]> list1 = new ArrayList<>();
            CsvReader reader1 = new CsvReader(new FileInputStream(edgeIndexFile), ',', Charset.forName("UTF-8"));
            while (reader1.readRecord()) {
                List<Integer> readerList = new ArrayList<>();
                int i = empMap.get(Integer.valueOf(reader.getValues()[0]));
                int j = empMap.get(Integer.valueOf(reader.getValues()[1]));
                readerList.add(i);
                readerList.add(j);
                Integer[] nsz = new Integer[readerList.size()];
                readerList.toArray(nsz);
                list1.add(nsz);
            }
            Integer[] integers1 = new Integer[list1.size()];
            list1.toArray(integers1);
            Map<String, Object> featureData = new HashMap<>();
            featureData.put("x", integers);
            featureData.put("edge_index", integers1);
            JSONObject config = new JSONObject();
            JSONObject head = new JSONObject();
            head.put("serviceId", serviceId);
            config.put("head", head);
            JSONObject body = new JSONObject();
            body.put("featureData", featureData);
            config.put("body", body);
            String result = httpClientPool.post(inferenceUrl, config);
            log.warn("Graph类型数据推理结果=============>" + result);
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(result);
            cn.hutool.json.JSONObject dataObject = JSONUtil.parseObj(jsonObject.get("data").toString());
//
//
//            csvFileUtil.createCSVFile(csvHead, featureDataList, uploadPath, fileName);
            //返回csv的文件路径
            return Result.OK(uploadPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("");
    }

    @PostMapping(value = "/inferenceImage")
    public Result<?> inferenceImage(@RequestParam Map<String, Object> params) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        log.info("====================>进入数据集推理方法" + params.toString());
        String inferenceUrl = params.get("inferenceUrl").toString();
        String serviceId = params.get("serviceId").toString();
        String name = params.get("name").toString();
        String tableName = params.get("tableName").toString();
        String header = params.get("header").toString();
        List<LinkedHashMap<String, Object>> featureDataList = publicSqlMapper.select("select * from fl_file_meta_detail_" + tableName);
        String url = inferenceUrl + "inference";
        JSONObject config = new JSONObject();
        JSONObject head = new JSONObject();
        head.put("serviceId", serviceId);
        config.put("head", head);
        JSONObject body = new JSONObject();
        JSONArray batchDataListJson = new JSONArray();
        for (int i = 0; i < featureDataList.size(); i++) {
            LinkedHashMap<String, Object> linkedHashMap = featureDataList.get(i);
            Iterator iterator = linkedHashMap.entrySet().iterator();
            String path = "";
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (entry.getKey().toString().equals("path")) {
                    path = entry.getValue().toString();
                }
            }
            Map<String, Object> featureData = getFeatureData(path);
            body.put("featureData", featureData);
            config.put("body", body);
            String result = httpClientPool.postWithoutTimeout(url, JSON.toJSONString(config));
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(result);
            batchDataListJson.add(jsonObject);
        }
        List<Object> csvHead = new ArrayList<>();
        csvHead.add("name");
        csvHead.add("label");
        csvHead.add("result");
        for (int i = 0; i < featureDataList.size(); i++) {
            cn.hutool.json.JSONObject empObject = JSONUtil.parseObj(batchDataListJson.get(i).toString());
            cn.hutool.json.JSONObject emp1Object = JSONUtil.parseObj(empObject.get("data"));
            String scoreObject = emp1Object.get("label").toString();
            // 生成到csv中
            LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
            LinkedHashMap<String, Object> fetureMap = featureDataList.get(i);
            dataMap.put("name", fetureMap.get("name"));
            if (fetureMap.containsKey("label")) {
                dataMap.put("label", fetureMap.get("label"));
            } else {
                csvHead.remove("label");
            }
            dataMap.put("score", scoreObject);
            featureDataList.set(i, dataMap);
        }
        String fileName = name + "_" + tableName;
        log.info("==============>fileName:" + fileName);
        log.info("=============>uploadPath:" + uploadPath);
        File path = new File(uploadPath);
        if (!path.exists()) {
            path.mkdirs();// 创建文件根目录
        }
        CsvFileUtil.createCSVFile(csvHead, featureDataList, uploadPath, fileName);
        log.info("=============>生成结果文件完毕:" + uploadPath);
        //返回csv的文件路径
        return Result.OK(uploadPath + fileName);
    }

    private Map<String, Object> getFeatureData1(String path, String header) {
        Map<String, Object> featureData = new HashMap<>();
        featureData.put("base64", this.fileToBase64(path));
        featureData.put("PIL_mode", "RGB");
//        featureData.put("size", this.getSize(header));
        return featureData;
    }

    private Map<String, Object> getFeatureData(String path) {
        Map<String, Object> featureData = new HashMap<>();
        try {
            LakefsUtil lakefsUtil = new LakefsUtil(lakefsUrl, lakefsName, lakefsPass);
            File file = lakefsUtil.getObject(path);
            featureData.put("base64", this.fileToBase64(file));
            featureData.put("PIL_mode", "RGB");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureData;
    }

    private int[] getSize(String header) {
        String a = header.substring(header.indexOf(":") + 3, header.length() - 3).replace("'", "").replace("\"", "");
        System.out.println(Integer.valueOf(a.split(",")[0]));
        int[] returnValue = new int[2];
        returnValue[0] = Integer.valueOf(a.split(",")[0]);
        returnValue[1] = Integer.valueOf(a.split(",")[1]);
        return returnValue;
    }

    private String fileToBase64(String path) {
        String base64 = null;
        InputStream in = null;
        try {
            File file = new File(path);
            in = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            in.read(bytes);
            base64 = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return base64;
    }

    private String fileToBase64(File file) {
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            in.read(bytes);
            base64 = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return base64;
    }

    @ApiOperation(value = "数据集列和特征值展示", notes = "数据集列和特征值展示")
    @GetMapping(value = "/columnAndFeatureOfMeta")
    public Result<?> columnAndFeatureOfMeta(@RequestParam(required = false) @ApiParam(name = "tableName", value = "表名", required = false) String tableName,
                                            HttpServletRequest req) {
        if (!checkToken(req, null)) {
            return Result.error(401, "Token失效，请重新登录!");
        }
        // 获取yaml中的
        StringBuffer yamlStr = new StringBuffer();
        yamlStr.append("select * from t_meta_yaml where table_name = '").append(tableName + "'");
        List<LinkedHashMap<String, Object>> yamlList = publicSqlMapper.select(yamlStr.toString());
        String targetName = "";
        if (yamlList != null && yamlList.size() > 0) {
            LinkedHashMap<String, Object> yamlMap = yamlList.get(0);
            Iterator iterator1 = yamlMap.entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator1.next();
                if (entry.getKey().equals("meta_yaml")) {
                    Yaml yaml = new Yaml();
                    Config config = yaml.loadAs(entry.getValue().toString(), Config.class);
                    targetName = config.getTargets().get(0).getName();
                }
            }
        }
        // 获取数据集列
        String tableNameStr = "fl_file_meta_detail_" + tableName;
        StringBuffer columnStr = new StringBuffer();
        columnStr.append("show COLUMNS from ").append(tableNameStr);
        List<LinkedHashMap<String, Object>> columnList = publicSqlMapper.select(columnStr.toString());
        List<String> colums = new ArrayList<>();
        if (columnList != null && columnList.size() > 0) {
            for (LinkedHashMap<String, Object> map : columnList) {
                String filedValue = map.get("Field").toString();
                if (!"id".equals(filedValue) && !"job_id".equals(filedValue) && !targetName.equals(filedValue) && !"resource_id".equals(filedValue)) {
                    colums.add(map.get("Field").toString());
                }
            }
        }
        // 获取特征值
        StringBuffer featureStr = new StringBuffer();
        featureStr.append("select DISTINCT(").append(targetName).append(" ) from ").append(tableNameStr).append(" order by ").append(targetName);
        List<LinkedHashMap<String, Object>> featureList = publicSqlMapper.select(featureStr.toString());
        LinkedList<String> features = new LinkedList<>();
        if (featureList != null && featureList.size() > 0) {
            for (LinkedHashMap<String, Object> map : featureList) {
                features.add(map.get(targetName).toString());
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("colums", colums);
        resultMap.put("features", features);
        return Result.OK(resultMap);
    }

    private boolean listIsEmpt(List list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    @ApiOperation(value = "选择数据集列和特征值", notes = "选择数据集列和特征值")
    @PostMapping(value = "/selectedColumnAndFeature")
    public Result<?> selectedColumnAndFeature(@RequestBody @ApiParam(name = "selectColumnAndFeatureModel", value = "被选中的列和特征值", required = true) SelectColumnAndFeatureModel selectColumnAndFeatureModel,
                                              HttpServletRequest req) {
        if (!checkToken(req, null)) {
            return Result.error(401, "Token失效，请重新登录!");
        }
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        String tenantId = req.getHeader("tenant_id");
        // 特征值不为空，数据列为空时
        if (!listIsEmpt(selectColumnAndFeatureModel.getFeatures()) && listIsEmpt(selectColumnAndFeatureModel.getColums())) {
            return Result.error("选择数据列不能为空！");
        }
        // 数据列不为空，特征值为空时
        if (!listIsEmpt(selectColumnAndFeatureModel.getColums()) && listIsEmpt(selectColumnAndFeatureModel.getFeatures())) {
            return Result.error("选择特征值不能为空！");
        }
        // 数据列和特征值都是空的时候按照全量数据处理
        if (listIsEmpt(selectColumnAndFeatureModel.getColums()) && listIsEmpt(selectColumnAndFeatureModel.getFeatures())) {
            return Result.error("请选择特征值和数据列！");
        }
        // 获取yaml中的
        StringBuffer yamlStr = new StringBuffer();
        yamlStr.append("select * from t_meta_yaml where table_name = '").append(selectColumnAndFeatureModel.getTableName() + "'");
        List<LinkedHashMap<String, Object>> yamlList = publicSqlMapper.select(yamlStr.toString());
        String targetName = "";
        if (yamlList != null && yamlList.size() > 0) {
            LinkedHashMap<String, Object> yamlMap = yamlList.get(0);
            Iterator iterator1 = yamlMap.entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator1.next();
                if (entry.getKey().equals("meta_yaml")) {
                    Yaml yaml = new Yaml();
                    Config config = yaml.loadAs(entry.getValue().toString(), Config.class);
                    targetName = config.getTargets().get(0).getName();
                }
            }
        }
        // 组装表头sql
        LinkedList<String> colums = selectColumnAndFeatureModel.getColums();
        LinkedList<String> features = selectColumnAndFeatureModel.getFeatures();
        LakefsUtil lakefsUtil = new LakefsUtil(lakefsUrl, lakefsName, lakefsPass);
        StringBuffer sqlStr = new StringBuffer();
        sqlStr.append("select resource_id,y, ").append(colums.toString().substring(1, colums.toString().length() - 1)).append(" from fl_file_meta_detail_").append(selectColumnAndFeatureModel.getTableName())
                .append(" where ").append(targetName).append(" in (").append(features.toString().substring(1, features.toString().length() - 1)).append(")");
        log.info("sqlStr============>" + sqlStr);
        List<LinkedHashMap<String, Object>> featureDataList = publicSqlMapper.select(sqlStr.toString());
        LinkedList<Object> csvHead = new LinkedList<>();
        LinkedHashMap<String, Object> headMap = featureDataList.get(0);
        Iterator iterator = headMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (!"id".equals(entry.getKey()) && !"job_id".equals(entry.getKey()) && !"path".equals(entry.getKey()) && !"path_small".equals(entry.getKey())) {
                if ("resource_id".equals(entry.getKey())) {
                    csvHead.add("id");
                    csvHead.add(targetName);
                } else {
                    if (colums.contains(entry.getKey())) {
                        csvHead.add(entry.getKey());
                    }
                }
            }
        }
        log.info("csvHead========================================>>>>>>>>>>" + csvHead);
        // 组装csv数据
        long currentTime = System.currentTimeMillis();
        String filePathStr = uploadPath + currentTime;
        File filePath = new File(filePathStr);
        if (!filePath.exists()) {
            filePath.mkdirs();// 创建文件根目录
        }
        CsvFileUtil.createCSVFile(csvHead, featureDataList, filePathStr, "train");
        try {
            // 处理yaml
            Config config = new Config();
            if (yamlList != null && yamlList.size() > 0) {
                LinkedHashMap<String, Object> yamlMap = yamlList.get(0);
                Iterator iterator1 = yamlMap.entrySet().iterator();

                while (iterator1.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator1.next();
                    if (entry.getKey().equals("meta_yaml")) {
                        Yaml yaml = new Yaml();
                        File yamlFile = new File(filePathStr + "/meta.yaml");
                        try {
                            yamlFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        config = yaml.loadAs(entry.getValue().toString(), Config.class);
                        log.info("yamlFile.getPath()============>" + yamlFile.getPath());
                        FileWriter fileWriter = null;
                        try {
                            DumperOptions options = new DumperOptions();
                            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                            yaml = new Yaml(new NullRepresenter(), options);
                            fileWriter = new FileWriter(yamlFile);
                            fileWriter.write(yaml.dumpAsMap(config)); //yamlPojo是我们的实体类对象，自行生成即可
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            JSONObject jsonObject = fileUploadService.saveFile(config.getAttributes().getName(), currentTime, filePathStr, null);
            CollectFileMeta collectFileMeta = JSON.toJavaObject(jsonObject, CollectFileMeta.class);
            collectFileMeta.setName(collectFileMeta.getName());
            collectFileMeta.setCreateParty(selectColumnAndFeatureModel.getCreateParty());
            collectFileMeta.setPartyId(Integer.valueOf(tenantId));
            collectFileMeta.setHidden(true);
            collectFileMeta.setJobId(selectColumnAndFeatureModel.getJobId());
            fileUploadService.save("0", collectFileMeta);
            return Result.OK(jsonObject);
        } catch (Exception e) {
            return Result.error("保存子数据集失败！");
        }
    }

    @ApiOperation(value = "数据集展示", notes = "数据集展示")
    @GetMapping(value = "/listOfMeta")
    public Result<?> listOfMeta(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                @RequestParam(required = true) @ApiParam(name = "tableName", value = "表名") String tableName,
                                @RequestParam(required = false) @ApiParam(name = "selectFeatures", value = "选择的特征值") String selectFeatures,
                                HttpServletRequest req) {
        if (!checkToken(req, null)) {
            return Result.error(401, "Token失效，请重新登录!");
        }
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        // 获取yaml中的
        StringBuffer yamlStr = new StringBuffer();
        yamlStr.append("select * from t_meta_yaml where table_name = '").append(tableName + "'");
        List<LinkedHashMap<String, Object>> yamlList = publicSqlMapper.select(yamlStr.toString());
        // 处理yaml
        List<String> targetColums = new ArrayList<>();
        String targetName = "";
        boolean hasTarget = false;
        if (yamlList != null && yamlList.size() > 0) {
            LinkedHashMap<String, Object> yamlMap = yamlList.get(0);
            Iterator iterator1 = yamlMap.entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator1.next();
                if (entry.getKey().equals("meta_yaml")) {
                    Yaml yaml = new Yaml();
                    Config config = yaml.loadAs(entry.getValue().toString(), Config.class);
                    if (config.getTargets() != null && config.getTargets().size() > 0) {
                        hasTarget = true;
                        targetName = config.getTargets().get(0).getName();
                        List<Targets> targetsList = config.getTargets();
                        for (Targets targets : targetsList) {
                            targetColums.add(targets.getName());
                        }
                    }
                }
            }
        }
        String tableNameStr = tableName;
        tableName = "fl_file_meta_detail_" + tableName;
        StringBuffer columnStr = new StringBuffer();
        columnStr.append("show COLUMNS from ").append(tableName);
        List<LinkedHashMap<String, Object>> columnList = publicSqlMapper.select(columnStr.toString());
        List<String> colums = new ArrayList<>();
        if (columnList != null && columnList.size() > 0) {
            for (LinkedHashMap<String, Object> map : columnList) {
                String filedValue = map.get("Field").toString();
                if (!"id".equals(filedValue) && !"job_id".equals(filedValue) && !filedValue.contains("path")) {
                    if ("resource_id".equals(filedValue)) {
                        colums.add("id");
                    } else {
                        colums.add(map.get("Field").toString());
                    }
                }
            }
        }
        int offset = (pageNo - 1) * pageSize;
        StringBuffer sqlStr = new StringBuffer();
        sqlStr.append("select * from ").append(tableName).append(" where job_id = 'base' ");
        if (selectFeatures != null && selectFeatures.length() > 0) {
            sqlStr.append(" and ").append(targetName).append(" in ('");
            String[] selectFeatureArr = selectFeatures.split(",");
            for (String selectFeature : selectFeatureArr) {
                sqlStr.append(selectFeature).append("','");
            }
            sqlStr.deleteCharAt(sqlStr.length() - 1);
            sqlStr.deleteCharAt(sqlStr.length() - 1);
            sqlStr.append(")");
        }
        sqlStr.append("limit ").append(offset).append(",").append(pageSize);
        List<LinkedHashMap<String, Object>> list = publicSqlMapper.select(sqlStr.toString());
        List<LinkedHashMap<String, Object>> returnList = new ArrayList<>();
        for (LinkedHashMap<String, Object> hashMap : list) {
            LinkedHashMap<String, Object> resultHashMap = new LinkedHashMap<>();
            Iterator it = hashMap.entrySet().iterator();
            if (hashMap.containsKey("path")) {
                // 图片数据集返回主键id
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    resultHashMap.put(entry.getKey().toString(), entry.getValue());
                }
            } else {
                //非图片数据集，转化一下resource_id
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    if (!("id".equals(entry.getKey()) || "job_id".equals(entry.getKey()))) {
                        if ("resource_id".equals(entry.getKey())) {
                            resultHashMap.put("id", entry.getValue());
                        } else {
                            resultHashMap.put(entry.getKey().toString(), entry.getValue());
                        }
                    }
                }
            }
            returnList.add(resultHashMap);
        }
        StringBuffer countStr = new StringBuffer();
        countStr.append("select count(*) cnt from ").append(tableName).append(" where job_id = 'base' ");
        if (selectFeatures != null && selectFeatures.length() > 0) {
            countStr.append(" and ").append(targetName).append(" in ('");
            String[] selectFeatureArr = selectFeatures.split(",");
            for (String selectFeature : selectFeatureArr) {
                countStr.append(selectFeature).append("','");
            }
            countStr.deleteCharAt(sqlStr.length() - 1);
            countStr.deleteCharAt(sqlStr.length() - 1);
            countStr.append(")");
        }
        List<LinkedHashMap<String, Object>> countlist = publicSqlMapper.select(countStr.toString());
        double count = 0;
        if (countlist != null && countlist.size() > 0) {
            LinkedHashMap<String, Object> map = countlist.get(0);
            count = (long) map.get("cnt");
        }
        Map<String, Object> result = new HashMap<>();
        // 获取特征值
        if (hasTarget) {
            LinkedList<Map<String, String>> features = getFeaturesByTableName(tableNameStr, targetName);
            result.put("targetColums", targetColums);
            result.put("features", features);
            result.put("returnFeatures", selectFeatures == null ? features : selectFeatures);
        }
        result.put("data", returnList);
        result.put("colums", colums);
        result.put("current", pageNo);
        result.put("Size", pageSize);
        result.put("total", (int) count);
        return Result.OK(result);
    }

    private LinkedList<Map<String, String>> getFeaturesByTableName(String tableName, String targetName) {
        LinkedList<Map<String, String>> features = new LinkedList<>();
        // 获取yaml中的
        StringBuffer yamlStr = new StringBuffer();
        yamlStr.append("select * from t_meta_yaml where table_name = '").append(tableName + "'");
        List<LinkedHashMap<String, Object>> yamlList = publicSqlMapper.select(yamlStr.toString());
        // 处理yaml
        if (yamlList != null && yamlList.size() > 0) {
            LinkedHashMap<String, Object> yamlMap = yamlList.get(0);
            Iterator iterator1 = yamlMap.entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator1.next();
                if (entry.getKey().equals("meta_yaml")) {
                    Yaml yaml = new Yaml();
                    Config config = yaml.loadAs(entry.getValue().toString(), Config.class);
                    List<LabelMapping> labelMappings = config.getLabelMapping();
                    log.info("labelMappings:{}", labelMappings);
                    for (LabelMapping labelMapping : labelMappings) {
                        Map<String, String> map = new HashMap<>();
                        map.put(labelMapping.getLabel(), labelMapping.getMapped());
                        features.add(map);
                    }
                }
            }
        }
        log.info("features:{}", features);
        return features;
    }


    @ApiOperation(value = "选择数据集", notes = "选择数据集")
    @PostMapping(value = "/selectedMeta")
    public Result<?> selectedMeta(@RequestBody @ApiParam(name = "selectCollectModel", value = "被选中数据对象", required = true) SelectCollectModel selectCollectModel,
                                  HttpServletRequest req) {
        if (!checkToken(req, null)) {
            return Result.error(401, "Token失效，请重新登录!");
        }
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        String tenantId = req.getHeader("tenant_id");
        LakefsUtil lakefsUtil = new LakefsUtil(lakefsUrl, lakefsName, lakefsPass);
        // 被选择的数据入库
        StringBuffer sqlStr = new StringBuffer();
        String tableName = "fl_file_meta_detail_" + selectCollectModel.getTableName();
        List<Map<String, String>> ids = selectCollectModel.getIds();
        // ids为空且选择的数据集特征，表示选择的是全部数据
        if ((ids == null || ids.size() == 0) && (selectCollectModel.getSelectedFeatures() == null || selectCollectModel.getSelectedFeatures().size() == 0)) {
            return Result.OK();
        }
        try {
            long currentTime = System.currentTimeMillis();
            String filePathStr = uploadPath + currentTime;
            String targetName = "";
            // 处理yaml
            StringBuffer yamlStr = new StringBuffer();
            yamlStr.append("select * from t_meta_yaml where table_name = '").append(selectCollectModel.getTableName() + "'");
            List<LinkedHashMap<String, Object>> yamlList = publicSqlMapper.select(yamlStr.toString());
            Config config = new Config();
            if (yamlList != null && yamlList.size() > 0) {
                LinkedHashMap<String, Object> yamlMap = yamlList.get(0);
                Iterator iterator1 = yamlMap.entrySet().iterator();

                while (iterator1.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator1.next();
                    if (entry.getKey().equals("meta_yaml")) {
                        Yaml yaml = new Yaml();
                        File yamlFile = new File(filePathStr + "/meta.yaml");
                        try {
                            yamlFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        config = yaml.loadAs(entry.getValue().toString(), Config.class);
                        if (config.getTargets() != null && config.getTargets().size() > 0) {
                            targetName = config.getTargets().get(0).getName();
                        }
                        log.info("yamlFile.getPath()============>" + yamlFile.getPath());
                        FileWriter fileWriter = null;
                        try {
                            DumperOptions options = new DumperOptions();
                            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                            yaml = new Yaml(new NullRepresenter(), options);
                            fileWriter = new FileWriter(yamlFile);
                            fileWriter.write(yaml.dumpAsMap(config)); //yamlPojo是我们的实体类对象，自行生成即可
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            StringBuffer columnStr = new StringBuffer();
            columnStr.append("show COLUMNS from ").append(tableName);
            List<LinkedHashMap<String, Object>> columnList = publicSqlMapper.select(columnStr.toString());
            List<String> colums = new ArrayList<>();
            if (columnList != null && columnList.size() > 0) {
                for (LinkedHashMap<String, Object> map : columnList) {
                    colums.add(map.get("Field").toString());
                }
            }
            StringBuffer idStr = new StringBuffer();
            if (selectCollectModel.getSelectedFeatures() != null && selectCollectModel.getSelectedFeatures().size() > 0) {
                sqlStr.append("select * from ").append(tableName).append(" where job_id = 'base' ");
                sqlStr.append(" and ").append(targetName).append(" in (");
                for (Map.Entry<String, String> entry : selectCollectModel.getSelectedFeatures().entrySet()) {
                    sqlStr.append("'").append(entry.getKey()).append("',");
                }
                sqlStr.substring(0, idStr.length() - 1);
                sqlStr.append(")");
            }
            if (ids != null || ids.size() > 0) {
                idStr = new StringBuffer();
                for (Map<String, String> idMap : ids) {
                    idStr.append("'").append(idMap.get("id")).append("',");
                }
                String empStr = idStr.substring(0, idStr.length() - 1);
                if (colums.contains("resource_id")) {
                    sqlStr.append("select * from ").append(tableName).append(" where resource_id in (").append(empStr).append(")");
                } else {
                    sqlStr.append("select * from ").append(tableName).append(" where id in (").append(empStr).append(")");
                }
            }
            log.info("sqlStr============>" + sqlStr);
            List<LinkedHashMap<String, Object>> featureDataList = publicSqlMapper.select(sqlStr.toString());
            List<Object> csvHead = new ArrayList<>();
            LinkedHashMap<String, Object> headMap = featureDataList.get(0);
            Iterator iterator = headMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (!"id".equals(entry.getKey()) && !"job_id".equals(entry.getKey()) && !"path".equals(entry.getKey()) && !"path_small".equals(entry.getKey())) {
                    if ("resource_id".equals(entry.getKey())) {
                        csvHead.add("id");
                    } else {
                        csvHead.add(entry.getKey());
                    }
                }
            }
            log.info("csvHead========================================>>>>>>>>>>" + csvHead.toString());

            File filePath = new File(filePathStr);
            if (!filePath.exists()) {
                filePath.mkdirs();// 创建文件根目录
            }
            CsvFileUtil.createCSVFile(csvHead, featureDataList, filePathStr, "train");
            File imagesPath = new File(filePathStr + "/images");
            if (!imagesPath.exists()) {
                imagesPath.mkdirs();// 创建图片根目录
            }
            log.info("currentTime文件夹===========>" + currentTime);
            String sourcePath = "";

            for (LinkedHashMap<String, Object> row : featureDataList) {
                Iterator iterator1 = row.entrySet().iterator();
                while (iterator1.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator1.next();
                    if ("path".equals(entry.getKey())) {
                        sourcePath = entry.getValue().toString();
                        File file = lakefsUtil.getObject(sourcePath);
                        String[] strings = sourcePath.split("/");
                        file.renameTo(new File(imagesPath + "/" + strings[strings.length - 1]));
                    }
                }
            }

            JSONObject jsonObject = fileUploadService.saveFile(config.getAttributes().getName(), currentTime, filePathStr, selectCollectModel.getSelectedColumns());
            CollectFileMeta collectFileMeta = JSON.toJavaObject(jsonObject, CollectFileMeta.class);
            collectFileMeta.setName(collectFileMeta.getName());
            collectFileMeta.setCreateParty(selectCollectModel.getCreateParty());
            collectFileMeta.setPartyId(Integer.valueOf(tenantId));
            collectFileMeta.setHidden(true);
            collectFileMeta.setJobId(selectCollectModel.getJobId());
            fileUploadService.save("0", collectFileMeta);
            return Result.OK(jsonObject);
        } catch (Exception e) {
            return Result.error("保存子数据集失败！");
        }
    }

    @ApiOperation(value = "查看图片", notes = "查看lakefs图片")
    @GetMapping(value = "/viewpic")
    public void viewpic(@RequestParam @ApiParam(name = "path", value = "文件路径", required = true) String path, HttpServletResponse response, @RequestParam String token) {
        token = token.replace(' ', '+');
        if (!checkToken(null, token)) {
            response.setStatus(401);
            return;
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        try {
            LakefsUtil lakefsUtil = new LakefsUtil(lakefsUrl, lakefsName, lakefsPass);
            File file = lakefsUtil.getObject(path);
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = null;
            try {
                outputStream = response.getOutputStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                response.flushBuffer();
            } catch (IOException e) {
                log.error("预览文件失败" + e.getMessage());
                response.setStatus(404);
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "心跳测试", notes = "心跳测试，直接返回成功")
    @GetMapping(value = "/healthz")
    public Result<?> healthz() {
        return Result.OK();
    }

    private boolean checkToken(HttpServletRequest req, String token) {
        if (StringUtils.isEmpty(token)) {
            token = req.getHeader("X-Access-Token");
        }
        log.info("token============>" + token);
        String url = "http://" + backendPath + ".fml-console:8080/jeecg-boot/sys/checkToken";
        String empResult = httpClientPool.get(url, token);
        log.info("checkTokenResult==========>" + empResult);
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(empResult);
        int code = Integer.valueOf(jsonObject.get("code").toString());
        if (code != 200) {
            return false;
        }
        return true;
    }


}