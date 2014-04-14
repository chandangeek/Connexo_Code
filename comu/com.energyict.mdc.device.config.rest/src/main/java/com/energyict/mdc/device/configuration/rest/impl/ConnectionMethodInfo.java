package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyInfo;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriInfo;

/**
 * This Info element represents the PartialConnectionTask in the domain model
 */
public class ConnectionMethodInfo {
    public long id;
    public String name;
    public String direction;
    public String connectionType;
    public String comPortPool;
    public boolean isDefault;
    public List<PropertyInfo> propertyInfos;

    public ConnectionMethodInfo() {
    }

    public static ConnectionMethodInfo from(PartialConnectionTask partialConnectionTask, UriInfo uriInfo) {
        ConnectionMethodInfo connectionMethodInfo = new ConnectionMethodInfo();
        connectionMethodInfo.id=partialConnectionTask.getId();
        connectionMethodInfo.name= partialConnectionTask.getName();
        connectionMethodInfo.direction=determineDirection(partialConnectionTask);
        connectionMethodInfo.connectionType= partialConnectionTask.getPluggableClass().getName();
        connectionMethodInfo.comPortPool= partialConnectionTask.getComPortPool().getName();
        connectionMethodInfo.isDefault= partialConnectionTask.isDefault();
        List<PropertySpec> propertySpecs = partialConnectionTask.getConnectionType().getPropertySpecs();
        TypedProperties typedProperties = partialConnectionTask.getTypedProperties();
        connectionMethodInfo.propertyInfos= new ArrayList<>();
        MdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, connectionMethodInfo.propertyInfos);
        return connectionMethodInfo;
    }

    private static String determineDirection(PartialConnectionTask partialConnectionTask) {
        if (partialConnectionTask instanceof PartialInboundConnectionTask) {
            return "Inbound";
        } else if (partialConnectionTask instanceof PartialOutboundConnectionTask) {
            return "Outbound";
        } else {
            return "Scheduled";
        }
    }
}
