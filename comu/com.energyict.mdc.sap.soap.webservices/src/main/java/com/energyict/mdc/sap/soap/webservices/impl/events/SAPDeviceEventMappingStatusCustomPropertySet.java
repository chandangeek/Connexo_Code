/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.events;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SAPDeviceEventMappingStatusCustomPropertySet implements CustomPropertySet<ServiceCall, SAPDeviceEventMappingStatusDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_ID = SAPDeviceEventMappingStatusCustomPropertySet.class.getName();

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    @Inject
    public SAPDeviceEventMappingStatusCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return CUSTOM_PROPERTY_SET_ID;
    }

    @Override
    public String getName() {
        return TranslationKeys.SAP_EVENT_MAPPING_STATUS_CPS.translate(thesaurus);
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return thesaurus.getFormat(SAPDeviceEventMappingStatusDomainExtension.FieldNames.SERVICE_CALL).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, SAPDeviceEventMappingStatusDomainExtension> getPersistenceSupport() {
        return new SAPDeviceEventMappingStatusPersistenceSupport();
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
                propertySpecService.stringSpec()
                        .named(SAPDeviceEventMappingStatusDomainExtension.FieldNames.PATH)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService.stringSpec()
                        .named(SAPDeviceEventMappingStatusDomainExtension.FieldNames.SEPARATOR)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService.longSpec()
                        .named(SAPDeviceEventMappingStatusDomainExtension.FieldNames.LOADED_ENTRIES_NUMBER)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.longSpec()
                        .named(SAPDeviceEventMappingStatusDomainExtension.FieldNames.FAILED_ENTRIES_NUMBER)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.longSpec()
                        .named(SAPDeviceEventMappingStatusDomainExtension.FieldNames.SKIPPED_ENTRIES_NUMBER)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }
}
