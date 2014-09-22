package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

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

    public CollectedLogBookDeviceCommand(DeviceLogBook deviceLogBook, MeterDataStoreCommand meterDataStoreCommand) {
        super();
        this.deviceLogBook = deviceLogBook;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        this.comServerDAO = comServerDAO;
        PreStoreLogBook logBookPreStorer = new PreStoreLogBook(getClock(), comServerDAO);
        PreStoreLogBook.LocalLogBook localLogBook = logBookPreStorer.preStore(this.deviceLogBook);
        updateMeterDataStorer(localLogBook);
    }

    private void updateMeterDataStorer(final PreStoreLogBook.LocalLogBook localLogBook) {
        if (localLogBook.getEndDeviceEvents().size() > 0) {
            DeviceIdentifier<Device> deviceIdentifier = this.comServerDAO.getDeviceIdentifierFor(this.deviceLogBook.getLogBookIdentifier());
            this.meterDataStoreCommand.addEventReadings(deviceIdentifier, localLogBook.getEndDeviceEvents());
            this.meterDataStoreCommand.addLastLogBookUpdater(this.deviceLogBook.getLogBookIdentifier(), localLogBook.getLastLogbook());
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
}