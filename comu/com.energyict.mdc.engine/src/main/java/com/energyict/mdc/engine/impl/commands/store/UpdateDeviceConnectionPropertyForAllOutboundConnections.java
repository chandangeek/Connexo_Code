package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceConnectionProperty;
import com.energyict.mdc.upl.properties.TypedProperties;

/**
 * Ported from EIServer 9.1
 * Credits for this goes to Claudiu Isac
 */
public class UpdateDeviceConnectionPropertyForAllOutboundConnections extends UpdateDeviceConnectionProperty {

    public final static String DESCRIPTION_TITLE = "Update device connection property on all outbound connections";

    public UpdateDeviceConnectionPropertyForAllOutboundConnections(DeviceConnectionProperty connectionProperty, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(connectionProperty, comTaskExecution, serviceProvider);
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        for (ConnectionTask connTask : connectionTask.getDevice().getConnectionTasks()) {
            TypedProperties properties = connTask.getTypedProperties();
            if (properties.getProperty(connectionTaskPropertyName) != null) {
                comServerDAO.updateConnectionTaskProperty(this.propertyValue, connTask, this.connectionTaskPropertyName);
            }
        }
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }
}
