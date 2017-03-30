/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MeasurementType;

import javax.inject.Inject;

public class RegisterTypeInfoFactory {

    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public RegisterTypeInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public RegisterTypeInfo asInfo(MeasurementType measurementType, boolean isLinkedByDeviceType, boolean inLoadProfileType) {
        RegisterTypeInfo info = new RegisterTypeInfo();
        if (inLoadProfileType) {
            measurementType = ((ChannelType) measurementType).getTemplateRegister();
        }
        info.id = measurementType.getId();
        info.obisCode = measurementType.getObisCode();
        info.isLinkedByDeviceType = isLinkedByDeviceType;
        ReadingType readingType = measurementType.getReadingType();
        info.readingType = readingTypeInfoFactory.from(readingType);
        if (readingType.isCumulative()){
            readingType.getCalculatedReadingType().ifPresent(
                    rt -> info.calculatedReadingType = readingTypeInfoFactory.from(rt)
            );
        }
        info.version = measurementType.getVersion();
        return  info;
    }

    public RegisterTypeInfo asInfo(MeasurementType measurementType, boolean isLinkedByDeviceType, boolean isLinkedByActiveRegisterSpec, boolean isLinkedByInactiveRegisterSpec) {
        RegisterTypeInfo info = this.asInfo(measurementType, isLinkedByDeviceType, false);
        info.isLinkedByActiveRegisterConfig = isLinkedByActiveRegisterSpec;
        info.isLinkedByInactiveRegisterConfig = isLinkedByInactiveRegisterSpec;
        return info;
    }
}
