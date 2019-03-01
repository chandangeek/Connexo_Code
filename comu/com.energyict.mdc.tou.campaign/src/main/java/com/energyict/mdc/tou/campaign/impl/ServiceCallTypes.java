/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl;


import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignCustomPropertySet;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignDomainExtension;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignServiceCallHandler;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseItemDomainExtension;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseItemPropertySet;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseItemServiceCallHandler;

public enum ServiceCallTypes {

    TIME_OF_USE_CAMPAIGN(
            TimeOfUseCampaignServiceCallHandler.NAME,
            TimeOfUseCampaignServiceCallHandler.VERSION,
            TimeOfUseCampaignCustomPropertySet.class.getSimpleName(),
            TimeOfUseCampaignDomainExtension.class.getName()),
    TIME_OF_USE_CAMPAIGN_ITEM(
            TimeOfUseItemServiceCallHandler.NAME,
            TimeOfUseItemServiceCallHandler.VERSION,
            TimeOfUseItemPropertySet.class.getSimpleName(),
            TimeOfUseItemDomainExtension.class.getName()),
    ;

    private final String typeName;
    private final String typeVersion;
    private final String customPropertySetClass;
    private final String persistenceSupportClass;

    ServiceCallTypes(String typeName, String typeVersion, String customPropertySetClass, String persistenceSupportClass) {
        this.typeName = typeName;
        this.typeVersion = typeVersion;
        this.customPropertySetClass = customPropertySetClass;
        this.persistenceSupportClass = persistenceSupportClass;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeVersion() {
        return typeVersion;
    }

    public String getCustomPropertySetClass() {
        return customPropertySetClass;
    }

    public String getPersistenceSupportClass() {
        return persistenceSupportClass;
    }
}