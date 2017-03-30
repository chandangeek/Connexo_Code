/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MeasurementType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelSpecInfoFactory {

    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public ChannelSpecInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public List<ChannelSpecInfo> asInfoList(List<ChannelSpec> channelSpecList) {
        if (channelSpecList == null) {
            return Collections.emptyList();
        }
        List<ChannelSpecInfo> infos = new ArrayList<>(channelSpecList.size());
        for (ChannelSpec channelSpec : channelSpecList) {
            infos.add(this.asInfo(channelSpec));
        }
        return infos;
    }

    public ChannelSpecInfo asInfo(ChannelSpec channelSpec) {
        ChannelSpecInfo info = new ChannelSpecInfo();
        info.id = channelSpec.getId();
        info.name = channelSpec.getReadingType().getFullAliasName();
        info.useMultiplier = channelSpec.isUseMultiplier();
        return info;
    }

    public ChannelSpecFullInfo asFullInfo(ChannelSpec channelSpec, ReadingType collectedReadingType, List<ReadingType> multipliedCalculatedRegisterTypes, boolean isLinkedByActiveDeviceConfiguration) {
        ChannelSpecFullInfo info = new ChannelSpecFullInfo();
        info.id = channelSpec.getId();
        info.name = channelSpec.getReadingType().getFullAliasName();
        info.overruledObisCode = channelSpec.getDeviceObisCode();
        channelSpec.getOverflow().ifPresent(bigDecimal -> info.overflowValue = bigDecimal);
        info.nbrOfFractionDigits = channelSpec.getNbrOfFractionDigits();
        info.measurementType = asShortInfo(channelSpec.getChannelType(), collectedReadingType, multipliedCalculatedRegisterTypes);
        info.useMultiplier = channelSpec.isUseMultiplier();
        if (collectedReadingType.getCalculatedReadingType().isPresent()) {
            info.calculatedReadingType = readingTypeInfoFactory.from(collectedReadingType.getCalculatedReadingType()
                    .get());
        }
        if (channelSpec.getCalculatedReadingType().isPresent()) {
            info.multipliedCalculatedReadingType = readingTypeInfoFactory.from(channelSpec.getCalculatedReadingType()
                    .get());
        }
        info.collectedReadingType = readingTypeInfoFactory.from(collectedReadingType);
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> info.possibleCalculatedReadingTypes.add(readingTypeInfoFactory
                .from(readingTypeConsumer)));
        info.parent = new VersionInfo<>(channelSpec.getLoadProfileSpec().getId(), channelSpec.getLoadProfileSpec()
                .getVersion());
        info.version = channelSpec.getVersion();
        info.isLinkedByActiveDeviceConfiguration = isLinkedByActiveDeviceConfiguration;
        return info;
    }

    public ChannelSpecShortInfo asShortInfo(ChannelType channelType,
                                            ReadingType collectedReadingType,
                                            List<ReadingType> multipliedCalculatedRegisterTypes) {
        ChannelSpecShortInfo info = new ChannelSpecShortInfo();
        info.id = channelType.getId();
        MeasurementType measurementType = channelType.getTemplateRegister();
        info.obisCode = measurementType.getObisCode();
        ReadingType readingType = measurementType.getReadingType();
        info.readingType = readingTypeInfoFactory.from(readingType);
        info.isCumulative = readingType.isCumulative();
        info.collectedReadingType = readingTypeInfoFactory.from(collectedReadingType);
        if (collectedReadingType.getCalculatedReadingType().isPresent()) {
            info.calculatedReadingType = readingTypeInfoFactory.from(collectedReadingType.getCalculatedReadingType()
                    .get());
        }
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> info.possibleCalculatedReadingTypes.add(readingTypeInfoFactory
                .from(readingTypeConsumer)));
        info.version = measurementType.getVersion();
        return info;
    }
}
