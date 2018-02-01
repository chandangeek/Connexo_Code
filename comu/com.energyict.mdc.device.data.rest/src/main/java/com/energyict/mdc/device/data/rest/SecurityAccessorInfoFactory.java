/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfoFactory;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfo;
import com.energyict.mdc.device.data.KeyAccessorStatus;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.pluggable.rest.PropertyValuesResourceProvider;
import com.energyict.mdc.upl.TypedProperties;

import javax.inject.Inject;
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
    private final com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory securityAccessorInfoFactory;

    @Inject
    public SecurityAccessorInfoFactory(MdcPropertyUtils mdcPropertyUtils,
                                       Thesaurus thesaurus,
                                       UserService userService,
                                       ExecutionLevelInfoFactory executionLevelInfoFactory,
                                       com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory securityAccessorInfoFactory) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.thesaurus = thesaurus;
        this.userService = userService;
        this.executionLevelInfoFactory = executionLevelInfoFactory;
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
    }

    private SecurityAccessorInfo from(SecurityAccessor<?> securityAccessor) {
        SecurityAccessorInfo info = securityAccessorInfoFactory.from(securityAccessor);
        info.status = thesaurus.getFormat(securityAccessor.getStatus()).format();
        info.canGeneratePassiveKey = KeyAccessorStatus.COMPLETE.equals(securityAccessor.getStatus());
        return info;
    }

    public SecurityAccessorInfo asKey(SecurityAccessor<?> securityAccessor) {
        SecurityAccessorInfo info = from(securityAccessor);
        List<PropertySpec> propertySpecs = securityAccessor.getPropertySpecs();

        TypedProperties actualTypedProperties = securityAccessorInfoFactory.getPropertiesActualValue(securityAccessor);
        boolean userHasViewPrivilege = securityAccessor.getKeyAccessorType().isCurrentUserAllowedToViewProperties("MDC");
        boolean userHasEditPrivilege = securityAccessor.getKeyAccessorType().isCurrentUserAllowedToEditProperties("MDC");

        MdcPropertyUtils.ValueVisibility valueVisibility = userHasViewPrivilege && userHasEditPrivilege? SHOW_VALUES: HIDE_VALUES;
        MdcPropertyUtils.PrivilegePresence withoutPrivileges = userHasViewPrivilege ? WITH_PRIVILEGES : WITHOUT_PRIVILEGES;
        info.currentProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, actualTypedProperties, valueVisibility, withoutPrivileges);

        TypedProperties tempTypedProperties = securityAccessorInfoFactory.getPropertiesTempValue(securityAccessor);
        info.tempProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, tempTypedProperties, valueVisibility, withoutPrivileges);
        return info;
    }

    public SecurityAccessorInfo asKeyWithLevels(SecurityAccessor<?> securityAccessor) {
        SecurityAccessorInfo info = asKey(securityAccessor);

        List<Group> groups = userService.getGroups();
        Set<SecurityAccessorUserAction> userActions = securityAccessor.getKeyAccessorType().getUserActions();
        info.editLevels = executionLevelInfoFactory.getEditPrivileges(userActions, groups);
        info.viewLevels = executionLevelInfoFactory.getViewPrivileges(userActions, groups);

        return info;
    }

    public SecurityAccessorInfo asCertificate(SecurityAccessor<?> securityAccessor,
                                              PropertyValuesResourceProvider aliasTypeAheadPropertyResourceProvider,
                                              PropertyDefaultValuesProvider trustStoreValuesProvider) {
        SecurityAccessorInfo info = securityAccessorInfoFactory.asCertificate(securityAccessor, aliasTypeAheadPropertyResourceProvider, trustStoreValuesProvider);
        info.status = thesaurus.getFormat(securityAccessor.getStatus()).format();
        info.canGeneratePassiveKey = KeyAccessorStatus.COMPLETE.equals(securityAccessor.getStatus());
        info.editable = securityAccessor.isEditable();
        return info;
    }
}
