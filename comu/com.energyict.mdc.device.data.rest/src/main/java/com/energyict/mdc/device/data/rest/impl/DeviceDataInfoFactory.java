package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.topology.TopologyService;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Groups functionality to create info objects for different sorts of data of a device
 */
public class DeviceDataInfoFactory {

    private final MeteringTranslationService meteringTranslationService;
    private final ValidationInfoFactory validationInfoFactory;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;
    private final ValidationRuleInfoFactory validationRuleInfoFactory;
    private final Clock clock;
    private final ResourceHelper resourceHelper;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public DeviceDataInfoFactory(MeteringTranslationService meteringTranslationService,
                                 ValidationInfoFactory validationInfoFactory,
                                 EstimationRuleInfoFactory estimationRuleInfoFactory,
                                 ValidationRuleInfoFactory validationRuleInfoFactory,
                                 Clock clock,
                                 ResourceHelper resourceHelper,
                                 ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.meteringTranslationService = meteringTranslationService;
        this.validationInfoFactory = validationInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
        this.validationRuleInfoFactory = validationRuleInfoFactory;
        this.clock = clock;
        this.resourceHelper = resourceHelper;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    ChannelDataInfo createChannelDataInfo(Channel channel, LoadProfileReading loadProfileReading, boolean isValidationActive, DeviceValidation deviceValidation, Device dataLoggerSlave) {
        ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();
        channelIntervalInfo.interval = IntervalInfo.from(loadProfileReading.getRange());
        channelIntervalInfo.readingTime = loadProfileReading.getReadingTime();
        channelIntervalInfo.validationActive = isValidationActive;

        Map<Channel, List<? extends ReadingQualityRecord>> readingQualities = loadProfileReading.getReadingQualities();
        List<? extends ReadingQualityRecord> readingQualityRecords = readingQualities.get(channel);
        if (readingQualityRecords == null) {
            readingQualityRecords = new ArrayList<>();
        }

        channelIntervalInfo.readingQualities = readingQualityRecords.stream()
                .filter(ReadingQualityRecord::isActual)
                .distinct()
                .filter(record -> record.getType().system().isPresent())
                .filter(record -> record.getType().category().isPresent())
                .filter(record -> record.getType().qualityIndex().isPresent())
                .filter(record -> (record.getType().getSystemCode() == QualityCodeSystem.ENDDEVICE.ordinal()))
                .map(rq -> getSimpleName(rq.getType()))
                .collect(Collectors.toList());


        Optional<IntervalReadingRecord> channelReading = loadProfileReading.getChannelValues()
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .findFirst();// There can be only one channel (or no channel at all if the channel has no dta for this interval)
        channelReading.ifPresent(reading -> {
            channelIntervalInfo.multiplier = channel.getMultiplier(reading.getTimeStamp()).orElseGet(() -> null);
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
        if (dataLoggerSlave != null) {
            channelIntervalInfo.slaveChannel = SlaveChannelInfo.from(dataLoggerSlave, channel);
        }
        return channelIntervalInfo;
    }

    /**
     * Find translation of the index of the given reading quality CIM code.
     */
    private String getSimpleName(ReadingQualityType type) {
        return this.meteringTranslationService.getDisplayName(type.qualityIndex().get());
    }

    private void addCalculatedValueInfo(Channel channel, ChannelDataInfo channelIntervalInfo, IntervalReadingRecord reading) {
        channelIntervalInfo.isBulk = channel.getReadingType().isCumulative();
        channel.getCalculatedReadingType(reading.getTimeStamp()).ifPresent(readingType -> {
            channelIntervalInfo.collectedValue = channelIntervalInfo.value;
            Quantity quantity = reading.getQuantity(readingType);
            channelIntervalInfo.value = getRoundedBigDecimal(quantity != null ? quantity.getValue() : null, channel);
        });
    }

    private static BigDecimal getRoundedBigDecimal(BigDecimal value, Channel channel) {
        return value != null ? value.setScale(channel.getNrOfFractionDigits(), BigDecimal.ROUND_UP) : value;
    }

    LoadProfileDataInfo createLoadProfileDataInfo(LoadProfileReading loadProfileReading, DeviceValidation deviceValidation, List<Channel> channels, Boolean validationStatus) {
        LoadProfileDataInfo channelIntervalInfo = new LoadProfileDataInfo();
        channelIntervalInfo.interval = IntervalInfo.from(loadProfileReading.getRange());
        channelIntervalInfo.readingTime = loadProfileReading.getReadingTime();

        Map<Long, List<String>> readingQualitiesDescriptionPerChannel = new HashMap<>();
        for (Channel channel : loadProfileReading.getReadingQualities().keySet()) {
            List<? extends ReadingQualityRecord> readingQualityRecords = loadProfileReading.getReadingQualities().get(channel);
            if (readingQualityRecords == null) {
                readingQualityRecords = new ArrayList<>();
            }

            List<String> readingQualitiesDescription = readingQualityRecords
                    .stream()
                    .filter(ReadingQualityRecord::isActual)
                    .distinct()
                    .filter(record -> record.getType().system().isPresent())
                    .filter(record -> record.getType().category().isPresent())
                    .filter(record -> record.getType().qualityIndex().isPresent())
                    .filter(record -> (record.getType().getSystemCode() == QualityCodeSystem.ENDDEVICE.ordinal()))
                    .map(rq -> getSimpleName(rq.getType()))
                    .collect(Collectors.toList());

            readingQualitiesDescriptionPerChannel.put(channel.getId(), readingQualitiesDescription);
        }
        channelIntervalInfo.readingQualities = readingQualitiesDescriptionPerChannel;

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
            channelIntervalInfo.channelValidationData.put(entry.getKey()
                    .getId(), validationInfoFactory.createMinimalVeeReadingInfo(entry.getKey(), entry.getValue(), deviceValidation));
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

    List<ReadingInfo> asReadingsInfoList(List<? extends Reading> readings, Register<?, ?> register, boolean isValidationStatusActive, Device dataLoggerSlave) {
        return readings
                .stream()
                .map(r -> createReadingInfo(r, register, isValidationStatusActive, dataLoggerSlave))
                .collect(Collectors.toList());
    }

    ReadingInfo createReadingInfo(Reading reading, Register<?, ?> register, boolean isValidationStatusActive, Device dataLoggerSlave) {
        if (reading instanceof NumericalReading) {
            return createNumericalReadingInfo((NumericalReading) reading, register, isValidationStatusActive, dataLoggerSlave);
        } else if (reading instanceof TextReading) {
            return createTextReadingInfo((TextReading) reading, register);
        }
        throw new IllegalArgumentException("Unsupported reading type: " + reading.getClass().getSimpleName());
    }

    private void setCommonReadingInfo(Reading reading, ReadingInfo readingInfo, Register<?, ?> register) {
        readingInfo.id = "" + reading.getTimeStamp().toEpochMilli() + register.getRegisterSpecId();
        readingInfo.timeStamp = reading.getTimeStamp();
        readingInfo.reportedDateTime = reading.getReportedDateTime();
        readingInfo.readingQualities = createReadingQualitiesInfo(reading);
        Pair<ReadingModificationFlag, QualityCodeSystem> modificationFlag = ReadingModificationFlag.getModificationFlag(reading.getActualReading());
        if (modificationFlag != null) {
            readingInfo.modificationFlag = modificationFlag.getFirst();
            readingInfo.editedInApp = resourceHelper.getApplicationInfo(modificationFlag.getLast());
        }
    }

    /**
     * Returns the CIM code and full translation of all reading qualities on the given interval reading
     */
    private List<ReadingQualityInfo> createReadingQualitiesInfo(Reading reading) {
        return reading.getActualReading().getReadingQualities().stream()
                .filter(ReadingQualityRecord::isActual)
                .map(ReadingQuality::getType)
                .distinct()
                .filter(type -> type.system().isPresent())
                .filter(type -> type.category().isPresent())
                .filter(type -> type.qualityIndex().isPresent())
                .map(type -> ReadingQualityInfo.fromReadingQualityType(meteringTranslationService, type))
                .collect(Collectors.toList());
    }

    private NumericalReadingInfo createNumericalReadingInfo(NumericalReading reading, Register<?, ?> register, boolean isValidationStatusActive, Device dataLoggerSlave) {
        NumericalReadingInfo numericalReadingInfo = new NumericalReadingInfo();
        int numberOfFractionDigits = ((NumericalRegister) register).getNumberOfFractionDigits();
        setCommonReadingInfo(reading, numericalReadingInfo, register);
        setMultiplier(register, numericalReadingInfo, reading);
        setInterval(reading, numericalReadingInfo);
        setCollectedValue(reading, register, numericalReadingInfo, numberOfFractionDigits);
        setDeltaValue(reading, numericalReadingInfo);
        setCalculatedValueIfApplicable(reading, register, numericalReadingInfo, numberOfFractionDigits);
        addValidationInfo(reading, numericalReadingInfo, isValidationStatusActive);
        setSlaveInformation(register, dataLoggerSlave, numericalReadingInfo);
        setEventDate(reading, numericalReadingInfo);
        return numericalReadingInfo;
    }

    private void setDeltaValue(NumericalReading reading, NumericalReadingInfo numericalReadingInfo) {
        reading.getDelta().ifPresent(deltaValue -> numericalReadingInfo.deltaValue=deltaValue);
    }

    private void setInterval(NumericalReading reading, NumericalReadingInfo numericalReadingInfo) {
        reading.getRange().ifPresent(instantRange -> numericalReadingInfo.interval = IntervalInfo.from(instantRange));
    }

    private void setSlaveInformation(Register<?, ?> register, Device dataLoggerSlave, NumericalReadingInfo numericalReadingInfo) {
        if (dataLoggerSlave != null) {
            numericalReadingInfo.slaveRegister = SlaveRegisterInfo.from(dataLoggerSlave, register);
        }
    }

    private void setEventDate(NumericalReading reading, NumericalReadingInfo numericalReadingInfo) {
        reading.getEventDate().ifPresent(eventDate -> numericalReadingInfo.eventDate = eventDate);
    }

    private void setCollectedValue(NumericalReading reading, Register<?, ?> register, NumericalReadingInfo numericalReadingInfo, int numberOfFractionDigits) {
        reading.getCollectedValue().ifPresent(collectedValue -> {
            numericalReadingInfo.value = collectedValue.getValue().setScale(numberOfFractionDigits, BigDecimal.ROUND_UP);
            numericalReadingInfo.unit = register.getRegisterSpec().getRegisterType().getUnit();
            numericalReadingInfo.rawValue = numericalReadingInfo.value;
        });
    }

    private void setCalculatedValueIfApplicable(NumericalReading reading, Register<?, ?> register, NumericalReadingInfo numericalReadingInfo, int numberOfFractionDigits) {
        reading.getCalculatedValue().ifPresent(calculatedValue -> {
            numericalReadingInfo.calculatedValue = calculatedValue.getValue().setScale(numberOfFractionDigits, BigDecimal.ROUND_UP);
            numericalReadingInfo.calculatedUnit = register.getRegisterSpec().getRegisterType().getUnit();
        });
    }

    private void setMultiplier(Register<?, ?> register, NumericalReadingInfo numericalReadingInfo, Reading reading) {
        if (reading.getTimeStamp() != null) {
            numericalReadingInfo.multiplier = register.getMultiplier(reading.getTimeStamp()).orElseGet(() -> null);
        }
    }

    private void addValidationInfo(Reading reading, NumericalReadingInfo readingInfo, boolean isValidationStatusActive) {
        readingInfo.validationStatus = isValidationStatusActive;
        reading.getValidationStatus().ifPresent(status -> {
            readingInfo.dataValidated = status.completelyValidated();
            Collection<? extends ReadingQuality> readingQualities = status.getReadingQualities();
            readingInfo.validationResult = ValidationStatus.forResult(ValidationResult.getValidationResult(readingQualities));
            readingInfo.suspectReason = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
            readingInfo.estimatedByRule = estimationRuleInfoFactory.createEstimationRuleInfo(readingQualities);
            List<? extends ReadingQuality> confirmedQualities = validationInfoFactory.getConfirmedQualities(reading.getActualReading(), readingQualities);
            readingInfo.isConfirmed = !confirmedQualities.isEmpty();
            readingInfo.confirmedInApps = confirmedQualities.stream()
                    .map(ReadingQuality::getType)
                    .map(ReadingQualityType::system)
                    .flatMap(Functions.asStream())
                    .map(resourceHelper::getApplicationInfo)
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), s -> s.isEmpty() ? null : s));
        });
    }

    private TextReadingInfo createTextReadingInfo(TextReading reading, Register<?, ?> register) {
        TextReadingInfo textReadingInfo = new TextReadingInfo();
        setCommonReadingInfo(reading, textReadingInfo, register);
        textReadingInfo.value = reading.getValue();
        textReadingInfo.interval = IntervalInfo.from(Range.atMost(reading.getTimeStamp()));
        return textReadingInfo;
    }

    RegisterInfo createRegisterInfo(Register register, DetailedValidationInfo registerValidationInfo, TopologyService topologyService) {
        if (register instanceof NumericalRegister) {
            RegisterInfo info = createRegisterInfo((NumericalRegister) register, topologyService);
            info.detailedValidationInfo = registerValidationInfo;
            return info;
        } else if (register instanceof TextRegister) {
            return createTextRegisterInfo((TextRegister) register, topologyService);
        }
        throw new IllegalArgumentException("Unsupported register type: " + register.getClass().getSimpleName());
    }

    private void addCommonRegisterInfo(Register<?, ?> register, RegisterInfo registerInfo, TopologyService topologyService) {
        RegisterSpec registerSpec = register.getRegisterSpec();
        Device device = register.getDevice();
        registerInfo.id = registerSpec.getId();
        registerInfo.registerType = registerSpec.getRegisterType().getId();
        registerInfo.readingType = readingTypeInfoFactory.from(register.getReadingType());
        registerInfo.obisCode = registerSpec.getDeviceObisCode();
        registerInfo.overruledObisCode = register.getDeviceObisCode();
        registerInfo.obisCodeDescription = register.getDeviceObisCode().getDescription();
        registerInfo.isCumulative = register.getReadingType().isCumulative();
        registerInfo.hasEvent = register.hasEventDate();
        //  register.getLastReading().isPresent(reading -> registerInfo.hasEvent = reading.)
        registerInfo.deviceName = device.getName();
        registerInfo.version = device.getVersion();
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        registerInfo.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        Optional<Register> slaveRegister = topologyService.getSlaveRegister(register, clock.instant());
        if (!slaveRegister.isPresent()) {
            register.getLastReading().ifPresent(reading -> registerInfo.lastReading = createReadingInfo(reading, register, false, null));
        } else {
            Register<?, ?> register1 = slaveRegister.get();
            register1.getLastReading().ifPresent(reading -> registerInfo.lastReading = createReadingInfo(reading, register1, false, null));
            registerInfo.dataloggerSlaveName = register1.getDevice().getName();
        }
    }

    private TextRegisterInfo createTextRegisterInfo(TextRegister textRegister, TopologyService topologyService) {
        TextRegisterInfo textRegisterInfo = new TextRegisterInfo();
        addCommonRegisterInfo(textRegister, textRegisterInfo, topologyService);
        return textRegisterInfo;
    }

    private RegisterInfo createRegisterInfo(NumericalRegister register, TopologyService topologyService) {
        RegisterInfo registerInfo = new RegisterInfo();
        addCommonRegisterInfo(register, registerInfo, topologyService);
        RegisterSpec registerSpec = register.getRegisterSpec();
        registerInfo.numberOfFractionDigits = ((NumericalRegisterSpec) registerSpec).getNumberOfFractionDigits();
        registerInfo.overruledNumberOfFractionDigits = ((NumericalRegisterSpec) registerSpec).getNumberOfFractionDigits();
        ((NumericalRegisterSpec) registerSpec).getOverflowValue().ifPresent(overflow -> registerInfo.overflow = overflow);
        registerInfo.useMultiplier = ((NumericalRegisterSpec) registerSpec).isUseMultiplier();
        register.getOverflow().ifPresent(overruledOverflowValue -> registerInfo.overruledOverflow = overruledOverflowValue);
        Instant timeStamp = register.getLastReadingDate().orElse(clock.instant());
        register.getCalculatedReadingType(timeStamp).ifPresent(calculatedReadingType -> registerInfo.calculatedReadingType = readingTypeInfoFactory.from(calculatedReadingType));
        registerInfo.multiplier = register.getMultiplier(timeStamp).orElseGet(() -> null);
        return registerInfo;
    }
}
