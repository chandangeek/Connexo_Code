package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.BillingRegister;
import com.energyict.mdc.device.data.Channel;
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
    private final Clock clock;
    private final ValidationRuleInfoFactory validationRuleInfoFactory;

    @Inject
    public DeviceDataInfoFactory(ValidationInfoFactory validationInfoFactory, EstimationRuleInfoFactory estimationRuleInfoFactory, Thesaurus thesaurus, Clock clock, ValidationRuleInfoFactory validationRuleInfoFactory) {
        this.validationInfoFactory = validationInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.validationRuleInfoFactory = validationRuleInfoFactory;
    }

    public ChannelDataInfo createChannelDataInfo(Channel channel, LoadProfileReading loadProfileReading, boolean isValidationActive, DeviceValidation deviceValidation) {
        ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();
        channelIntervalInfo.interval = IntervalInfo.from(loadProfileReading.getRange());
        channelIntervalInfo.readingTime = loadProfileReading.getReadingTime();
        channelIntervalInfo.intervalFlags = new ArrayList<>();
        channelIntervalInfo.validationStatus = isValidationActive;
        channelIntervalInfo.intervalFlags.addAll(loadProfileReading.getFlags().stream().map(flag -> thesaurus.getString(flag.name(), flag.name())).collect(Collectors.toList()));
        Optional<IntervalReadingRecord> channelReading = loadProfileReading.getChannelValues().entrySet().stream().map(Map.Entry::getValue).findFirst();// There can be only one channel (or no channel at all if the channel has no dta for this interval)

        channelReading.ifPresent(reading -> {
            channelIntervalInfo.value = getRoundedBigDecimal(reading.getValue(), channel);
            channel.getReadingType().getCalculatedReadingType().ifPresent(calculatedReadingType -> {
                channelIntervalInfo.isBulk = true;
                channelIntervalInfo.collectedValue = channelIntervalInfo.value;
                Quantity quantity = reading.getQuantity(calculatedReadingType);
                channelIntervalInfo.value = getRoundedBigDecimal(quantity != null ? quantity.getValue() : null, channel);
            });
            channelIntervalInfo.reportedDateTime = reading.getReportedDateTime();
        });
        if (!channelReading.isPresent() && loadProfileReading.getReadingTime() != null) {
            channelIntervalInfo.reportedDateTime = loadProfileReading.getReadingTime();
        }

        Optional<DataValidationStatus> dataValidationStatus = loadProfileReading.getChannelValidationStates().entrySet().stream().map(Map.Entry::getValue).findFirst();
        dataValidationStatus.ifPresent(status -> {
            channelIntervalInfo.validationInfo = validationInfoFactory.createVeeReadingInfoWithModificationFlags(channel, status, deviceValidation, channelReading);
        });
        if (!channelReading.isPresent() && !dataValidationStatus.isPresent()) {
            // we have a reading with no data and no validation result => it's a placeholder (missing value) which hasn't validated ( = detected ) yet
            channelIntervalInfo.validationInfo = new VeeReadingInfo();
            channelIntervalInfo.validationInfo.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
            if(channelIntervalInfo.isBulk) {
                channelIntervalInfo.validationInfo.bulkValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
            }
            channelIntervalInfo.validationInfo.dataValidated = false;
        }
        return channelIntervalInfo;
    }

    private static BigDecimal getRoundedBigDecimal(BigDecimal value, Channel channel) {
        return value != null ? value.setScale(channel.getChannelSpec().getNbrOfFractionDigits(), BigDecimal.ROUND_UP) : value;
    }

    public LoadProfileDataInfo createLoadProfileDataInfo(LoadProfileReading loadProfileReading, DeviceValidation deviceValidation, List<Channel> channels) {
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
                if (channel.getReadingType().getCalculatedReadingType().isPresent()) {
                    channelIntervalInfo.channelCollectedData.put(channel.getId(), collectedValue);
                    Quantity quantity = entry.getValue().getQuantity(channel.getReadingType().getCalculatedReadingType().get());
                    String calculatedValue = quantity != null ? getRoundedBigDecimal(quantity.getValue(), channel).toString() : "";
                    channelIntervalInfo.channelData.put(channel.getId(), calculatedValue);
                } else {
                    channelIntervalInfo.channelData.put(channel.getId(), collectedValue);
                }
            }
        }

        for (Map.Entry<Channel, DataValidationStatus> entry : loadProfileReading.getChannelValidationStates().entrySet()) {
            channelIntervalInfo.channelValidationData.put(entry.getKey().getId(), validationInfoFactory.createVeeReadingInfo(entry.getKey(), entry.getValue(), deviceValidation));
        }

        for (Channel channel : channels) {
            if (channelIntervalInfo.channelData.containsKey(channel.getId()) && channelIntervalInfo.channelData.get(channel.getId()) == null
                    && !channelIntervalInfo.channelValidationData.containsKey(channel.getId())) {
                // This means it is a missing value what hasn't been validated( = detected ) yet
                VeeReadingInfo notValidatedMissing = new VeeReadingInfo();
                notValidatedMissing.dataValidated = false;
                notValidatedMissing.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
                if(channel.getReadingType().isCumulative()) {
                    notValidatedMissing.bulkValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
                }
                channelIntervalInfo.channelValidationData.put(channel.getId(), notValidatedMissing);
            }
        }

        for (Channel channel : loadProfileReading.getChannelValues().keySet()) {
            channelIntervalInfo.validationActive |= deviceValidation.isValidationActive(channel, clock.instant());
        }

        return channelIntervalInfo;
    }

    public List<ReadingInfo> asReadingsInfoList(List<? extends Reading> readings, RegisterSpec registerSpec, boolean isValidationStatusActive) {
        return readings
                .stream()
                .map(r -> createReadingInfo(r, registerSpec, isValidationStatusActive))
                .collect(Collectors.toList());
    }

    public ReadingInfo createReadingInfo(Reading reading, RegisterSpec registerSpec, boolean isValidationStatusActive) {
        if (reading instanceof BillingReading) {
            return createBillingReadingInfo((BillingReading) reading, (NumericalRegisterSpec) registerSpec, isValidationStatusActive);
        } else if (reading instanceof NumericalReading) {
            return createNumericalReadingInfo((NumericalReading) reading, (NumericalRegisterSpec) registerSpec, isValidationStatusActive);
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

    public BillingReadingInfo createBillingReadingInfo(BillingReading reading, NumericalRegisterSpec registerSpec, boolean isValidationStatusActive) {
        BillingReadingInfo billingReadingInfo = new BillingReadingInfo();
        setCommonReadingInfo(reading, billingReadingInfo);
        if (reading.getQuantity() != null) {
            billingReadingInfo.value = reading.getQuantity().getValue();
        }
        billingReadingInfo.unitOfMeasure = registerSpec.getUnit();
        if (reading.getRange().isPresent()) {
            billingReadingInfo.interval = IntervalInfo.from(reading.getRange().get());
        }
        billingReadingInfo.validationStatus = isValidationStatusActive;
        reading.getValidationStatus().ifPresent(status -> {
            billingReadingInfo.dataValidated = status.completelyValidated();
            billingReadingInfo.validationResult = ValidationStatus.forResult(ValidationResult.getValidationResult(status.getReadingQualities()));
            billingReadingInfo.suspectReason = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
            billingReadingInfo.estimatedByRule = estimationRuleInfoFactory.createEstimationRuleInfo(status.getReadingQualities());
        });
        return billingReadingInfo;
    }

    public NumericalReadingInfo createNumericalReadingInfo(NumericalReading reading, NumericalRegisterSpec registerSpec, boolean isValidationStatusActive) {
        NumericalReadingInfo numericalReadingInfo = new NumericalReadingInfo();
        setCommonReadingInfo(reading, numericalReadingInfo);
        if (reading.getQuantity() != null) {
            numericalReadingInfo.value = reading.getQuantity().getValue();
            numericalReadingInfo.rawValue = reading.getQuantity().getValue();
        }
        if (numericalReadingInfo.value != null) {
            int numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
            numericalReadingInfo.value = numericalReadingInfo.value.setScale(numberOfFractionDigits, BigDecimal.ROUND_UP);
            numericalReadingInfo.rawValue = numericalReadingInfo.value;
        }
        numericalReadingInfo.unitOfMeasure = registerSpec.getUnit();

        numericalReadingInfo.validationStatus = isValidationStatusActive;
        reading.getValidationStatus().ifPresent(status -> {
            numericalReadingInfo.dataValidated = status.completelyValidated();
            numericalReadingInfo.validationResult = ValidationStatus.forResult(ValidationResult.getValidationResult(status.getReadingQualities()));
            numericalReadingInfo.suspectReason = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
            numericalReadingInfo.estimatedByRule = estimationRuleInfoFactory.createEstimationRuleInfo(status.getReadingQualities());
        });
        return numericalReadingInfo;
    }

    public TextReadingInfo createTextReadingInfo(TextReading reading) {
        TextReadingInfo textReadingInfo = new TextReadingInfo();
        setCommonReadingInfo(reading, textReadingInfo);
        textReadingInfo.value = reading.getValue();

        return textReadingInfo;
    }

    public FlagsReadingInfo createFlagsReadingInfo(FlagsReading reading) {
        FlagsReadingInfo flagsReadingInfo = new FlagsReadingInfo();
        setCommonReadingInfo(reading, flagsReadingInfo);
        flagsReadingInfo.value = reading.getFlags();
        return flagsReadingInfo;
    }

    public RegisterInfo createRegisterInfo(Register register, DetailedValidationInfo registerValidationInfo){
        if (register instanceof BillingRegister) {
            return createBillingRegisterInfo((BillingRegister) register, registerValidationInfo);
        } else if (register instanceof NumericalRegister) {
            return createNumericalRegisterInfo((NumericalRegister) register, registerValidationInfo);
        } else if (register instanceof TextRegister) {
            return createTextRegisterInfo((TextRegister)register);
        } else if (register instanceof FlagsRegister) {
            return createFlagsRegisterInfo((FlagsRegister)register);
        }

        throw new IllegalArgumentException("Unsupported register type: " + register.getClass().getSimpleName());
    }

    private void addCommonRegisterInfo(Register register, RegisterInfo registerInfo) {
        RegisterSpec registerSpec = register.getRegisterSpec();
        registerInfo.id = registerSpec.getId();
        registerInfo.registerType = registerSpec.getRegisterType().getId();
        registerInfo.readingType = new ReadingTypeInfo(registerSpec.getRegisterType().getReadingType());
        registerInfo.timeOfUse = registerSpec.getRegisterType().getTimeOfUse();
        registerInfo.obisCode = registerSpec.getObisCode();
        registerInfo.overruledObisCode = registerSpec.getDeviceObisCode();
        registerInfo.obisCodeDescription = registerSpec.getObisCode().getDescription();
        registerInfo.unitOfMeasure = registerSpec.getUnit();
        registerInfo.isCumulative = registerSpec.getReadingType().isCumulative();
        Optional<? extends Reading> lastReading = register.getLastReading();
        lastReading.ifPresent(reading -> registerInfo.lastReading = createReadingInfo(reading, registerSpec, false));
    }

    public BillingRegisterInfo createBillingRegisterInfo(BillingRegister register, DetailedValidationInfo registerValidationInfo) {
        BillingRegisterInfo billingRegisterInfo = new BillingRegisterInfo();
        addCommonRegisterInfo(register, billingRegisterInfo);
        billingRegisterInfo.detailedValidationInfo = registerValidationInfo;
        return billingRegisterInfo;
    }

    public FlagsRegisterInfo createFlagsRegisterInfo(FlagsRegister flagsRegister){
        FlagsRegisterInfo flagsRegisterInfo = new FlagsRegisterInfo();
        addCommonRegisterInfo(flagsRegister, flagsRegisterInfo);
        return flagsRegisterInfo;
    }

    public TextRegisterInfo createTextRegisterInfo(TextRegister textRegister){
        TextRegisterInfo textRegisterInfo = new TextRegisterInfo();
        addCommonRegisterInfo(textRegister, textRegisterInfo);
        return textRegisterInfo;
    }

    public NumericalRegisterInfo createNumericalRegisterInfo(NumericalRegister numericalRegister, DetailedValidationInfo registerValidationInfo){
        NumericalRegisterInfo numericalRegisterInfo = new NumericalRegisterInfo();
        addCommonRegisterInfo(numericalRegister, numericalRegisterInfo);
        NumericalRegisterSpec registerSpec = (NumericalRegisterSpec)numericalRegister.getRegisterSpec();
        numericalRegisterInfo.numberOfDigits = registerSpec.getNumberOfDigits();
        numericalRegisterInfo.numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
        numericalRegisterInfo.overflow = registerSpec.getOverflowValue();
        numericalRegisterInfo.detailedValidationInfo = registerValidationInfo;
        return numericalRegisterInfo;
    }
}
