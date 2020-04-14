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

import com.google.inject.Module;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class PodNotificationCustomPropertySet implements CustomPropertySet<ServiceCall,
        PodNotificationDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public PodNotificationCustomPropertySet() {
    }

    @Inject
    public PodNotificationCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.POD_NOTIFICATION_CPS).format();
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
    public PersistenceSupport<ServiceCall, PodNotificationDomainExtension> getPersistenceSupport() {
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
                        .named(PodNotificationDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(PodNotificationDomainExtension.FieldNames.POD_ID.javaName(), TranslationKeys.POD_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, PodNotificationDomainExtension> {
        private final String TABLE_NAME = "SAP_UD8_PN_SC_CPS";
        private final String FK = "FK_SAP_UD8_PN_SC_CPS";

        @Override
        public String componentName() {
            return "UD8";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return PodNotificationDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<PodNotificationDomainExtension> persistenceClass() {
            return PodNotificationDomainExtension.class;
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
            table.column(PodNotificationDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(PodNotificationDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .add();
            table.column(PodNotificationDomainExtension.FieldNames.POD_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(PodNotificationDomainExtension.FieldNames.POD_ID.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
    }
