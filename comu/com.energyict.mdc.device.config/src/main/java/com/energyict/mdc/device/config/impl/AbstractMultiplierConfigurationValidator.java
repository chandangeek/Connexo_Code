/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeMridFilter;

import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public abstract class AbstractMultiplierConfigurationValidator {

    abstract String getReadingTypeFieldName();

    boolean invalidCalculatedReadingType(ConstraintValidatorContext constraintValidatorContext, ReadingType specReadingType, ReadingType calculatedReadingType) {
        if (readingTypeIsCount(specReadingType)) {
            if (readingTypeIsSecondaryMetered(specReadingType)) {
                ReadingTypeMridFilter readingTypeMridFilter = ReadingTypeMridFilter.fromTemplateReadingType(specReadingType).setCommodity(Commodity.ELECTRICITY_PRIMARY_METERED).anyMultiplier().anyUnit();
                if (!calculatedReadingType.getMRID().matches(readingTypeMridFilter.getRegex())) {
                    invalidCalculatedReadingType(constraintValidatorContext);
                    return true;
                }
            } else {
                ReadingTypeMridFilter readingTypeMridFilter = ReadingTypeMridFilter.fromTemplateReadingType(specReadingType).anyMultiplier().anyUnit();
                if (!calculatedReadingType.getMRID().matches(readingTypeMridFilter.getRegex())) {
                    invalidCalculatedReadingType(constraintValidatorContext);
                    return true;
                }
            }
        } else if (readingTypeIsSecondaryMetered(specReadingType)) {
            ReadingTypeMridFilter readingTypeMridFilter = ReadingTypeMridFilter.fromTemplateReadingType(specReadingType).setCommodity(Commodity.ELECTRICITY_PRIMARY_METERED);
            if (!calculatedReadingType.getMRID().matches(readingTypeMridFilter.getRegex())) {
                invalidCalculatedReadingType(constraintValidatorContext);
                return true;
            }
        }
        return false;
    }

    boolean calculatedReadingTypeIsNotPresent(ConstraintValidatorContext constraintValidatorContext, Optional<ReadingType> calculatedReadingType) {
        if (!calculatedReadingType.isPresent()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CALCULATED_READINGTYPE_CANNOT_BE_EMPTY + "}").addConstraintViolation();
            return true;
        }
        return false;
    }

    boolean readingTypeCanNotBeMultiplied(ConstraintValidatorContext constraintValidatorContext, ReadingType specReadingType) {
        if (readingTypeCanNotBeMultiplied(specReadingType)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.READINGTYPE_CAN_NOT_BE_MULTIPLIED + "}")
                    .addPropertyNode(getReadingTypeFieldName()).addConstraintViolation();
            return true;
        }
        return false;
    }


    boolean readingTypeCanNotBeMultiplied(ReadingType readingType) {
        return readingTypeContainsPrimaryMetered(readingType) ||
                !(readingTypeIsCount(readingType) || readingTypeIsSecondaryMetered(readingType));
    }

    boolean readingTypeIsSecondaryMetered(ReadingType readingType) {
        return readingType.getCommodity().compareTo(Commodity.ELECTRICITY_SECONDARY_METERED) == 0;
    }

    boolean readingTypeIsCount(ReadingType readingType) {
        return readingType.getUnit().compareTo(ReadingTypeUnit.COUNT) == 0;
    }

    boolean readingTypeContainsPrimaryMetered(ReadingType readingType) {
        return readingType.getCommodity().compareTo(Commodity.ELECTRICITY_PRIMARY_METERED) == 0;
    }

    void invalidCalculatedReadingType(ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CALCULATED_READINGTYPE_DOES_NOT_MATCH_CRITERIA + "}").addConstraintViolation();
    }
}
