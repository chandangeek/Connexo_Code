/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirementsCollector;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.data.DeviceService;

import java.util.List;

/**
 * Abstract class for providing info objects.<br>
 * Currently, there are {@link UsagePointChannelInfoFactory} and {@link UsagePointRegisterInfoFactory}
 *
 * @param <T> represents info class provided by factory
 * @param <I> represents info class for info class provided by factory ;-)
 */
public abstract class AbstractUsagePointChannelInfoFactory<T, I> implements UsagePointChannelInfoSeparator<T> {

    private String purpose;

    AbstractUsagePointChannelInfoFactory(String purpose) {
        this.purpose = purpose;
    }

    abstract UsagePointDevicePartInfoBuilder<I> getUsagePointDevicePartInfoBuilder();

    protected abstract DeviceService getDeviceService();

    void fillDevicePartList(List<I> list, ReadingType readingType, UsagePointMetrologyConfiguration metrologyConfiguration, UsagePoint usagePoint) {
        ReadingTypeDeliverable readingTypeDeliverable = metrologyConfiguration.getDeliverables().stream()
                .filter(deliverable -> deliverable.getReadingType().equals(readingType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Mismatch between %s configuration and reading type deliverable", purpose)));
        ReadingTypeRequirementsCollector requirementsCollector = new ReadingTypeRequirementsCollector();
        readingTypeDeliverable.getFormula().getExpressionNode().accept(requirementsCollector);
        MeterActivation meterActivationOld = null;
        List<MeterActivation> meterActivations = usagePoint.getMeterActivations();
        for (int i = meterActivations.size() - 1; i >= 0; i--) {
            MeterActivation meterActivation = meterActivations.get(i);
            if (meterActivationOld != null
                    && meterActivation.getMeter().equals(meterActivationOld.getMeter())
                    && !list.isEmpty()
                    && meterActivationOld.getStart().equals(meterActivation.getEnd())) {
                getUsagePointDevicePartInfoBuilder().updateFrom(list.get(list.size() - 1), meterActivation.getStart()
                        .toEpochMilli());
            } else {
                UsagePointDevicePartInfoBuilder<I> deviceChannelInfo = getUsagePointDevicePartInfoBuilder();
                meterActivation.getMeter()
                        .map(Meter::getAmrId)
                        .map(Long::parseLong)
                        .flatMap(getDeviceService()::findDeviceById)
                        .ifPresent(device -> {
                            deviceChannelInfo.device = device.getName();
                            deviceChannelInfo.from = meterActivation.getStart().toEpochMilli();
                            deviceChannelInfo.until = meterActivation.getEnd() != null ?
                                    meterActivation.getEnd().toEpochMilli() :
                                    null;
                            requirementsCollector.getReadingTypeRequirements().stream()
                                    .flatMap(readingTypeRequirement -> readingTypeRequirement.getMatchingChannelsFor(
                                            meterActivation.getChannelsContainer()).stream())
                                    .forEach(ch -> {
                                        ReadingType mainReadingType = ch.getMainReadingType();
                                        com.energyict.mdc.device.data.Channel channelOnDevice = device.getChannels()
                                                .stream()
                                                .filter(deviceChannel -> ch.getReadingTypes()
                                                        .contains(deviceChannel.getReadingType()))
                                                .findFirst()
                                                .orElse(null);
                                        deviceChannelInfo.channel = new IdWithNameInfo(
                                                channelOnDevice != null ? channelOnDevice.getId() : null,
                                                mainReadingType.getFullAliasName());
                                    });
                            list.add(deviceChannelInfo.build());
                        });
            }
            meterActivationOld = meterActivation;
        }
    }

    /**
     * Builder for internal info object used in info object provided by particular factory
     *
     * @param <U> defines type of info object to build
     */
    abstract class UsagePointDevicePartInfoBuilder<U> {

        abstract U build();

        // kind of trick to not create a separate updater
        abstract void updateFrom(U element, long value);

        Long from;
        Long until;
        String device;
        IdWithNameInfo channel;
    }
}
