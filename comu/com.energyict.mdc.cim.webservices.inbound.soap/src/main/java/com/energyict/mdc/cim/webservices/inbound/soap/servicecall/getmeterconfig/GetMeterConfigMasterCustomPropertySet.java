/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig;

import com.elster.jupiter.cps.*;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterConfigChecklist;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.*;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.GetMeterConfigMasterCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + GetMeterConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class GetMeterConfigMasterCustomPropertySet implements CustomPropertySet<ServiceCall, GetMeterConfigMasterDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "GetMeterConfigMasterCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public GetMeterConfigMasterCustomPropertySet() {
    }

    @Inject
    public GetMeterConfigMasterCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setCustomPropertySetService(customPropertySetService);
        this.thesaurus = thesaurus;
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setServiceCallService(ServiceCallService serviceCallService) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public String getName() {
        return GetMeterConfigMasterCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, GetMeterConfigMasterDomainExtension> getPersistenceSupport() {
        return new GetMeterConfigMasterCustomPropertyPersistenceSupport();
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
                        .bigDecimalSpec()
                        .named(GetMeterConfigMasterDomainExtension.FieldNames.CALLS_SUCCESS.javaName(), TranslationKeys.CALLS_SUCCESS)
                        .describedAs(TranslationKeys.CALLS_SUCCESS)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(GetMeterConfigMasterDomainExtension.FieldNames.CALLS_FAILED.javaName(), TranslationKeys.CALLS_ERROR)
                        .describedAs(TranslationKeys.CALLS_ERROR)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(GetMeterConfigMasterDomainExtension.FieldNames.CALLS_EXPECTED.javaName(), TranslationKeys.CALLS_EXPECTED)
                        .describedAs(TranslationKeys.CALLS_EXPECTED)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterConfigMasterDomainExtension.FieldNames.CALLBACK_URL.javaName(), TranslationKeys.CALL_BACK_URL)
                        .describedAs(TranslationKeys.CALL_BACK_URL)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class GetMeterConfigMasterCustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, GetMeterConfigMasterDomainExtension> {
        private final String TABLE_NAME = "MCC_SCS_CNT";
        private final String FK = "FK_MCC_SCS_CNT";

        @Override
        public String componentName() {
            return "PKS";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return GetMeterConfigMasterDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<GetMeterConfigMasterDomainExtension> persistenceClass() {
            return GetMeterConfigMasterDomainExtension.class;
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
            table.column(GetMeterConfigMasterDomainExtension.FieldNames.CALLS_SUCCESS.databaseName())
                    .number()
                    .map(GetMeterConfigMasterDomainExtension.FieldNames.CALLS_SUCCESS.javaName())
                    .notNull()
                    .add();
            table.column(GetMeterConfigMasterDomainExtension.FieldNames.CALLS_FAILED.databaseName())
                    .number()
                    .map(GetMeterConfigMasterDomainExtension.FieldNames.CALLS_FAILED.javaName())
                    .notNull()
                    .add();
            table.column(GetMeterConfigMasterDomainExtension.FieldNames.CALLS_EXPECTED.databaseName())
                    .number()
                    .map(GetMeterConfigMasterDomainExtension.FieldNames.CALLS_EXPECTED.javaName())
                    .notNull()
                    .add();
            table.column(GetMeterConfigMasterDomainExtension.FieldNames.CALLBACK_URL.databaseName())
                    .varChar()
                    .map(GetMeterConfigMasterDomainExtension.FieldNames.CALLBACK_URL.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return MeterConfigChecklist.APPLICATION_NAME;
        }
    }
}
