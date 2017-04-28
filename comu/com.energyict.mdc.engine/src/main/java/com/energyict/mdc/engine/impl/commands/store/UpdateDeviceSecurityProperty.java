package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceSecurityProperty;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will update a security property of a {@link com.energyict.mdc.device.data.Device}
 * from information that was collected during the device communication session.
 * <p/>
 * This is for example the case when a message was sent to agree a new encryption key with a meter.
 *
 * @author khe
 * @since 2017-01-23 (08:44)
 */
public class UpdateDeviceSecurityProperty extends DeviceCommandImpl {

    private final DeviceIdentifier deviceIdentifier;
    private final String propertyName;
    private final Object propertyValue;

    public UpdateDeviceSecurityProperty(DeviceSecurityProperty securityProperty, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.deviceIdentifier = securityProperty.getDeviceIdentifier();
        this.propertyName = securityProperty.getPropertyName();
        this.propertyValue = securityProperty.getPropertyValue();
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.updateDeviceSecurityProperty(deviceIdentifier, propertyName, propertyValue);
    }

    @Override
    public String getDescriptionTitle() {
        return "Update device security property";
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