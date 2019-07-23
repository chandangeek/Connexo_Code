/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.servicecall.DefaultState;

import java.util.Optional;

public enum ServiceCallTypes {

    FIRMWARE_CAMPAIGN(
            FirmwareCampaignServiceCallHandler.NAME,
            FirmwareCampaignServiceCallHandler.VERSION,
            FirmwareCampaignServiceCallHandler.APPLICATION,
            FirmwareCampaignCustomPropertySet.class.getSimpleName(),
            FirmwareCampaignDomainExtension.class.getName(),
            null),
    FIRMWARE_CAMPAIGN_ITEM(
            FirmwareCampaignItemServiceCallHandler.NAME,
            FirmwareCampaignItemServiceCallHandler.VERSION,
            FirmwareCampaignItemServiceCallHandler.APPLICATION,
            FirmwareCampaignItemCustomPropertySet.class.getSimpleName(),
            FirmwareCampaignItemDomainExtension.class.getName(),
            FirmwareCampaignItemServiceCallHandler.RETRY_STATE),
    ;

    private final String typeName;
    private final String typeVersion;
    private final String reservedByApplication;
    private final String customPropertySetClass;
    private final String persistenceSupportClass;
    private final DefaultState retryState;

    ServiceCallTypes(String typeName, String typeVersion, String reservedByApplication, String customPropertySetClass, String persistenceSupportClass, DefaultState retryState) {
        this.typeName = typeName;
        this.typeVersion = typeVersion;
        this.reservedByApplication = reservedByApplication;
        this.customPropertySetClass = customPropertySetClass;
        this.persistenceSupportClass = persistenceSupportClass;
        this.retryState = retryState;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeVersion() {
        return typeVersion;
    }

    public Optional<String> getApplication() {
        return Optional.ofNullable(reservedByApplication);
    }

    public String getCustomPropertySetClass() {
        return customPropertySetClass;
    }

    public String getPersistenceSupportClass() {
        return persistenceSupportClass;
    }

    public DefaultState getRetryState() {
        return retryState;
    }
}