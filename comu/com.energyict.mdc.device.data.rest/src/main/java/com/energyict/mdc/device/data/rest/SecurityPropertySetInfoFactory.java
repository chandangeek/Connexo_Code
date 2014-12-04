package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import com.energyict.mdc.device.data.rest.impl.SecurityPropertySetInfo;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

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

    public List<SecurityPropertySetInfo> asInfo(Device device, UriInfo uriInfo) {
        return device.getDeviceConfiguration().getSecurityPropertySets().stream().map(s -> asInfo(device, uriInfo, s))
                .sorted((p1, p2) -> p1.name.compareToIgnoreCase(p2.name))
                .collect(toList());
    }

    public SecurityPropertySetInfo asInfo(Device device, UriInfo uriInfo, SecurityPropertySet securityPropertySet) {
        SecurityPropertySetInfo securityPropertySetInfo = new SecurityPropertySetInfo();
        securityPropertySetInfo.id = securityPropertySet.getId();
        securityPropertySetInfo.name = securityPropertySet.getName();
        securityPropertySetInfo.authenticationLevel = SecurityLevelInfo.from(securityPropertySet.getAuthenticationDeviceAccessLevel(), thesaurus);
        securityPropertySetInfo.encryptionLevel = SecurityLevelInfo.from(securityPropertySet.getEncryptionDeviceAccessLevel(), thesaurus);

        securityPropertySetInfo.userHasViewPrivilege = securityPropertySet.currentUserIsAllowedToViewDeviceProperties();
        securityPropertySetInfo.userHasEditPrivilege = securityPropertySet.currentUserIsAllowedToEditDeviceProperties();

        TypedProperties typedProperties = getTypedPropertiesForSecurityPropertySet(device, securityPropertySet);

        securityPropertySetInfo.properties = new ArrayList<>();
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, securityPropertySet.getPropertySpecs(), typedProperties, securityPropertySetInfo.properties, securityPropertySetInfo.userHasViewPrivilege && securityPropertySetInfo.userHasEditPrivilege, true);

        securityPropertySetInfo.status = new IdWithNameInfo();
        if (!getStatus(device, securityPropertySet, typedProperties)) {
            securityPropertySetInfo.status.id = CompletionState.INCOMPLETE;
            securityPropertySetInfo.status.name = thesaurus.getString(MessageSeeds.INCOMPLETE.getKey(), MessageSeeds.INCOMPLETE.getDefaultFormat());
        } else {
            securityPropertySetInfo.status.id = CompletionState.COMPLETE;
            securityPropertySetInfo.status.name = thesaurus.getString(MessageSeeds.COMPLETE.getKey(), MessageSeeds.COMPLETE.getDefaultFormat());
        }
        if (!securityPropertySetInfo.userHasViewPrivilege) {
            securityPropertySetInfo.properties.stream().forEach(p -> p.propertyValueInfo = new PropertyValueInfo<>(p.propertyValueInfo.propertyHasValue));
            if (!securityPropertySetInfo.userHasEditPrivilege) {
                securityPropertySetInfo.properties.stream().forEach(p -> p.propertyTypeInfo = new PropertyTypeInfo());
            }
        }
        return securityPropertySetInfo;
    }

    private boolean getStatus(Device device, SecurityPropertySet securityPropertySet,TypedProperties typedProperties) {
        if (device.getAllSecurityProperties(securityPropertySet).isEmpty()) {
            if (typedProperties == TypedProperties.empty()) {
                return true;
            } else {
                return false;
            }
        } else {
            for (SecurityProperty securityProperty : device.getAllSecurityProperties(securityPropertySet)) {
                if (!securityProperty.isComplete()) {
                    return false;
                }
            }
        }
        return true;
    }

    private TypedProperties getTypedPropertiesForSecurityPropertySet(Device device, SecurityPropertySet securityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        for (SecurityProperty securityProperty : device.getAllSecurityProperties(securityPropertySet)) {
            typedProperties.setProperty(securityProperty.getName(), securityProperty.getValue());
        }
        return typedProperties;
    }

    static enum CompletionState {
        COMPLETE(MessageSeeds.COMPLETE), INCOMPLETE(MessageSeeds.INCOMPLETE);
        private final MessageSeeds seed;

        CompletionState(MessageSeeds seed) {
            this.seed = seed;
        }
    }
}


