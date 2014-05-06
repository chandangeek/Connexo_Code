package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.comserver.commands.CompositeDeviceCommand;
import com.energyict.comserver.commands.DeviceCommand;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;

import java.util.List;

/**
 * Provides factory services for {@link DeviceCommand}s
 * that are created from {@link com.energyict.mdc.protocol.api.device.data.CollectedData}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-22 (16:35)
 */
public interface DeviceCommandFactory {

    /**
     * Creates a {@link CompositeDeviceCommand} for all
     * the specified {@link ServerCollectedData}.
     *
     *
     *
     * @param collectedData The ServerCollectedData
     * @param communicationLogLevel
     * @param issueService
     * @return The CompositeDeviceCommand
     */
    public CompositeDeviceCommand newCompositeForAll(List<ServerCollectedData> collectedData, ComServer.LogLevel communicationLogLevel, IssueService issueService);

    /**
     * Creates a {@link DeviceCommand} for every {@link ServerCollectedData}.
     *
     *
     * @param collectedData The List of ServerCollectedData
     * @param issueService
     * @return The List of DeviceCommands
     */
    public List<DeviceCommand> newForAll(List<ServerCollectedData> collectedData, IssueService issueService);

}