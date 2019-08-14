package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.PropertyValueType;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceDialectProperty;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.tasks.CompletionCode;

import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will update a protocol dialect property of a {@link Device}
 * from information that was collected during the device communication session.
 *
 * @author khe
 * @since 2017-01-23 (09:36)
 */
public class UpdateDeviceDialectProperty extends DeviceCommandImpl {
    private final DeviceIdentifier deviceIdentifier;
    private final String propertyName;
    private final Object propertyValue;

    public UpdateDeviceDialectProperty(DeviceDialectProperty deviceDialectProperty, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.deviceIdentifier = deviceDialectProperty.getDeviceIdentifier();
        this.propertyName = deviceDialectProperty.getPropertyName();
        this.propertyValue = deviceDialectProperty.getPropertyValue();
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        Optional<com.energyict.mdc.protocol.api.device.offline.OfflineDevice> offlineDevice = comServerDAO.findOfflineDevice(deviceIdentifier, new DeviceOfflineFlags());

        if (offlineDevice.isPresent()) {
            PropertyValueType valueType = comServerDAO.getDeviceProtocolPropertyValueType(deviceIdentifier, propertyName);
            if (valueType.equals(PropertyValueType.UNSUPPORTED)) {
                addIssueToExecutionLogger(
                        CompletionCode.ConfigurationError,
                        this.getIssueService().newProblem(this, MessageSeeds.UNSUPPORTED_PROTOCOL_PROPERTY_TYPE, deviceIdentifier, propertyValue.getClass().getSimpleName())
                );
                return;
            }

            try {
                Object convertedPropertyValue = valueType.convertValue(propertyValue);
                comServerDAO.updateDeviceDialectProperty(deviceIdentifier, propertyName, convertedPropertyValue);
            } catch (ClassCastException e) {
                addIssueToExecutionLogger(
                        CompletionCode.ConfigurationError,
                        this.getIssueService().newProblem(this, MessageSeeds.INCOMPATIBLE_PROTOCOL_PROPERTY_VALUE, deviceIdentifier, propertyValue.getClass().getSimpleName(), valueType.getValueTypeClass().getSimpleName())
                );
                return;
            }
        } else {
            addIssueToExecutionLogger(
                    CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(this, MessageSeeds.UNKNOWN_DEVICE, deviceIdentifier)
            );
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Update device dialect property";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.deviceIdentifier);
            builder.addProperty("property").append(propertyName);
            builder.addProperty("value").append(propertyValue.toString());
        }
    }

}