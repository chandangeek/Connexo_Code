package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;

import java.util.function.Consumer;

/**
 * Provides functionality to update the FirmwareVersion(s) of a Device
 */
public class CollectedFirmwareVersionDeviceCommand extends DeviceCommandImpl {

    private final CollectedFirmwareVersion collectedFirmwareVersions;
    private final ComTaskExecution comTaskExecution;

    public CollectedFirmwareVersionDeviceCommand(ServiceProvider serviceProvider, CollectedFirmwareVersion collectedFirmwareVersions, ComTaskExecution comTaskExecution) {
        super(comTaskExecution, serviceProvider);
        this.collectedFirmwareVersions = collectedFirmwareVersions;
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.updateFirmwareVersions(collectedFirmwareVersions);
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        collectedFirmwareVersions.getActiveMeterFirmwareVersion().ifPresent(addBuilderProperty(builder, "active meter firmware version"));
        collectedFirmwareVersions.getPassiveMeterFirmwareVersion().ifPresent(addBuilderProperty(builder, "passive meter firmware version"));
        collectedFirmwareVersions.getActiveCommunicationFirmwareVersion().ifPresent(addBuilderProperty(builder, "active communication firmware version"));
        collectedFirmwareVersions.getPassiveCommunicationFirmwareVersion().ifPresent(addBuilderProperty(builder, "passive communication firmware version"));
    }

    private Consumer<String> addBuilderProperty(DescriptionBuilder builder, String propertyName) {
        return fwVersion -> builder.addProperty(propertyName).append(fwVersion);
    }

    @Override
    public String getDescriptionTitle() {
        return "Collected firmware version";
    }
}
