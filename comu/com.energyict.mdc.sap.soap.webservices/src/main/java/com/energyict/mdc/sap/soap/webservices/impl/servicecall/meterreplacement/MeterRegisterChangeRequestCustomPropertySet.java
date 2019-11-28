/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement;

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

import com.google.inject.Module;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MeterRegisterChangeRequestCustomPropertySet implements CustomPropertySet<ServiceCall, MeterRegisterChangeRequestDomainExtension> {
    public static final String MODEL_NAME = "LR2";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MeterRegisterChangeRequestCustomPropertySet() {
        // for OSGI purpose
    }

    @Inject
    public MeterRegisterChangeRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return MeterRegisterChangeRequestCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, MeterRegisterChangeRequestDomainExtension> getPersistenceSupport() {
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
                        .named(MeterRegisterChangeRequestDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterRegisterChangeRequestDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterRegisterChangeRequestDomainExtension.FieldNames.UUID.javaName(), TranslationKeys.UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterRegisterChangeRequestDomainExtension.FieldNames.LRN.javaName(), TranslationKeys.LRN)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MeterRegisterChangeRequestDomainExtension.FieldNames.END_DATE.javaName(), TranslationKeys.END_DATE)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterRegisterChangeRequestDomainExtension.FieldNames.TIME_ZONE.javaName(), TranslationKeys.TIME_ZONE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterRegisterChangeRequestDomainExtension.FieldNames.ERROR_CODE.javaName(), TranslationKeys.ERROR_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterRegisterChangeRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MeterRegisterChangeRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_LR2_CR_SC_CPS";
        private final String FK = "FK_SAP_LR2_CR_SC_CPS";

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
            return MeterRegisterChangeRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MeterRegisterChangeRequestDomainExtension> persistenceClass() {
            return MeterRegisterChangeRequestDomainExtension.class;
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
            table.column(MeterRegisterChangeRequestDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterRegisterChangeRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .add();
            Column oldRequestIdColumn = table.column(MeterRegisterChangeRequestDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterRegisterChangeRequestDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7, 1))
                    .add();
            table.column(MeterRegisterChangeRequestDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterRegisterChangeRequestDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .since(Version.version(10, 7, 1))
                    .previously(oldRequestIdColumn)
                    .add();
            table.column(MeterRegisterChangeRequestDomainExtension.FieldNames.UUID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterRegisterChangeRequestDomainExtension.FieldNames.UUID.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            table.column(MeterRegisterChangeRequestDomainExtension.FieldNames.LRN.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterRegisterChangeRequestDomainExtension.FieldNames.LRN.javaName())
                    .notNull()
                    .add();
            table.column(MeterRegisterChangeRequestDomainExtension.FieldNames.END_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MeterRegisterChangeRequestDomainExtension.FieldNames.END_DATE.javaName())
                    .notNull()
                    .add();
            table.column(MeterRegisterChangeRequestDomainExtension.FieldNames.TIME_ZONE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterRegisterChangeRequestDomainExtension.FieldNames.TIME_ZONE.javaName())
                    .add();
            table.column(MeterRegisterChangeRequestDomainExtension.FieldNames.ERROR_CODE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterRegisterChangeRequestDomainExtension.FieldNames.ERROR_CODE.javaName())
                    .add();
            table.column(MeterRegisterChangeRequestDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .varChar(DESCRIPTION_LENGTH)
                    .map(MeterRegisterChangeRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
