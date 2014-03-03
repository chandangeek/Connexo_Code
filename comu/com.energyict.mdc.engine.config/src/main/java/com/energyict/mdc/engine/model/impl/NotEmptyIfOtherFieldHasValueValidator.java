package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.util.Checks;
import java.lang.reflect.InvocationTargetException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.beanutils.BeanUtils;

public class NotEmptyIfOtherFieldHasValueValidator
    implements ConstraintValidator<NotEmptyIfOtherFieldHasValue, Object> {

    private String fieldName;
    private String expectedFieldValue;
    private String dependFieldName;

    @Override
    public void initialize(final NotEmptyIfOtherFieldHasValue annotation) {
        fieldName          = annotation.fieldName();
        expectedFieldValue = annotation.fieldValue();
        dependFieldName    = annotation.dependFieldName();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext ctx) {

        if (value == null) {
            return true;
        }

        try {
            final String fieldValue       = BeanUtils.getProperty(value, fieldName);
            final String dependFieldValue = BeanUtils.getProperty(value, dependFieldName);

            if (expectedFieldValue.equals(fieldValue) && Checks.is(dependFieldValue).emptyOrOnlyWhiteSpace()) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
                    .addNode(dependFieldName)
                    .addConstraintViolation();
                    return false;
            }

        } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            throw new RuntimeException(ex);

        }

        return true;
    }

}