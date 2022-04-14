/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MasterUtilitiesDeviceLocationNotificationCustomPropertySet implements CustomPropertySet<ServiceCall,
        MasterUtilitiesDeviceLocationNotificationDomainExtension> {
    public static final String MODEL_NAME = "UD7";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    @Inject
    public MasterUtilitiesDeviceLocationNotificationCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.MASTER_UTILITIES_DEVICE_LOCATION_NOTIFICATION_CPS).format();
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
    public PersistenceSupport<ServiceCall, MasterUtilitiesDeviceLocationNotificationDomainExtension> getPersistenceSupport() {
        return new CustomPropertyPersistenceSupport(thesaurus);
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
                        .named(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.REQUEST_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.BULK.javaName(), TranslationKeys.BULK)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.UUID.javaName(), TranslationKeys.UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.ATTEMPT_NUMBER.javaName(), TranslationKeys.ATTEMPT_NUMBER)
                        .describedAs(TranslationKeys.ATTEMPT_NUMBER)
                        .fromThesaurus(thesaurus)
                        .finish()

        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MasterUtilitiesDeviceLocationNotificationDomainExtension> {
        private final String TABLE_NAME = "SAP_UD7_MASTER_LN_SC_CPS";
        private final String FK = "FK_SAP_UD7_MASTER_LN_SC_CPS";

        private Thesaurus thesaurus;

        private CustomPropertyPersistenceSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return MODEL_NAME;
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MasterUtilitiesDeviceLocationNotificationDomainExtension> persistenceClass() {
            return MasterUtilitiesDeviceLocationNotificationDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.of(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                }
            });
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .add();
            table.column(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.UUID.databaseName())
                    .varChar()
                    .map(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.UUID.javaName())
                    .add();
            table.column(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.BULK.databaseName())
                    .bool()
                    .map(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.BULK.javaName())
                    .notNull()
                    .add();
            table.column(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.ATTEMPT_NUMBER.databaseName())
                    .number()
                    .map(MasterUtilitiesDeviceLocationNotificationDomainExtension.FieldNames.ATTEMPT_NUMBER.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
