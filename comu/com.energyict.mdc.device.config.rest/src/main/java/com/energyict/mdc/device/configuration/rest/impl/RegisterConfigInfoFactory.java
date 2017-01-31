/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.configuration.rest.RegisterConfigInfo;

import javax.inject.Inject;
import java.util.List;

public class RegisterConfigInfoFactory {

    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public RegisterConfigInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public RegisterConfigInfo asInfo(NumericalRegisterSpec registerSpec, List<ReadingType> multipliedCalculatedRegisterTypes) {
        RegisterConfigInfo info = new RegisterConfigInfo();
        info.id = registerSpec.getId();
        info.name = registerSpec.getRegisterType().getReadingType().getFullAliasName();
        info.registerType = registerSpec.getRegisterType().getId();
        info.readingType = readingTypeInfoFactory.from(registerSpec.getRegisterType().getReadingType());
        info.obisCode = registerSpec.getObisCode();
        info.overruledObisCode = registerSpec.getDeviceObisCode();
        info.obisCodeDescription = registerSpec.getObisCode().getDescription();
        info.asText = registerSpec.isTextual();
        info.collectedReadingType = readingTypeInfoFactory.from(registerSpec.getReadingType());
        info.version = registerSpec.getVersion();
        info.parent = new VersionInfo<>(registerSpec.getDeviceConfiguration()
                .getId(), registerSpec.getDeviceConfiguration().getVersion());
        info.numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
        registerSpec.getOverflowValue().ifPresent(overflow -> info.overflow = overflow);
        info.useMultiplier = registerSpec.isUseMultiplier();
        if (info.useMultiplier) {
            info.calculatedReadingType = readingTypeInfoFactory.from(registerSpec.getCalculatedReadingType().get());
        }
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> info.possibleCalculatedReadingTypes.add(readingTypeInfoFactory
                .from(readingTypeConsumer)));
        return info;
    }

    public RegisterConfigInfo asInfo(TextualRegisterSpec registerSpec, List<ReadingType> multipliedCalculatedRegisterTypes) {
        RegisterConfigInfo info = new RegisterConfigInfo();
        info.id = registerSpec.getId();
        info.name = registerSpec.getRegisterType().getReadingType().getFullAliasName();
        info.registerType = registerSpec.getRegisterType().getId();
        info.readingType = readingTypeInfoFactory.from(registerSpec.getRegisterType().getReadingType());
        info.obisCode = registerSpec.getObisCode();
        info.overruledObisCode = registerSpec.getDeviceObisCode();
        info.obisCodeDescription = registerSpec.getObisCode().getDescription();
        info.asText = registerSpec.isTextual();
        info.collectedReadingType = readingTypeInfoFactory.from(registerSpec.getReadingType());
        info.version = registerSpec.getVersion();
        info.parent = new VersionInfo<>(registerSpec.getDeviceConfiguration()
                .getId(), registerSpec.getDeviceConfiguration().getVersion());
        info.numberOfFractionDigits = null;
        info.overflow = null;
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> info.possibleCalculatedReadingTypes.add(readingTypeInfoFactory
                .from(readingTypeConsumer)));
        return info;
    }

    public RegisterConfigInfo from(RegisterSpec registerSpec, List<ReadingType> multipliedCalculatedRegisterTypes) {
        if (registerSpec.isTextual()) {
            return asInfo((TextualRegisterSpec) registerSpec, multipliedCalculatedRegisterTypes);
        } else {
            return asInfo((NumericalRegisterSpec) registerSpec, multipliedCalculatedRegisterTypes);
        }
    }
}
