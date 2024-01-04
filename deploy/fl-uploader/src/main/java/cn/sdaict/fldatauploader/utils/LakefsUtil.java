package cn.sdaict.fldatauploader.utils;

import io.lakefs.clients.api.*;
import io.lakefs.clients.api.auth.HttpBasicAuth;
import io.lakefs.clients.api.model.*;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.File;

/**
 * @Description: lakefs工具类
 * @author: zhaoxh
 * @date: 2021年12月20日 10:23
 */
public class LakefsUtil {
    private static String lakefsUrl;
    private static String lakefsUserName;
    private static String lakefsPassWord;


    public static void setLakefsUrl(String lakefsUrl) {
        LakefsUtil.lakefsUrl = lakefsUrl;
    }

    public static void setLakefsUserName(String lakefsUserName) {
        LakefsUtil.lakefsUserName = lakefsUserName;
    }

    public static void setLakefsPassWord(String lakefsPassWord) {
        LakefsUtil.lakefsPassWord = lakefsPassWord;
    }

    public static String getLakefsUrl() {
        return lakefsUrl;
    }

    private static ApiClient lakefsClient = null;

    public LakefsUtil(String lakefsUrl, String lakefsUserName, String lakefsPassWord) {
        if (!lakefsUrl.startsWith("http")) {
            lakefsUrl = "http://" + lakefsUrl;
        }
        if (!lakefsUrl.endsWith("/api/v1")) {
            lakefsUrl = lakefsUrl.concat("/api/v1");
        }
        setLakefsUrl(lakefsUrl);
        setLakefsUserName(lakefsUserName);
        setLakefsPassWord(lakefsPassWord);
    }

    private ApiClient initLakefs(String lakefsUrl, String lakefsUserName, String lakefsPassWord) {
        if (lakefsClient == null) {
            try {
                lakefsClient = Configuration.getDefaultApiClient();
                lakefsClient.setBasePath(lakefsUrl);

                // Configure HTTP basic authorization: basic_auth
                HttpBasicAuth basic_auth = (HttpBasicAuth) lakefsClient.getAuthentication("basic_auth");
                basic_auth.setUsername(lakefsUserName);
                basic_auth.setPassword(lakefsPassWord);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return lakefsClient;
    }

    // 所有仓库
    public RepositoryList getRepositoryList() throws Exception {
        initLakefs(lakefsUrl, lakefsUserName, lakefsPassWord);
        RepositoriesApi api = new RepositoriesApi(lakefsClient);
        String prefix = null;
        String after = null;
        Integer amount = 1000;
        return api.listRepositories(prefix, after, amount);
    }

    // 创建仓库
    public Repository createRepository(String repositoryName, String branch, String storageNamespace) throws Exception {
        initLakefs(lakefsUrl, lakefsUserName, lakefsPassWord);
        RepositoriesApi api = new RepositoriesApi(lakefsClient);

        RepositoryCreation repositoryCreation = new RepositoryCreation();
        repositoryCreation.setName(repositoryName);
        repositoryCreation.setDefaultBranch(branch);
        repositoryCreation.setStorageNamespace(storageNamespace);
        Repository createRepositoryResult = api.createRepository(repositoryCreation, false);
        return createRepositoryResult;
    }

    // 上传文件
    public ObjectStats uploadObject(String repository, String branch, String lakefsFilePath, File localFile) throws Exception {
        initLakefs(lakefsUrl, lakefsUserName, lakefsPassWord);
        ObjectsApi api = new ObjectsApi(lakefsClient);

        String storageClass = null;
        String ifNoneMatch = null;

        ObjectStats response = api.uploadObject(repository, branch, lakefsFilePath, storageClass, ifNoneMatch, localFile);
        return response;
    }

    // 提交
    public Commit commit(String repository, String branch, String message) throws Exception {
        initLakefs(lakefsUrl, lakefsUserName, lakefsPassWord);
        CommitsApi api = new CommitsApi(lakefsClient);

        CommitCreation commitCreation = new CommitCreation();
        commitCreation.setMessage(message);
        Commit response = api.commit(repository, branch, commitCreation);

        return response;
    }

    // 查询所有tag
    public RefList listTags(String repository) throws Exception {
        initLakefs(lakefsUrl, lakefsUserName, lakefsPassWord);
        TagsApi api = new TagsApi(lakefsClient);
        String prefix = null;
        String after = null;
        Integer amount = 1000; // 最大1000，超过报错
        RefList response = api.listTags(repository, prefix, after, amount);

        return response;
    }

    // 创建tag
    public Ref createTag(String repository, String id, String ref) throws Exception {
        initLakefs(lakefsUrl, lakefsUserName, lakefsPassWord);
        TagsApi api = new TagsApi(lakefsClient);

        TagCreation tagCreation = new TagCreation();
        tagCreation.setId(id);
        tagCreation.setRef(ref);
        Ref response = api.createTag(repository, tagCreation);

        return response;
    }

    // 删除tag
    public void deleteTag(String repository, String tag) throws Exception {
        initLakefs(lakefsUrl, lakefsUserName, lakefsPassWord);
        TagsApi api = new TagsApi(lakefsClient);

        api.deleteTag(repository, tag);
    }

    public File getObject(String path) throws Exception {
        String[] strings = path.split("/");
        initLakefs(lakefsUrl, lakefsUserName, lakefsPassWord);
        ObjectsApi api = new ObjectsApi(lakefsClient);
        File file = api.getObject(strings[0], strings[1], strings[2] + "/" + strings[3]);
        return file;
    }

    public static String getPinYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] input = inputString.trim().toCharArray();
        String output = "";
        try {
            for (int i = 0; i < input.length; i++) {
                if (Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {  //判断字符是否是中文
                    //toHanyuPinyinStringArray 如果传入的不是汉字，就不能转换成拼音，那么直接返回null
                    //由于中文有很多是多音字，所以这些字会有多个String，在这里我们默认的选择第一个作为pinyin
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                    output += temp[0];
                } else {
                    output += Character.toString(input[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return output;
    }
}
