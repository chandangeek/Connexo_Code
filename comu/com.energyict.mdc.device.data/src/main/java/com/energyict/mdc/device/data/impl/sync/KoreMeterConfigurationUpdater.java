package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.impl.ChannelUpdaterImpl;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.RegisterUpdaterImpl;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.function.Function;

/**
 * Syncing Kore Meter Configuration with register updater values
 * Copyrights EnergyICT
 * Date: 2/06/2016
 * Time: 14:24
 */
public class KoreMeterConfigurationUpdater extends AbstractSyncDeviceWithKoreMeter {

    private final Clock clock;
    private ReadingType readingType;
    private Integer overruledNbrOfFractionDigits;
    private BigDecimal overruledOverflowValue;
    private Function<Void, Meter.MeterConfigurationBuilder> meterConfigurationBuilderProvider;

    @Inject
    public KoreMeterConfigurationUpdater(MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, EventService eventService) {
        super(meteringService, readingTypeUtilService, eventService, null);
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
        super.setStart(this.clock.instant());
        this.setDevice(device);

        endCurrentMeterConfigurationIfPresent();

        Meter.MeterConfigurationBuilder meterConfigurationBuilder = meterConfigurationBuilderProvider.apply(null);
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
        Meter.MeterConfigurationBuilder meterConfigurationBuilder = getDevice().getMeter().get().startingConfigurationOn(getStart());
        createMeterConfigurationsForChannelSpecs(meterConfigurationBuilder, true);
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
        Meter.MeterConfigurationBuilder meterConfigurationBuilder = getDevice().getMeter().get().startingConfigurationOn(getStart());
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
        createMeterConfigurationsForRegisterSpecs(meterConfigurationBuilder, true);
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

// Old sample code to verify
//    private boolean requiredToCreateNewMeterConfiguration(Optional<MeterConfiguration> meterConfiguration, ReadingType readingType, BigDecimal overruledOverflowValue, Integer overruledNbrOfFractionDigits) {
//        if (meterConfiguration.isPresent()) {
//            boolean required;
//            Optional<MeterReadingTypeConfiguration> readingTypeConfiguration = meterConfiguration.get().getReadingTypeConfiguration(readingType);
//            if (overruledOverflowValue == null) {
//                required = readingTypeConfiguration.isPresent() && readingTypeConfiguration.get().getOverflowValue().isPresent();
//            } else {
//                required = !readingTypeConfiguration.isPresent() || !readingTypeConfiguration.get().getOverflowValue().isPresent() || !readingTypeConfiguration.get()
//                        .getOverflowValue()
//                        .get()
//                        .equals(overruledOverflowValue);
//            }
//
//            if (overruledNbrOfFractionDigits == null) {
//                required |= readingTypeConfiguration.isPresent() && readingTypeConfiguration.get().getNumberOfFractionDigits().isPresent();
//            } else {
//                required |= !readingTypeConfiguration.isPresent() || readingTypeConfiguration.get().getNumberOfFractionDigits().getAsInt() != overruledNbrOfFractionDigits;
//            }
//            return required;
//        } else {
//            return true;
//        }
//    }


// Registers
//                DeviceImpl.this.findKoreMeter(getMdcAmrSystem()).ifPresent(koreMeter -> {
    //                    Optional<MeterConfiguration> currentMeterConfiguration = koreMeter.getConfiguration(updateInstant);
    //                    if (requiredToCreateNewMeterConfiguration(currentMeterConfiguration, register.getReadingType(), overruledOverflowValue, overruledNbrOfFractionDigits)) { // if we need to update it
    //                        if (currentMeterConfiguration.isPresent()) {
    //                            MeterConfiguration meterConfiguration = currentMeterConfiguration.get();
    //                            meterConfiguration.endAt(updateInstant);
    //                            Meter.MeterConfigurationBuilder newMeterConfigBuilder = koreMeter.startingConfigurationOn(updateInstant);
    //                            NumericalRegisterSpec registerSpec = (NumericalRegisterSpec) register.getRegisterSpec();
    //                            meterConfiguration.getReadingTypeConfigs().stream().forEach(meterReadingTypeConfiguration -> {
    //                                Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = newMeterConfigBuilder.configureReadingType(meterReadingTypeConfiguration.getMeasured());
    //                                if (registerWeNeedToUpdate(meterReadingTypeConfiguration)) {
    //                                    if (overruledOverflowValue == null) {
    //                                        meterReadingTypeConfigurationBuilder.withOverflowValue(registerSpec.getOverflowValue().orElse(null));
    //                                    } else {
    //                                        meterReadingTypeConfigurationBuilder.withOverflowValue(overruledOverflowValue);
    //                                    }
    //                                } else if (meterReadingTypeConfiguration.getOverflowValue().isPresent()) {
    //                                    meterReadingTypeConfigurationBuilder.withOverflowValue(meterReadingTypeConfiguration.getOverflowValue().get());
    //                                }
    //                                if (registerWeNeedToUpdate(meterReadingTypeConfiguration)) {
    //                                    if (overruledNbrOfFractionDigits == null) {
    //                                        meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(registerSpec.getNumberOfFractionDigits());
    //                                    } else {
    //                                        meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(overruledNbrOfFractionDigits);
    //                                    }
    //                                } else if (meterReadingTypeConfiguration.getNumberOfFractionDigits().isPresent()) {
    //                                    meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(meterReadingTypeConfiguration.getNumberOfFractionDigits().getAsInt());
    //                                }
    //                                meterReadingTypeConfiguration.getCalculated()
    //                                        .ifPresent(readingType -> meterReadingTypeConfigurationBuilder.withMultiplierOfType(getDefaultMultiplierType()).calculating(readingType));
    //                            });
    //                            newMeterConfigBuilder.create();
    //                        } else {
    //                            Meter.MeterConfigurationBuilder newMeterConfigBuilder = koreMeter.startingConfigurationOn(updateInstant);
    //                            getDeviceConfiguration().getRegisterSpecs().stream().filter(spec -> spec instanceof NumericalRegisterSpec)
    //                                    .map(numeriaclSpec -> ((NumericalRegisterSpec) numeriaclSpec)).forEach(registerSpec -> {
    //                                Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = newMeterConfigBuilder.configureReadingType(registerSpec.getReadingType());
    //                                if (registerWeNeedToUpdate(registerSpec) && overruledOverflowValue != null) {
    //                                    meterReadingTypeConfigurationBuilder.withOverflowValue(overruledOverflowValue);
    //                                } else if (registerSpec.getOverflowValue().isPresent()) {
    //                                    meterReadingTypeConfigurationBuilder.withOverflowValue(registerSpec.getOverflowValue().get());
    //                                }
    //                                if (registerWeNeedToUpdate(registerSpec) && overruledNbrOfFractionDigits != null) {
    //                                    meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(overruledNbrOfFractionDigits);
    //                                } else {
    //                                    meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(registerSpec.getNumberOfFractionDigits());
    //                                }
    //                                if (getMultiplier().compareTo(BigDecimal.ONE) == 1 && registerSpec.isUseMultiplier()) {
    //                                    meterReadingTypeConfigurationBuilder.withMultiplierOfType(getDefaultMultiplierType()).calculating((registerSpec.getCalculatedReadingType().get()));
    //                                }
    //                            });
    //                            newMeterConfigBuilder.create();
    //                        }
    //                    }
    //                });

//    private boolean registerWeNeedToUpdate(RegisterSpec registerSpec) {
//         return registerSpec.getReadingType().equals(register.getReadingType());
//     }

    //    private boolean registerWeNeedToUpdate(MeterReadingTypeConfiguration meterReadingTypeConfiguration) {
    //        return meterReadingTypeConfiguration.getMeasured().equals(register.getReadingType());
    //    }

//Channels
//    Instant updateInstant = DeviceImpl.this.clock.instant();
//
//            DeviceImpl.this.findKoreMeter(getMdcAmrSystem()).ifPresent(koreMeter -> {
//                Optional<MeterConfiguration> currentMeterConfiguration = koreMeter.getConfiguration(updateInstant);
//                if (requiredToCreateNewMeterConfiguration(currentMeterConfiguration, channel.getReadingType(), overruledOverflowValue, overruledNbrOfFractionDigits)) { // if we need to update it
//                    if (currentMeterConfiguration.isPresent()) {
//                        MeterConfiguration meterConfiguration = currentMeterConfiguration.get();
//                        meterConfiguration.endAt(updateInstant);
//                        Meter.MeterConfigurationBuilder newMeterConfigBuilder = koreMeter.startingConfigurationOn(updateInstant);
//                        meterConfiguration.getReadingTypeConfigs().stream().forEach(meterReadingTypeConfiguration -> {
//                            Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = newMeterConfigBuilder.configureReadingType(meterReadingTypeConfiguration.getMeasured());
//                            if (channelWeNeedToUpdate(meterReadingTypeConfiguration)) {
//                                if (overruledOverflowValue == null) {
//                                    meterReadingTypeConfigurationBuilder.withOverflowValue(channel.getChannelSpec().getOverflow().orElse(null));
//                                } else {
//                                    meterReadingTypeConfigurationBuilder.withOverflowValue(overruledOverflowValue);
//                                }
//                            } else if (meterReadingTypeConfiguration.getOverflowValue().isPresent()) {
//                                meterReadingTypeConfigurationBuilder.withOverflowValue(meterReadingTypeConfiguration.getOverflowValue().get());
//                            }
//                            if (channelWeNeedToUpdate(meterReadingTypeConfiguration)) {
//                                if (overruledNbrOfFractionDigits == null) {
//                                    meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(channel.getChannelSpec().getNbrOfFractionDigits());
//                                } else {
//                                    meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(overruledNbrOfFractionDigits);
//                                }
//                            } else if (meterReadingTypeConfiguration.getNumberOfFractionDigits().isPresent()) {
//                                meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(meterReadingTypeConfiguration.getNumberOfFractionDigits().getAsInt());
//                            }
//                            meterReadingTypeConfiguration.getCalculated()
//                                    .ifPresent(readingType -> meterReadingTypeConfigurationBuilder.withMultiplierOfType(getDefaultMultiplierType()).calculating(readingType));
//                        });
//                        newMeterConfigBuilder.create();
//                    } else {
//                        Meter.MeterConfigurationBuilder newMeterConfigBuilder = koreMeter.startingConfigurationOn(updateInstant);
//                        getDeviceConfiguration().getChannelSpecs().forEach(channelSpec -> {
//                            Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = newMeterConfigBuilder.configureReadingType(channelSpec.getReadingType());
//                            if (channelWeNeedToUpdate(channelSpec) && overruledOverflowValue != null) {
//                                meterReadingTypeConfigurationBuilder.withOverflowValue(overruledOverflowValue);
//                            } else if (channelSpec.getOverflow().isPresent()) {
//                                meterReadingTypeConfigurationBuilder.withOverflowValue(channelSpec.getOverflow().get());
//                            }
//                            if (channelWeNeedToUpdate(channelSpec) && overruledNbrOfFractionDigits != null) {
//                                meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(overruledNbrOfFractionDigits);
//                            } else {
//                                meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(channelSpec.getNbrOfFractionDigits());
//                            }
//                            if (getMultiplier().compareTo(BigDecimal.ONE) == 1 && channelSpec.isUseMultiplier()) {
//                                meterReadingTypeConfigurationBuilder.withMultiplierOfType(getDefaultMultiplierType()).calculating(getMultipliedReadingTypeForChannelSpec(channelSpec));
//                            }
//                        });
//                        newMeterConfigBuilder.create();
//                    }
//                }
//            });

//    private boolean channelWeNeedToUpdate(ChannelSpec channelSpec) {
//        return channelSpec.getReadingType().equals(channel.getReadingType());
//    }
//
//    private boolean channelWeNeedToUpdate(MeterReadingTypeConfiguration meterReadingTypeConfiguration) {
//        return meterReadingTypeConfiguration.getMeasured().equals(channel.getReadingType());
//    }
//
//    private ReadingType getMultipliedReadingTypeForChannelSpec(ChannelSpec channelSpec) {
//        ReadingType calculatedReadingType = channelSpec.getCalculatedReadingType().get();
//        if (channelSpec.getReadingType().isCumulative()) {
//            String code = readingTypeUtilService.createReadingTypeCodeBuilderFrom(calculatedReadingType)
//                    .accumulate(channelSpec.getReadingType().getAccumulation()).code();
//            return readingTypeUtilService.findOrCreateReadingType(code, calculatedReadingType.getAliasName());
//        } else {
//            return calculatedReadingType;
//        }
//    }


}
