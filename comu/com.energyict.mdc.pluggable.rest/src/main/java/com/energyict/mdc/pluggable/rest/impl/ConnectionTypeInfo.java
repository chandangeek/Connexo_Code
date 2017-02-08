/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.KeyAccessorPropertySpecWithPossibleValues;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

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

    public static ConnectionTypeInfo from(ConnectionTypePluggableClass connectionTypePluggableClass, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils, Optional<DeviceConfiguration> deviceConfigurationOption) {
        ConnectionTypeInfo connectionTypeInfo = new ConnectionTypeInfo();
        connectionTypeInfo.id = connectionTypePluggableClass.getId();
        connectionTypeInfo.name = connectionTypePluggableClass.getName();
        connectionTypeInfo.properties = new ArrayList<>();
        List<PropertySpec> propertySpecs = connectionTypePluggableClass
                .getPropertySpecs().stream()
                .map(propertySpec -> {
                    return deviceConfigurationOption.map(deviceConfiguration1 ->
                                 KeyAccessorPropertySpecWithPossibleValues.addValuesIfApplicable(() -> deviceConfiguration1.getDeviceType().getKeyAccessorTypes(), propertySpec))
                            .orElse(propertySpec);
                })
                .collect(toList());
        TypedProperties typedProperties = connectionTypePluggableClass.getProperties(propertySpecs);
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, connectionTypeInfo.properties);
        return connectionTypeInfo;
    }
}
