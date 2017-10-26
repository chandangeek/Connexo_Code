/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.device.config.DeviceSecurityAccessorType;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfoFactory;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.KeyAccessorStatus;
import com.energyict.mdc.device.data.rest.impl.SecurityAccessorInfo;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.pluggable.rest.PropertyValuesResourceProvider;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.PrivilegePresence.WITHOUT_PRIVILEGES;
import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.PrivilegePresence.WITH_PRIVILEGES;
import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.ValueVisibility.HIDE_VALUES;
import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.ValueVisibility.SHOW_VALUES;

public class SecurityAccessorInfoFactory {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Thesaurus thesaurus;
    private final UserService userService;
    private final ExecutionLevelInfoFactory executionLevelInfoFactory;

    @Inject
    public SecurityAccessorInfoFactory(MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus, UserService userService, ExecutionLevelInfoFactory executionLevelInfoFactory) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.thesaurus = thesaurus;
        this.userService = userService;
        this.executionLevelInfoFactory = executionLevelInfoFactory;
    }

    private SecurityAccessorInfo from(SecurityAccessor<?> securityAccessor) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = securityAccessor.getKeyAccessorType().getId();
        info.name = securityAccessor.getKeyAccessorType().getName();
        info.description = securityAccessor.getKeyAccessorType().getDescription();
        info.swapped = securityAccessor.isSwapped();
        info.version = securityAccessor.getVersion();
        info.modificationDate = securityAccessor.getModTime();
        info.status = thesaurus.getFormat(securityAccessor.getStatus()).format();
        info.canGeneratePassiveKey = KeyAccessorStatus.COMPLETE.equals(securityAccessor.getStatus());
        info.hasTempValue = securityAccessor.getTempValue().isPresent();
        info.hasActualValue = securityAccessor.getActualValue().isPresent();
        securityAccessor.getActualValue().ifPresent(ka->ka.getExpirationTime().ifPresent(expiration -> info.expirationTime = expiration));

        return info;
    }

    public SecurityAccessorInfo asKey(SecurityAccessor<?> securityAccessor) {
        SecurityAccessorInfo info = from(securityAccessor);
        List<PropertySpec> propertySpecs = securityAccessor.getPropertySpecs();

        TypedProperties actualTypedProperties = getPropertiesActualValue(securityAccessor);
        boolean userHasViewPrivilege = ((DeviceSecurityAccessorType) securityAccessor.getKeyAccessorType()).currentUserIsAllowedToViewDeviceProperties();
        boolean userHasEditPrivilege = ((DeviceSecurityAccessorType) securityAccessor.getKeyAccessorType()).currentUserIsAllowedToEditDeviceProperties();

        MdcPropertyUtils.ValueVisibility valueVisibility = userHasViewPrivilege && userHasEditPrivilege? SHOW_VALUES: HIDE_VALUES;
        MdcPropertyUtils.PrivilegePresence withoutPrivileges = userHasViewPrivilege ? WITH_PRIVILEGES : WITHOUT_PRIVILEGES;
        info.currentProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, actualTypedProperties, valueVisibility, withoutPrivileges);

        TypedProperties tempTypedProperties = getPropertiesTempValue(securityAccessor);
        info.tempProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, tempTypedProperties, valueVisibility, withoutPrivileges);
        return info;
    }

    public SecurityAccessorInfo asKeyWithLevels(SecurityAccessor<?> securityAccessor, DeviceType deviceType) {
        SecurityAccessorInfo info = asKey(securityAccessor);

        List<Group> groups = userService.getGroups();
        Set<DeviceSecurityUserAction> userActions = deviceType.getSecurityAccessorTypeUserActions(securityAccessor.getKeyAccessorType());
        info.editLevels = executionLevelInfoFactory.getEditPrivileges(userActions, groups);
        info.viewLevels = executionLevelInfoFactory.getViewPrivileges(userActions, groups);

        return info;
    }

    public SecurityAccessorInfo asCertificate(SecurityAccessor<?> securityAccessor, PropertyValuesResourceProvider aliasTypeAheadPropertyValueProvider, PropertyDefaultValuesProvider trustStoreValuesProvider) {
        List<PropertySpec> propertySpecs = securityAccessor.getPropertySpecs();
        SecurityAccessorInfo info = from(securityAccessor);
        TypedProperties actualTypedProperties = getPropertiesActualValue(securityAccessor);

        info.currentProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, actualTypedProperties, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);
        Collections.sort(info.currentProperties, (PropertyInfo info1, PropertyInfo info2) -> info1.key.equals("trustStore") ? -1 : 0);

        TypedProperties tempTypedProperties = getPropertiesTempValue(securityAccessor);
        info.tempProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, tempTypedProperties, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);
        Collections.sort(info.tempProperties, (PropertyInfo info1, PropertyInfo info2) -> info1.key.equals("trustStore") ? -1 : 0);

        if (securityAccessor instanceof CertificateAccessor) {
            ((CertificateAccessor) securityAccessor).getActualValue().ifPresent(cw->cw.getLastReadDate().ifPresent(date -> info.lastReadDate = date));
        }

        return info;
    }

    private TypedProperties getPropertiesTempValue(SecurityAccessor<?> securityAccessor) {
        TypedProperties tempTypedProperties = TypedProperties.empty();
        securityAccessor.getTempValue().ifPresent(ka->ka.getProperties().entrySet().forEach(e->tempTypedProperties.setProperty(e.getKey(),e.getValue())));
        return tempTypedProperties;
    }

    private TypedProperties getPropertiesActualValue(SecurityAccessor<?> securityAccessor) {
        TypedProperties actualTypedProperties = TypedProperties.empty();
        securityAccessor.getActualValue().ifPresent(ka->ka.getProperties().entrySet().forEach(e1 -> actualTypedProperties.setProperty(e1.getKey(), e1.getValue())));
        return actualTypedProperties;
    }

}