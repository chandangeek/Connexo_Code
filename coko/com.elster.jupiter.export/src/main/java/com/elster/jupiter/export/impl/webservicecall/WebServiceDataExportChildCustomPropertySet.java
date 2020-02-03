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

public class WebServiceDataExportChildCustomPropertySet implements CustomPropertySet<ServiceCall, WebServiceDataExportChildDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_CHILD_ID = "com.elster.jupiter.export.impl.webservicecall.WebServiceDataExportChildCustomPropertySet";

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    @Inject
    public WebServiceDataExportChildCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return CUSTOM_PROPERTY_SET_CHILD_ID;
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
    public PersistenceSupport<ServiceCall, WebServiceDataExportChildDomainExtension> getPersistenceSupport() {
        return new WebServiceDataExportChildPersistentSupport();
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
                        .named(WebServiceDataExportChildDomainExtension.FieldNames.DEVICE_NAME.javaName(), TranslationKeys.DEVICE_NAME)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(WebServiceDataExportChildDomainExtension.FieldNames.READING_TYPE_MRID.javaName(), TranslationKeys.READING_TYPE_MRID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService
                        .longSpec()
                        .named(WebServiceDataExportChildDomainExtension.FieldNames.DATA_SOURCE_ID.javaName(), TranslationKeys.DATA_SOURCE_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(WebServiceDataExportChildDomainExtension.FieldNames.CUSTOM_INFO.javaName(), TranslationKeys.CUSTOM_INFO)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }
}
