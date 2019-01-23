/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueZoneValidator implements ConstraintValidator<UniqueName, ZoneImpl> {

    private String message;
    private MeteringZoneService zoneService;

    @Inject
    public UniqueZoneValidator(MeteringZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ZoneImpl zoneType, ConstraintValidatorContext context) {
        return zoneType == null || checkValidity(zoneType, context);
    }

    private boolean checkValidity(ZoneImpl zone, ConstraintValidatorContext context) {
        Optional<? extends Zone> alreadyExisting = ((MeteringZoneServiceImpl) zoneService).getZone(zone.getName(), zone.getZoneType().getName(), zone.getApplication());
        return !alreadyExisting.isPresent() || !checkExisting(zone, alreadyExisting.get(), context);
    }

    private boolean checkExisting(Zone zone, Zone alreadyExisting, ConstraintValidatorContext context) {
        if (areNotTheSame(zone, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areNotTheSame(Zone zone, Zone alreadyExisting) {
        return zone.getId() != alreadyExisting.getId();
    }
}
