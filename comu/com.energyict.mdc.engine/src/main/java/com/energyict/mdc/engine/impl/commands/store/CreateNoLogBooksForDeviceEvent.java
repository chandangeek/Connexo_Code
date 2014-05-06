package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.meterdata.NoLogBooksForDevice;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdw.core.NoLogBooksForDeviceEvent;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will create an {@link com.energyict.mdw.core.NoLogBooksForDeviceEvent}.
 *
 * @author sva
 * @since 14/12/12 - 9:15
 */

public class CreateNoLogBooksForDeviceEvent extends DeviceCommandImpl {

    private final DeviceIdentifier deviceIdentifier;

    public CreateNoLogBooksForDeviceEvent(NoLogBooksForDevice collectedDeviceData, IssueService issueService) {
        super(issueService);
        deviceIdentifier = collectedDeviceData.getDeviceIdentifier();
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        NoLogBooksForDeviceEvent event = new NoLogBooksForDeviceEvent(comServerDAO.getThisComServer(), deviceIdentifier);
        comServerDAO.signalEvent(event);
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

}
