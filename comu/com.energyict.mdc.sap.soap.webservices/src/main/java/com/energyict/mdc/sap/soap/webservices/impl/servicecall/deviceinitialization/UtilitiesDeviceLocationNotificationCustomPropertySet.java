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

import static com.elster.jupiter.orm.Table.MAX_STRING_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class UtilitiesDeviceLocationNotificationCustomPropertySet implements CustomPropertySet<ServiceCall,
        UtilitiesDeviceLocationNotificationDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public UtilitiesDeviceLocationNotificationCustomPropertySet() {
    }

    @Inject
    public UtilitiesDeviceLocationNotificationCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.UTILITIES_DEVICE_LOCATION_NOTIFICATION_CPS).format();
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
    public PersistenceSupport<ServiceCall, UtilitiesDeviceLocationNotificationDomainExtension> getPersistenceSupport() {
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
                        .named(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.LOCATION_ID.javaName(), TranslationKeys.LOCATION_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.INSTALLATION_NUMBER.javaName(), TranslationKeys.INSTALLATION_NUMBER)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.POINT_OF_DELIVERY.javaName(), TranslationKeys.POD_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.DIVISION_CATEGORY_CODE.javaName(), TranslationKeys.DIVISION_CATEGORY_CODE)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, UtilitiesDeviceLocationNotificationDomainExtension> {
        private final String TABLE_NAME = "SAP_UD6_LN_SC_CPS";
        private final String FK = "FK_SAP_UD6_LN_SC_CPS";

        @Override
        public String componentName() {
            return "UD6";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<UtilitiesDeviceLocationNotificationDomainExtension> persistenceClass() {
            return UtilitiesDeviceLocationNotificationDomainExtension.class;
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
            table.column(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.LOCATION_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.LOCATION_ID.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.INSTALLATION_NUMBER.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.INSTALLATION_NUMBER.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.POINT_OF_DELIVERY.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.POINT_OF_DELIVERY.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.DIVISION_CATEGORY_CODE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.DIVISION_CATEGORY_CODE.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.LOCATION_INFORMATION.databaseName())
                    .varChar(MAX_STRING_LENGTH)
                    .map(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.LOCATION_INFORMATION.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.MODIFICATION_INFORMATION.databaseName())
                    .varChar(MAX_STRING_LENGTH)
                    .map(UtilitiesDeviceLocationNotificationDomainExtension.FieldNames.MODIFICATION_INFORMATION.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}