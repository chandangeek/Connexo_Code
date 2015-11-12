package com.energyict.mdc.device.data.importers.impl.readingsimport;

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
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.FileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceReadingsImportProcessor implements FileImportProcessor<DeviceReadingsImportRecord> {

    private final DeviceDataImporterContext context;

    private Device device;
    private Multimap<ReadingType, IntervalReading> channelReadingsToStore = HashMultimap.create();
    private Map<ReadingType, Instant> lastReadingPerChannel = new HashMap<>();
    private List<Reading> registerReadingsToStore = new ArrayList<>();

    DeviceReadingsImportProcessor(DeviceDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(DeviceReadingsImportRecord data, FileImportLogger logger) throws ProcessorException {
        setDevice(data);
        validateReadingDate(device, data.getReadingDateTime(), data.getLineNumber());
        for (int i = 0; i < data.getReadingTypes().size(); i++) {
            String readingTypeMRID = data.getReadingTypes().get(i);
            ReadingType readingType = this.context.getMeteringService().getReadingType(readingTypeMRID)
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_SUCH_READING_TYPE, data.getLineNumber(), readingTypeMRID));
            validateReadingType(readingType, data);
            if (i < data.getValues().size()) {
                BigDecimal readingValue = data.getValues().get(i);
                ValueValidator validator = createValueValidator(device, readingType, logger, data.getLineNumber());
                readingValue = validator.validateAndCorrectValue(readingValue);
                addReading(readingType, readingValue, data.getReadingDateTime().toInstant());
            }
        }
    }

    @Override
    public void complete() {
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

    private void setDevice(DeviceReadingsImportRecord data) {
        if (device == null || !device.getmRID().equals(data.getDeviceMRID())) {
            complete();//when new mrid comes we store all previous data read
            device = this.context.getDeviceService().findByUniqueMrid(data.getDeviceMRID())
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceMRID()));
            validateDeviceState(data, device);
        }
    }

    private void validateDeviceState(DeviceReadingsImportRecord data, Device device) {
        if (device.getState().getName().equals(DefaultState.DECOMMISSIONED.getKey())
                && !((User)context.getThreadPrincipalService().getPrincipal()).hasPrivilege("MDC", Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA)) {
            throw new ProcessorException(MessageSeeds.READING_IMPORT_NOT_ALLOWED_FOR_DECOMMISSIONED_DEVICE, data.getLineNumber(), device.getmRID());
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
        return meterActivations.isEmpty() || meterActivations.stream().filter(ma -> ma.getInterval().toOpenClosedRange().contains(timeStamp)).findFirst().isPresent();
    }

    private void validateReadingType(ReadingType readingType, DeviceReadingsImportRecord data) {
        if (readingType.isRegular() && readingType.getMeasuringPeriod().isApplicable()) {
            throw new ProcessorException(MessageSeeds.NOT_SUPPORTED_READING_TYPE, data.getLineNumber(), readingType.getMRID());
        }
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
                if (value.compareTo(registerSpec.getOverflowValue()) > 0) {
                    throw new ProcessorException(MessageSeeds.READING_VALUE_DOES_NOT_MATCH_REGISTER_CONFIG_OVERFLOW, lineNumber, readingType.getMRID(), device.getmRID());
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
                if (value.compareTo(channelSpec.getOverflow()) > 0) {
                    throw new ProcessorException(MessageSeeds.READING_VALUE_DOES_NOT_MATCH_CHANNEL_CONFIG_OVERFLOW, lineNumber, readingType.getMRID(), device.getmRID());
                }
                if (value.scale() > channelSpec.getNbrOfFractionDigits()) {
                    value = value.setScale(channelSpec.getNbrOfFractionDigits(), RoundingMode.DOWN);
                    logger.warning(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG, lineNumber, value.toPlainString());
                }
                return value;
            };
        }
        throw new ProcessorException(MessageSeeds.DEVICE_DOES_NOT_SUPPORT_READING_TYPE, lineNumber, readingType.getMRID(), device.getmRID());
    }

    private void addReading(ReadingType readingType, BigDecimal value, Instant timeStamp) {
        if (readingType.isRegular()) {
            channelReadingsToStore.put(readingType, IntervalReadingImpl.of(timeStamp, value));
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
