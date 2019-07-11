/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

public enum ServiceCallTypes {

    FIRMWARE_CAMPAIGN(
            FirmwareCampaignServiceCallHandler.NAME,
            FirmwareCampaignServiceCallHandler.VERSION,
            FirmwareCampaignServiceCallHandler.APPLICATION,
            FirmwareCampaignCustomPropertySet.class.getSimpleName(),
            FirmwareCampaignDomainExtension.class.getName()),
    FIRMWARE_CAMPAIGN_ITEM(
            FirmwareCampaignItemServiceCallHandler.NAME,
            FirmwareCampaignItemServiceCallHandler.VERSION,
            FirmwareCampaignItemServiceCallHandler.APPLICATION,
            FirmwareCampaignItemCustomPropertySet.class.getSimpleName(),
            FirmwareCampaignItemDomainExtension.class.getName()),
    ;

    private final String typeName;
    private final String typeVersion;
    private final String reservedByApplication;
    private final String customPropertySetClass;
    private final String persistenceSupportClass;

    ServiceCallTypes(String typeName, String typeVersion, String reservedByApplication, String customPropertySetClass, String persistenceSupportClass) {
        this.typeName = typeName;
        this.typeVersion = typeVersion;
        this.reservedByApplication = reservedByApplication;
        this.customPropertySetClass = customPropertySetClass;
        this.persistenceSupportClass = persistenceSupportClass;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeVersion() {
        return typeVersion;
    }

    public String getReservedByApplication() {
        return reservedByApplication;
    }

    public String getCustomPropertySetClass() {
        return customPropertySetClass;
    }

    public String getPersistenceSupportClass() {
        return persistenceSupportClass;
    }
}