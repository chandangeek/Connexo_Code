/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.data.DeviceDataServices;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author sva
 * @since 30/03/2016 - 15:43
 */
@Component(name = "com.energyict.servicecall.ami.CompletionOptionsCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + CompletionOptionsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class CompletionOptionsCustomPropertySet implements CustomPropertySet<ServiceCall, CompletionOptionsServiceCallDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "CompletionOptionsCustomPropertySet";

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;

    public CompletionOptionsCustomPropertySet() {
    }

    @Inject
    public CompletionOptionsCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getName() {
        return CompletionOptionsCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(CustomPropertySetsTranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, CompletionOptionsServiceCallDomainExtension> getPersistenceSupport() {
        return new MyPersistenceSupport();
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
                this.propertySpecService
                        .longSpec()
                        .named(CompletionOptionsServiceCallDomainExtension.FieldNames.DESTINATION_SPEC.javaName(), CustomPropertySetsTranslationKeys.DESTINATION_SPEC)
                        .describedAs(CustomPropertySetsTranslationKeys.DESTINATION_SPEC)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(CompletionOptionsServiceCallDomainExtension.FieldNames.DESTINATION_IDENTIFICATION.javaName(), CustomPropertySetsTranslationKeys.DESTINATION_IDENTIFICATION)
                        .describedAs(CustomPropertySetsTranslationKeys.DESTINATION_IDENTIFICATION)
                        .fromThesaurus(thesaurus)
                        .finish());
    }

    private class MyPersistenceSupport implements PersistenceSupport<ServiceCall, CompletionOptionsServiceCallDomainExtension> {
        private final String TABLE_NAME = "DDC_SCS_COMPLETION_OPTIONS";
        private final String FK = "FK_DDC_SCS_COMPLETION_OPTIONS";

        @Override
        public String componentName() {
            return "COP";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return CompletionOptionsServiceCallDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<CompletionOptionsServiceCallDomainExtension> persistenceClass() {
            return CompletionOptionsServiceCallDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table
                    .column(CompletionOptionsServiceCallDomainExtension.FieldNames.DESTINATION_SPEC.databaseName())
                    .varChar()
                    .map(CompletionOptionsServiceCallDomainExtension.FieldNames.DESTINATION_SPEC.javaName())
                    .add();
            table
                    .column(CompletionOptionsServiceCallDomainExtension.FieldNames.DESTINATION_IDENTIFICATION.databaseName())
                    .varChar()
                    .map(CompletionOptionsServiceCallDomainExtension.FieldNames.DESTINATION_IDENTIFICATION.javaName())
                    .add();
        }

        @Override
        public String application() {
            return "MultiSense";
        }
    }
}