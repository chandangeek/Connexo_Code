/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.EnumSet;
import java.util.Set;

/**
 * Validates that it is only allowed for a channel to have a ReadingType with an interval.
 */
public class ReadingTypeIntervalOnlyForChannelValidator implements ConstraintValidator<ReadingTypeInterval, MeasurementType> {

    private ReadingTypeInterval readingTypeInterval;

    @Override
    public void initialize(ReadingTypeInterval readingTypeInterval) {
        this.readingTypeInterval = readingTypeInterval;
    }

    @Override
    public boolean isValid(MeasurementType measurementType, ConstraintValidatorContext constraintValidatorContext) {
        ReadingType readingType = measurementType.getReadingType();
        if (readingType != null) {
            if (readingTypeInterval.measurementType().equals(MeasurementTypeImpl.REGISTER_DISCRIMINATOR)) {
                if (validMacroPeriod(readingType)
                // allow load profile capture registers        || readingType.getMeasuringPeriod().isApplicable()
                ) {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext.
                            buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.REGISTER_TYPE_SHOULD_NOT_HAVE_INTERVAL_READINGTYPE + "}").
                            addPropertyNode(MeasurementTypeImpl.Fields.READING_TYPE.fieldName()).
                            addConstraintViolation();
                    return false;
                }
            }
        }
        return true; // the fact that the ReadingType must be present will be validated in another validator
    }

    private boolean validMacroPeriod(ReadingType readingType) {
        return readingType.getMacroPeriod().isApplicable() &&
                !(readingType.getMacroPeriod().equals(MacroPeriod.BILLINGPERIOD) ||
                        ((this.recurringMacroPeriods().contains(readingType.getMacroPeriod())) && readingType.getAggregate().isApplicable()));
    }

    private Set<MacroPeriod> recurringMacroPeriods() {
        return EnumSet.of(MacroPeriod.DAILY, MacroPeriod.MONTHLY, MacroPeriod.YEARLY);
    }

}
