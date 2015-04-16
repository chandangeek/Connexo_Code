package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides functionality to update the FirmwareVersion(s) of a Device
 */
public class CollectedFirmwareVersionDeviceCommand extends DeviceCommandImpl {

    private final DeviceIdentifier<?> deviceDeviceIdentifier;
    private final Optional<String> activeMeterFirmwareVersion;
    private final Optional<String> passiveMeterFirmwareVersion;
    private final Optional<String> activeCommunicationFirmwareVersion;
    private final Optional<String> passiveCommunicationFirmwareVersion;

    public CollectedFirmwareVersionDeviceCommand(ServiceProvider serviceProvider, DeviceIdentifier<?> deviceDeviceIdentifier, Optional<String> activeMeterFirmwareVersion, Optional<String> passiveMeterFirmwareVersion, Optional<String> activeCommunicationFirmwareVersion, Optional<String> passiveCommunicationFirmwareVersion) {
        super(serviceProvider);
        this.deviceDeviceIdentifier = deviceDeviceIdentifier;
        this.activeMeterFirmwareVersion = activeMeterFirmwareVersion;
        this.passiveMeterFirmwareVersion = passiveMeterFirmwareVersion;
        this.activeCommunicationFirmwareVersion = activeCommunicationFirmwareVersion;
        this.passiveCommunicationFirmwareVersion = passiveCommunicationFirmwareVersion;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        // TODO
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        activeMeterFirmwareVersion.ifPresent(addBuilderProperty(builder, "active meter firmware version"));
        passiveMeterFirmwareVersion.ifPresent(addBuilderProperty(builder, "passive meter firmware version"));
        activeCommunicationFirmwareVersion.ifPresent(addBuilderProperty(builder, "active communication firmware version"));
        passiveCommunicationFirmwareVersion.ifPresent(addBuilderProperty(builder, "passive communication firmware version"));
    }

    private Consumer<String> addBuilderProperty(DescriptionBuilder builder, String propertyName) {
        return fwVersion -> builder.addProperty(propertyName).append(fwVersion);
    }

    @Override
    public String getDescriptionTitle() {
        return "Collected firmware version";
    }
}
