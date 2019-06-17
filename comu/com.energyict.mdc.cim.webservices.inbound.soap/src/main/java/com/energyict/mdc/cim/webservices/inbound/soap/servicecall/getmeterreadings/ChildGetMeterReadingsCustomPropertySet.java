/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

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
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
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

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.ChildGetMeterReadingsCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + ChildGetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class ChildGetMeterReadingsCustomPropertySet implements CustomPropertySet<ServiceCall, ChildGetMeterReadingsDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "ChildGetMeterReadingsCustomPropertySet";
    public static final String CUSTOM_PROPERTY_SET_ID = ChildGetMeterReadingsDomainExtension.class.getName();

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework
    public ChildGetMeterReadingsCustomPropertySet() {
    }

    @Inject
    public ChildGetMeterReadingsCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
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
    public String getId() {
        return CUSTOM_PROPERTY_SET_ID;
    }

    @Override
    public String getName() {
        return ChildGetMeterReadingsCustomPropertySet.class.getName();
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
    public PersistenceSupport<ServiceCall, ChildGetMeterReadingsDomainExtension> getPersistenceSupport() {
        return new ChildGetMeterReadingsCustomPropertyPersistenceSupport();
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
                        .named(ChildGetMeterReadingsDomainExtension.FieldNames.COMMUNICATION_TASK.javaName(), TranslationKeys.COMMUNICATION_TASK)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(ChildGetMeterReadingsDomainExtension.FieldNames.TRIGGER_DATE.javaName(), TranslationKeys.TRIGGER_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(ChildGetMeterReadingsDomainExtension.FieldNames.ACTUAL_START_DATE.javaName(), TranslationKeys.ACTUAL_START_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(ChildGetMeterReadingsDomainExtension.FieldNames.ACTUAL_END_DATE.javaName(), TranslationKeys.ACTUAL_END_DATE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class ChildGetMeterReadingsCustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, ChildGetMeterReadingsDomainExtension> {
        private final String TABLE_NAME = "GMR_METER_READINGS_CPS_GM3";
        private final String FK = "FK_GMR_MRSCCPS_GM3";

        @Override
        public String componentName() {
            return "GM3";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return ChildGetMeterReadingsDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<ChildGetMeterReadingsDomainExtension> persistenceClass() {
            return ChildGetMeterReadingsDomainExtension.class;
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
                    .column(ChildGetMeterReadingsDomainExtension.FieldNames.COMMUNICATION_TASK.databaseName())
                    .varChar()
                    .map(ChildGetMeterReadingsDomainExtension.FieldNames.COMMUNICATION_TASK.javaName())
                    .notNull()
                    .add();
            table.column(ChildGetMeterReadingsDomainExtension.FieldNames.TRIGGER_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(ChildGetMeterReadingsDomainExtension.FieldNames.TRIGGER_DATE.javaName())
                    .add();
            table.column(ChildGetMeterReadingsDomainExtension.FieldNames.ACTUAL_START_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(ChildGetMeterReadingsDomainExtension.FieldNames.ACTUAL_START_DATE.javaName())
                    .add();
            table.column(ChildGetMeterReadingsDomainExtension.FieldNames.ACTUAL_END_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(ChildGetMeterReadingsDomainExtension.FieldNames.ACTUAL_END_DATE.javaName())
                    .add();
        }

        @Override
        public String application() {
            return "MultiSense";
        }
    }
}
