/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class ProtocolDialectInfo {

    public long id;
    public String name;
    public boolean availableForUse;
    public List<PropertyInfo> properties;
    public long version;
    public VersionInfo<Long> parent;

    public ProtocolDialectInfo() {
    }

    public static ProtocolDialectInfo from(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        ProtocolDialectInfo protocolDialectInfo = new ProtocolDialectInfo();
        protocolDialectInfo.id = protocolDialectConfigurationProperties.getId();
        protocolDialectInfo.name = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDisplayName();
        protocolDialectInfo.availableForUse = true;

        List<PropertySpec> propertySpecs = protocolDialectConfigurationProperties.getPropertySpecs();
        TypedProperties typedProperties = protocolDialectConfigurationProperties.getTypedProperties();
        protocolDialectInfo.properties = new ArrayList();
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, protocolDialectInfo.properties);
        protocolDialectInfo.version = protocolDialectConfigurationProperties.getVersion();
        DeviceConfiguration deviceConfiguration = protocolDialectConfigurationProperties.getDeviceConfiguration();
        protocolDialectInfo.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());

        return protocolDialectInfo;
    }

    public static List<ProtocolDialectInfo> from(List<ProtocolDialectConfigurationProperties> protocolDialectConfigurationPropertiesList, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        List<ProtocolDialectInfo> protocolDialectInfos = new ArrayList<>(protocolDialectConfigurationPropertiesList.size());
        for (ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties : protocolDialectConfigurationPropertiesList) {
            protocolDialectInfos.add(ProtocolDialectInfo.from(protocolDialectConfigurationProperties, uriInfo, mdcPropertyUtils));
        }
        return protocolDialectInfos;
    }
}
