/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FirmwareCampaignItemCustomPropertySet implements CustomPropertySet<ServiceCall, FirmwareCampaignItemDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_ID = FirmwareCampaignItemDomainExtension.class.getName();

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    private final FirmwareServiceImpl firmwareService;

    @Inject
    public FirmwareCampaignItemCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService,
                                                 FirmwareServiceImpl firmwareService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.firmwareService = firmwareService;
    }

    @Override
    public String getId() {
        return CUSTOM_PROPERTY_SET_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.FIRMWARE_CAMPAIGN_ITEM_CPS).format();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return thesaurus.getFormat(TranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, FirmwareCampaignItemDomainExtension> getPersistenceSupport() {
        return new FirmwareCampaignItemPersistenceSupport(firmwareService);
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                propertySpecService
                        .referenceSpec(Device.class)
                        .named(FirmwareCampaignItemDomainExtension.FieldNames.DEVICE.javaName(), TranslationKeys.DEVICE)
                        .describedAs(TranslationKeys.DEVICE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }
}
