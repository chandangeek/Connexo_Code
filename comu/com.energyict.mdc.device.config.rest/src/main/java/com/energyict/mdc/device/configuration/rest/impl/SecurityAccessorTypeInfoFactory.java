/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorTypePurposeTranslation;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfoFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.upl.TypedProperties;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SecurityAccessorTypeInfoFactory {
    private final ExecutionLevelInfoFactory executionLevelInfoFactory;
    private final UserService userService;
    private final Thesaurus thesaurus;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public SecurityAccessorTypeInfoFactory(ExecutionLevelInfoFactory executionLevelInfoFactory, UserService userService, Thesaurus thesaurus, MdcPropertyUtils mdcPropertyUtils) {
        this.executionLevelInfoFactory = executionLevelInfoFactory;
        this.userService = userService;
        this.thesaurus = thesaurus;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public SecurityAccessorTypeInfo from(SecurityAccessorType securityAccessorType) {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = securityAccessorType.getId();
        info.version = securityAccessorType.getVersion();
        info.name = securityAccessorType.getName();
        info.description = securityAccessorType.getDescription();
        info.keyPurpose = new KeyPurposeInfo(securityAccessorType.getKeyPurpose());
        info.keyType = new KeyTypeInfo(securityAccessorType.getKeyType());
        info.storageMethod = securityAccessorType.getKeyEncryptionMethod();
        info.trustStoreId = !info.keyType.isKey && securityAccessorType.getTrustStore().isPresent() ? securityAccessorType
                .getTrustStore().get().getId() : 0;
        info.purpose = purposeToInfo(securityAccessorType.getPurpose());
        if (securityAccessorType.getKeyType().getCryptographicType().requiresDuration() && securityAccessorType.getDuration().isPresent()) {
            info.duration = new TimeDurationInfo(securityAccessorType.getDuration().get(), thesaurus);
        }

        if (securityAccessorType.keyTypeIsHSM()) {
            info.label = securityAccessorType.getHsmKeyType().getLabel();
            info.hsmJssKeyType = securityAccessorType.getHsmKeyType().getHsmJssKeyType();
            info.importCapability =  securityAccessorType.getHsmKeyType().getImportCapability();
            info.renewCapability = securityAccessorType.getHsmKeyType().getRenewCapability();
            info.keySize =  securityAccessorType.getHsmKeyType().getKeySize();
            info.isReversible = securityAccessorType.getHsmKeyType().isReversible();
        }
        info.isWrapper = securityAccessorType.isWrapper();

        return info;
    }

    public SecurityAccessorTypeInfo withSecurityLevels(SecurityAccessorType securityAccessorType) {
        SecurityAccessorTypeInfo info = from(securityAccessorType);
        Set<SecurityAccessorUserAction> allUserActions = EnumSet.allOf(SecurityAccessorUserAction.class);
        List<Group> groups = userService.getGroups();
        Set<SecurityAccessorUserAction> keyAccessorTypeUserActions = securityAccessorType.getUserActions();
        info.editLevels = executionLevelInfoFactory.getEditPrivileges(keyAccessorTypeUserActions, groups);
        info.defaultEditLevels = executionLevelInfoFactory.getEditPrivileges(allUserActions, groups);
        info.viewLevels = executionLevelInfoFactory.getViewPrivileges(keyAccessorTypeUserActions, groups);
        info.defaultViewLevels = executionLevelInfoFactory.getViewPrivileges(allUserActions, groups);
        return info;
    }

    public SecurityAccessorTypeInfo from(SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDeviceType) {
        SecurityAccessorTypeInfo info = from(securityAccessorTypeOnDeviceType.getSecurityAccessorType());
        info.keyRenewalCommandSpecification = new IdWithNameInfo(DeviceMessageSpecInfo.NOT_SET_ID, getNotSetName());
        securityAccessorTypeOnDeviceType.getKeyRenewalDeviceMessageSpecification().ifPresent(
                deviceMessageSpec -> {
                    info.keyRenewalCommandSpecification = new IdWithNameInfo(deviceMessageSpec.getId().name(), deviceMessageSpec.getName());
                }
        );
        info.serviceKeyRenewalCommandSpecification = new IdWithNameInfo(DeviceMessageSpecInfo.NOT_SET_ID, getNotSetName());
        securityAccessorTypeOnDeviceType.getServiceKeyRenewalDeviceMessageSpecification().ifPresent(
                deviceMessageSpec -> {
                    info.serviceKeyRenewalCommandSpecification = new IdWithNameInfo(deviceMessageSpec.getId().name(), deviceMessageSpec.getName());
                }
        );

        Optional<SecurityAccessorType> wrappingSecurityAccessor = securityAccessorTypeOnDeviceType.getDeviceSecurityAccessorType().getWrappingSecurityAccessor();

        if (wrappingSecurityAccessor.isPresent()) {
            SecurityAccessorType securityAccessorType = wrappingSecurityAccessor.get();
            info.wrapperIdAndName = new IdWithNameInfo(securityAccessorType.getId(), securityAccessorType.getName());
        } else {
            info.wrapperIdAndName = new IdWithNameInfo(getNotSetSecurityAccessorWrapper().id, getNotSetSecurityAccessorWrapper().name);
        }


        TypedProperties typedProperties = TypedProperties.empty();
        Collection<PropertySpec> propertySpecs = new ArrayList<>();
        securityAccessorTypeOnDeviceType
                .getKeyRenewalAttributes()
                .stream()
                .forEach(attribute-> {
                    typedProperties.setProperty(attribute.getName(), attribute.getValue());
                    propertySpecs.add(attribute.getSpecification());
                });
        if (propertySpecs.size() > 0) {
            info.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, typedProperties);
        }
        TypedProperties serviceTypedProperties = TypedProperties.empty();
        propertySpecs.clear();
        securityAccessorTypeOnDeviceType
                .getServiceKeyRenewalAttributes()
                .stream()
                .forEach(attribute-> {
                    serviceTypedProperties.setProperty(attribute.getName(), attribute.getValue());
                    propertySpecs.add(attribute.getSpecification());
                });
        if (propertySpecs.size() > 0) {
            info.serviceProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, serviceTypedProperties);
        }
        return info;
    }

    public SecurityAccessorTypeInfo withSecurityLevels(SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDeviceType) {
        SecurityAccessorTypeInfo info = from(securityAccessorTypeOnDeviceType);
        Set<SecurityAccessorUserAction> allUserActions = EnumSet.allOf(SecurityAccessorUserAction.class);
        List<Group> groups = userService.getGroups();
        Set<SecurityAccessorUserAction> keyAccessorTypeUserActions = securityAccessorTypeOnDeviceType.getDeviceSecurityAccessorType().getSecurityAccessor().getUserActions();
        info.editLevels = executionLevelInfoFactory.getEditPrivileges(keyAccessorTypeUserActions, groups);
        info.defaultEditLevels = executionLevelInfoFactory.getEditPrivileges(allUserActions, groups);
        info.viewLevels = executionLevelInfoFactory.getViewPrivileges(keyAccessorTypeUserActions, groups);
        info.defaultViewLevels = executionLevelInfoFactory.getViewPrivileges(allUserActions, groups);
        return info;
    }

    public IdWithNameInfo purposeToInfo(SecurityAccessorType.Purpose purpose) {
        return new IdWithNameInfo(purpose.name(), SecurityAccessorTypePurposeTranslation.translate(purpose, thesaurus));
    }

    public SecurityAccessorType.Purpose purposeFromInfo(IdWithNameInfo info) {
        return SecurityAccessorType.Purpose.valueOf(info.id.toString());
    }

    public SecurityAccessorTypeInfo getNotSetSecurityAccessorWrapper() {
        return SecurityAccessorTypeInfo.getNotAvailable(getNotSetName());
    }

    private String getNotSetName() {
        return thesaurus.getString(MessageSeeds.SECACC_WRAPPER_NOT_SET.getKey(), MessageSeeds.SECACC_WRAPPER_NOT_SET.getDefaultFormat());
    }

}
