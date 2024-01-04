package cn.sdaict.fldatauploader.controller;

import cn.hutool.json.JSONUtil;
import cn.sdaict.fldatauploader.entity.ResourceTypeEnum;
import cn.sdaict.fldatauploader.service.FileUploadService;
import cn.sdaict.fldatauploader.service.ObjectStoreService;
import cn.sdaict.fldatauploader.utils.HttpClientPool;
import cn.sdaict.fldatauploader.utils.LakefsUtil;
import cn.sdaict.fldatauploader.utils.PublicSqlMapper;
import cn.sdaict.fldatauploader.vo.CollectFileMeta;
import cn.sdaict.fldatauploader.vo.Result;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

/**
 * uploader
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class FileUploadController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectStoreService objectStoreService;

    // 获取上传的文件夹，具体路径参考application.properties中的配置
    @Value("${web.upload-path}")
    private String uploadPath;

    @Value("${kafka.topic}")
    private String topic;

    @Value("${file-upload.mode}")
    private String mode;

    @Value(value = "${file-upload.lakefs-path}")
    private String branch;

    @Value(value = "${backend-path}")
    private String backendPath;

    @Autowired
    private PublicSqlMapper publicSqlMapper;

    @Autowired
    private HttpClientPool httpClientPool;

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * upload zip file
     *
     * @param request
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/uploadZip")
    public Result<?> uploadZip(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest req) {
        String userName = checkToken(request);
        if (userName == null) {
            return Result.error(401, "Token失效，请重新登录!");
        }
        HttpServletResponse response =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        JSONObject resultJosn = new JSONObject();
        String zipFileName = file.getOriginalFilename();
        assert zipFileName != null;
        if (!zipFileName.toLowerCase().endsWith("zip")) {
            return Result.error("not zip file");
        }
        String repositoryName = LakefsUtil.getPinYin(zipFileName.replace(".zip", ""));
        // 目录增加时间戳，防止重复
        long currentTime = System.currentTimeMillis();
        File filePath = new File(uploadPath + currentTime);
        if (!filePath.exists()) {
            filePath.mkdirs(); // 创建文件根目录
        }
        File saveZipfile = new File(uploadPath + currentTime + "/" + LakefsUtil.getPinYin(zipFileName));
        try {
            log.info("start save zipfile " + saveZipfile.getAbsolutePath());
            file.transferTo(saveZipfile);
            log.info("end save zipfile " + saveZipfile.getAbsolutePath());

            ZipFile zFile = new ZipFile(saveZipfile);
            zFile.setFileNameCharset("UTF-8");

            // if meta.yaml exit
            boolean isMetaExit = false;
            List<FileHeader> headerList = zFile.getFileHeaders();
            for (FileHeader fileHeader : headerList) {
                log.info("文件名称：" + fileHeader.getFileName());
                if (fileHeader.getFileName().equals("meta.yaml")) {
                    isMetaExit = true;
                    break;
                }
            }
            if (!isMetaExit) {
                return Result.error("meta.yaml not exit");
            }

            // unzip file
            String name = zipFileName.replace(".zip", "");
            String unzipDest = uploadPath + currentTime + "/" + repositoryName;
            log.info("start unzip zipfile to " + unzipDest);
            zFile.extractAll(unzipDest);
            log.info("end unzip zipfile " + unzipDest);

            resultJosn = fileUploadService.saveFile(name, currentTime, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.error("数据集上传失败！");
        }

        // delete file
        // saveZipfile.delete();

        return Result.OK(resultJosn);
    }

    @PostMapping(value = "/save")
    public Result<?> save(
            @RequestParam(required = false, defaultValue = "0")
            @ApiParam(name = "isDvc", value = "是否多版本，0否，1是", required = false)
                    String isDvc,
            @RequestBody CollectFileMeta collectFileMeta,
            HttpServletRequest request) {
        String userName = checkToken(request);
        if (userName == null) {
            return Result.error(401, "Token失效，请重新登录!");
        }
        String token = request.getHeader("X-Access-Token");
        collectFileMeta.setCreateBy(userName);
        collectFileMeta.setToken(token);
        collectFileMeta.setResourceType(ResourceTypeEnum.USER_UPLOAD.toString());
        return fileUploadService.save(isDvc, collectFileMeta);
    }

    @PostMapping(value = "/upload")
    @ApiOperation("给数据标注系统使用的上传接口")
    public Result<?> upload(
            @ApiParam Integer partyId,
            @ApiParam String partyName,
            @ApiParam String userName,
            @ApiParam String resourceType,
            @RequestParam("file") MultipartFile file) {
        HttpServletResponse response =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        JSONObject resultJosn = new JSONObject();
        String zipFileName = file.getOriginalFilename();
        assert zipFileName != null;
        if (!zipFileName.toLowerCase().endsWith("zip")) {
            return Result.error("not zip file");
        }
        String repositoryName = LakefsUtil.getPinYin(zipFileName.replace(".zip", ""));
        // 目录增加时间戳，防止重复
        long currentTime = System.currentTimeMillis();
        File filePath = new File(uploadPath + currentTime);
        if (!filePath.exists()) {
            filePath.mkdirs(); // 创建文件根目录
        }
        File saveZipfile = new File(uploadPath + currentTime + "/" + LakefsUtil.getPinYin(zipFileName));
        try {
            log.info("start save zipfile " + saveZipfile.getAbsolutePath());
            file.transferTo(saveZipfile);
            log.info("end save zipfile " + saveZipfile.getAbsolutePath());

            ZipFile zFile = new ZipFile(saveZipfile);
            zFile.setFileNameCharset("UTF-8");

            // if meta.yaml exit
            boolean isMetaExit = false;
            List<FileHeader> headerList = zFile.getFileHeaders();
            for (FileHeader fileHeader : headerList) {
                log.info("文件名称：" + fileHeader.getFileName());
                if (fileHeader.getFileName().equals("meta.yaml")) {
                    isMetaExit = true;
                    break;
                }
            }
            if (!isMetaExit) {
                return Result.error("meta.yaml not exit");
            }
            // unzip file
            String name = zipFileName.replace(".zip", "");
            String unzipDest = uploadPath + currentTime + "/" + repositoryName;
            log.info("start unzip zipfile to " + unzipDest);
            zFile.extractAll(unzipDest);
            log.info("end unzip zipfile " + unzipDest);
            resultJosn = fileUploadService.saveFile(name, currentTime, null, null);
            CollectFileMeta collectFileMeta =
                    JSON.parseObject(resultJosn.toString(), CollectFileMeta.class);
            collectFileMeta.setPartyId(partyId);
            collectFileMeta.setCreateParty(partyName);
            collectFileMeta.setCreateBy(userName);
            collectFileMeta.setResourceType(ResourceTypeEnum.LABLING_SYSTEM.toString());
            if (StringUtils.isNotEmpty(resourceType)) {
                collectFileMeta.setResourceType(resourceType);
            }
            return fileUploadService.save("0", collectFileMeta);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.error("数据集上传失败！");
        }
    }

    private String checkToken(HttpServletRequest req) {
        String token = req.getHeader("X-Access-Token");
        String url = "http://" + backendPath + ".fml-console:8080/jeecg-boot/sys/checkToken";
        String empResult = httpClientPool.get(url, token);
        log.info("checkTokenResult========>" + empResult);
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(empResult);
        String userName = jsonObject.get("result").toString();
        int code = Integer.valueOf(jsonObject.get("code").toString());
        if (code != 200) {
            return null;
        }
        return userName;
    }
}
