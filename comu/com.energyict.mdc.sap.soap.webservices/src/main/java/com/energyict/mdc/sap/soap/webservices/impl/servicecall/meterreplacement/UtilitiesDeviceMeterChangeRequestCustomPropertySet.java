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
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;

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

public class UtilitiesDeviceMeterChangeRequestCustomPropertySet implements CustomPropertySet<ServiceCall, UtilitiesDeviceMeterChangeRequestDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public UtilitiesDeviceMeterChangeRequestCustomPropertySet() {
    }

    @Inject
    public UtilitiesDeviceMeterChangeRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.UTILITIES_DEVICE_METER_CHANGE_CPS).format();
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
    public PersistenceSupport<ServiceCall, UtilitiesDeviceMeterChangeRequestDomainExtension> getPersistenceSupport() {
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
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.REQUEST_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.UUID.javaName(), TranslationKeys.UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.SERIAL_ID.javaName(), TranslationKeys.SERIAL_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.DEVICE_TYPE.javaName(), TranslationKeys.DEVICE_TYPE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.MATERIAL_ID.javaName(), TranslationKeys.MATERIAL_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.MANUFACTURER.javaName(), TranslationKeys.MANUFACTURER)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.MANUFACTURER_SERIAL_ID.javaName(), TranslationKeys.MANUFACTURER_SERIAL_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ACTIVATION_GROUP_AMI_FUNCTIONS.javaName(), TranslationKeys.ACTIVATION_GROUP_AMI_FUNCTIONS)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.METER_FUNCTION_GROUP.javaName(), TranslationKeys.METER_FUNCTION_GROUP)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ATTRIBUTE_MESSAGE.javaName(), TranslationKeys.ATTRIBUTE_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.CHARACTERISTICS_ID.javaName(), TranslationKeys.CHARACTERISTICS_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.CHARACTERISTICS_VALUE.javaName(), TranslationKeys.CHARACTERISTICS_VALUE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ERROR_CODE.javaName(), TranslationKeys.ERROR_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.SHIPMENT_DATE.javaName(), TranslationKeys.SHIPMENT_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.DEACTIVATION_DATE.javaName(), TranslationKeys.SHIPMENT_DATE)
                        .fromThesaurus(thesaurus)
                        .finish()

        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, UtilitiesDeviceMeterChangeRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_LR7_CR_SC_CPS";
        private final String FK = "FK_SAP_LR7_CR_SC_CPS";

        @Override
        public String componentName() {
            return "LR7";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<UtilitiesDeviceMeterChangeRequestDomainExtension> persistenceClass() {
            return UtilitiesDeviceMeterChangeRequestDomainExtension.class;
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
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.UUID.databaseName())
                    .varChar()
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.UUID.javaName())
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.SERIAL_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.SERIAL_ID.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.DEVICE_TYPE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.DEVICE_TYPE.javaName())
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.MATERIAL_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.MATERIAL_ID.javaName())
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.MANUFACTURER.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.MANUFACTURER.javaName())
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.MANUFACTURER_SERIAL_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.MANUFACTURER_SERIAL_ID.javaName())
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ACTIVATION_GROUP_AMI_FUNCTIONS.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ACTIVATION_GROUP_AMI_FUNCTIONS.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.METER_FUNCTION_GROUP.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.METER_FUNCTION_GROUP.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ATTRIBUTE_MESSAGE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ATTRIBUTE_MESSAGE.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.CHARACTERISTICS_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.CHARACTERISTICS_ID.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.CHARACTERISTICS_VALUE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.CHARACTERISTICS_VALUE.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.SHIPMENT_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.SHIPMENT_DATE.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.DEACTIVATION_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.DEACTIVATION_DATE.javaName())
                    .since(Version.version(10, 9, 20))
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ERROR_CODE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ERROR_CODE.javaName())
                    .add();
            table.column(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .varChar(DESCRIPTION_LENGTH)
                    .map(UtilitiesDeviceMeterChangeRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .add();

        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
