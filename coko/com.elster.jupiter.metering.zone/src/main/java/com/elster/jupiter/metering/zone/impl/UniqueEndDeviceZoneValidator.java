/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.MeteringZoneService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;

public class UniqueEndDeviceZoneValidator implements ConstraintValidator<UniqueName, EndDeviceZoneImpl> {

    private String message;
    private MeteringZoneService zoneService;

    @Inject
    public UniqueEndDeviceZoneValidator(MeteringZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(EndDeviceZoneImpl endDeviceZone, ConstraintValidatorContext context) {
        return endDeviceZone == null || checkValidity(endDeviceZone, context);
    }

    private boolean checkValidity(EndDeviceZoneImpl endDeviceZone, ConstraintValidatorContext context) {
        List<EndDeviceZone> existingEndDeviceZone = ((MeteringZoneServiceImpl) zoneService).getByEndDevice(endDeviceZone.getEndDevice()).find();
        Optional<EndDeviceZone> alreadyExisting = existingEndDeviceZone.stream().filter(edz -> edz.getZone().getZoneType().equals(endDeviceZone.getZone().getZoneType())).findFirst();
        return !alreadyExisting.isPresent() || !checkExisting(endDeviceZone, alreadyExisting.get(), context);
    }

    private boolean checkExisting(EndDeviceZone zone, EndDeviceZone alreadyExisting, ConstraintValidatorContext context) {
        if (areTheSameZoneType(zone, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("zone").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areTheSameZoneType(EndDeviceZone zone, EndDeviceZone alreadyExisting) {
        return zone.getZone().getZoneType().getId() == alreadyExisting.getZone().getZoneType().getId();
    }
}
