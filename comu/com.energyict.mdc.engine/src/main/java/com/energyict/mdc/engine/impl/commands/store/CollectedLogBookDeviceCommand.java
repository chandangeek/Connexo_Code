package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides functionality to store {@link com.energyict.mdc.protocol.api.device.BaseLogBook} data into the system
 *
 * @author sva
 * @since 10/12/12 - 11:13
 */
public class CollectedLogBookDeviceCommand extends DeviceCommandImpl {

    private final DeviceLogBook deviceLogBook;
    private final MeterDataStoreCommand meterDataStoreCommand;
    private ComServerDAO comServerDAO;


    private interface Duo<A,B>{}

    public CollectedLogBookDeviceCommand(DeviceLogBook deviceLogBook, MeterDataStoreCommand meterDataStoreCommand) {
        super();
        this.deviceLogBook = deviceLogBook;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        this.comServerDAO = comServerDAO;
        LocalLogBook localLogBook = filterFutureDatesAndCalculateLastReading();
        storeEventsAndUpdateLastLogbook(localLogBook);
    }

    private void storeEventsAndUpdateLastLogbook(LocalLogBook localLogBook) {
        if (localLogBook.endDeviceEvents.size() > 0) {
            DeviceIdentifier<Device> deviceIdentifier = this.comServerDAO.getDeviceIdentifierFor(this.deviceLogBook.getLogBookIdentifier());
            this.meterDataStoreCommand.addEventReadings(deviceIdentifier, localLogBook.endDeviceEvents);
            this.meterDataStoreCommand.addLastLogBookUpdater(this.deviceLogBook.getLogBookIdentifier(), localLogBook.lastLogbook);
        }
    }

    private LocalLogBook filterFutureDatesAndCalculateLastReading() {
        List<EndDeviceEvent> filteredEndDeviceEvents = new ArrayList<>();
        Date lastLogbook = null;
        Date currentDate = getClock().now();
        for (EndDeviceEvent endDeviceEvent : MeterDataFactory.createEndDeviceEventsFor(this.deviceLogBook)) {
            if (!endDeviceEvent.getCreatedDateTime().after(currentDate)) {
                filteredEndDeviceEvents.add(endDeviceEvent);
                if (lastLogbook == null || endDeviceEvent.getCreatedDateTime().after(lastLogbook)) {
                    lastLogbook = endDeviceEvent.getCreatedDateTime();
                }
            }
        }
        return new LocalLogBook(filteredEndDeviceEvents, lastLogbook);
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("logbook").append(this.deviceLogBook.getLogBookIdentifier());
            builder.addProperty("nr of events").append(this.deviceLogBook.getCollectedMeterEvents().size());
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    private class LocalLogBook implements Duo<List<EndDeviceEvent>, Date> {

        private final List<EndDeviceEvent> endDeviceEvents;
        private final Date lastLogbook;

        private LocalLogBook(List<EndDeviceEvent> endDeviceEvents, Date lastLogBook) {
            this.endDeviceEvents = endDeviceEvents;
            this.lastLogbook = lastLogBook;
        }
    }
}