package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.BillingRegister;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.FlagsReading;
import com.energyict.mdc.device.data.FlagsRegister;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.TextReading;
import com.energyict.mdc.device.data.TextRegister;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Groups functionality to create info objects for different sorts of data of a device
 */
public class DeviceDataInfoFactory {

    private final ValidationInfoFactory validationInfoFactory;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;
    private final Thesaurus thesaurus;
    private final ValidationRuleInfoFactory validationRuleInfoFactory;
    private final Clock clock;

    @Inject
    public DeviceDataInfoFactory(ValidationInfoFactory validationInfoFactory, EstimationRuleInfoFactory estimationRuleInfoFactory, Thesaurus thesaurus, ValidationRuleInfoFactory validationRuleInfoFactory, Clock clock) {
        this.validationInfoFactory = validationInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
        this.thesaurus = thesaurus;
        this.validationRuleInfoFactory = validationRuleInfoFactory;
        this.clock = clock;
    }

    public ChannelDataInfo createChannelDataInfo(Channel channel, LoadProfileReading loadProfileReading, boolean isValidationActive, DeviceValidation deviceValidation) {
        ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();
        channelIntervalInfo.interval = IntervalInfo.from(loadProfileReading.getRange());
        channelIntervalInfo.readingTime = loadProfileReading.getReadingTime();
        channelIntervalInfo.intervalFlags = new ArrayList<>();
        channelIntervalInfo.validationActive = isValidationActive;
        channelIntervalInfo.intervalFlags.addAll(loadProfileReading.getFlags().stream().map(flag -> thesaurus.getString(flag.name(), flag.name())).collect(Collectors.toList()));
        Optional<IntervalReadingRecord> channelReading = loadProfileReading.getChannelValues().entrySet().stream().map(Map.Entry::getValue).findFirst();// There can be only one channel (or no channel at all if the channel has no dta for this interval)
        channelIntervalInfo.multiplier = channel.getMultiplier().orElseGet(() -> null);
        channelReading.ifPresent(reading -> {
            channelIntervalInfo.value = getRoundedBigDecimal(reading.getValue(), channel);
            addCalculatedValueInfo(channel, channelIntervalInfo, reading);
            channelIntervalInfo.reportedDateTime = reading.getReportedDateTime();
        });
        if (!channelReading.isPresent() && loadProfileReading.getReadingTime() != null) {
            channelIntervalInfo.reportedDateTime = loadProfileReading.getReadingTime();
        }

        Optional<DataValidationStatus> dataValidationStatus = loadProfileReading.getChannelValidationStates().entrySet().stream().map(Map.Entry::getValue).findFirst();
        dataValidationStatus.ifPresent(status -> {
            channelIntervalInfo.mainValidationInfo = validationInfoFactory.createMainVeeReadingInfo(status, deviceValidation, channelReading.orElse(null));
            channelIntervalInfo.bulkValidationInfo = validationInfoFactory.createBulkVeeReadingInfo(channel, status, deviceValidation, channelReading.orElse(null));
        });
        if (!channelReading.isPresent() && !dataValidationStatus.isPresent()) {
            // we have a reading with no data and no validation result => it's a placeholder (missing value) which hasn't validated ( = detected ) yet
            channelIntervalInfo.mainValidationInfo = new MinimalVeeReadingValueInfo();
            channelIntervalInfo.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
            if (channel.getReadingType().isCumulative()) {
                channelIntervalInfo.bulkValidationInfo = new MinimalVeeReadingValueInfo();
                channelIntervalInfo.bulkValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
            }
            channelIntervalInfo.dataValidated = false;
        }
        return channelIntervalInfo;
    }

    private void addCalculatedValueInfo(Channel channel, ChannelDataInfo channelIntervalInfo, IntervalReadingRecord reading) {
        channelIntervalInfo.isBulk = channel.getReadingType().isCumulative();
        channel.getCalculatedReadingType(reading.getTimeStamp()).ifPresent(readingType -> {
            channelIntervalInfo.collectedValue = channelIntervalInfo.value;
            Quantity quantity = reading.getQuantity(readingType);
            channelIntervalInfo.value = getRoundedBigDecimal(quantity != null? quantity.getValue(): null, channel);
        });
    }

