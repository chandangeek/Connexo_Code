/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import com.energyict.mdc.device.data.rest.impl.SecurityPropertySetInfo;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.upl.TypedProperties;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.PrivilegePresence.WITH_PRIVILEGES;
import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.ValueVisibility.SHOW_VALUES;
import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 9/30/14.
 */
public class SecurityPropertySetInfoFactory {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Thesaurus thesaurus;

    @Inject
    public SecurityPropertySetInfoFactory(MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.thesaurus = thesaurus;
    }

    public List<SecurityPropertySetInfo> asSecuritySetsInfo(Device device, UriInfo uriInfo) {
        return device.getDeviceConfiguration()
                .getSecurityPropertySets()
                .stream()
                .map(s -> asSecuritySetsInfo(device, uriInfo, s))
                .sorted((p1, p2) -> p1.name.compareToIgnoreCase(p2.name))
                .collect(toList());
    }

    public List<SecurityPropertySetInfo> asHsmRelatedSecuritySetsInfo(Device device, UriInfo uriInfo) {
        return device.getDeviceConfiguration()
                .getSecurityPropertySets().stream()
                .filter(s -> s.getConfigurationSecurityProperties().stream().anyMatch(sp ->sp.getSecurityAccessorType().keyTypeIsHSM()))
                .map(s -> asSecuritySetsInfo(device, uriInfo, s))
                .sorted((p1, p2) -> p1.name.compareToIgnoreCase(p2.name))
                .collect(toList());
    }

    public SecurityPropertySetInfo asSecuritySetsInfo(Device device, UriInfo uriInfo, SecurityPropertySet securityPropertySet) {
        SecurityPropertySetInfo info = new SecurityPropertySetInfo();
        info.id = securityPropertySet.getId();
        info.name = securityPropertySet.getName();
        info.authenticationLevel = SecurityLevelInfo.from(securityPropertySet.getAuthenticationDeviceAccessLevel());
        info.encryptionLevel = SecurityLevelInfo.from(securityPropertySet.getEncryptionDeviceAccessLevel());
        info.client = getClientAsPropertyInfo(securityPropertySet);
        info.securitySuite = SecurityLevelInfo.from(securityPropertySet.getSecuritySuite());
        info.requestSecurityLevel = SecurityLevelInfo.from(securityPropertySet.getRequestSecurityLevel());
        info.responseSecurityLevel = SecurityLevelInfo.from(securityPropertySet.getResponseSecurityLevel());

        TypedProperties typedProperties = this.toTypedProperties(securityPropertySet.getConfigurationSecurityProperties());
        info.properties = new ArrayList<>();
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, securityPropertySet.getPropertySpecs(), typedProperties, info.properties, SHOW_VALUES, WITH_PRIVILEGES);

        // Sort the properties by their (translated) name
        info.properties.sort(Comparator.comparing(propertyInfo -> propertyInfo.name));

        info.version = securityPropertySet.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        info.hasServiceKeys = isSetHasServiceKeys(device, securityPropertySet);
        return info;
    }

    private PropertyInfo getClientAsPropertyInfo(SecurityPropertySet securityPropertySet) {
        if (securityPropertySet.getClientSecurityPropertySpec().isPresent()) {
            return mdcPropertyUtils.convertPropertySpecToPropertyInfo(securityPropertySet.getClientSecurityPropertySpec().get(), securityPropertySet.getClient());
        } else {
            return null;
        }
    }

    private TypedProperties toTypedProperties(List<ConfigurationSecurityProperty> securityProperties) {
        TypedProperties typedProperties = TypedProperties.empty();
        for (ConfigurationSecurityProperty securityProperty : securityProperties) {
            typedProperties.setProperty(securityProperty.getName(), securityProperty.getSecurityAccessorType());
        }
        return typedProperties;
    }

    public boolean isSetHasServiceKeys(Device device, SecurityPropertySet securityPropertySet) {
        return device.getSecurityAccessors().stream()
                .filter(keyAccessor ->
                        securityPropertySet.getConfigurationSecurityProperties().stream().anyMatch(configurationSecurityProperty ->
                                configurationSecurityProperty.getSecurityAccessorType().getId() == keyAccessor.getSecurityAccessorType().getId()))
                .anyMatch(s -> s.isServiceKey());
    }
}
