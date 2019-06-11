/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import aQute.bnd.annotation.ProviderType;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueNameValidator implements ConstraintValidator<UniqueName, TimeOfUseCampaign> {
    private String message;
    private final TimeOfUseCampaignService timeOfUseCampaignService;

    @Inject
    public UniqueNameValidator(TimeOfUseCampaignService timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(TimeOfUseCampaign timeOfUseCampaign, ConstraintValidatorContext context) {
        if (timeOfUseCampaignService.getCampaign(timeOfUseCampaign.getName())
                .filter(timeOfUseCampaign1 -> timeOfUseCampaign1.getId() != timeOfUseCampaign.getId()).isPresent()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return false;
        }
        return true;
    }
}
