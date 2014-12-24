package com.energyict.mdc.engine.impl.commands.store;


import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolProperty;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;

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
    private final IssueService issueService;
    private final ComTaskExecution comTaskExecution;

    public UpdateDeviceProtocolProperty(DeviceProtocolProperty deviceProtocolProperty, IssueService issueService, ComTaskExecution comTaskExecution) {
        super();
        this.issueService = issueService;
        this.comTaskExecution = comTaskExecution;
        this.deviceIdentifier = deviceProtocolProperty.getDeviceIdentifier();
        this.propertySpec = deviceProtocolProperty.getPropertySpec();
        this.propertyValue = deviceProtocolProperty.getPropertyValue();
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        try {
            if (comServerDAO.findOfflineDevice(deviceIdentifier, new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG)) != null) {
                try {
                    if (propertySpec.validateValueIgnoreRequired(propertyValue)) {
                        comServerDAO.updateDeviceProtocolProperty(deviceIdentifier, propertySpec.getName(), propertyValue);
                    }
                } catch (InvalidValueException | DeviceProtocolPropertyException e) {
                    getExecutionLogger().addIssue(
                            CompletionCode.ConfigurationWarning,
                            getIssueService().newWarning(this, MessageSeeds.PROPERTY_VALIDATION_FAILED.getKey(), propertySpec.getName(), propertyValue), comTaskExecution);
                }
            }
        } catch (CanNotFindForIdentifier e) {
            getExecutionLogger().addIssue(
                    CompletionCode.ConfigurationWarning,
                    getIssueService().newWarning(deviceIdentifier, e.getMessageSeed().getKey()), comTaskExecution);
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
