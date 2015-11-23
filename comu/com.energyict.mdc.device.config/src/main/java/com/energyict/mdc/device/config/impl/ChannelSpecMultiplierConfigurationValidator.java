package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeMridFilter;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 18.11.15
 * Time: 14:51
 */
public class ChannelSpecMultiplierConfigurationValidator implements ConstraintValidator<ValidChannelSpecMultiplierConfiguration, ChannelSpecImpl> {

    @Override
    public void initialize(ValidChannelSpecMultiplierConfiguration validChannelSpecMultiplierConfiguration) {

    }

    @Override
    public boolean isValid(ChannelSpecImpl channelSpec, ConstraintValidatorContext constraintValidatorContext) {
        if (channelSpec.isUseMultiplier()) {
            ReadingType channelSpecReadingType = channelSpec.getReadingType();
            if (readingTypeCanNotBeMultiplied(constraintValidatorContext, channelSpecReadingType)){
                return false;
            }
            if (calculatedReadingTypeIsNotPresent(constraintValidatorContext, channelSpec)){
                return false;
            }
            if (invalidCalculatedReadingType(constraintValidatorContext, getReadingTypeToCompare(channelSpecReadingType), channelSpec.getCalculatedReadingType().get())){
                return false;
            }
        }
        return true;
    }

    private ReadingType getReadingTypeToCompare(ReadingType channelSpecReadingType) {
        return channelSpecReadingType.getCalculatedReadingType().orElse(channelSpecReadingType);
    }

    private boolean invalidCalculatedReadingType(ConstraintValidatorContext constraintValidatorContext, ReadingType channelSpecReadingType, ReadingType calculatedReadingType) {
        if (readingTypeIsCount(channelSpecReadingType)) {
            if (readingTypeIsSecondaryMetered(channelSpecReadingType)) {
                ReadingTypeMridFilter readingTypeMridFilter = ReadingTypeMridFilter.fromTemplateReadingType(channelSpecReadingType).setCommodity(Commodity.ELECTRICITY_PRIMARY_METERED).anyMultiplier().anyUnit();
                if (!calculatedReadingType.getMRID().matches(readingTypeMridFilter.getRegex())) {
                    invalidCalculatedReadingType(constraintValidatorContext);
                    return true;
                }
            } else {
                ReadingTypeMridFilter readingTypeMridFilter = ReadingTypeMridFilter.fromTemplateReadingType(channelSpecReadingType).anyMultiplier().anyUnit();
                if (!calculatedReadingType.getMRID().matches(readingTypeMridFilter.getRegex())) {
                    invalidCalculatedReadingType(constraintValidatorContext);
                    return true;
                }
            }
        } else if (readingTypeIsSecondaryMetered(channelSpecReadingType)) {
            ReadingTypeMridFilter readingTypeMridFilter = ReadingTypeMridFilter.fromTemplateReadingType(channelSpecReadingType).setCommodity(Commodity.ELECTRICITY_PRIMARY_METERED);
            if (!calculatedReadingType.getMRID().matches(readingTypeMridFilter.getRegex())) {
                invalidCalculatedReadingType(constraintValidatorContext);
                return true;
            }
        }
        return false;
    }

    private boolean calculatedReadingTypeIsNotPresent(ConstraintValidatorContext constraintValidatorContext, ChannelSpecImpl channelSpec) {
        if (!channelSpec.getCalculatedReadingType().isPresent()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CALCULATED_READINGTYPE_CANNOT_BE_EMPTY + "}").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean readingTypeCanNotBeMultiplied(ConstraintValidatorContext constraintValidatorContext, ReadingType channelSpecReadingType) {
        if (readingTypeCanNotBeMultiplied(channelSpecReadingType)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.READINGTYPE_CAN_NOT_BE_MULTIPLIED + "}")
                    .addPropertyNode(ChannelSpecImpl.ChannelSpecFields.CHANNEL_TYPE.fieldName() + "readingType").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean readingTypeCanNotBeMultiplied(ReadingType channelSpecReadingType) {
        return readingTypeContainsPrimaryMetered(channelSpecReadingType) ||
                !(readingTypeIsCount(channelSpecReadingType) || readingTypeIsSecondaryMetered(channelSpecReadingType));
    }

    private boolean readingTypeIsSecondaryMetered(ReadingType channelSpecReadingType) {
        return channelSpecReadingType.getCommodity().compareTo(Commodity.ELECTRICITY_SECONDARY_METERED) == 0;
    }

    private boolean readingTypeIsCount(ReadingType channelSpecReadingType) {
        return channelSpecReadingType.getUnit().compareTo(ReadingTypeUnit.COUNT) == 0;
    }

    private boolean readingTypeContainsPrimaryMetered(ReadingType channelSpecReadingType) {
        return channelSpecReadingType.getCommodity().compareTo(Commodity.ELECTRICITY_PRIMARY_METERED) == 0;
    }

    private void invalidCalculatedReadingType(ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CALCULATED_READINGTYPE_DOES_NOT_MATCH_CRITERIA + "}").addConstraintViolation();
    }
}
