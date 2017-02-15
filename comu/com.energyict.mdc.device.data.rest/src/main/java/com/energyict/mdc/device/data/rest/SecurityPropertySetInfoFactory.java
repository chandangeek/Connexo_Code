/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.impl.DefaultTranslationKey;
import com.energyict.mdc.device.data.rest.impl.SecurityPropertySetInfo;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.PrivilegePresence.WITH_PRIVILEGES;
import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.ValueVisibility.HIDE_VALUES;
import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.ValueVisibility.SHOW_VALUES;
import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 9/30/14.
 */
public class SecurityPropertySetInfoFactory {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Thesaurus thesaurus;

    enum CompletionState {
        COMPLETE(DefaultTranslationKey.COMPLETE), INCOMPLETE(DefaultTranslationKey.INCOMPLETE);
        private final DefaultTranslationKey translationKey;

        CompletionState(DefaultTranslationKey translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslation(Thesaurus thesaurus) {
            return thesaurus.getFormat(this.translationKey).format();
        }
    }

    @Inject
    public SecurityPropertySetInfoFactory(MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.thesaurus = thesaurus;
    }

    public List<SecurityPropertySetInfo> asInfo(Device device, UriInfo uriInfo) {
        return device.getDeviceConfiguration()
                .getSecurityPropertySets()
                .stream()
                .map(s -> asInfo(device, uriInfo, s))
                .sorted((p1, p2) -> p1.name.compareToIgnoreCase(p2.name))
                .collect(toList());
    }

    public SecurityPropertySetInfo asInfo(Device device, UriInfo uriInfo, SecurityPropertySet securityPropertySet) {
        SecurityPropertySetInfo info = new SecurityPropertySetInfo();
        info.id = securityPropertySet.getId();
        info.name = securityPropertySet.getName();
        info.authenticationLevel = SecurityLevelInfo.from(securityPropertySet.getAuthenticationDeviceAccessLevel());
        info.encryptionLevel = SecurityLevelInfo.from(securityPropertySet.getEncryptionDeviceAccessLevel());

//        info.userHasViewPrivilege = securityPropertySet.currentUserIsAllowedToViewDeviceProperties();
//        info.userHasEditPrivilege = securityPropertySet.currentUserIsAllowedToEditDeviceProperties();

        List<SecurityProperty> securityProperties = device.getSecurityProperties(securityPropertySet);
        TypedProperties typedProperties = this.toTypedProperties(securityProperties);

        info.properties = new ArrayList<>();
        MdcPropertyUtils.ValueVisibility valueVisibility = info.userHasViewPrivilege && info.userHasEditPrivilege? SHOW_VALUES: HIDE_VALUES;
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, securityPropertySet.getPropertySpecs(), typedProperties, info.properties, valueVisibility, WITH_PRIVILEGES);

        info.status = new IdWithNameInfo();
        CompletionState status = getStatus(device, securityPropertySet);
        info.status.id = status;
        info.status.name = status.getTranslation(this.thesaurus);
//        if (!info.userHasViewPrivilege) {
//            info.properties.stream().forEach(p -> p.propertyValueInfo = new PropertyValueInfo<>(p.propertyValueInfo.propertyHasValue));
//            if (!info.userHasEditPrivilege) {
//                info.properties.stream().forEach(p -> p.propertyTypeInfo = new PropertyTypeInfo());
//            }
//        }
        info.version = securityPropertySet.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        return info;
    }

    private TypedProperties toTypedProperties(List<SecurityProperty> securityProperties) {
        TypedProperties typedProperties = TypedProperties.empty();
        for (SecurityProperty securityProperty : securityProperties) {
            typedProperties.setProperty(securityProperty.getName(), securityProperty.getValue());
        }
        return typedProperties;
    }

    private CompletionState getStatus(Device device, SecurityPropertySet securityPropertySet) {
        if (device.securityPropertiesAreValid(securityPropertySet)) {
            return CompletionState.COMPLETE;
        }
        else {
            return CompletionState.INCOMPLETE;
        }
    }

}