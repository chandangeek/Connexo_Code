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
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.InstantFactory;
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

import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class UtilitiesDeviceRegisterCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, UtilitiesDeviceRegisterCreateRequestDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public UtilitiesDeviceRegisterCreateRequestCustomPropertySet() {
    }

    @Inject
    public UtilitiesDeviceRegisterCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.UTILITIES_DEVICE_REGISTER_CREATE_REQUEST_CPS).format();
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
    public PersistenceSupport<ServiceCall, UtilitiesDeviceRegisterCreateRequestDomainExtension> getPersistenceSupport() {
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
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.OBIS.javaName(), TranslationKeys.OBIS)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.RECURRENCE_CODE.javaName(), TranslationKeys.RECURRENCE_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.LRN.javaName(), TranslationKeys.LRN)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.START_DATE.javaName(), TranslationKeys.START_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.END_DATE.javaName(), TranslationKeys.END_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.TIME_ZONE.javaName(), TranslationKeys.TIME_ZONE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DIVISION_CATEGORY.javaName(), TranslationKeys.DIVISION_CATEGORY)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.REGISTER_ID.javaName(), TranslationKeys.REGISTER_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.TOTAL_DIGIT_NUMBER_VALUE.javaName(), TranslationKeys.TOTAL_DIGIT_NUMBER_VALUE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.FRACTION_DIGIT_NUMBER_VALUE.javaName(), TranslationKeys.FRACTION_DIGIT_NUMBER_VALUE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.ERROR_CODE.javaName(), TranslationKeys.ERROR_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, UtilitiesDeviceRegisterCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_UD1_RCR_SC_CPS";
        private final String FK = "FK_SAP_UD1_RCR_SC_CPS";

        private Thesaurus thesaurus;

        private CustomPropertyPersistenceSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return "UD1";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<UtilitiesDeviceRegisterCreateRequestDomainExtension> persistenceClass() {
            return UtilitiesDeviceRegisterCreateRequestDomainExtension.class;
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
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .add();
            Column oldOBISColumn = table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.OBIS.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.OBIS.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7, 1))
                    .add();
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.OBIS.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.OBIS.javaName())
                    .since(Version.version(10, 7, 1))
                    .previously(oldOBISColumn)
                    .add();
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.RECURRENCE_CODE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.RECURRENCE_CODE.javaName())
                    .add();
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.LRN.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.LRN.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.START_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.START_DATE.javaName())
                    .add();
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.END_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.END_DATE.javaName())
                    .add();
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.TIME_ZONE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.TIME_ZONE.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DIVISION_CATEGORY.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DIVISION_CATEGORY.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.REGISTER_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.REGISTER_ID.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_CODE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_CODE.javaName())
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .varChar(DESCRIPTION_LENGTH)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .add();
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.TOTAL_DIGIT_NUMBER_VALUE.databaseName())
                    .number()
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.TOTAL_DIGIT_NUMBER_VALUE.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.FRACTION_DIGIT_NUMBER_VALUE.databaseName())
                    .number()
                    .map(UtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.FRACTION_DIGIT_NUMBER_VALUE.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