    private static BigDecimal getRoundedBigDecimal(BigDecimal value, Channel channel) {
        return value != null ? value.setScale(channel.getChannelSpec().getNbrOfFractionDigits(), BigDecimal.ROUND_UP) : value;
    }

    public LoadProfileDataInfo createLoadProfileDataInfo(LoadProfileReading loadProfileReading, DeviceValidation deviceValidation, List<Channel> channels, Boolean validationStatus) {
        LoadProfileDataInfo channelIntervalInfo = new LoadProfileDataInfo();
        channelIntervalInfo.interval = IntervalInfo.from(loadProfileReading.getRange());
        channelIntervalInfo.readingTime = loadProfileReading.getReadingTime();
        channelIntervalInfo.intervalFlags = loadProfileReading
                .getFlags()
                .stream()
                .map(flag -> thesaurus.getString(flag.name(), flag.name()))
                .collect(Collectors.toList());
        if (loadProfileReading.getChannelValues().isEmpty()) {
            for (Channel channel : channels) {
                channelIntervalInfo.channelData.put(channel.getId(), null);
            }
            channelIntervalInfo.channelCollectedData = channelIntervalInfo.channelData;
        } else {
            for (Map.Entry<Channel, IntervalReadingRecord> entry : loadProfileReading.getChannelValues().entrySet()) {
                Channel channel = entry.getKey();
                BigDecimal value = getRoundedBigDecimal(entry.getValue().getValue(), channel);
                String collectedValue = value != null ? value.toString() : "";
                if (channel.getCalculatedReadingType(entry.getValue().getTimeStamp()).isPresent()) {
                    channelIntervalInfo.channelCollectedData.put(channel.getId(), collectedValue);
                    Quantity quantity = entry.getValue().getQuantity(channel.getCalculatedReadingType(entry.getValue().getTimeStamp()).get());
                    String calculatedValue = quantity != null ? getRoundedBigDecimal(quantity.getValue(), channel).toString() : "";
                    channelIntervalInfo.channelData.put(channel.getId(), calculatedValue);
                } else {
                    channelIntervalInfo.channelData.put(channel.getId(), collectedValue);
                }
            }
        }

        for (Map.Entry<Channel, DataValidationStatus> entry : loadProfileReading.getChannelValidationStates().entrySet()) {
            channelIntervalInfo.channelValidationData.put(entry.getKey().getId(), validationInfoFactory.createMinimalVeeReadingInfo(entry.getKey(), entry.getValue(), deviceValidation));
        }

        for (Channel channel : channels) {
            if (channelIntervalInfo.channelData.containsKey(channel.getId()) && channelIntervalInfo.channelData.get(channel.getId()) == null
                    && !channelIntervalInfo.channelValidationData.containsKey(channel.getId())) {
                // This means it is a missing value what hasn't been validated( = detected ) yet
                MinimalVeeReadingInfo notValidatedMissing = new MinimalVeeReadingInfo();
                notValidatedMissing.dataValidated = false;
                notValidatedMissing.validationStatus = validationStatus;
                notValidatedMissing.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
                if (channel.getReadingType().isCumulative()) {
                    notValidatedMissing.bulkValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
                }
                channelIntervalInfo.channelValidationData.put(channel.getId(), notValidatedMissing);
            }
        }

        return channelIntervalInfo;
    }

    public List<ReadingInfo> asReadingsInfoList(List<? extends Reading> readings, Register<?, ?> register, boolean isValidationStatusActive, Device device) {
        return readings
                .stream()
                .map(r -> createReadingInfo(r, register, isValidationStatusActive, device))
                .collect(Collectors.toList());
    }

    public ReadingInfo createReadingInfo(Reading reading, Register<?, ?> register, boolean isValidationStatusActive, Device device) {
        if (reading instanceof BillingReading) {
            return createBillingReadingInfo((BillingReading) reading, register, device, isValidationStatusActive);
        } else if (reading instanceof NumericalReading) {
            return createNumericalReadingInfo((NumericalReading) reading, register, device, isValidationStatusActive);
        } else if (reading instanceof TextReading) {
            return createTextReadingInfo((TextReading) reading);
        } else if (reading instanceof FlagsReading) {
            return createFlagsReadingInfo((FlagsReading) reading);

        }
        throw new IllegalArgumentException("Unsupported reading type: " + reading.getClass().getSimpleName());
    }

