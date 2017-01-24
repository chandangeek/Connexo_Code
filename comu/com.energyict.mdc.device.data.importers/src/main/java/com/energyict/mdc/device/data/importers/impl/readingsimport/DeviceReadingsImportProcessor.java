package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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
import java.util.stream.Collectors;

public class DeviceReadingsImportProcessor extends AbstractDeviceDataFileImportProcessor<DeviceReadingsImportRecord> {

    private Device device;
    private Multimap<ReadingType, IntervalReading> channelReadingsToStore = HashMultimap.create();
    private Map<ReadingType, Instant> lastReadingPerChannel = new HashMap<>();
    private List<Reading> registerReadingsToStore = new ArrayList<>();

    DeviceReadingsImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    public void process(DeviceReadingsImportRecord data, FileImportLogger logger) throws ProcessorException {
        setDevice(data, logger);
        validateReadingDate(device, data.getReadingDateTime(), data.getLineNumber());
        for (int i = 0; i < data.getReadingTypes().size(); i++) {
            String readingTypeMRID = data.getReadingTypes().get(i);
            ReadingType readingType = getContext().getMeteringService().getReadingType(readingTypeMRID)
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_SUCH_READING_TYPE, data.getLineNumber(), readingTypeMRID));
            ZoneId deviceZoneId = getMeterActivationEffectiveAt(device.getMeterActivationsMostRecentFirst(), data.getReadingDateTime().toInstant())
                    .map(MeterActivation::getChannelsContainer)
                    .map(ChannelsContainer::getZoneId)
                    .orElse(data.getReadingDateTime().getZone());
            validateReadingType(readingType, data.getReadingDateTime().withZoneSameInstant(deviceZoneId), data.getLineNumber());
            if (i < data.getValues().size()) {
                BigDecimal readingValue = data.getValues().get(i);
                ValueValidator validator = createValueValidator(device, readingType, logger, data.getLineNumber());
                readingValue = validator.validateAndCorrectValue(readingValue);
                addReading(readingType, readingValue, data.getReadingDateTime().toInstant());
            }
        }
    }

    @Override
    public void complete(FileImportLogger logger) {
        if (device == null || channelReadingsToStore.isEmpty() && registerReadingsToStore.isEmpty()) {
            return;
        }
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        List<IntervalBlockImpl> intervalBlocks = channelReadingsToStore.asMap().entrySet().stream().map(channelReadings -> {
            IntervalBlockImpl block = IntervalBlockImpl.of(channelReadings.getKey().getMRID());
            block.addAllIntervalReadings(new ArrayList<>(channelReadings.getValue()));
            return block;
        }).collect(Collectors.toList());
        meterReading.addAllIntervalBlocks(intervalBlocks);
        meterReading.addAllReadings(registerReadingsToStore);
        device.store(meterReading);
        updateLastReading();
        resetState();
    }

    private void resetState() {
        device = null;
        channelReadingsToStore.clear();
        lastReadingPerChannel.clear();
        registerReadingsToStore.clear();
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

    private void updateLastReading() {
        device.getChannels().stream()
                .filter(channel -> channelReadingsToStore.containsKey(channel.getReadingType()))
                .map(channel -> Pair.of(channel.getLoadProfile(), lastReadingPerChannel.get(channel.getReadingType())))
                .forEach(pair -> device.getLoadProfileUpdaterFor(pair.getFirst()).setLastReadingIfLater(pair.getLast()).update());
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

    private boolean hasMeterActivationEffectiveAt(List<MeterActivation> meterActivations, Instant timeStamp) {
        return meterActivations.isEmpty() || getMeterActivationEffectiveAt(meterActivations, timeStamp).isPresent();
    }

    private Optional<MeterActivation> getMeterActivationEffectiveAt(List<MeterActivation> meterActivations, Instant timeStamp) {
        return meterActivations.stream().filter(ma -> ma.getInterval().toOpenClosedRange().contains(timeStamp)).findFirst();
    }

    private void validateReadingType(ReadingType readingType, ZonedDateTime readingDate, long lineNumber) {
        if (readingType.isRegular()) {
            if (readingType.getMeasuringPeriod().isApplicable()) {
                throw new ProcessorException(MessageSeeds.NOT_SUPPORTED_READING_TYPE, lineNumber, readingType.getMRID());
            }
            if (MacroPeriod.DAILY == readingType.getMacroPeriod() && !validTimeOfDay(readingDate)) {
                throw new ProcessorException(MessageSeeds.READING_DATE_INCORRECT_FOR_DAILY_CHANNEL, lineNumber, readingType.getMRID(), readingDate.getZone());
            }
            if (MacroPeriod.MONTHLY == readingType.getMacroPeriod() && !(readingDate.getDayOfMonth() == 1 && validTimeOfDay(readingDate))) {
                throw new ProcessorException(MessageSeeds.READING_DATE_INCORRECT_FOR_MONTHLY_CHANNEL, lineNumber, readingType.getMRID(), readingDate.getZone());
            }
        }
    }

    private boolean validTimeOfDay(ZonedDateTime dateTime) {
        return dateTime.getHour() == 0 && dateTime.getMinute() == 0 && dateTime.getSecond() == 0 && dateTime.getNano() == 0;
    }

    private ValueValidator createValueValidator(Device device, ReadingType readingType, FileImportLogger logger, long lineNumber) {
        Optional<Register> register = device.getRegisters().stream().filter(r -> r.getReadingType().equals(readingType)).findFirst();
        if (register.isPresent()) {
            if (register.get().getRegisterSpec().isTextual()) {
                //textual registers are not supported
                throw new ProcessorException(MessageSeeds.NOT_SUPPORTED_READING_TYPE, lineNumber, readingType.getMRID());
            }
            NumericalRegisterSpec registerSpec = (NumericalRegisterSpec) register.get().getRegisterSpec();
            return value -> {
                if (registerSpec.getOverflowValue().isPresent()) {
                    if (value.compareTo(registerSpec.getOverflowValue().get()) > 0) {
                        throw new ProcessorException(MessageSeeds.READING_VALUE_DOES_NOT_MATCH_REGISTER_CONFIG_OVERFLOW, lineNumber, readingType.getMRID(), device.getName());
                    }
                }
                if (value.scale() > registerSpec.getNumberOfFractionDigits()) {
                    value = value.setScale(registerSpec.getNumberOfFractionDigits(), RoundingMode.DOWN);
                    logger.warning(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG, lineNumber, value.toPlainString());
                }
                return value;
            };
        }
        Optional<Channel> channel = device.getChannels().stream().filter(ch -> ch.getReadingType().equals(readingType)).findFirst();
        if (channel.isPresent()) {
            ChannelSpec channelSpec = channel.get().getChannelSpec();
            return value -> {
                if (channelSpec.getOverflow().isPresent()) {
                    if (value.compareTo(channelSpec.getOverflow().get()) > 0) {
                        throw new ProcessorException(MessageSeeds.READING_VALUE_DOES_NOT_MATCH_CHANNEL_CONFIG_OVERFLOW, lineNumber, readingType.getMRID(), device.getName());
                    }
                }
                if (value.scale() > channelSpec.getNbrOfFractionDigits()) {
                    value = value.setScale(channelSpec.getNbrOfFractionDigits(), RoundingMode.DOWN);
                    logger.warning(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG, lineNumber, value.toPlainString());
                }
                return value;
            };
        }
        throw new ProcessorException(MessageSeeds.DEVICE_DOES_NOT_SUPPORT_READING_TYPE, lineNumber, readingType.getMRID(), device.getName());
    }

    private void addReading(ReadingType readingType, BigDecimal value, Instant timeStamp) {
        if (readingType.isRegular()) {
            channelReadingsToStore.put(readingType, IntervalReadingImpl.of(timeStamp, value, Collections.emptyList()));
            if (!lastReadingPerChannel.containsKey(readingType) || timeStamp.isAfter(lastReadingPerChannel.get(readingType))) {
                lastReadingPerChannel.put(readingType, timeStamp);
            }
        } else {
            registerReadingsToStore.add(ReadingImpl.of(readingType.getMRID(), value, timeStamp));
        }
    }

    interface ValueValidator {

        BigDecimal validateAndCorrectValue(BigDecimal value);

    }
}
