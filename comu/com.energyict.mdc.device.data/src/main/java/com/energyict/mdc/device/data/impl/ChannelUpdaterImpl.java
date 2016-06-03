package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.math.BigDecimal;
import java.time.Clock;

/**
 * Copyrights EnergyICT
 * Date: 2/06/2016
 * Time: 17:10
 */
public class ChannelUpdaterImpl implements Channel.ChannelUpdater {

    private final Channel channel;
    private final MeteringService meteringService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final Clock clock;

    private Integer overruledNbrOfFractionDigits;
    private BigDecimal overruledOverflowValue;
    private ObisCode overruledObisCode;

    ChannelUpdaterImpl(MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, Channel channel) {
        this.meteringService = meteringService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.clock = clock;
        this.channel = channel;
    }

    public ReadingType getReadingType() {
        return channel.getReadingType();
    }

    public Integer getOverruledNbrOfFractionDigits() {
        return overruledNbrOfFractionDigits;
    }

    @Override
    public Channel.ChannelUpdater setNumberOfFractionDigits(Integer overruledNbrOfFractionDigits) {
        this.overruledNbrOfFractionDigits  = overruledNbrOfFractionDigits;
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
         if (this.overruledNbrOfFractionDigits != null || this.overruledOverflowValue != null){
             device.syncWithKore(new KoreMeterConfigurationUpdater(this.meteringService, this.readingTypeUtilService, this.clock).withChannelUpdater(this));
         }
         if (this.overruledObisCode != null){
           new DeviceObisCodeUsageUpdater().update(device, getReadingType(), overruledObisCode);
         }
         device.save();
     }
}
