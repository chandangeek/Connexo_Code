/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
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

public class MasterUtilitiesDeviceCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, MasterUtilitiesDeviceCreateRequestDomainExtension> {
    public static final String MODEL_NAME = "UD5";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MasterUtilitiesDeviceCreateRequestCustomPropertySet() {
    }

    @Inject
    public MasterUtilitiesDeviceCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.MASTER_UTILITIES_DEVICE_CREATE_REQUEST_CPS).format();
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
    public PersistenceSupport<ServiceCall, MasterUtilitiesDeviceCreateRequestDomainExtension> getPersistenceSupport() {
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
                        .named(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.REQUEST_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.BULK.javaName(), TranslationKeys.BULK)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.UUID.javaName(), TranslationKeys.UUID)
                        .fromThesaurus(thesaurus)
                        .finish()

        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MasterUtilitiesDeviceCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_UD5_MASTER_CR_SC_CPS";
        private final String FK = "FK_SAP_UD5_MASTER_CR_SC_CPS";

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
            return MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MasterUtilitiesDeviceCreateRequestDomainExtension> persistenceClass() {
            return MasterUtilitiesDeviceCreateRequestDomainExtension.class;
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
            table.column(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .add();
            table.column(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.UUID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.UUID.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            table.column(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.BULK.databaseName())
                    .bool()
                    .map(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.BULK.javaName())
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