    private void setCommonReadingInfo(Reading reading, ReadingInfo readingInfo) {
        readingInfo.id = reading.getTimeStamp();
        readingInfo.timeStamp = reading.getTimeStamp();
        readingInfo.reportedDateTime = reading.getReportedDateTime();
        readingInfo.modificationFlag = ReadingModificationFlag.getModificationFlag(reading.getActualReading());
    }

    private BillingReadingInfo createBillingReadingInfo(BillingReading reading, Register<?, ?> register, Device device, boolean isValidationStatusActive) {
        BillingReadingInfo billingReadingInfo = new BillingReadingInfo();
        setCommonReadingInfo(reading, billingReadingInfo);
        setMultiplierIfApplicable(billingReadingInfo, device);
        if (reading.getQuantity() != null) {
            billingReadingInfo.value = reading.getQuantity().getValue();
            setCalculatedValueIfApplicable(reading, register, billingReadingInfo, 0);
        }
        if (reading.getRange().isPresent()) {
            billingReadingInfo.interval = IntervalInfo.from(reading.getRange().get());
        }
        addValidationInfo(reading, billingReadingInfo, isValidationStatusActive);
        return billingReadingInfo;
    }

    private NumericalReadingInfo createNumericalReadingInfo(NumericalReading reading, Register<?, ?> register, Device device, boolean isValidationStatusActive) {
        NumericalReadingInfo numericalReadingInfo = new NumericalReadingInfo();
        setCommonReadingInfo(reading, numericalReadingInfo);
        setMultiplierIfApplicable(numericalReadingInfo, device);

        Quantity collectedValue = reading.getQuantityFor(register.getReadingType());
        if(collectedValue != null){
            int numberOfFractionDigits = ((NumericalRegisterSpec) register.getRegisterSpec()).getNumberOfFractionDigits();
            numericalReadingInfo.value = collectedValue.getValue().setScale(numberOfFractionDigits, BigDecimal.ROUND_UP);
            numericalReadingInfo.rawValue = numericalReadingInfo.value;
            setCalculatedValueIfApplicable(reading, register, numericalReadingInfo, numberOfFractionDigits);
        }
        addValidationInfo(reading, numericalReadingInfo, isValidationStatusActive);
        return numericalReadingInfo;
    }

    private void setCalculatedValueIfApplicable(NumericalReading reading, Register<?, ?> register, NumericalReadingInfo numericalReadingInfo, int numberOfFractionDigits) {
        if(register.getCalculatedReadingType(reading.getTimeStamp()).isPresent() ){
            Quantity calculatedQuantity = reading.getQuantityFor(register.getCalculatedReadingType(reading.getTimeStamp()).get());
            if(calculatedQuantity != null){
                numericalReadingInfo.calculatedValue = calculatedQuantity.getValue().setScale(numberOfFractionDigits, BigDecimal.ROUND_UP);
            }
        }
    }

    private void setMultiplierIfApplicable(NumericalReadingInfo readingInfo, Device device) {
        if(!device.getMultiplier().equals(BigDecimal.ONE)){
            readingInfo.multiplier = device.getMultiplier();
        }
    }

    private void addValidationInfo(Reading reading, NumericalReadingInfo readingInfo, boolean isValidationStatusActive) {
        readingInfo.validationStatus = isValidationStatusActive;
        reading.getValidationStatus().ifPresent(status -> {
            readingInfo.dataValidated = status.completelyValidated();
            readingInfo.validationResult = ValidationStatus.forResult(ValidationResult.getValidationResult(status.getReadingQualities()));
            readingInfo.suspectReason = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
            readingInfo.estimatedByRule = estimationRuleInfoFactory.createEstimationRuleInfo(status.getReadingQualities());
            readingInfo.isConfirmed = validationInfoFactory.isConfirmedData(reading.getActualReading(), status.getReadingQualities());
        });
    }

    private TextReadingInfo createTextReadingInfo(TextReading reading) {
        TextReadingInfo textReadingInfo = new TextReadingInfo();
        setCommonReadingInfo(reading, textReadingInfo);
        textReadingInfo.value = reading.getValue();

        return textReadingInfo;
    }

