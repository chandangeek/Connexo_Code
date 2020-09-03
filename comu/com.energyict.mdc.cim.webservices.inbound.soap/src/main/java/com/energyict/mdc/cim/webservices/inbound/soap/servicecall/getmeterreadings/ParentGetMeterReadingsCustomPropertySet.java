/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
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

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.ParentGetMeterReadingsCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + ParentGetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class ParentGetMeterReadingsCustomPropertySet implements CustomPropertySet<ServiceCall, ParentGetMeterReadingsDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "ParentGetMeterReadingsCustomPropertySet";
    public static final String CUSTOM_PROPERTY_SET_ID = ParentGetMeterReadingsDomainExtension.class.getName();

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework
    public ParentGetMeterReadingsCustomPropertySet() {
    }

    @Inject
    public ParentGetMeterReadingsCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.setPropertySpecService(propertySpecService);
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
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public String getId() {
        return CUSTOM_PROPERTY_SET_ID;
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(TranslationKeys.PGMR_CPS).format();
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
    public PersistenceSupport<ServiceCall, ParentGetMeterReadingsDomainExtension> getPersistenceSupport() {
        return new ParentGetMeterReadingsCustomPropertyPersistenceSupport();
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
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.SOURCE.javaName(), TranslationKeys.SOURCE)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.CALLBACK_URL.javaName(), TranslationKeys.CALL_BACK_URL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.CORRELATION_ID.javaName(), TranslationKeys.CORRELATION_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_START.javaName(), TranslationKeys.TIME_PERIOD_START)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_END.javaName(), TranslationKeys.TIME_PERIOD_END)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.READING_TYPES.javaName(), TranslationKeys.READING_TYPES)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.LOAD_PROFILES.javaName(), TranslationKeys.LOAD_PROFILES)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.REGISTER_GROUPS.javaName(), TranslationKeys.REGISTER_GROUP)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.SCHEDULE_STRATEGY.javaName(), TranslationKeys.SCHEDULE_STRATEGY)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.CONNECTION_METHOD.javaName(), TranslationKeys.CONNECTION_METHOD)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.RESPONSE_STATUS.javaName(), TranslationKeys.RESPONSE_STATUS)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private static class ParentGetMeterReadingsCustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, ParentGetMeterReadingsDomainExtension> {
        private static final String TABLE_NAME = "GMR_PARENT_CPS";
        private static final String FK = "FK_GMR_PARENT_CPS";

        @Override
        public String componentName() {
            return "GM1";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return ParentGetMeterReadingsDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<ParentGetMeterReadingsDomainExtension> persistenceClass() {
            return ParentGetMeterReadingsDomainExtension.class;
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
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.SOURCE.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.SOURCE.javaName())
                    .notNull()
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.CALLBACK_URL.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.CALLBACK_URL.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.CORRELATION_ID.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.CORRELATION_ID.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_START.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_START.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_END.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_END.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.READING_TYPES.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.READING_TYPES.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.LOAD_PROFILES.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.LOAD_PROFILES.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.REGISTER_GROUPS.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.REGISTER_GROUPS.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.SCHEDULE_STRATEGY.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.SCHEDULE_STRATEGY.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.CONNECTION_METHOD.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.CONNECTION_METHOD.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.RESPONSE_STATUS.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.RESPONSE_STATUS.javaName())
                    .since(Version.version(10, 7))
                    .add();
        }

        @Override
        public String application() {
            return "MultiSense";
        }
    }
}
