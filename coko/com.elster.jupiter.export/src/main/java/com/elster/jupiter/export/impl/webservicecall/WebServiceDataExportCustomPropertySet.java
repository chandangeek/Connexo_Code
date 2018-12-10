/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class WebServiceDataExportCustomPropertySet implements CustomPropertySet<ServiceCall, WebServiceDataExportDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_ID = "com.elster.jupiter.export.impl.webservicecall.WebServiceDataExportCustomPropertySet";

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    @Inject
    public WebServiceDataExportCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return WebServiceDataExportCustomPropertySet.CUSTOM_PROPERTY_SET_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.SERVICE_CALL_CPS_NAME).format();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return thesaurus.getFormat(TranslationKeys.SERVICE_CALL_CPS_DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, WebServiceDataExportDomainExtension> getPersistenceSupport() {
        return new WebServiceDataExportPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return false;
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
                        .named(WebServiceDataExportDomainExtension.FieldNames.UUID.javaName(), TranslationKeys.UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .longSpec()
                        .named(WebServiceDataExportDomainExtension.FieldNames.TIMEOUT.javaName(), TranslationKeys.TIMEOUT)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(WebServiceDataExportDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }
}
