/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement;

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

import com.google.inject.Module;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MasterUtilitiesDeviceMeterChangeRequestCustomPropertySet implements CustomPropertySet<ServiceCall, MasterUtilitiesDeviceMeterChangeRequestDomainExtension> {
    public static final String MODEL_NAME = "LR6";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MasterUtilitiesDeviceMeterChangeRequestCustomPropertySet() {
    }

    @Inject
    public MasterUtilitiesDeviceMeterChangeRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.MASTER_UTILITIES_DEVICE_METER_CHANGE_CPS).format();
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
    public PersistenceSupport<ServiceCall, MasterUtilitiesDeviceMeterChangeRequestDomainExtension> getPersistenceSupport() {
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
                        .named(MasterUtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.REQUEST_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MasterUtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.UUID.javaName(), TranslationKeys.UUID)
                        .fromThesaurus(thesaurus)
                        .finish()

        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MasterUtilitiesDeviceMeterChangeRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_LR6_MASTER_CR_SC_CPS";
        private final String FK = "FK_SAP_LR6_MASTER_CR_SC_CPS";

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
            return MasterUtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MasterUtilitiesDeviceMeterChangeRequestDomainExtension> persistenceClass() {
            return MasterUtilitiesDeviceMeterChangeRequestDomainExtension.class;
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
            table.column(MasterUtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(MasterUtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .add();
            table.column(MasterUtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.UUID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MasterUtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.UUID.javaName())
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
