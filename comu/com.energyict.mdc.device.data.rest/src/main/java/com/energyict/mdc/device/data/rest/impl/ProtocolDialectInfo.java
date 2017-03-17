/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProtocolDialectInfo {

    public long id;
    public String name;
    public String displayName;
    public boolean availableForUse;
    public List<PropertyInfo> properties;
    public DeviceInfo device;

    public ProtocolDialectInfo() {
    }

    public static ProtocolDialectInfo from(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, Optional<ProtocolDialectProperties> protocolDialectProperties, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils, Device device) {
        ProtocolDialectInfo protocolDialectInfo = new ProtocolDialectInfo();
        protocolDialectInfo.id = protocolDialectConfigurationProperties.getId();
        protocolDialectInfo.name = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDeviceProtocolDialectName();
        protocolDialectInfo.displayName = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDisplayName();
        protocolDialectInfo.availableForUse = true;

        List<PropertySpec> propertySpecs = protocolDialectConfigurationProperties.getPropertySpecs();
        protocolDialectInfo.properties = new ArrayList<>();
        if (protocolDialectProperties.isPresent()) {
            mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, protocolDialectProperties.get().getTypedProperties(), protocolDialectInfo.properties);
        } else {
            mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, TypedProperties.inheritingFrom(protocolDialectConfigurationProperties.getTypedProperties()), protocolDialectInfo.properties);
        }

        protocolDialectInfo.device = DeviceInfo.from(device);
        return protocolDialectInfo;
    }

    public static List<ProtocolDialectInfo> from(Device device, List<ProtocolDialectConfigurationProperties> protocolDialectPropertiesList, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        List<ProtocolDialectInfo> protocolDialectInfos = new ArrayList<>(protocolDialectPropertiesList.size());
        for (ProtocolDialectConfigurationProperties protocolDialectProperties : protocolDialectPropertiesList) {
            Optional<ProtocolDialectProperties> properties = device.getProtocolDialectProperties(protocolDialectProperties.getDeviceProtocolDialectName());
            protocolDialectInfos.add(ProtocolDialectInfo.from(protocolDialectProperties, properties, uriInfo, mdcPropertyUtils, device));
        }
        return protocolDialectInfos;
    }

}