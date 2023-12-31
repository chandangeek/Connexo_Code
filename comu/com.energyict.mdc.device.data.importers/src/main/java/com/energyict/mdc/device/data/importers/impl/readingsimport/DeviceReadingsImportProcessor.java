/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.NumericalRegisterSpec;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceReadingsData;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeviceReadingsImportProcessor extends AbstractDeviceDataFileImportProcessor<DeviceReadingsImportRecord> {

    private Device device;
    private DeviceReadingsData deviceReadingsData = new DeviceReadingsData();
    private Map<Device, DeviceReadingsData> slaves = new HashMap<>();

    DeviceReadingsImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    /**
     * Processes the readings from file (each row represents a device reading).
     * The processor supports register and channel readings by CIM code, OBIS code and Channel/register name.
     * If the obis code is duplicated then the reading is not added and an error message is shown. Also allows time intervals lesser than 1 day.
     * The values are stored along with the reading quality code that indicates they were manually added.
     *
     * @param data Represents device reading import record, which is a single row in the file.
     * @param logger Represents the message handler.
     * @throws ProcessorException
     */
    @Override
    public void process(DeviceReadingsImportRecord data, FileImportLogger logger) throws ProcessorException {
        setDevice(data, logger);
        validateReadingDate(device, data.getReadingDateTime(), data.getLineNumber());
        for (int i = 0; i < data.getReadingTypes().size(); i++) {
            String readingTypeString = data.getReadingTypes().get(i);
            MeteringService meteringService = getContext().getMeteringService();
            ReadingType masterReadingType = (meteringService.getReadingType(readingTypeString)
                    .orElseGet(() -> meteringService.getReadingTypeByName(readingTypeString)
                            .orElseGet(() -> getReadingTypeByObisCode(readingTypeString, data.getLineNumber())
                                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_SUCH_READING_TYPE, data.getLineNumber(), readingTypeString)))));
            ZoneId deviceZoneId = getZoneId(device, data);
            ZonedDateTime timeStamp = data.getReadingDateTime().withZoneSameInstant(deviceZoneId);
            ValueValidator validator;

            Optional<Channel> masterDeviceChannel = device.getChannels().stream().filter((c) -> c.getReadingType().equals(masterReadingType)).findFirst();
            Optional<Register> masterDeviceRegister = device.getRegisters().stream().filter((r) -> r.getReadingType().equals(masterReadingType)).findFirst();

            if (masterDeviceChannel.isPresent()) {
                List<DataLoggerChannelUsage> channelUsages = getContext().getTopologyService()
                        .findDataLoggerChannelUsagesForChannels(masterDeviceChannel.get(), Range.singleton(timeStamp.toInstant()));
                if (!channelUsages.isEmpty()) {
                    for (DataLoggerChannelUsage usage : channelUsages) {
                        Device slave = usage.getPhysicalGatewayReference().getOrigin();
                        DeviceReadingsData slaveReadingsData = computeNewSlaveIfAbsent(slave);
                        validateDeviceState(data, slave);
                        Optional<Channel> slaveChannel = slave.getChannels().stream().filter((c) -> usage.getSlaveChannel().getReadingTypes().contains(c.getReadingType())).findFirst();
                        if (slaveChannel.isPresent() && i < data.getValues().size()) {
                            ReadingType slaveReadingType = slaveChannel.get().getReadingType();
                            ZoneId slaveZoneId = getZoneId(slave, data);
                            validateReadingType(slaveReadingType, data.getReadingDateTime().withZoneSameInstant(slaveZoneId), data.getLineNumber());
                            validator = createValueValidatorForChannel(slaveChannel.get(), slave, slaveReadingType.getMRID(), logger, data.getLineNumber());

                            addReading(slaveReadingsData, data, validator, slaveReadingType, data.getValues().get(i));
                        }
                    }
                } else {
                    if (i < data.getValues().size()) {
                        validateReadingType(masterReadingType, timeStamp, data.getLineNumber());
                        validator = createValueValidatorForChannel(masterDeviceChannel.get(), device, masterReadingType.getMRID(), logger, data.getLineNumber());

                        addReading(deviceReadingsData, data, validator, masterReadingType, data.getValues().get(i));
                    }
                }
            } else if (masterDeviceRegister.isPresent()) {
                Optional<Register> slaveRegister = getContext().getTopologyService().getSlaveRegister(masterDeviceRegister.get(), data.getReadingDateTime().toInstant());
                if (slaveRegister.isPresent()) {
                    Device slave = slaveRegister.get().getDevice();
                    DeviceReadingsData slaveReadingsData = computeNewSlaveIfAbsent(slave);
                    validateDeviceState(data, slave);
                    if (i < data.getValues().size()) {
                        ReadingType slaveReadingType = slaveRegister.get().getReadingType();
                        ZoneId slaveZoneId = getZoneId(slave, data);
                        validateReadingType(slaveReadingType, data.getReadingDateTime().withZoneSameInstant(slaveZoneId), data.getLineNumber());
                        validator = createValueValidatorForRegister(slaveRegister.get(), slave, slaveReadingType.getMRID(), logger, data.getLineNumber());

                        addReading(slaveReadingsData, data, validator, slaveReadingType, data.getValues().get(i));
                    }
                } else {
                    if (i < data.getValues().size()) {
                        validateReadingType(masterReadingType, timeStamp, data.getLineNumber());
                        validator = createValueValidatorForRegister(masterDeviceRegister.get(), device, masterReadingType.getMRID(), logger, data.getLineNumber());

                        addReading(deviceReadingsData, data, validator, masterReadingType, data.getValues().get(i));
                    }
                }
            }
        }
    }

    private void addReading(DeviceReadingsData deviceReadingsData, DeviceReadingsImportRecord data, ValueValidator validator, ReadingType readingType, BigDecimal readingValue) {
        readingValue = validator.validateAndCorrectValue(readingValue);
        Instant timeStamp = data.getReadingDateTime().toInstant();
        if (readingType.isRegular()) {
            Set<ReadingQualityType> readingQualityType = Collections.singleton(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED));
            IntervalReading reading = IntervalReadingImpl.of(timeStamp, readingValue, readingQualityType);

            deviceReadingsData.getChannelReadingsToStore().put(readingType, reading);
            if (!deviceReadingsData.getLastReadingPerChannel().containsKey(readingType) || timeStamp.isAfter(deviceReadingsData.getLastReadingPerChannel().get(readingType))) {
                deviceReadingsData.getLastReadingPerChannel().put(readingType, timeStamp);
            }
        } else {
            ReadingImpl reading = ReadingImpl.of(readingType.getMRID(), readingValue, timeStamp);
            reading.addQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED));
            deviceReadingsData.getRegisterReadingsToStore().add(reading);
        }
    }

    private ZoneId getZoneId(Device device, DeviceReadingsImportRecord data) {
        return getMeterActivationEffectiveAt(device.getMeterActivationsMostRecentFirst(), data.getReadingDateTime().toInstant())
                .map(MeterActivation::getChannelsContainer)
                .map(ChannelsContainer::getZoneId)
                .orElse(data.getReadingDateTime().getZone());
    }

    private DeviceReadingsData computeNewSlaveIfAbsent(Device slave) {
        return slaves.computeIfAbsent(slave, key -> new DeviceReadingsData());
    }

    private void validateReadingDate(Device device, ZonedDateTime readingDate, long lineNumber) {
        List<MeterActivation> meterActivations = device.getMeterActivationsMostRecentFirst();
        if (!hasMeterActivationEffectiveAt(meterActivations, readingDate.toInstant())) {
            MeterActivation firstMeterActivation = meterActivations.get(meterActivations.size() - 1);
            if (firstMeterActivation.getRange().hasLowerBound() && !readingDate.toInstant().isAfter(firstMeterActivation.getStart())) {
                throw new ProcessorException(MessageSeeds.READING_DATE_BEFORE_METER_ACTIVATION, lineNumber,
                        DefaultDateTimeFormatters.shortDate().withShortTime().build().format(readingDate));
            }
            MeterActivation lastMeterActivation = meterActivations.get(0);
            if (lastMeterActivation.getRange().hasUpperBound() && readingDate.toInstant().isAfter(lastMeterActivation.getEnd())) {
                throw new ProcessorException(MessageSeeds.READING_DATE_AFTER_METER_ACTIVATION, lineNumber,
                        DefaultDateTimeFormatters.shortDate().withShortTime().build().format(readingDate));
            }
        }
    }

    private Optional<ReadingType> getReadingTypeByObisCode(String readingTypeStr, long lineNumber) {
        List<Integer> channelsList = IntStream.range(0, device.getChannels().size()).boxed()
                .filter(opt -> device.getChannels().get(opt).getObisCode().toString().equals(readingTypeStr))
                .collect(Collectors.toList());

        List<Integer> registersList = IntStream.range(0, device.getRegisters().size()).boxed()
                .filter(opt -> device.getRegisters().get(opt).getObisCode().toString().equals(readingTypeStr))
                .collect(Collectors.toList());

        Optional<ReadingType> readingType = Optional.empty();
        if (registersList.size() > 1 || channelsList.size() > 1 || (channelsList.size() + registersList.size() > 1)) {
            throw new ProcessorException(MessageSeeds.READING_TYPE_DUPLICATED_OBIS_CODE, lineNumber, readingTypeStr, device
                    .getName());
        }
        if (channelsList.size() == 1) {
            readingType = Optional.ofNullable(device.getChannels().get(channelsList.get(0)).getReadingType());
        }
        if (registersList.size() == 1) {
            readingType = Optional.ofNullable(device.getRegisters().get(registersList.get(0)).getReadingType());
        }
        return readingType;
    }

    @Override
    public void complete(FileImportLogger logger) {
        if (device == null || deviceReadingsData.isEmpty() && slaves.isEmpty()) {
            return;
        }
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        if (!deviceReadingsData.getChannelReadingsToStore().isEmpty()) {
            meterReading.addAllIntervalBlocks(getIntervalBlocksFromChannelReadings(deviceReadingsData.getChannelReadingsToStore()));
        }
        if (!deviceReadingsData.getRegisterReadingsToStore().isEmpty()) {
            meterReading.addAllReadings(deviceReadingsData.getRegisterReadingsToStore());
        }
        if (!deviceReadingsData.getChannelReadingsToStore().isEmpty() || !deviceReadingsData.getRegisterReadingsToStore().isEmpty()) {
            device.store(meterReading);
        }
        if (!deviceReadingsData.getChannelReadingsToStore().isEmpty() && !deviceReadingsData.getLastReadingPerChannel().isEmpty()) {
            updateLastReading(device, deviceReadingsData.getChannelReadingsToStore(), deviceReadingsData.getLastReadingPerChannel());
        }

        slaves.forEach((slave, slaveReadingsData) -> {
            MeterReadingImpl slaveMeterReading = MeterReadingImpl.newInstance();
            if (!slaveReadingsData.getChannelReadingsToStore().isEmpty()) {
                slaveMeterReading.addAllIntervalBlocks(getIntervalBlocksFromChannelReadings(slaveReadingsData.getChannelReadingsToStore()));
            }
            if (!slaveReadingsData.getRegisterReadingsToStore().isEmpty()) {
                slaveMeterReading.addAllReadings(slaveReadingsData.getRegisterReadingsToStore());
            }
            if (!slaveReadingsData.getChannelReadingsToStore().isEmpty() || !slaveReadingsData.getRegisterReadingsToStore().isEmpty()) {
                slave.store(slaveMeterReading);
            }
            if (!slaveReadingsData.getChannelReadingsToStore().isEmpty() && !slaveReadingsData.getLastReadingPerChannel().isEmpty()) {
                updateLastReading(slave, slaveReadingsData.getChannelReadingsToStore(), slaveReadingsData.getLastReadingPerChannel());
            }
        });

        resetState();
    }

    private List<IntervalBlockImpl> getIntervalBlocksFromChannelReadings(Multimap<ReadingType, IntervalReading> channelReadingsToStore) {
        return channelReadingsToStore.asMap().entrySet().stream().map(channelReadings -> {
            IntervalBlockImpl block = IntervalBlockImpl.of(channelReadings.getKey().getMRID());
            block.addAllIntervalReadings(new ArrayList<>(channelReadings.getValue()));
            return block;
        }).collect(Collectors.toList());
    }

    private void resetState() {
        device = null;
        deviceReadingsData.clear();
        slaves.clear();
    }

    private void setDevice(DeviceReadingsImportRecord data, FileImportLogger logger) {
        if (device == null ||
                (!device.getmRID().equals(data.getDeviceIdentifier()) && !device.getName().equals(data.getDeviceIdentifier()))) {
            complete(logger);//when new identifier comes we store all previous data read
            device = findDeviceByIdentifier(data.getDeviceIdentifier())
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        }
        validateDeviceState(data, device);
    }

    private void validateDeviceState(DeviceReadingsImportRecord data, Device device) {
        if (device.getState().getName().equals(DefaultState.IN_STOCK.getKey())) {
            throw new ProcessorException(MessageSeeds.READING_IMPORT_NOT_ALLOWED_FOR_IN_STOCK_DEVICE, data.getLineNumber(), device.getName());
        }
        if (device.getState().getName().equals(DefaultState.DECOMMISSIONED.getKey())
                && !((User) getContext().getThreadPrincipalService().getPrincipal()).hasPrivilege("MDC", Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA)) {
            throw new ProcessorException(MessageSeeds.READING_IMPORT_NOT_ALLOWED_FOR_DECOMMISSIONED_DEVICE, data.getLineNumber(), device.getName());
        }
    }

    private void updateLastReading(Device device, Multimap<ReadingType, IntervalReading> channelReadingsToStore, Map<ReadingType, Instant> lastReadingPerChannel) {
        device.getChannels().stream()
                .filter(channel -> channelReadingsToStore.containsKey(channel.getReadingType()))
                .map(channel -> Pair.of(channel.getLoadProfile(), lastReadingPerChannel.get(channel.getReadingType())))
                .forEach(pair -> device.getLoadProfileUpdaterFor(pair.getFirst()).setLastReadingIfLater(pair.getLast()).update());
    }

    private boolean hasMeterActivationEffectiveAt(List<MeterActivation> meterActivations, Instant timeStamp) {
        return meterActivations.isEmpty() || getMeterActivationEffectiveAt(meterActivations, timeStamp).isPresent();
    }

    private Optional<MeterActivation> getMeterActivationEffectiveAt(List<MeterActivation> meterActivations, Instant timeStamp) {
        return meterActivations.stream().filter(ma -> ma.getInterval().toOpenClosedRange().contains(timeStamp)).findFirst();
    }

    private void validateReadingType(ReadingType readingType, ZonedDateTime readingDate, long lineNumber) {
        if (readingType.isRegular()) {
            if (MacroPeriod.DAILY == readingType.getMacroPeriod() && !validTimeOfDay(readingDate)) {
                throw new ProcessorException(MessageSeeds.READING_DATE_INCORRECT_FOR_DAILY_CHANNEL, lineNumber, readingType.getMRID(), readingDate.getZone());
            }
            if (MacroPeriod.MONTHLY == readingType.getMacroPeriod() && !(readingDate.getDayOfMonth() == 1 && validTimeOfDay(readingDate))) {
                throw new ProcessorException(MessageSeeds.READING_DATE_INCORRECT_FOR_MONTHLY_CHANNEL, lineNumber, readingType.getMRID(), readingDate.getZone());
            }
            if (MacroPeriod.YEARLY == readingType.getMacroPeriod() && !(readingDate.getDayOfYear() == 1 && validTimeOfDay(readingDate))) {
                throw new ProcessorException(MessageSeeds.READING_DATE_INCORRECT_FOR_YEARLY_CHANNEL, lineNumber, readingType.getMRID(), readingDate.getZone());
            }
            if (readingType.getMeasuringPeriod()
                    .isApplicable() && !validMinutes(readingType.getMeasuringPeriod(), readingDate)) {
                throw new ProcessorException(MessageSeeds.READING_DATE_INCORRECT_FOR_MINUTES_CHANNEL, lineNumber, readingType
                        .getMRID(), readingType.getMeasuringPeriod().getMinutes(), readingDate.getZone());
            }
        }
    }

    private boolean validTimeOfDay(ZonedDateTime dateTime) {
        return dateTime.getHour() == 0 && dateTime.getMinute() == 0 && dateTime.getSecond() == 0 && dateTime.getNano() == 0;
    }

    private boolean validMinutes(TimeAttribute timeAttribute, ZonedDateTime dateTime) {
        return timeAttribute.getMinutes() > 0 && (dateTime.getMinute() % timeAttribute.getMinutes()) == 0;
    }

    private ValueValidator createValueValidatorForChannel(Channel channel, Device device, String readingTypeString, FileImportLogger logger, long lineNumber) {
        ChannelSpec channelSpec = channel.getChannelSpec();
        return value -> {
            if (channelSpec.getOverflow().isPresent()) {
                if (value.compareTo(channelSpec.getOverflow().get()) > 0) {
                    throw new ProcessorException(MessageSeeds.READING_VALUE_DOES_NOT_MATCH_CHANNEL_CONFIG_OVERFLOW, lineNumber, readingTypeString, device.getName());
                }
            }
            if (value.scale() > channelSpec.getNbrOfFractionDigits()) {
                value = value.setScale(channelSpec.getNbrOfFractionDigits(), RoundingMode.DOWN);
                logger.warning(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG, lineNumber, value.toPlainString());
            }
            return value;
        };
    }

    private ValueValidator createValueValidatorForRegister(Register register, Device device, String readingTypeString, FileImportLogger logger, long lineNumber) {
        if (register.getRegisterSpec().isTextual()) {
            //textual registers are not supported
            throw new ProcessorException(MessageSeeds.NOT_SUPPORTED_READING_TYPE, lineNumber, readingTypeString);
        }
        NumericalRegisterSpec registerSpec = (NumericalRegisterSpec) register.getRegisterSpec();
        return value -> {
            if (registerSpec.getOverflowValue().isPresent()) {
                if (value.compareTo(registerSpec.getOverflowValue().get()) > 0) {
                    throw new ProcessorException(MessageSeeds.READING_VALUE_DOES_NOT_MATCH_REGISTER_CONFIG_OVERFLOW, lineNumber, readingTypeString, device.getName());
                }
            }
            if (value.scale() > registerSpec.getNumberOfFractionDigits()) {
                value = value.setScale(registerSpec.getNumberOfFractionDigits(), RoundingMode.DOWN);
                logger.warning(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG, lineNumber, value.toPlainString());
            }
            return value;
        };
    }

    interface ValueValidator {

        BigDecimal validateAndCorrectValue(BigDecimal value);

    }
}
