/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirementsCollector;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import java.util.Collection;
import java.util.List;

/**
 * Abstract class for providing info objects.<br>
 * Currently, there are {@link UsagePointChannelInfoFactory} and {@link UsagePointRegisterInfoFactory}
 */
abstract class AbstractUsagePointChannelInfoFactory {

    abstract DeviceService getDeviceService();

    void fillDevicePartList(List<UsagePointDeviceChannelInfo> list, ReadingType readingType,
                            UsagePointMetrologyConfiguration metrologyConfiguration, UsagePoint usagePoint) {
        ReadingTypeDeliverable readingTypeDeliverable = metrologyConfiguration.getContracts().stream()
                .map(MetrologyContract::getDeliverables)
                .flatMap(Collection::stream)
                .filter(deliverable -> deliverable.getReadingType().equals(readingType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mismatch between channels configuration and reading type deliverable"));
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
                list.get(list.size() - 1).from = meterActivation.getStart().toEpochMilli();
            } else {
                UsagePointDeviceChannelInfo deviceChannelInfo = new UsagePointDeviceChannelInfo();
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
                                    .forEach(ch ->
                                            deviceChannelInfo.channel = createDeviceChannelInfo(device, ch)
                                    );
                            list.add(deviceChannelInfo);
                        });
            }
            meterActivationOld = meterActivation;
        }
    }

    abstract IdWithNameInfo createDeviceChannelInfo(Device device, Channel ch);
}
