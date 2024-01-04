package cn.sdaict.fldatauploader.utils;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * minio文件上传工具类
 */
@Slf4j
public class MinioUtil {
    private static String minioUrl;
    private static String minioName;
    private static String minioPass;
    private static String bucketName;

    public static void setMinioUrl(String minioUrl) {
        MinioUtil.minioUrl = minioUrl;
    }

    public static void setMinioName(String minioName) {
        MinioUtil.minioName = minioName;
    }

    public static void setMinioPass(String minioPass) {
        MinioUtil.minioPass = minioPass;
    }

    public static void setBucketName(String bucketName) {
        MinioUtil.bucketName = bucketName;
    }

    public static String getMinioUrl() {
        return minioUrl;
    }

    public static String getBucketName() {
        return bucketName;
    }

    private static MinioClient minioClient = null;

    public MinioUtil(String minioUrl, String minioName, String minioPass, String bucketName) {
        if (!minioUrl.startsWith("http")) {
            minioUrl = "http://" + minioUrl;
        }
        if (!minioUrl.endsWith("/")) {
            minioUrl = minioUrl.concat("/");
        }
        setMinioUrl(minioUrl);
        setMinioName(minioName);
        setMinioPass(minioPass);
        setBucketName(bucketName);
    }


    /**
     * 上传文件
     */
    public String upload(InputStream stream, String orgName, String bizPath, String customBucket, Map<String, String> metadata) {
        String file_url = "";
        //update-begin-author:wangshuai date:20201012 for: 过滤上传文件夹名特殊字符，防止攻击
        bizPath = filter(bizPath);
        //update-end-author:wangshuai date:20201012 for: 过滤上传文件夹名特殊字符，防止攻击
        String newBucket = bucketName;
        if (isNotEmpty(customBucket)) {
            newBucket = customBucket;
        }
        try {
            initMinio(minioUrl, minioName, minioPass);
            // 检查存储桶是否已经存在
            if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(newBucket).build())) {
                log.info("Bucket already exists.");
            } else {
                // 创建一个名为ota的存储桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(newBucket).build());
                log.info("create a new bucket.");
            }

            // 获取文件名
            orgName = getFileName(orgName);
            String objectName = bizPath + "/" + orgName;

