package com.energyict.mdc.engine.impl.commands.store;


import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolProperty;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.impl.WarningImpl;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will update a protocol property of a {@link Device device}
 * from information that was collected during the device communication session.
 *
 * @author sva
 * @since 16/10/2014 - 16:19
 */
public class UpdateDeviceProtocolProperty extends DeviceCommandImpl {


    private final DeviceIdentifier deviceIdentifier;
    private final PropertySpec propertySpec;
    private final Object propertyValue;

    public UpdateDeviceProtocolProperty(DeviceProtocolProperty deviceProtocolProperty) {
        super();
        this.deviceIdentifier = deviceProtocolProperty.getDeviceIdentifier();
        this.propertySpec = deviceProtocolProperty.getPropertySpec();
        this.propertyValue = deviceProtocolProperty.getPropertyValue();
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        if (comServerDAO.findOfflineDevice(deviceIdentifier) != null) {
            try {
                if (propertySpec.validateValue(propertyValue, false)) {
                    comServerDAO.updateDeviceProtocolProperty(deviceIdentifier, propertySpec.getName(), propertyValue);
                }
            } catch (InvalidValueException e) {
                addIssueToExecutionLogger(comServerDAO, CompletionCode.ConfigurationWarning,
                        new WarningImpl(this, "invalidDeviceProtocolPropertyCollected", propertySpec)
                );
            } catch (NotFoundException e) {
                ;   //Do nothing, move on. We can't update the property on a non-existing device.
            }
        } else {
            addIssueToExecutionLogger(comServerDAO, CompletionCode.ConfigurationWarning,
                    new WarningImpl(this, "collectedDeviceProtocolPropertyForUnknownDevice", deviceIdentifier)
            );
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
}
