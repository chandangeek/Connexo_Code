/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceKeyAccessorType;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfoFactory;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.KeyAccessor;
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

    private SecurityAccessorInfo from(KeyAccessor<?> keyAccessor) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = keyAccessor.getKeyAccessorType().getId();
        info.name = keyAccessor.getKeyAccessorType().getName();
        info.description = keyAccessor.getKeyAccessorType().getDescription();
        info.swapped = keyAccessor.isSwapped();
        info.version = keyAccessor.getVersion();
        info.modificationDate = keyAccessor.getModTime();
        info.status = thesaurus.getFormat(keyAccessor.getStatus()).format();
        info.canGeneratePassiveKey = KeyAccessorStatus.COMPLETE.equals(keyAccessor.getStatus());
        info.hasTempValue = keyAccessor.getTempValue().isPresent();
        info.hasActualValue = keyAccessor.getActualValue().isPresent();
        keyAccessor.getActualValue().ifPresent(ka->ka.getExpirationTime().ifPresent(expiration -> info.expirationTime = expiration));

        return info;
    }

    public SecurityAccessorInfo asKey(KeyAccessor<?> keyAccessor) {
        SecurityAccessorInfo info = from(keyAccessor);
        List<PropertySpec> propertySpecs = keyAccessor.getPropertySpecs();

        TypedProperties actualTypedProperties = getPropertiesActualValue(keyAccessor);
        boolean userHasViewPrivilege = ((DeviceKeyAccessorType) keyAccessor.getKeyAccessorType()).currentUserIsAllowedToViewDeviceProperties();
        boolean userHasEditPrivilege = ((DeviceKeyAccessorType) keyAccessor.getKeyAccessorType()).currentUserIsAllowedToEditDeviceProperties();

        MdcPropertyUtils.ValueVisibility valueVisibility = userHasViewPrivilege && userHasEditPrivilege? SHOW_VALUES: HIDE_VALUES;
        MdcPropertyUtils.PrivilegePresence withoutPrivileges = userHasViewPrivilege ? WITH_PRIVILEGES : WITHOUT_PRIVILEGES;
        info.currentProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, actualTypedProperties, valueVisibility, withoutPrivileges);

        TypedProperties tempTypedProperties = getPropertiesTempValue(keyAccessor);
        info.tempProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, tempTypedProperties, valueVisibility, withoutPrivileges);
        return info;
    }

    public SecurityAccessorInfo asKeyWithLevels(KeyAccessor<?> keyAccessor, DeviceType deviceType) {
        SecurityAccessorInfo info = asKey(keyAccessor);

        List<Group> groups = userService.getGroups();
        Set<DeviceSecurityUserAction> userActions = deviceType.getKeyAccessorTypeUserActions(keyAccessor.getKeyAccessorType());
        info.editLevels = executionLevelInfoFactory.getEditPrivileges(userActions, groups);
        info.viewLevels = executionLevelInfoFactory.getViewPrivileges(userActions, groups);

        return info;
    }

    public SecurityAccessorInfo asCertificate(KeyAccessor<?> keyAccessor, PropertyValuesResourceProvider aliasTypeAheadPropertyValueProvider, PropertyDefaultValuesProvider trustStoreValuesProvider) {
        List<PropertySpec> propertySpecs = keyAccessor.getPropertySpecs();
        SecurityAccessorInfo info = from(keyAccessor);
        TypedProperties actualTypedProperties = getPropertiesActualValue(keyAccessor);

        info.currentProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, actualTypedProperties, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);
        Collections.sort(info.currentProperties, (PropertyInfo info1, PropertyInfo info2) -> info1.key.equals("trustStore") ? -1 : 0);

        TypedProperties tempTypedProperties = getPropertiesTempValue(keyAccessor);
        info.tempProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, tempTypedProperties, aliasTypeAheadPropertyValueProvider, trustStoreValuesProvider);
        Collections.sort(info.tempProperties, (PropertyInfo info1, PropertyInfo info2) -> info1.key.equals("trustStore") ? -1 : 0);

        if (keyAccessor instanceof CertificateAccessor) {
            ((CertificateAccessor)keyAccessor).getActualValue().ifPresent(cw->cw.getLastReadDate().ifPresent(date -> info.lastReadDate = date));
        }

        return info;
    }

    private TypedProperties getPropertiesTempValue(KeyAccessor<?> keyAccessor) {
        TypedProperties tempTypedProperties = TypedProperties.empty();
        keyAccessor.getTempValue().ifPresent(ka->ka.getProperties().entrySet().forEach(e->tempTypedProperties.setProperty(e.getKey(),e.getValue())));
        return tempTypedProperties;
    }

    private TypedProperties getPropertiesActualValue(KeyAccessor<?> keyAccessor) {
        TypedProperties actualTypedProperties = TypedProperties.empty();
        keyAccessor.getActualValue().ifPresent(ka->ka.getProperties().entrySet().forEach(e1 -> actualTypedProperties.setProperty(e1.getKey(), e1.getValue())));
        return actualTypedProperties;
    }

}