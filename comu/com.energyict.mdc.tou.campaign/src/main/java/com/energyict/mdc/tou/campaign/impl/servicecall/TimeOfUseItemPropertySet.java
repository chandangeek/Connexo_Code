/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.tou.campaign.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TimeOfUseItemPropertySet implements CustomPropertySet<ServiceCall, TimeOfUseItemDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_ID = TimeOfUseItemDomainExtension.class.getName();

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile TimeOfUseCampaignServiceImpl timeOfUseCampaignService;

    @Inject
    public TimeOfUseItemPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService,
                                    CustomPropertySetService customPropertySetService,
                                    TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        customPropertySetService.addCustomPropertySet(this);
    }

    @Override
    public String getId() {
        return CUSTOM_PROPERTY_SET_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.TIME_OF_USE_ITEM_CPS).format();
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
    public PersistenceSupport<ServiceCall, TimeOfUseItemDomainExtension> getPersistenceSupport() {
        return new TimeOfUseItemPersistenceSupport(timeOfUseCampaignService);
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
                        .named(TimeOfUseItemDomainExtension.FieldNames.DEVICE.javaName(), TranslationKeys.DEVICE)
                        .describedAs(TranslationKeys.DEVICE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }
}