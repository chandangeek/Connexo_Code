package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyInfo;
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

    public static ProtocolDialectInfo from(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, TypedProperties typedProperties, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        ProtocolDialectInfo protocolDialectInfo = new ProtocolDialectInfo();
        protocolDialectInfo.id = protocolDialectConfigurationProperties.getId();
        protocolDialectInfo.name = protocolDialectConfigurationProperties.getDeviceProtocolDialectName();
        protocolDialectInfo.availableForUse = true;

        List<PropertySpec> propertySpecs = protocolDialectConfigurationProperties.getPropertySpecs();
        protocolDialectInfo.properties = new ArrayList();
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, protocolDialectInfo.properties);

        return protocolDialectInfo;
    }

    public static List<ProtocolDialectInfo> from(List<ProtocolDialectConfigurationProperties> protocolDialectPropertiesList, TypedProperties typedProperties, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        List<ProtocolDialectInfo> protocolDialectInfos = new ArrayList<>(protocolDialectPropertiesList.size());
        for (ProtocolDialectConfigurationProperties protocolDialectProperties : protocolDialectPropertiesList) {
            protocolDialectInfos.add(ProtocolDialectInfo.from(protocolDialectProperties, typedProperties, uriInfo, mdcPropertyUtils));
        }
        return protocolDialectInfos;
    }
}
