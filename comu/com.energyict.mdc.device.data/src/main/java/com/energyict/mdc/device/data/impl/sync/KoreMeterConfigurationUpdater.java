/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.impl.ChannelUpdaterImpl;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.RegisterUpdaterImpl;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class KoreMeterConfigurationUpdater extends AbstractSyncDeviceWithKoreMeter {

    private final Clock clock;
    private ReadingType readingType;
    private Integer overruledNbrOfFractionDigits;
    private BigDecimal overruledOverflowValue;
    private Function<Void, Meter.MeterConfigurationBuilder> meterConfigurationBuilderProvider;

    @Inject
    public KoreMeterConfigurationUpdater(ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, EventService eventService) {
        super(deviceService, readingTypeUtilService, eventService, null);
        this.clock = clock;
    }

    public KoreMeterConfigurationUpdater withRegisterUpdater(RegisterUpdaterImpl registerUpdater) {
        this.readingType = registerUpdater.getReadingType();
        this.overruledNbrOfFractionDigits = registerUpdater.getOverruledNbrOfFractionDigits();
        this.overruledOverflowValue = registerUpdater.getOverruledOverflowValue();
        this.meterConfigurationBuilderProvider = (x) -> meterconfigurationBuilderForRegisters();
        return this;
    }

    public KoreMeterConfigurationUpdater withChannelUpdater(ChannelUpdaterImpl channelUpdater) {
        this.readingType = channelUpdater.getReadingType();
        this.overruledNbrOfFractionDigits = channelUpdater.getOverruledNbrOfFractionDigits();
        this.overruledOverflowValue = channelUpdater.getOverruledOverflowValue();
        this.meterConfigurationBuilderProvider = (x) -> meterconfigurationBuilderForChannels();
        return this;
    }


    @Override
    public void syncWithKore(DeviceImpl device) {
        Instant now = this.clock.instant();
        super.setStart(now);
        this.setDevice(device);


        List<MeterReadingTypeConfiguration> readingTypeConfigs = new ArrayList<>();
        device.getMeterReference()
                .get()
                .getConfiguration(now)
                .ifPresent(meterConfiguration -> readingTypeConfigs.addAll(meterConfiguration.getReadingTypeConfigs()));

        endCurrentMeterConfigurationIfPresent();

        /*device.getChannels().stream()
                .forEach(channel -> {
                    readingTypeConfigs.stream()
                            .filter(meterReadingTypeConfiguration -> meterReadingTypeConfiguration.getMeasured().getMRID().compareToIgnoreCase(channel.getReadingType().getMRID())==0)
                            .forEach(meterReadingTypeConfiguration -> {
                                Channel.ChannelUpdater channelUpdater = device.getChannelUpdaterFor(channel);
                                meterReadingTypeConfiguration.getOverflowValue().ifPresent(value -> channelUpdater.setOverflowValue(value));
                                meterReadingTypeConfiguration.getNumberOfFractionDigits().ifPresent(value -> channelUpdater.setNumberOfFractionDigits(value));
                                channelUpdater.update();
                            });

                });*/


        Meter.MeterConfigurationBuilder meterConfigurationBuilder = meterConfigurationBuilderProvider.apply(null);
        device.getChannels().stream()
                .forEach(channel -> {
                    readingTypeConfigs.stream()
                            .filter(meterReadingTypeConfiguration -> meterReadingTypeConfiguration.getMeasured().getMRID().compareToIgnoreCase(channel.getReadingType().getMRID())==0)
                            .forEach(meterReadingTypeConfiguration -> {
                                if (this.readingType.getMRID().compareToIgnoreCase(channel.getReadingType().getMRID())!=0) {
                                    /*configureReadingType(
                                            meterConfigurationBuilder,
                                            channel.getReadingType(),
                                            meterReadingTypeConfiguration.getNumberOfFractionDigits().getAsInt(),
                                            meterReadingTypeConfiguration.getOverflowValue(),
                                            null);*/
                                    Meter.MeterReadingTypeConfigurationBuilder builder = meterConfigurationBuilder.configureReadingType(channel.getReadingType());
                                    meterReadingTypeConfiguration.getOverflowValue().ifPresent(overruledOverflowValue -> builder.withOverflowValue(overruledOverflowValue));
                                    meterReadingTypeConfiguration.getNumberOfFractionDigits().ifPresent(numberOfFractionDigits -> builder.withNumberOfFractionDigits(numberOfFractionDigits));
                                }
                            });

                });

        /*readingTypeConfigs.stream()
                .forEach(meterReadingTypeConfiguration -> {
                            Meter.MeterReadingTypeConfigurationBuilder builder = meterConfigurationBuilder.configureReadingType(meterReadingTypeConfiguration.getMeasured().);
                            meterReadingTypeConfiguration.getOverflowValue().ifPresent(overruledOverflowValue -> builder.withOverflowValue(overruledOverflowValue));
                            meterReadingTypeConfiguration.getNumberOfFractionDigits().ifPresent(numberOfFractionDigits -> builder.withNumberOfFractionDigits(numberOfFractionDigits));
                            builder.create();
                        }
                    );*/

        if (overruledNbrOfFractionDigits != null && overruledOverflowValue != null) {
            meterConfigurationBuilder.configureReadingType(this.readingType)
                    .withOverflowValue(overruledOverflowValue)
                    .withNumberOfFractionDigits(overruledNbrOfFractionDigits).create();
        } else if (overruledNbrOfFractionDigits == null) {
            meterConfigurationBuilder.configureReadingType(this.readingType)
                    .withOverflowValue(overruledOverflowValue).create();
        } else {
            meterConfigurationBuilder.configureReadingType(this.readingType)
                    .withNumberOfFractionDigits(overruledNbrOfFractionDigits).create();
        }
    }

    private Meter.MeterConfigurationBuilder meterconfigurationBuilderForRegisters() {
        Meter.MeterConfigurationBuilder meterConfigurationBuilder = getDevice().getMeterReference()
                .get()
                .startingConfigurationOn(getStart());
        createMeterConfigurationsForChannelSpecs(meterConfigurationBuilder);
        getDevice().getDeviceConfiguration().getRegisterSpecs().stream()
                .filter(registerSpec -> registerSpec.getReadingType() != this.readingType)
                .map(registerSpec1 -> ((NumericalRegisterSpec) registerSpec1))
                .forEach(registerSpec ->
                        configureReadingType(
                                meterConfigurationBuilder,
                                registerSpec.getReadingType(),
                                registerSpec.getNumberOfFractionDigits(),
                                registerSpec.getOverflowValue(),
                                (registerSpec.isUseMultiplier() ? registerSpec.getCalculatedReadingType().get() : null))
                );
        return meterConfigurationBuilder;
    }

    private Meter.MeterConfigurationBuilder meterconfigurationBuilderForChannels() {
        Meter.MeterConfigurationBuilder meterConfigurationBuilder = getDevice().getMeterReference()
                .get()
                .startingConfigurationOn(getStart());
        getDevice().getDeviceConfiguration().getChannelSpecs().stream()
                .filter(channelSpec -> channelSpec.getReadingType() != this.readingType)
                .forEach(channelSpec ->
                        configureReadingType(
                                meterConfigurationBuilder,
                                channelSpec.getReadingType(),
                                channelSpec.getNbrOfFractionDigits(),
                                channelSpec.getOverflow(),
                                (channelSpec.isUseMultiplier() ? channelSpec.getCalculatedReadingType().get() : null))
                );
        createMeterConfigurationsForRegisterSpecs(meterConfigurationBuilder);
        return meterConfigurationBuilder;
    }

    @Override
    public boolean canUpdateCurrentMeterActivation() {
        return false; // a new Meter Activation is started;
    }

    @Override
    protected MeterActivation doActivateMeter(Instant generalizedStartDate) {
        throw new UnsupportedOperationException();
    }
}
