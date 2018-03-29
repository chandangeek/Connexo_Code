/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.GetEndDeviceEventsCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + GetEndDeviceEventsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class GetEndDeviceEventsCustomPropertySet implements CustomPropertySet<ServiceCall, GetEndDeviceEventsDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "GetEndDeviceEventsCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public GetEndDeviceEventsCustomPropertySet() {
    }

    @Inject
    public GetEndDeviceEventsCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        customPropertySetService.addCustomPropertySet(this);
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
        return GetEndDeviceEventsCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, GetEndDeviceEventsDomainExtension> getPersistenceSupport() {
        return new CustomPropertyPersistenceSupport();
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
                        .stringSpec()
                        .named(GetEndDeviceEventsDomainExtension.FieldNames.METER.javaName(), TranslationKeys.METER_CONFIG)
                        .describedAs(TranslationKeys.METER_CONFIG)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetEndDeviceEventsDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .describedAs(TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetEndDeviceEventsDomainExtension.FieldNames.FROM_DATE.javaName(), TranslationKeys.FROM_DATE)
                        .describedAs(TranslationKeys.FROM_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetEndDeviceEventsDomainExtension.FieldNames.TO_DATE.javaName(), TranslationKeys.TO_DATE)
                        .describedAs(TranslationKeys.TO_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetEndDeviceEventsDomainExtension.FieldNames.CALLBACK_URL.javaName(), TranslationKeys.CALL_BACK_URL)
                        .describedAs(TranslationKeys.CALL_BACK_URL)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, GetEndDeviceEventsDomainExtension> {
        private final String TABLE_NAME = "MCZ_SCS_CNT";
        private final String FK = "FK_MCZ_SCS_CNT";

        @Override
        public String componentName() {
            return "PKZ";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return GetEndDeviceEventsDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<GetEndDeviceEventsDomainExtension> persistenceClass() {
            return GetEndDeviceEventsDomainExtension.class;
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
            table.column(GetEndDeviceEventsDomainExtension.FieldNames.METER.databaseName())
                    .varChar()
                    .map(GetEndDeviceEventsDomainExtension.FieldNames.METER.javaName())
                    .notNull()
                    .add();
            table.column(GetEndDeviceEventsDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .varChar()
                    .map(GetEndDeviceEventsDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .add();
            table.column(GetEndDeviceEventsDomainExtension.FieldNames.FROM_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(GetEndDeviceEventsDomainExtension.FieldNames.FROM_DATE.javaName())
                    .add();
            table.column(GetEndDeviceEventsDomainExtension.FieldNames.TO_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(GetEndDeviceEventsDomainExtension.FieldNames.TO_DATE.javaName())
                    .add();
            table.column(GetEndDeviceEventsDomainExtension.FieldNames.CALLBACK_URL.databaseName())
                    .varChar()
                    .map(GetEndDeviceEventsDomainExtension.FieldNames.CALLBACK_URL.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return MeterConfigChecklist.APPLICATION_NAME;
        }
    }
}
