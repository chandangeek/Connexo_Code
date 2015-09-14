package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolProperty;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will update a protocol property of a Device
 * from information that was collected during the device communication session.
 *
 * @author sva
 * @since 16/10/2014 - 16:19
 */
public class UpdateDeviceProtocolProperty extends DeviceCommandImpl {

    private final DeviceIdentifier deviceIdentifier;
    private final PropertySpec propertySpec;
    private final Object propertyValue;

    public UpdateDeviceProtocolProperty(DeviceProtocolProperty deviceProtocolProperty, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.deviceIdentifier = deviceProtocolProperty.getDeviceIdentifier();
        this.propertySpec = deviceProtocolProperty.getPropertySpec();
        this.propertyValue = deviceProtocolProperty.getPropertyValue();
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        try {
            if (comServerDAO.findOfflineDevice(deviceIdentifier, new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG)).isPresent()) {
                try {
                    if (propertySpec.validateValueIgnoreRequired(propertyValue)) {
                        comServerDAO.updateDeviceProtocolProperty(deviceIdentifier, propertySpec.getName(), propertyValue);
                    }
                } catch (InvalidValueException | DeviceProtocolPropertyException e) {
                    this.addIssue(
                            CompletionCode.ConfigurationWarning,
                            this.getIssueService().newWarning(this, MessageSeeds.PROPERTY_VALIDATION_FAILED.getKey(), propertySpec.getName(), propertyValue));
                }
            }
        } catch (CanNotFindForIdentifier e) {
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(deviceIdentifier, e.getMessageSeed().getKey(), deviceIdentifier));
        }
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.deviceIdentifier);
            builder.addProperty("property").append(propertySpec.getName());
            builder.addProperty("value").append(propertySpec.getValueFactory().toStringValue(propertyValue));
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Update device protocol property";
    }

}