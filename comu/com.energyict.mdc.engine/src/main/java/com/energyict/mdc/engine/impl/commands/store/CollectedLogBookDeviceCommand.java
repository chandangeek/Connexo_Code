package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

/**
 * Provides functionality to store {@link com.energyict.mdc.protocol.api.device.BaseLogBook} data into the system
 *
 * @author sva
 * @since 10/12/12 - 11:13
 */
public class CollectedLogBookDeviceCommand extends DeviceCommandImpl {

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
        Pair<DeviceIdentifier<Device>, PreStoreLogBook.LocalLogBook> localLogBook = logBookPreStorer.preStore(this.deviceLogBook);
        updateMeterDataStorer(localLogBook);
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

    @Override
    public String getDescriptionTitle() {
        return "Collected logbook data";
    }

}