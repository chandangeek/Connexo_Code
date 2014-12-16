package com.energyict.mdc.engine.impl.commands.store;


import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolProperty;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

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

    public UpdateDeviceProtocolProperty(DeviceProtocolProperty deviceProtocolProperty, IssueService issueService) {
        super();
        this.issueService = issueService;
        this.deviceIdentifier = deviceProtocolProperty.getDeviceIdentifier();
        this.propertySpec = deviceProtocolProperty.getPropertySpec();
        this.propertyValue = deviceProtocolProperty.getPropertyValue();
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        if (comServerDAO.findDevice(deviceIdentifier) != null) {
            try {
                if (propertySpec.validateValueIgnoreRequired(propertyValue)) {
                    comServerDAO.updateDeviceProtocolProperty(deviceIdentifier, propertySpec.getName(), propertyValue);
                }
            } catch (InvalidValueException e) {
                //TODO this will probably don't log much ..
                issueService.newIssueCollector().addWarning(this, "invalidDeviceProtocolPropertyCollected", propertySpec);
            } catch (NotFoundException e) {
                   //Do nothing, move on. We can't update the property on a non-existing device.
            }
        } else {
            //TODO this will probably don't log much ..
            issueService.newIssueCollector().addWarning(this, "collectedDeviceProtocolPropertyForUnknownDevice", deviceIdentifier);
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
