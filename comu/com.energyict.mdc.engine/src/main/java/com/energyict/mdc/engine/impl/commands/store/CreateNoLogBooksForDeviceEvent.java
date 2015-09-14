package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.EventType;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.NoLogBooksForDeviceEvent;
import com.energyict.mdc.engine.impl.meterdata.NoLogBooksForDevice;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will create an NoLogBooksForDeviceEvent.
 *
 * @author sva
 * @since 14/12/12 - 9:15
 */

public class CreateNoLogBooksForDeviceEvent extends DeviceCommandImpl {

    private final DeviceIdentifier deviceIdentifier;

    public CreateNoLogBooksForDeviceEvent(NoLogBooksForDevice collectedDeviceData, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        deviceIdentifier = collectedDeviceData.getDeviceIdentifier();
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.signalEvent(EventType.NO_LOGBOOKS_FOR_DEVICE.topic(), new NoLogBooksForDeviceEvent(this.deviceIdentifier));
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.deviceIdentifier);
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "No logbooks for device event";
    }

}