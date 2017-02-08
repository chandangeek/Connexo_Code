/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.exception.MessageSeed;

class InvalidNodeException extends LocalizedException {

    InvalidNodeException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    InvalidNodeException(Thesaurus thesaurus, MessageSeed messageSeed, int value) {
        super(thesaurus, messageSeed, value);
    }

    static InvalidNodeException functionNotAllowedInAutoMode(Thesaurus thesaurus, Function function) {
        InvalidNodeException exception = new InvalidNodeException(thesaurus, MessageSeeds.FUNCTION_NOT_ALLOWED_IN_AUTOMODE, function.name());
        exception.set("Function", function);
        throw exception;
    }

    static InvalidNodeException incompatibleIntervalLengths(Thesaurus thesaurus, IntervalLength il1, IntervalLength il2) {
        throw new InvalidNodeException(thesaurus, MessageSeeds.INCOMPATIBLE_INTERVAL_LENGTHS, il1.toString(), il2.toString());
    }

    static InvalidNodeException deliverableReadingTypeIsNotCompatibleWithFormula(Thesaurus thesaurus, ReadingType readingType, ReadingTypeDeliverable deliverable) {
        throw new InvalidNodeException(
                thesaurus,
                MessageSeeds.READINGTYPE_OF_DELIVERABLE_IS_NOT_COMPATIBLE_WITH_FORMULA,
                readingType.getMRID() + " (" + readingType.getFullAliasName() + ")",
                deliverable.getFormula().getExpressionNode().toString(),
                deliverable.getName());
    }

    static InvalidNodeException customPropertyNotConfigured(Thesaurus thesaurus, PropertySpec propertySpec, CustomPropertySet customPropertySet) {
        throw new InvalidNodeException(
                thesaurus,
                MessageSeeds.CUSTOM_PROPERTY_SET_NOT_CONFIGURED_ON_METROLOGY_CONFIGURATION,
                propertySpec.getName(),
                customPropertySet.getName());
    }

    static InvalidNodeException customPropertySetNotVersioned(Thesaurus thesaurus, CustomPropertySet customPropertySet) {
        throw new InvalidNodeException(
                thesaurus,
                MessageSeeds.CUSTOM_PROPERTY_SET_NOT_VERSIONED,
                customPropertySet.getName());
    }

    static InvalidNodeException customPropertySetNoLongerActive(Thesaurus thesaurus, CustomPropertySet customPropertySet) {
        throw new InvalidNodeException(
                thesaurus,
                MessageSeeds.CUSTOM_PROPERTY_SET_NO_LONGER_ACTIVE,
                customPropertySet.getName());
    }

    static InvalidNodeException customPropertyMustBeNumericalOrSyntheticLoadProfile(Thesaurus thesaurus, CustomPropertySet customPropertySet, PropertySpec propertySpec) {
        throw new InvalidNodeException(
                thesaurus,
                MessageSeeds.CUSTOM_PROPERTY_MUST_BE_NUMERICAL_OR_SYNTHETIC_LOAD_PROFILE,
                propertySpec.getDisplayName(),
                customPropertySet.getName());
    }

    private InvalidNodeException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

}