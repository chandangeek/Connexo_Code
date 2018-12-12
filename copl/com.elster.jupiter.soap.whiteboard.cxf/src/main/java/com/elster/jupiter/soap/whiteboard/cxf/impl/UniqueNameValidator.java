/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Created by bvn on 6/13/16.
 */
public class UniqueNameValidator implements ConstraintValidator<UniqueName, EndPointConfigurationImpl> {

    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public UniqueNameValidator(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public void initialize(UniqueName uniqueName) {

    }

    @Override
    public boolean isValid(EndPointConfigurationImpl endPointConfiguration, ConstraintValidatorContext context) {
        Optional<EndPointConfiguration> found = endPointConfigurationService.getEndPointConfiguration(endPointConfiguration
                .getName());
        if (found.isPresent() && found.get().getId() != endPointConfiguration.getId()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(EndPointConfigurationImpl.Fields.NAME.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
