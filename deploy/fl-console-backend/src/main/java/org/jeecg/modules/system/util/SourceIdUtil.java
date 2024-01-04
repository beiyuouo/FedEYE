package org.jeecg.modules.system.util;

import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 生成sourceId工具类
 */
@Component
public class SourceIdUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成sourceId
     * 
     * @param prefix 前缀,如：JOB,DATA,MODEL
     * @return
     */
    public String generateSourceId(String prefix) {
        // 获取当前日期 yyyyMMddHHmmss
        String date = new java.text.SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        // JOB+日期+4位随机字符串
        String resourceId = prefix + date + RandomStringUtils.randomNumeric(4);
        if (redisTemplate.hasKey(resourceId)) {
            return generateSourceId(prefix);
        } else {
            redisTemplate.opsForValue().set(resourceId, null);
        }
        return resourceId;
    }

}
