/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.ServerDevice;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.SyncDeviceWithKoreMeter;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public abstract class AbstractSyncDeviceWithKoreMeter implements SyncDeviceWithKoreMeter {

    private final ServerDeviceService deviceService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final EventService eventService;

    private Instant start;
    private ServerDevice device;
    private MultiplierType multiplierType;

    AbstractSyncDeviceWithKoreMeter(ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, EventService eventService, Instant start) {
        this.deviceService = deviceService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.eventService = eventService;
        this.start = generalizeDatesToMinutes(start);
        this.multiplierType = getDefaultMultiplierType();
    }

    protected void setDevice(ServerDevice device) {
        this.device = device;
    }

    protected ServerDevice getDevice() {
        return device;
    }

    protected Instant getStart() {
        return start;
    }

    protected void setStart(Instant start) {
        this.start = generalizeDatesToMinutes(start);
    }

    void endCurrentMeterConfigurationIfPresent() {
        device.getMeter()
                .get()
                .getConfiguration(start)
                .ifPresent(meterConfiguration -> meterConfiguration.endAt(start));
    }

    void endCurrentMeterActivationIfPresent() {
        device.getMeter().get().getCurrentMeterActivation().ifPresent(meterActivation -> meterActivation.endAt(start));
    }

    protected void obsoleteKoreDevice() {
        if (device.getMeter().isPresent()) {
            this.device.getMeter().get().makeObsolete();
        }
    }

    private Meter.MeterConfigurationBuilder meterconfigurationBuilder() {
        Meter.MeterConfigurationBuilder meterConfigurationBuilder = device.getMeter()
                .get()
                .startingConfigurationOn(start);
        createMeterConfigurationsForChannelSpecs(meterConfigurationBuilder);
        createMeterConfigurationsForRegisterSpecs(meterConfigurationBuilder);
        return meterConfigurationBuilder;
    }

    Optional<MeterConfiguration> createKoreMeterConfiguration() {
        if (!device.getDeviceConfiguration().getChannelSpecs().isEmpty() || !device.getDeviceConfiguration().getRegisterSpecs().isEmpty()) {
            return Optional.of(meterconfigurationBuilder().create());
        }
        return Optional.empty();
    }

    public MeterActivation activateMeter(Instant start) {
        return doActivateMeter(generalizeDatesToMinutes(start));
    }

    abstract MeterActivation doActivateMeter(Instant generalizedStartDate);

    void addKoreChannelsIfNecessary(MeterActivation activation) {
        device.getChannels().forEach((x) -> addKoreChannelIfNecessary(activation, x));
        device.getRegisters().forEach((x) -> addKoreChannelIfNecessary(activation, x));
    }

    private void addKoreChannelIfNecessary(MeterActivation activation, Channel channel) {
        addKoreChannelIfNecessary(activation, channel.getReadingType());
    }

    private void addKoreChannelIfNecessary(MeterActivation activation, Register register) {
        addKoreChannelIfNecessary(activation, register.getReadingType());
    }

    private void addKoreChannelIfNecessary(MeterActivation activation, ReadingType readingType) {
        if (!activation.getReadingTypes().contains(readingType)) {
            activation.getChannelsContainer().createChannel(readingType);
        }
    }

    void createMeterConfigurationsForChannelSpecs(Meter.MeterConfigurationBuilder meterConfigurationBuilder) {
        device.getDeviceConfiguration().getChannelSpecs()
                .forEach(channelSpec ->
                        configureReadingType(
                                meterConfigurationBuilder,
                                channelSpec.getReadingType(),
                                channelSpec.getNbrOfFractionDigits(),
                                channelSpec.getOverflow(),
                                channelSpec.isUseMultiplier() ? getMultipliedReadingTypeForChannelSpec(channelSpec) : null));
    }

    void createMeterConfigurationsForRegisterSpecs(Meter.MeterConfigurationBuilder meterConfigurationBuilder) {
        device.getDeviceConfiguration().getRegisterSpecs().stream().filter(registerSpec -> !registerSpec.isTextual())
                .map(registerSpec1 -> ((NumericalRegisterSpec) registerSpec1))
                .forEach(registerSpec ->
                        configureReadingType(
                                meterConfigurationBuilder,
                                registerSpec.getReadingType(),
                                registerSpec.getNumberOfFractionDigits(),
                                registerSpec.getOverflowValue(),
                                registerSpec.isUseMultiplier() ? registerSpec.getCalculatedReadingType().get() : null));
    }

    Meter.MeterReadingTypeConfigurationBuilder configureReadingType(Meter.MeterConfigurationBuilder meterConfigurationBuilder, ReadingType readingType, int numberOfFractionDigits, Optional<BigDecimal> overflowValue, ReadingType calculatedReadingType) {
        Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = meterConfigurationBuilder
                .configureReadingType(readingType)
                .withNumberOfFractionDigits(numberOfFractionDigits);
        overflowValue.ifPresent(meterReadingTypeConfigurationBuilder::withOverflowValue);
        if (calculatedReadingType != null) {
            meterReadingTypeConfigurationBuilder
                    .withMultiplierOfType(multiplierType)
                    .calculating(calculatedReadingType);
        }
        return meterReadingTypeConfigurationBuilder;
    }

    ReadingType getMultipliedReadingTypeForChannelSpec(ChannelSpec channelSpec) {
        ReadingType calculatedReadingType = channelSpec.getCalculatedReadingType().get();
        if (channelSpec.getReadingType().isCumulative()) {
            String code = readingTypeUtilService.createReadingTypeCodeBuilderFrom(calculatedReadingType)
                    .accumulate(channelSpec.getReadingType().getAccumulation()).code();
            return readingTypeUtilService.findOrCreateReadingType(code, calculatedReadingType.getAliasName());
        } else {
            return calculatedReadingType;
        }
    }

    protected Optional<BigDecimal> getMultiplier(Optional<? extends MeterActivation> activation) {
        if (activation.isPresent() && activation.get().getMultiplier(multiplierType).isPresent()) {
            return activation.get().getMultiplier(multiplierType);
        }
        return Optional.of(MULTIPLIER_ONE);
    }

    protected void setMultiplier(MeterActivation activation, BigDecimal multiplier) {
        activation.setMultiplier(multiplierType, multiplier);
    }

    /**
     * Ends the given {@link MeterActivation} at the given start and create a new one by cloning the previouslu ended one's data.
     *
     * @param end date of the given MeterActivation, and becomes the start date of the newly created one
     * @param meterActivation MeterActivation to end
     * @param newUsagePoint the UsagePoint for the new meterActivation. If not given the meterActivation's UsagePoint is used
     * @return the new MeterActivation
     */
    MeterActivation endMeterActivationAndRestart(Instant end, Optional<? extends MeterActivation> meterActivation, Optional<UsagePoint> newUsagePoint) {
        meterActivation.ifPresent(ma -> ma.endAt(end));
        MeterActivation newMeterActivation;

        if (newUsagePoint.isPresent()) {
            Optional<MeterRole> meterRole = meterActivation.flatMap(MeterActivation::getMeterRole);
            if (meterRole.isPresent()) {
                newMeterActivation = device.getMeter().get().activate(newUsagePoint.get(), meterRole.get(), end);
            } else {
                newMeterActivation = device.getMeter().get().activate(newUsagePoint.get(), end);
            }
        } else if (meterActivation.flatMap(MeterActivation::getUsagePoint).isPresent()) {
            newMeterActivation = device.getMeter()
                    .get()
                    .activate(
                            meterActivation.flatMap(MeterActivation::getUsagePoint).get(),
                            meterActivation.flatMap(MeterActivation::getMeterRole).get(),
                            end);
        } else {
            newMeterActivation = device.getMeter().get().activate(end);
        }
        meterActivation.flatMap(ma -> ma.getMultiplier(multiplierType))
                .ifPresent(multiplier -> newMeterActivation.setMultiplier(multiplierType, multiplier));
        // We need channels on the newMeterActivation;
        this.addKoreChannelsIfNecessary(newMeterActivation);
        device.getKoreHelper().setCurrentMeterActivation(Optional.of(newMeterActivation));
        // we need to create new channelReferences if applicable
        this.eventService.postEvent(EventType.RESTARTED_METERACTIVATION.topic(), device);
        return newMeterActivation;
    }

    private MultiplierType getDefaultMultiplierType() {
        return this.deviceService.findDefaultMultiplierType();
    }

    /**
     * We truncate the start- and endDates to minutes as it is not always required to provide seconds in the FrontEnd.
     * This way we prevent having startDates before endDates in the same minute...
     *
     * @param when the date to generalize
     * @return the truncated date
     */
    Instant generalizeDatesToMinutes(Instant when) {
        if (when != null) {
            return when.truncatedTo(ChronoUnit.MINUTES);
        } else {
            return when;
        }
    }

}
