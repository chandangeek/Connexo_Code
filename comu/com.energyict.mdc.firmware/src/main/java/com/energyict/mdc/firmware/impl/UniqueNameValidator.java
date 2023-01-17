/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;


import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignDomainExtension;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueNameValidator implements ConstraintValidator<UniqueName, HasUniqueName> {

    private boolean caseSensitive;
    private final FirmwareService firmwareService;

    @Inject
    public UniqueNameValidator(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        this.caseSensitive = constraintAnnotation.caseSensitive();
    }

    @Override
    public boolean isValid(HasUniqueName value, ConstraintValidatorContext context) {
        if (!value.isValidName(this.caseSensitive)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(firmwareService.getThesaurus().getSimpleFormat(MessageSeeds.NAME_MUST_BE_UNIQUE).format())
                    .addPropertyNode(FirmwareCampaignDomainExtension.FieldNames.NAME.javaName())
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}
