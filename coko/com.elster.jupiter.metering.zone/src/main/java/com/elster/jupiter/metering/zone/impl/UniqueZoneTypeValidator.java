/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.ZoneType;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueZoneTypeValidator implements ConstraintValidator<UniqueName, ZoneTypeImpl> {

    private String message;
    private MeteringZoneService zoneService;

    @Inject
    public UniqueZoneTypeValidator(MeteringZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ZoneTypeImpl zoneType, ConstraintValidatorContext context) {
        return zoneType == null || checkValidity(zoneType, context);
    }

    private boolean checkValidity(ZoneTypeImpl zoneType, ConstraintValidatorContext context) {
        Optional<? extends ZoneType> alreadyExisting = ((MeteringZoneServiceImpl) zoneService).getZoneType(zoneType.getName());
        return !alreadyExisting.isPresent() || !checkExisting(zoneType, alreadyExisting.get(), context);
    }

    private boolean checkExisting(ZoneType zoneType, ZoneType alreadyExisting, ConstraintValidatorContext context) {
        if (areNotTheSame(zoneType, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areNotTheSame(ZoneType zoneType, ZoneType alreadyExisting) {
        return zoneType.getId() != alreadyExisting.getId();
    }
}
