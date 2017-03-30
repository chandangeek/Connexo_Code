/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class ConnectionTypeInfo {
    public long id;
    public String name;
    public List<PropertyInfo> properties;

    public ConnectionTypeInfo() {
    }

    public static ConnectionTypeInfo from(long id, String name) {
        ConnectionTypeInfo connectionTypeInfo = new ConnectionTypeInfo();
        connectionTypeInfo.id = id;
        connectionTypeInfo.name = name;
        return connectionTypeInfo;
    }

    public static ConnectionTypeInfo from(ConnectionTypePluggableClass connectionTypePluggableClass, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        ConnectionTypeInfo connectionTypeInfo = new ConnectionTypeInfo();
        connectionTypeInfo.id = connectionTypePluggableClass.getId();
        connectionTypeInfo.name = connectionTypePluggableClass.getName();
        connectionTypeInfo.properties = new ArrayList<>();
        List<PropertySpec> propertySpecs = connectionTypePluggableClass.getPropertySpecs();
        TypedProperties typedProperties = connectionTypePluggableClass.getProperties(propertySpecs);
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, connectionTypeInfo.properties);
        return connectionTypeInfo;
    }
}
