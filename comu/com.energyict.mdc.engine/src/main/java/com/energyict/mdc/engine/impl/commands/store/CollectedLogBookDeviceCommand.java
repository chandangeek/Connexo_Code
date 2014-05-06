package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.energyict.comserver.core.ComServerDAO;
import com.energyict.comserver.time.Clocks;
import com.energyict.mdc.MeterDataFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.meterdata.DeviceLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierById;
import com.energyict.util.Pair;

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

    public CollectedLogBookDeviceCommand(DeviceLogBook deviceLogBook, IssueService issueService) {
        super(issueService);
        this.deviceLogBook = deviceLogBook;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        LogBookIdentifier logBookIdentifier = this.deviceLogBook.getLogBookIdentifier();
        LogBook logBook = (LogBook) logBookIdentifier.getLogBook();

        LocalLogBook localLogBook = filterFutureDatesAndCalculateLastReading();
        storeEventsAndUpdateLastLogbook(comServerDAO, logBook, localLogBook);
    }

    private void storeEventsAndUpdateLastLogbook(ComServerDAO comServerDAO, LogBook logBook, LocalLogBook localLogBook) {
        if (localLogBook.endDeviceEvents.size() > 0) {
            MeterReadingImpl meterReading = new MeterReadingImpl();
            meterReading.addAllEndDeviceEvents(localLogBook.endDeviceEvents);
            Device device = logBook.getDevice();
            comServerDAO.storeMeterReadings(new DeviceIdentifierById((int) device.getId()), meterReading);
            LogBook.LogBookUpdater logBookUpdaterFor = device.getLogBookUpdaterFor(logBook);
            logBookUpdaterFor.setLastLogBookIfLater(localLogBook.lastLogbook);
            logBookUpdaterFor.update();
        }
    }

    private LocalLogBook filterFutureDatesAndCalculateLastReading() {
        List<EndDeviceEvent> filteredEndDeviceEvents = new ArrayList<>();
        Date lastLogbook = null;
        Date currentDate = Clocks.getAppServerClock().now();
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

    private class LocalLogBook extends Pair<List<EndDeviceEvent>, Date> {

        private final List<EndDeviceEvent> endDeviceEvents;
        private final Date lastLogbook;

        public LocalLogBook(List<EndDeviceEvent> endDeviceEvents, Date lastLogBook) {
            super(endDeviceEvents, lastLogBook);
            this.endDeviceEvents = endDeviceEvents;
            this.lastLogbook = lastLogBook;
        }
    }
}