/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.tou.campaign.impl.TranslationKeys;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseItemPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + TimeOfUseItemPropertySet.CUSTOM_PROPERTY_SET_NAME, immediate = true)
public class TimeOfUseItemPropertySet implements CustomPropertySet<ServiceCall, TimeOfUseItemDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "TimeOfUseItemPropertySet";

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;

    public TimeOfUseItemPropertySet() {
    }

    @Inject
    public TimeOfUseItemPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(TimeOfUseCampaignServiceImpl.COMPONENT_NAME, Layer.SOAP);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setServiceCallService(ServiceCallService serviceCallService) {
        // required for proper startup; do not delete
    }

    @Override
    public String getName() {
        return TimeOfUseItemPropertySet.class.getSimpleName();
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
        return new TimeOfUseItemPersistenceSupport();
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
                        .stringSpec()
                        .named(TimeOfUseItemDomainExtension.FieldNames.DEVICE_NAME.javaName(), TranslationKeys.DEVICE_NAME)
                        .describedAs(TranslationKeys.DEVICE_NAME)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }
}