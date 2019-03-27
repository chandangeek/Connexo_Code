package com.energyict.mdc.device.data.importers.impl.deviceeventsimport;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import java.time.Instant;
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
        device.store(meterReading);
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
