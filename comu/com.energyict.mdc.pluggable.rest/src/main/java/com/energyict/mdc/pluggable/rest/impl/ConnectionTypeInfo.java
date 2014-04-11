package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyInfo;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriInfo;

/**
 * Copyrights EnergyICT
 * Date: 3/04/14
 * Time: 13:40
 */
public class ConnectionTypeInfo {
    public long id;
    public String name;
    public List<PropertyInfo> propertyInfos;

    public ConnectionTypeInfo() {
    }

    public static ConnectionTypeInfo from(long id, String name) {
        ConnectionTypeInfo connectionTypeInfo = new ConnectionTypeInfo();
        connectionTypeInfo.id = id;
        connectionTypeInfo.name = name;
        return connectionTypeInfo;
    }

    public static ConnectionTypeInfo from(ConnectionTypePluggableClass connectionTypePluggableClass, UriInfo uriInfo) {
        ConnectionTypeInfo connectionTypeInfo = new ConnectionTypeInfo();
        connectionTypeInfo.id = connectionTypePluggableClass.getId();
        connectionTypeInfo.name = connectionTypePluggableClass.getName();
        connectionTypeInfo.propertyInfos= new ArrayList<>();
        List<PropertySpec> propertySpecs = connectionTypePluggableClass.getPropertySpecs();
        TypedProperties typedProperties = connectionTypePluggableClass.getProperties(propertySpecs);
        MdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, connectionTypeInfo.propertyInfos);
        return connectionTypeInfo;
    }
}
