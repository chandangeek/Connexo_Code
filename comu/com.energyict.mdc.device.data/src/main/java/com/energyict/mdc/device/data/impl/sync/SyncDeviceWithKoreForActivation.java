/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class SyncDeviceWithKoreForActivation extends AbstractSyncDeviceWithKoreMeter {

    private DeviceImpl device;

    @Inject
    public SyncDeviceWithKoreForActivation(DeviceImpl device, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, EventService eventService, Instant start) {
        super(deviceService, readingTypeUtilService, eventService, start);
        this.device = device;
    }

    @Override
    MeterActivation doActivateMeter(Instant generalizedActivationDate) {
        Optional<MeterActivation> affectedMeterActivation = device.getMeterActivationsMostRecentFirst()
                .stream()
                .filter(ma -> ma.getRange().contains(generalizedActivationDate))
                .findFirst();
        MeterActivation newActivation = null;
        if (affectedMeterActivation.isPresent()) { // if we already have meter activation, it is possible that it has data
            newActivation = affectedMeterActivation.get().split(generalizedActivationDate);
            removeReadingQualities(newActivation.getChannelsContainer().getChannels());
            device.refreshMeter();
        } else {
            newActivation = device.getKoreHelper().activateMeter(generalizedActivationDate);
        }
        device.getKoreHelper().reloadCurrentMeterActivation();
        return newActivation;
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        super.setDevice(device);
        this.device = device;
        this.activateMeter(getStart());
    }

    @Override
    public boolean canUpdateCurrentMeterActivation() {
        return false;
    }

    /**
     * Data have been copied from old to new channels, but we should erase validation related qualities: see COMU-3231
     *
     * @param channels
     */
    private static void removeReadingQualities(List<Channel> channels) {
        channels.stream()
                .flatMap(channel -> channel.findReadingQualities()
                        // TODO: think of what systems should be taken into account when removing validation related qualities
                        .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM))
                        .actual()
                        .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.REASONABILITY, QualityCodeCategory.VALIDATION))
                        .stream())
                .forEach(ReadingQualityRecord::delete);
    }
}
