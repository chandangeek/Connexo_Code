package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Base methods that can be used to sync the (Kore) Meter with the configuration of this (MDC) Device
 *
 * Additional behaviour related to 'Kore' objects
 *
 * Copyrights EnergyICT
 * Date: 21/04/2016
 * Time: 13:14
 */
public abstract class AbstractSyncDeviceWithKoreMeter implements SyncDeviceWithKoreMeter {

    private final MeteringService meteringService;
    private final MdcReadingTypeUtilService readingTypeUtilService;

    private Instant start;
    private DeviceImpl device;
    private MultiplierType multiplierType;

    AbstractSyncDeviceWithKoreMeter(MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, Instant start) {
        this.meteringService = meteringService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.start = start;
        this.multiplierType = getDefaultMultiplierType();
    }

    protected void setDevice(DeviceImpl device) {
        this.device = device;
    }

    protected DeviceImpl getDevice() {
        return device;
    }

    protected Instant getStart() {
        return start;
    }

    protected void setStart(Instant start) {
        this.start = start;
    }

    protected void endCurrentMeterConfigurationIfPresent() {
        device.getMeter().get().getConfiguration(start).ifPresent(meterConfiguration -> meterConfiguration.endAt(start));
    }

    protected void endCurrentMeterActivationIfPresent() {
        device.getMeter().get().getCurrentMeterActivation().ifPresent(meterActivation -> meterActivation.endAt(start));
    }

    protected void obsoleteKoreDevice() {
        if (device.getMeter().isPresent()) {
            this.device.getMeter().get().makeObsolete();
        }
    }
    protected Meter.MeterConfigurationBuilder meterconfigurationBuilder(boolean withCalculatedReadingType){
        Meter.MeterConfigurationBuilder meterConfigurationBuilder = device.getMeter().get().startingConfigurationOn(start);
        createMeterConfigurationsForChannelSpecs(meterConfigurationBuilder, withCalculatedReadingType);
        createMeterConfigurationsForRegisterSpecs(meterConfigurationBuilder, withCalculatedReadingType);
        return meterConfigurationBuilder;
    }

    protected Optional<MeterConfiguration> createKoreMeterConfiguration(boolean addCalculatedReadingType) {
        if (device.getDeviceConfiguration().getChannelSpecs().size() > 0 || device.getDeviceConfiguration().getRegisterSpecs().size() > 0) {
            return Optional.of(meterconfigurationBuilder(addCalculatedReadingType).create());
        }
        return Optional.empty();
    }

    protected abstract MeterActivation activateMeter(Instant start);

    protected void addKoreChannelsIfNecessary(MeterActivation activation) {
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

    protected void createMeterConfigurationsForChannelSpecs(Meter.MeterConfigurationBuilder meterConfigurationBuilder, boolean addCalculatedReadingType) {
        device.getDeviceConfiguration().getChannelSpecs()
                .forEach(channelSpec ->
                    configureReadingType(
                        meterConfigurationBuilder,
                        channelSpec.getReadingType(),
                        channelSpec.getNbrOfFractionDigits(),
                        channelSpec.getOverflow(),
                        (addCalculatedReadingType && channelSpec.isUseMultiplier() ?  getMultipliedReadingTypeForChannelSpec(channelSpec) : null))
                );
    }

    protected void createMeterConfigurationsForRegisterSpecs(Meter.MeterConfigurationBuilder meterConfigurationBuilder, boolean addCalculatedReadingType) {
        device.getDeviceConfiguration().getRegisterSpecs().stream().filter(registerSpec -> !registerSpec.isTextual())
                .map(registerSpec1 -> ((NumericalRegisterSpec) registerSpec1))
                .forEach(registerSpec ->
                    configureReadingType(
                        meterConfigurationBuilder,
                        registerSpec.getReadingType(),
                        registerSpec.getNumberOfFractionDigits(),
                        registerSpec.getOverflowValue(),
                        (addCalculatedReadingType && registerSpec.isUseMultiplier() ?  registerSpec.getCalculatedReadingType().get() : null ))
                );
    }

    protected  Meter.MeterReadingTypeConfigurationBuilder configureReadingType(Meter.MeterConfigurationBuilder meterConfigurationBuilder, ReadingType readingType, int numberOfFractionDigits, Optional<BigDecimal> overflowValue, ReadingType calculatedReadingType){
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
        if (activation.isPresent() && activation.get().getMultiplier(multiplierType).isPresent()){
            return activation.get().getMultiplier(multiplierType);
        }
        return Optional.of(MULTIPLIER_ONE);
    }

    protected void setMultiplier(MeterActivation activation, BigDecimal multiplier){
        activation.setMultiplier(multiplierType, multiplier);
    }

    /**
     * Ends the given {@link MeterActivation} at the given start and create a new one by cloning the previouslu ended one's data.
     * @param end date of the given MeterActivation, and becomes the start date of the newly created one
     * @param meterActivation MeterActivation to end
     * @param usagePoint the UsagePoint for the new meterActivation. If not given the meterActivation's UsagePoint is used
     * @return the new MeterActivation
     */
    protected MeterActivation endMeterActivationAndRestart(Instant end, Optional<MeterActivation> meterActivation, Optional<UsagePoint> usagePoint) {
        Optional<BigDecimal> multiplier = Optional.empty();
        if (meterActivation.isPresent()) {
            if (!usagePoint.isPresent()) {
                usagePoint = meterActivation.get().getUsagePoint();
            }
            multiplier = meterActivation.get().getMultiplier(multiplierType);
            meterActivation.get().endAt(end);
        }
        MeterActivation newMeterActivation;
        if (usagePoint.isPresent()) {
            newMeterActivation = device.getMeter().get().activate(usagePoint.get(), end);
        } else {
            newMeterActivation = device.getMeter().get().activate(end);
        }
        multiplier.ifPresent(m -> newMeterActivation.setMultiplier(multiplierType, m));
        // We need channels on the newMeterActivation;
        this.addKoreChannelsIfNecessary(newMeterActivation);
        return newMeterActivation;
    }

    private MultiplierType getDefaultMultiplierType() {
        Optional<MultiplierType> mt = this.meteringService.getMultiplierType(MULTIPLIER_TYPE);
        if (! mt.isPresent()){
            return this.meteringService.createMultiplierType(MULTIPLIER_TYPE);
        }
        return mt.get();
    }

}