    private FlagsReadingInfo createFlagsReadingInfo(FlagsReading reading) {
        FlagsReadingInfo flagsReadingInfo = new FlagsReadingInfo();
        setCommonReadingInfo(reading, flagsReadingInfo);
        flagsReadingInfo.value = reading.getFlags();
        return flagsReadingInfo;
    }

    public RegisterInfo createRegisterInfo(Register register, DetailedValidationInfo registerValidationInfo) {
        if (register instanceof BillingRegister) {
            BillingRegisterInfo info = createBillingRegisterInfo((BillingRegister) register);
            info.detailedValidationInfo = registerValidationInfo;
            return info;
        } else if (register instanceof NumericalRegister) {
            NumericalRegisterInfo info = createNumericalRegisterInfo((NumericalRegister) register);
            info.detailedValidationInfo = registerValidationInfo;
            return info;
        } else if (register instanceof TextRegister) {
            return createTextRegisterInfo((TextRegister) register);
        } else if (register instanceof FlagsRegister) {
            return createFlagsRegisterInfo((FlagsRegister) register);
        }

        throw new IllegalArgumentException("Unsupported register type: " + register.getClass().getSimpleName());
    }

    private void addCommonRegisterInfo(Register register, RegisterInfo registerInfo) {
        RegisterSpec registerSpec = register.getRegisterSpec();
        Device device = register.getDevice();
        registerInfo.id = registerSpec.getId();
        registerInfo.registerType = registerSpec.getRegisterType().getId();
        registerInfo.readingType = new ReadingTypeInfo(register.getReadingType());
        registerInfo.obisCode = registerSpec.getObisCode();
        registerInfo.overruledObisCode = registerSpec.getDeviceObisCode();
        registerInfo.obisCodeDescription = registerSpec.getObisCode().getDescription();
        registerInfo.isCumulative = register.getReadingType().isCumulative();
        registerInfo.mRID = device.getmRID();
        registerInfo.version = device.getVersion();
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        registerInfo.parent = new VersionInfo(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        Optional<? extends Reading> lastReading = register.getLastReading();
        lastReading.ifPresent(reading -> registerInfo.lastReading = createReadingInfo(reading, register, false, device));
    }

    public BillingRegisterInfo createBillingRegisterInfo(BillingRegister register) {
        BillingRegisterInfo billingRegisterInfo = new BillingRegisterInfo();
        addCommonRegisterInfo(register, billingRegisterInfo);
        register.getCalculatedReadingType(register.getLastReadingDate().orElse(clock.instant())).ifPresent(calculatedReadingType -> billingRegisterInfo.calculatedReadingType = new ReadingTypeInfo(calculatedReadingType));
        if (register.getRegisterSpec().isUseMultiplier() &&  !register.getDevice().getMultiplier().equals(BigDecimal.ONE)) {
            billingRegisterInfo.multiplier = register.getDevice().getMultiplier();
        }
        return billingRegisterInfo;
    }

    public FlagsRegisterInfo createFlagsRegisterInfo(FlagsRegister flagsRegister) {
        FlagsRegisterInfo flagsRegisterInfo = new FlagsRegisterInfo();
        addCommonRegisterInfo(flagsRegister, flagsRegisterInfo);
        return flagsRegisterInfo;
    }

    private TextRegisterInfo createTextRegisterInfo(TextRegister textRegister){
        TextRegisterInfo textRegisterInfo = new TextRegisterInfo();
        addCommonRegisterInfo(textRegister, textRegisterInfo);
        return textRegisterInfo;
    }

    public NumericalRegisterInfo createNumericalRegisterInfo(NumericalRegister numericalRegister) {
        NumericalRegisterInfo numericalRegisterInfo = new NumericalRegisterInfo();
        addCommonRegisterInfo(numericalRegister, numericalRegisterInfo);
        NumericalRegisterSpec registerSpec = numericalRegister.getRegisterSpec();
        numericalRegisterInfo.numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
        numericalRegisterInfo.overflow = registerSpec.getOverflowValue();
        numericalRegister.getCalculatedReadingType(numericalRegister.getLastReadingDate().orElse(clock.instant())).ifPresent(calculatedReadingType -> numericalRegisterInfo.calculatedReadingType = new ReadingTypeInfo(calculatedReadingType));
        numericalRegisterInfo.multiplier = numericalRegister.getMultiplier().orElseGet(() -> null);
        return numericalRegisterInfo;
    }
}
