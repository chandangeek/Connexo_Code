package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

public class ProtocolDialectInfo {

    public long id;
    public String name;
    public boolean availableForUse;
    public List<PropertyInfo> properties;

    public ProtocolDialectInfo() {
    }

    public static ProtocolDialectInfo from(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, ProtocolDialectProperties protocolDialectProperties, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        ProtocolDialectInfo protocolDialectInfo = new ProtocolDialectInfo();
        protocolDialectInfo.id = protocolDialectConfigurationProperties.getId();
        protocolDialectInfo.name = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDisplayName();
        protocolDialectInfo.availableForUse = true;

        List<PropertySpec> propertySpecs = protocolDialectConfigurationProperties.getPropertySpecs();
        protocolDialectInfo.properties = new ArrayList<>();
        if (protocolDialectProperties != null) {
            mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, protocolDialectProperties.getTypedProperties(), protocolDialectInfo.properties);
        } else {
            mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, TypedProperties.inheritingFrom(protocolDialectConfigurationProperties.getTypedProperties()), protocolDialectInfo.properties);
        }

        return protocolDialectInfo;
    }

    public static List<ProtocolDialectInfo> from(Device device, List<ProtocolDialectConfigurationProperties> protocolDialectPropertiesList, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        List<ProtocolDialectInfo> protocolDialectInfos = new ArrayList<>(protocolDialectPropertiesList.size());
        for (ProtocolDialectConfigurationProperties protocolDialectProperties : protocolDialectPropertiesList) {
            ProtocolDialectProperties properties = device.getProtocolDialectProperties(protocolDialectProperties.getDeviceProtocolDialectName());
                protocolDialectInfos.add(ProtocolDialectInfo.from(protocolDialectProperties, properties, uriInfo, mdcPropertyUtils));
        }
        return protocolDialectInfos;
    }
}
