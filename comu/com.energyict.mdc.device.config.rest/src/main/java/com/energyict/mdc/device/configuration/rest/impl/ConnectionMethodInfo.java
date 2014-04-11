package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyInfo;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriInfo;

/**
 * This Info element represents the PartialConnectionTask in the domain model
 */
public class ConnectionMethodInfo {
    public long id;
    public String direction;
    public String connectionType;
    public List<PropertyInfo> propertyInfos;

    public ConnectionMethodInfo() {
    }

    public static ConnectionMethodInfo from(PartialConnectionTask partialConnectionTask, UriInfo uriInfo) {
        ConnectionMethodInfo connectionMethodInfo = new ConnectionMethodInfo();
        connectionMethodInfo.id=partialConnectionTask.getId();
        connectionMethodInfo.direction=determineDirection(partialConnectionTask);
        connectionMethodInfo.connectionType= Joiner.on(',').join(partialConnectionTask.getConnectionType().getSupportedComPortTypes());
        List<PropertySpec> propertySpecs = partialConnectionTask.getConnectionType().getPropertySpecs();
        TypedProperties typedProperties = partialConnectionTask.getTypedProperties();
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        MdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, propertyInfoList);
        connectionMethodInfo.propertyInfos= propertyInfoList;
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
