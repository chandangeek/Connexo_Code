package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceIpAddress;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will update the ip address of a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}
 * from information that was collected during the device communication session.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:48)
 */
public class UpdateDeviceIpAddress extends DeviceCommandImpl {

    private DeviceIdentifier deviceIdentifier;
    private String ipAddress;
    private ConnectionTask connectionTask;
    private String connectionTaskPropertyName;

    public UpdateDeviceIpAddress(DeviceIpAddress ipAddressProperties, IssueService issueService, Clock clock) {
        super();
        this.deviceIdentifier = ipAddressProperties.getDeviceIdentifier();
        this.ipAddress = ipAddressProperties.getIpAddress();
        this.connectionTask = ipAddressProperties.getConnectionTask();
        this.connectionTaskPropertyName = ipAddressProperties.getConnectionTaskPropertyName();
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.updateIpAddress(this.ipAddress, this.connectionTask, this.connectionTaskPropertyName);
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.deviceIdentifier);
            builder.addProperty("IP address").append(this.ipAddress);
        }
    }
}