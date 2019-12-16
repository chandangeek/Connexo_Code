/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection;

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

import com.google.inject.Module;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MasterConnectionStatusChangeCustomPropertySet implements CustomPropertySet<ServiceCall, MasterConnectionStatusChangeDomainExtension> {
    public static final String MODEL_NAME = "CS2";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MasterConnectionStatusChangeCustomPropertySet() {
        // for OSGI purpose
    }

    @Inject
    public MasterConnectionStatusChangeCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.MASTER_CONNECTION_STATUS_CHANGE_CPS).format();
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
    public PersistenceSupport<ServiceCall, MasterConnectionStatusChangeDomainExtension> getPersistenceSupport() {
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
                        .named(MasterConnectionStatusChangeDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.REQUEST_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(MasterConnectionStatusChangeDomainExtension.FieldNames.UUID.javaName(), TranslationKeys.UUID)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MasterConnectionStatusChangeDomainExtension> {
        private final String TABLE_NAME = "SAP_CS2_MASTER_CR_SC_CPS";
        private final String FK = "FK_SAP_CS2_MASTER_CR_SC_CPS";

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
            return MasterConnectionStatusChangeDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MasterConnectionStatusChangeDomainExtension> persistenceClass() {
            return MasterConnectionStatusChangeDomainExtension.class;
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
            Column oldRequestIdColumn = table.column(MasterConnectionStatusChangeDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(MasterConnectionStatusChangeDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7, 1))
                    .add();
            table.column(MasterConnectionStatusChangeDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(MasterConnectionStatusChangeDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .since(Version.version(10, 7, 1))
                    .previously(oldRequestIdColumn)
                    .add();
            table.column(MasterConnectionStatusChangeDomainExtension.FieldNames.UUID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MasterConnectionStatusChangeDomainExtension.FieldNames.UUID.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
