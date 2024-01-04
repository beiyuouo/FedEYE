package org.jeecg.modules.system.util;

import java.security.SecureRandom;

/**
 * 使用secureRandom生成随机六位数字
 */
public class RandomCode {

    private static final String ALPHA_NUMERIC_STRING = "0123456789";

    public static String generateCode(int length) {
        StringBuilder builder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(index));
        }
        return builder.toString();
    }

}
