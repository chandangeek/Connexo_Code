package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedLogBookEvent;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import com.elster.jupiter.util.Pair;

import java.util.Optional;

/**
 * Provides functionality to store {@link com.energyict.mdc.protocol.api.device.BaseLogBook} data into the system.
 *
 * @author sva
 * @since 10/12/12 - 11:13
 */
public class CollectedLogBookDeviceCommand extends DeviceCommandImpl<CollectedLogBookEvent> {

    private final static String DESCRIPTION_TITLE = "Collected logbook data";

    private final DeviceLogBook deviceLogBook;
    private final MeterDataStoreCommand meterDataStoreCommand;

    public CollectedLogBookDeviceCommand(DeviceLogBook deviceLogBook, ComTaskExecution comTaskExecution, MeterDataStoreCommand meterDataStoreCommand) {
        super(comTaskExecution, meterDataStoreCommand.getServiceProvider());
        this.deviceLogBook = deviceLogBook;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        PreStoreLogBook logBookPreStorer = new PreStoreLogBook(this.getClock(), comServerDAO);
        Optional<Pair<DeviceIdentifier<Device>, PreStoreLogBook.LocalLogBook>> localLogBook = logBookPreStorer.preStore(this.deviceLogBook);
        if (localLogBook.isPresent()) {
            updateMeterDataStorer(localLogBook.get());
        }
        else {
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(
                            this,
                            MessageSeeds.UNKNOWN_DEVICE_LOG_BOOK.getKey(),
                            this.deviceLogBook.getLogBookIdentifier()));
        }
    }

    private void updateMeterDataStorer(final Pair<DeviceIdentifier<Device>, PreStoreLogBook.LocalLogBook> localLogBook) {
        if (!localLogBook.getLast().getEndDeviceEvents().isEmpty()) {
            this.meterDataStoreCommand.addEventReadings(localLogBook.getFirst(), localLogBook.getLast().getEndDeviceEvents());
            this.meterDataStoreCommand.addLastLogBookUpdater(this.deviceLogBook.getLogBookIdentifier(), localLogBook.getLast().getLastLogbook());
        }
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

    protected Optional<CollectedLogBookEvent> newEvent(Issue issue) {
        CollectedLogBookEvent event  =  new CollectedLogBookEvent(new ComServerEventServiceProvider(), deviceLogBook);
        if (issue != null){
            event.setIssue(issue);
        }
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}