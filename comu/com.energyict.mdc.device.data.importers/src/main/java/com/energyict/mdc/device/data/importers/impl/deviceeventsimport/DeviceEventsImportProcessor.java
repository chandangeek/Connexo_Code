package com.energyict.mdc.device.data.importers.impl.deviceeventsimport;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class DeviceEventsImportProcessor extends AbstractDeviceDataFileImportProcessor<DeviceEventsImportRecord> {
    private Device device;
    public DeviceEventsImportProcessor(DeviceDataImporterContext deviceDataImporterContext) {
        super(deviceDataImporterContext);
    }

    @Override
    public void process(DeviceEventsImportRecord data, FileImportLogger logger) throws ProcessorException {
        setDevice(data,logger);
        Optional<LogBook> logbook = getLogBookByOBIS(device, data.getLogbookOBIScode());
        if ( logbook.isPresent())
            addDeviceEvents(logbook.get(), data);
        else
            throw new ProcessorException(MessageSeeds.INVALID_DEVICE_LOGBOOK_OBIS_CODE,
                    data.getLineNumber(),data.getLogbookOBIScode(),device.getName());
    }

    private void addDeviceEvents(LogBook logBook, DeviceEventsImportRecord data) {
        Instant eventTime = data.getDateTime().toInstant();
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        EndDeviceEventImpl endDeviceEvent = EndDeviceEventImpl.of(data.getEventCode(), eventTime);
        endDeviceEvent.setLogBookId(logBook.getId());
        endDeviceEvent.setDescription(data.getDescription());
        endDeviceEvent.setType(data.getDeviceCode());
        endDeviceEvent.setLogBookPosition(Integer.parseInt(data.getEventLogID()));
        meterReading.addEndDeviceEvent(endDeviceEvent);
        device.store(meterReading, getValidReadingDate(data, eventTime));
    }

    private Instant getValidReadingDate(DeviceEventsImportRecord data, Instant eventTime) {
        Instant readingDate;
        if(data.getReadingDate() != null) {
            validateReadingDate(device, data.getDateTime(), data.getReadingDate(), data.getLineNumber());
            readingDate = data.getReadingDate().toInstant();
            return readingDate;
        }
        return null;
    }

    private void validateReadingDate(Device device, ZonedDateTime eventDate, ZonedDateTime readingDate, long lineNumber) {
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
        //reading date MUST be after event date
        if(readingDate.isBefore(eventDate)) {
            throw new ProcessorException(MessageSeeds.READING_DATE_BEFORE_EVENT_DATE, lineNumber,
                    DefaultDateTimeFormatters.shortDate().withShortTime().build().format(readingDate),
                    DefaultDateTimeFormatters.shortDate().withShortTime().build().format(eventDate),
                    device.getName());
        }
    }

    private boolean hasMeterActivationEffectiveAt(List<MeterActivation> meterActivations, Instant timeStamp) {
        return meterActivations.isEmpty() || getMeterActivationEffectiveAt(meterActivations, timeStamp).isPresent();
    }

    private Optional<MeterActivation> getMeterActivationEffectiveAt(List<MeterActivation> meterActivations, Instant timeStamp) {
        return meterActivations.stream().filter(ma -> ma.getInterval().toOpenClosedRange().contains(timeStamp)).findFirst();
    }

    private Optional<LogBook> getLogBookByOBIS(Device device, String logbookOBIScode) {
        return device.getLogBooks().stream()
                .filter(l -> l.getDeviceObisCode().toString().equals(logbookOBIScode))
                .findFirst();
    }

    @Override
    public void complete(FileImportLogger logger) {

    }

    private void setDevice(DeviceEventsImportRecord data, FileImportLogger logger) {
        if (device == null ||
                (!device.getmRID().equals(data.getDeviceIdentifier()) && !device.getName().equals(data.getDeviceIdentifier()))) {
            complete(logger);
            device = findDeviceByIdentifier(data.getDeviceIdentifier())
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        }
    }
}