            // 使用putObject上传一个本地文件到存储桶中。
            if (objectName.startsWith("/")) {
                objectName = objectName.substring(1);
            }
            // Create headers
            Map<String, String> headers = new HashMap<>(1);
            // Add custom content type
            headers.put("Content-Type", "application/octet-stream");
            PutObjectArgs objectArgs = PutObjectArgs.builder().object(objectName)
                    .bucket(newBucket)
                    .headers(headers)
                    .userMetadata(metadata)
                    .stream(stream, stream.available(), -1).build();
            minioClient.putObject(objectArgs);
            stream.close();
            file_url = newBucket + "/" + objectName;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }
        return file_url;
    }

    /**
     * 文件上传
     *
     * @param file
     * @param bizPath
     * @return
     */
    public String upload(MultipartFile file, String bizPath, Map<String, String> metadata) throws Exception{
        String orgName = file.getOriginalFilename();
        if ("".equals(orgName)) {
            orgName = file.getName();
        }
        return upload(file.getInputStream(), orgName, bizPath, null, metadata);
    }

    public String upload(File file, String bizPath, Map<String, String> metadata) throws Exception{
        return upload(new FileInputStream(file), file.getName(), bizPath, null, metadata);
    }

    /**
     * 获取文件流
     *
     * @param bucketName
     * @param objectName
     * @return
     */
    public InputStream getMinioFile(String bucketName, String objectName) {
        InputStream inputStream = null;
        try {
            initMinio(minioUrl, minioName, minioPass);
            GetObjectArgs objectArgs = GetObjectArgs.builder().object(objectName)
                    .bucket(bucketName).build();
            inputStream = minioClient.getObject(objectArgs);
        } catch (Exception e) {
            log.info("文件获取失败" + e.getMessage());
        }
        return inputStream;
    }

    /**
     * 删除文件
     *
     * @param bucketName
     * @param objectName
     * @throws Exception
     */
    public void removeObject(String bucketName, String objectName) {
        try {
            initMinio(minioUrl, minioName, minioPass);
            RemoveObjectArgs objectArgs = RemoveObjectArgs.builder().object(objectName)
                    .bucket(bucketName).build();
            minioClient.removeObject(objectArgs);
        } catch (Exception e) {
            log.info("文件删除失败" + e.getMessage());
        }
    }

    /**
     * 获取文件外链
     *
     * @param bucketName
     * @param objectName
     * @param expires
     * @return
     */
    public String getObjectURL(String bucketName, String objectName, Integer expires) {
        initMinio(minioUrl, minioName, minioPass);
        try {
            GetPresignedObjectUrlArgs objectArgs = GetPresignedObjectUrlArgs.builder().object(objectName)
                    .bucket(bucketName)
                    .expiry(expires).build();
            String url = minioClient.getPresignedObjectUrl(objectArgs);
            return URLDecoder.decode(url, "UTF-8");
        } catch (Exception e) {
            log.info("文件路径获取失败" + e.getMessage());
        }
        return null;
    }

    /**
     * 初始化客户端
     *
     * @param minioUrl
     * @param minioName
     * @param minioPass
     * @return
     */
    private MinioClient initMinio(String minioUrl, String minioName, String minioPass) {
        if (minioClient == null) {
            try {
                minioClient = MinioClient.builder()
                        .endpoint(minioUrl)
                        .credentials(minioName, minioPass)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return minioClient;
    }

    /**
     * 上传文件到minio
     *
     * @param stream
     * @param relativePath
     * @return
     */
    public String upload(InputStream stream, String relativePath) throws Exception {
        initMinio(minioUrl, minioName, minioPass);
        if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            log.info("Bucket already exists.");
        } else {
            // 创建一个名为ota的存储桶
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("create a new bucket.");
        }
        PutObjectArgs objectArgs = PutObjectArgs.builder().object(relativePath)
                .bucket(bucketName)
                .contentType("application/octet-stream")
                .stream(stream, stream.available(), -1).build();
        minioClient.putObject(objectArgs);
        stream.close();
        return minioUrl + bucketName + "/" + relativePath;
    }


    /**
     * 判断文件名是否带盘符，重新处理
     *
     * @param fileName
     * @return
     */
    public String getFileName(String fileName) {
        //判断是否带有盘符信息
        // Check for Unix-style path
        int unixSep = fileName.lastIndexOf('/');
        // Check for Windows-style path
        int winSep = fileName.lastIndexOf('\\');
        // Cut off at latest possible point
        int pos = (winSep > unixSep ? winSep : unixSep);
        if (pos != -1) {
            // Any sort of path separator found...
            fileName = fileName.substring(pos + 1);
        }
        //替换上传文件名字的特殊字符
        fileName = fileName.replace("=", "").replace(",", "").replace("&", "")
                .replace("#", "").replace("“", "").replace("”", "");
        //替换上传文件名字中的空格
        fileName = fileName.replaceAll("\\s", "");
        return fileName;
    }

    public boolean isEmpty(Object object) {
        if (object == null) {
            return (true);
        }
        if ("".equals(object)) {
            return (true);
        }
        if ("null".equals(object)) {
            return (true);
        }
        return (false);
    }

    public boolean isNotEmpty(Object object) {
        if (object != null && !object.equals("") && !object.equals("null")) {
            return (true);
        }
        return (false);
    }

    public String filter(String str) throws PatternSyntaxException {
        // 清除掉所有特殊字符
        String regEx = "[`_《》~!@#$%^&*()+=|{}':;',\\[\\].<>?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    public void createBucket(String bucketName) throws Exception{
        initMinio(minioUrl, minioName, minioPass);
        // 检查存储桶是否已经存在
        if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            log.info("Bucket already exists.");
        } else {
            // 创建一个名为ota的存储桶
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("create a new bucket: " + bucketName);
        }
    }
}

