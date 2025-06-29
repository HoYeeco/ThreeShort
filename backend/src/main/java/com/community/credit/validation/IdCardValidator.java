package com.community.credit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

/**
 * 身份证号码验证器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public class IdCardValidator implements ConstraintValidator<IdCard, String> {
    
    private boolean allowEmpty;
    
    @Override
    public void initialize(IdCard constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果为空且允许为空，则验证通过
        if (!StringUtils.hasText(value)) {
            return allowEmpty;
        }
        
        // 使用工具类验证身份证号码
        return com.community.credit.utils.IdCardValidator.validate(value);
    }
} 