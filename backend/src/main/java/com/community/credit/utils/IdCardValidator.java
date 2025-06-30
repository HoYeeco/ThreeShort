
package com.community.credit.utils;

import java.util.regex.Pattern;

/**
 * 身份证验证工具类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public class IdCardValidator {
    
    /**
     * 18位身份证校验码权重
     */
    private static final int[] FACTORS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    
    /**
     * 18位身份证校验码字符值
     */
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    
    /**
     * 15位身份证正则表达式
     */
    private static final Pattern PATTERN_15 = Pattern.compile("^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$");
    
    /**
     * 18位身份证正则表达式
     */
    private static final Pattern PATTERN_18 = Pattern.compile("^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}(\\d|X|x)$");

    /**
     * 验证身份证号码
     * 
     * @param idCard 身份证号码
     * @return 验证结果
     */
    public static boolean validate(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return false;
        }
        
        // 去除空格
        idCard = idCard.trim().replace(" ", "");
        
        // 15位身份证验证
        if (idCard.length() == 15) {
            return validate15(idCard);
        }
        
        // 18位身份证验证
        if (idCard.length() == 18) {
            return validate18(idCard);
        }
        
        return false;
    }
    
    /**
     * 验证15位身份证
     * 
     * @param idCard 15位身份证号码
     * @return 验证结果
     */
    private static boolean validate15(String idCard) {
        return PATTERN_15.matcher(idCard).matches();
    }
    
    /**
     * 验证18位身份证
     * 
     * @param idCard 18位身份证号码
     * @return 验证结果
     */
    private static boolean validate18(String idCard) {
        // 基本格式验证
        if (!PATTERN_18.matcher(idCard).matches()) {
            return false;
        }
        
        // 校验码验证
        return validateCheckCode(idCard);
    }
    
    /**
     * 验证18位身份证校验码
     * 
     * @param idCard 18位身份证号码
     * @return 验证结果
     */
    private static boolean validateCheckCode(String idCard) {
        try {
            int sum = 0;
            
            // 计算前17位的加权和
            for (int i = 0; i < 17; i++) {
                int digit = Character.getNumericValue(idCard.charAt(i));
                sum += digit * FACTORS[i];
            }
            
            // 计算校验码
            int remainder = sum % 11;
            char expectedCheckCode = CHECK_CODES[remainder];
            char actualCheckCode = Character.toUpperCase(idCard.charAt(17));
            
            return expectedCheckCode == actualCheckCode;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取身份证校验码
     * 
     * @param idCard17 身份证前17位
     * @return 校验码
     */
    public static char getCheckCode(String idCard17) {
        if (idCard17 == null || idCard17.length() != 17) {
            throw new IllegalArgumentException("身份证前17位长度必须为17");
        }
        
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            int digit = Character.getNumericValue(idCard17.charAt(i));
            sum += digit * FACTORS[i];
        }
        
        int remainder = sum % 11;
        return CHECK_CODES[remainder];
    }

} 