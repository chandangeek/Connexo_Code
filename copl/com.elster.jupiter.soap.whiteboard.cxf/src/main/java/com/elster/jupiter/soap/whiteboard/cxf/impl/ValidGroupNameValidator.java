package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by bvn on 6/13/16.
 */
public class ValidGroupNameValidator implements ConstraintValidator<ValidGroupName, EndPointConfigurationImpl> {

    @Inject
    public ValidGroupNameValidator() {
    }

    @Override
    public void initialize(ValidGroupName validGroupName) {

    }

    @Override
    public boolean isValid(EndPointConfigurationImpl endPointConfiguration, ConstraintValidatorContext context) {
        if (EndPointAuthentication.BASIC_AUTHENTICATION.equals(((InboundEndPointConfiguration) endPointConfiguration).getAuthenticationMethod())) {
            if (!((InboundEndPointConfiguration) endPointConfiguration).getGroup().isPresent()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(EndPointConfigurationImpl.Fields.GROUP.fieldName()).addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
