package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceConnectionProperty;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.util.Map;
import java.util.stream.Collectors;

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
            Map<String, Object> filteredMap = connectionPropertyNameAndValue.entrySet().stream()
                    .filter(entry -> properties.getProperty(entry.getKey()) != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (!filteredMap.isEmpty()) {
                comServerDAO.updateConnectionTaskProperties(connTask, filteredMap);
            }
        }
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }
}
