/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.energyict.mdc.masterdata.MeasurementType;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class RegisterTypeOnDeviceTypeInfoFactory {

    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public RegisterTypeOnDeviceTypeInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public RegisterTypeOnDeviceTypeInfo asInfo(MeasurementType measurementType,
                                               boolean isLinkedByDeviceType,
                                               boolean isLinkedByActiveRegisterSpec,
                                               boolean isLinkedByInactiveRegisterSpec,
                                               Optional<RegisteredCustomPropertySet> registeredCustomPropertySet,
                                               List<ReadingType> multipliedCalculatedRegisterTypes,
                                               ReadingType collectedReadingType) {
        RegisterTypeOnDeviceTypeInfo info = new RegisterTypeOnDeviceTypeInfo();

        info.id = measurementType.getId();
        info.obisCode = measurementType.getObisCode();
        info.isLinkedByDeviceType = isLinkedByDeviceType;
        ReadingType readingType = measurementType.getReadingType();
        info.readingType = readingTypeInfoFactory.from(readingType);
        info.isCumulative = readingType.isCumulative();
        info.collectedReadingType = readingTypeInfoFactory.from(collectedReadingType);
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> info.possibleCalculatedReadingTypes.add(readingTypeInfoFactory
                .from(readingTypeConsumer)));
        info.version = measurementType.getVersion();

        info.isLinkedByActiveRegisterConfig = isLinkedByActiveRegisterSpec;
        info.isLinkedByInactiveRegisterConfig = isLinkedByInactiveRegisterSpec;
        if (registeredCustomPropertySet.isPresent()) {
            info.customPropertySet = new DeviceTypeCustomPropertySetInfo(registeredCustomPropertySet.get());
        }
        return info;
    }
}