/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.impl.sync.KoreMeterConfigurationUpdater;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;

public class ChannelUpdaterImpl implements Channel.ChannelUpdater {

    private final Channel channel;
    private final EventService eventService;
    private final ServerDeviceService deviceService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final Clock clock;

    private Integer overruledNbrOfFractionDigits;
    private BigDecimal overruledOverflowValue;
    private ObisCode overruledObisCode;

    ChannelUpdaterImpl(ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, Channel channel, EventService eventService) {
        this.deviceService = deviceService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.clock = clock;
        this.channel = channel;
        this.eventService = eventService;
    }

    public ReadingType getReadingType() {
        return channel.getReadingType();
    }

    public Integer getOverruledNbrOfFractionDigits() {
        return overruledNbrOfFractionDigits;
    }

    @Override
    public Channel.ChannelUpdater setNumberOfFractionDigits(Integer overruledNbrOfFractionDigits) {
        this.overruledNbrOfFractionDigits = overruledNbrOfFractionDigits;
        return this;
    }

    public BigDecimal getOverruledOverflowValue() {
        return overruledOverflowValue;
    }

    @Override
    public Channel.ChannelUpdater setOverflowValue(BigDecimal overruledOverflowValue) {
        this.overruledOverflowValue = overruledOverflowValue;
        return this;
    }

    public ObisCode getOverruledObisCode() {
        return overruledObisCode;
    }

    @Override
    public Channel.ChannelUpdater setObisCode(ObisCode overruledObisCode) {
        this.overruledObisCode = overruledObisCode;
        return this;
    }

    @Override
    public void update() {
        DeviceImpl device = (DeviceImpl) channel.getDevice();
        if (numberOfFractionDigitsHasChanged() || overflowValueHasChanged()) {
            device.syncWithKore(new KoreMeterConfigurationUpdater(this.deviceService, this.readingTypeUtilService, this.clock, eventService)
                    .withChannelUpdater(this));
            device.executeSyncs();
        }
        if (obisCodeHasChanged()) {
            new DeviceObisCodeUsageUpdater().update(device, getReadingType(), overruledObisCode);
        }
        device.validateForUpdate();
        device.postSave();
    }

    private boolean numberOfFractionDigitsHasChanged() {
        return this.overruledNbrOfFractionDigits != null
                && this.channel.getNrOfFractionDigits() != this.overruledNbrOfFractionDigits;
    }

    private boolean overflowValueHasChanged() {
        return this.overruledOverflowValue != null
                && !this.channel.getOverflow().equals(Optional.ofNullable(this.overruledOverflowValue));
    }

    private boolean obisCodeHasChanged() {
        return !this.channel.getObisCode().equals(this.overruledObisCode);
    }
}
