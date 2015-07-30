package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.users.User;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DeviceReadingsImportProcessor implements FileImportProcessor<DeviceReadingsImportRecord> {

    private final DeviceDataImporterContext context;

    DeviceReadingsImportProcessor(DeviceDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(DeviceReadingsImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = this.context.getDeviceService().findByUniqueMrid(data.getDeviceMrid())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceMrid()));

        Instant readingDate = data.getReadingDateTime().toInstant();
        validateDeviceState(data, device);
        validateReadingDate(device, data.getReadingDateTime(), data.getLineNumber());
        List<MeterReading> readings = new ArrayList<>();
        Set<ReadingType> readingTypesUpdated = new HashSet<>();
        for (int i = 0; i < data.getReadingTypes().size(); i++) {
            String readingTypeMRID = data.getReadingTypes().get(i);
            ReadingType readingType = this.context.getMeteringService().getReadingType(readingTypeMRID)
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_SUCH_READING_TYPE, data.getLineNumber(), readingTypeMRID));
            validateReadingType(readingType, data);
            if (i < data.getValues().size()) {
                BigDecimal readingValue = data.getValues().get(i);
                ValueValidator validator = createValueValidator(device, readingType, logger, data.getLineNumber());
                readingValue = validator.validateAndCorrectValue(readingValue);
                readings.add(createReading(readingType, readingValue, readingDate));
                readingTypesUpdated.add(readingType);
            }
        }
        if (!readingTypesUpdated.isEmpty()) {
            readings.stream().forEach(device::store);
            updateLastReading(device, readingTypesUpdated, readingDate);
        }
    }

    private void validateDeviceState(DeviceReadingsImportRecord data, Device device) {
        if (device.getState().getName().equals(DefaultState.DECOMMISSIONED.getKey())
                && !((User)context.getThreadPrincipalService().getPrincipal()).hasPrivilege("MDC", Privileges.ADMINISTER_DECOMMISSIONED_DEVICE_DATA)) {
            throw new ProcessorException(MessageSeeds.READING_IMPORT_NOT_ALLOWED_FOR_DECOMMISSIONED_DEVICE, data.getLineNumber(), device.getmRID());
        }
    }

    private void updateLastReading(Device device, Set<ReadingType> readingTypes, Instant lastReading) {
        device.getChannels().stream()
                .filter(channel -> readingTypes.contains(channel.getReadingType()))
                .map(Channel::getLoadProfile)
                .forEach(loadProfile -> device.getLoadProfileUpdaterFor(loadProfile).setLastReadingIfLater(lastReading).update());
    }

    private void validateReadingDate(Device device, ZonedDateTime readingDate, long lineNumber) {
        List<MeterActivation> meterActivations = device.getMeterActivationsMostRecentFirst();
        if (!hasMeterActivationEffectiveAt(meterActivations, readingDate.toInstant())) {
            MeterActivation firstMeterActivation = meterActivations.get(meterActivations.size() - 1);
            if (firstMeterActivation.getRange().hasLowerBound() && readingDate.toInstant().isBefore(firstMeterActivation.getRange().lowerEndpoint())) {
                throw new ProcessorException(MessageSeeds.READING_DATE_BEFORE_METER_ACTIVATION, lineNumber,
                        DefaultDateTimeFormatters.shortDate().withShortTime().build().format(readingDate));
            }
            MeterActivation lastMeterActivation = meterActivations.get(0);
            if (lastMeterActivation.getRange().hasUpperBound() && readingDate.toInstant().isAfter(lastMeterActivation.getRange().upperEndpoint())) {
                throw new ProcessorException(MessageSeeds.READING_DATE_AFTER_METER_ACTIVATION, lineNumber,
                        DefaultDateTimeFormatters.shortDate().withShortTime().build().format(readingDate));
            }
        }
    }

    private boolean hasMeterActivationEffectiveAt(List<MeterActivation> meterActivations, Instant timeStamp) {
        return meterActivations.isEmpty() || meterActivations.stream().filter(ma -> ma.isEffectiveAt(timeStamp)).findFirst().isPresent();
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

    private MeterReading createReading(ReadingType readingType, BigDecimal value, Instant timeStamp) {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        if (readingType.isRegular()) {
            IntervalReadingImpl intervalReading = IntervalReadingImpl.of(timeStamp, value);
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(readingType.getMRID());
            intervalBlock.addIntervalReading(intervalReading);
            meterReading.addIntervalBlock(intervalBlock);
        } else {
            Reading reading = ReadingImpl.of(readingType.getMRID(), value, timeStamp);
            meterReading.addReading(reading);
        }
        return meterReading;
    }

    interface ValueValidator {

        BigDecimal validateAndCorrectValue(BigDecimal value);

    }
}
