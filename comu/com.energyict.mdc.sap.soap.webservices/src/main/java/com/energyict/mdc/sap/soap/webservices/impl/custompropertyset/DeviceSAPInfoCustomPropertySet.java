/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.orm.Table.MAX_STRING_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class DeviceSAPInfoCustomPropertySet implements CustomPropertySet<Device, DeviceSAPInfoDomainExtension> {
    public static final String CPS_ID = DeviceSAPInfoCustomPropertySet.class.getName();
    public static final String MODEL_NAME = "DI1";

    // Common for all domain objects
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final CustomPropertySetService customPropertySetService;

    DeviceSAPInfoCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus, SAPCustomPropertySets sapCustomPropertySets, CustomPropertySetService customPropertySetService) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public String getId() {
        return CPS_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CPS_DEVICE_SAP_INFO).format();
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return thesaurus.getFormat(TranslationKeys.DOMAIN_NAME_DEVICE).format();
    }

    @Override
    public PersistenceSupport<Device, DeviceSAPInfoDomainExtension> getPersistenceSupport() {
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
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.allOf(EditPrivilege.class);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName(), TranslationKeys.CPS_DEVICE_IDENTIFIER)
                        .describedAs(TranslationKeys.CPS_DEVICE_IDENTIFIER_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_LOCATION.javaName(), TranslationKeys.CPS_DEVICE_LOCATION)
                        .describedAs(TranslationKeys.CPS_DEVICE_LOCATION_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.INSTALLATION_NUMBER.javaName(), TranslationKeys.CPS_INSTALLATION_NUMBER)
                        .describedAs(TranslationKeys.CPS_INSTALLATION_NUMBER_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.POINT_OF_DELIVERY.javaName(), TranslationKeys.CPS_POINT_OF_DELIVERY)
                        .describedAs(TranslationKeys.CPS_POINT_OF_DELIVERY_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.DIVISION_CATEGORY_CODE.javaName(), TranslationKeys.CPS_DIVISION_CATEGORY_CODE)
                        .describedAs(TranslationKeys.CPS_DIVISION_CATEGORY_CODE_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_LOCATION_INFORMATION.javaName(), TranslationKeys.CPS_DEVICE_LOCATION_INFORMATION)
                        .describedAs(TranslationKeys.CPS_DEVICE_LOCATION_INFORMATION_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.MODIFICATION_INFORMATION.javaName(), TranslationKeys.CPS_MODIFICATION_INFORMATION)
                        .describedAs(TranslationKeys.CPS_MODIFICATION_INFORMATION_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.ACTIVATION_GROUP_AMI_FUNCTIONS.javaName(), TranslationKeys.CPS_ACTIVATION_GROUP_AMI_FUNCTIONS)
                        .describedAs(TranslationKeys.CPS_ACTIVATION_GROUP_AMI_FUNCTIONS_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.METER_FUNCTION_GROUP.javaName(), TranslationKeys.CPS_METER_FUNCTION_GROUP)
                        .describedAs(TranslationKeys.CPS_METER_FUNCTION_GROUP_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.ATTRIBUTE_MESSAGE.javaName(), TranslationKeys.CPS_ATTRIBUTE_MESSAGE)
                        .describedAs(TranslationKeys.CPS_ATTRIBUTE_MESSAGE_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.CHARACTERISTICS_ID.javaName(), TranslationKeys.CPS_CHARACTERISTICS_ID)
                        .describedAs(TranslationKeys.CPS_CHARACTERISTICS_ID_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.CHARACTERISTICS_VALUE.javaName(), TranslationKeys.CPS_CHARACTERISTICS_VALUE)
                        .describedAs(TranslationKeys.CPS_CHARACTERISTICS_VALUE_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(DeviceSAPInfoDomainExtension.FieldNames.REGISTERED.javaName(), TranslationKeys.REGISTERED)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish()
        );
    }

    class CustomPropertyPersistenceSupport implements PersistenceSupport<Device, DeviceSAPInfoDomainExtension> {
        private final String TABLE_NAME = "SAP_CAS_DI1";
        private final String FK = "FK_SAP_CAS_DI1";
        private final String IDX = "IDX_SAP_CAS_DI1_DEVID";

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
            return DeviceSAPInfoDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<DeviceSAPInfoDomainExtension> persistenceClass() {
            return DeviceSAPInfoDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.of(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(SAPCustomPropertySets.class).toInstance(sapCustomPropertySets);
                    bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                    bind(Thesaurus.class).toInstance(thesaurus);
                }
            });
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            /*table.column("DEVICE_IDENTIFIER")
                    .number()
                    .map(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName())
                    .upTo(Version.version(10, 7))
                    .add();*/
            Column deviceIdColumnString = table.column(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName())
                    .since(Version.version(10, 7))
                    .add();
            table.index(IDX).on(deviceIdColumnString).add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_LOCATION.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_LOCATION.javaName())
                    .since(Version.version(10, 7))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.INSTALLATION_NUMBER.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.INSTALLATION_NUMBER.javaName())
                    .since(Version.version(10, 9, 15))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.POINT_OF_DELIVERY.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.POINT_OF_DELIVERY.javaName())
                    .since(Version.version(10, 7))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.DIVISION_CATEGORY_CODE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.DIVISION_CATEGORY_CODE.javaName())
                    .since(Version.version(10, 9, 15))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_LOCATION_INFORMATION.databaseName())
                    .varChar(MAX_STRING_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_LOCATION_INFORMATION.javaName())
                    .since(Version.version(10, 9, 15))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.MODIFICATION_INFORMATION.databaseName())
                    .varChar(MAX_STRING_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.MODIFICATION_INFORMATION.javaName())
                    .since(Version.version(10, 9, 15))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.ACTIVATION_GROUP_AMI_FUNCTIONS.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.ACTIVATION_GROUP_AMI_FUNCTIONS.javaName())
                    .since(Version.version(10, 9, 15))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.METER_FUNCTION_GROUP.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.METER_FUNCTION_GROUP.javaName())
                    .since(Version.version(10, 9, 15))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.ATTRIBUTE_MESSAGE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.ATTRIBUTE_MESSAGE.javaName())
                    .since(Version.version(10, 9, 15))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.CHARACTERISTICS_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.CHARACTERISTICS_ID.javaName())
                    .since(Version.version(10, 9, 15))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.CHARACTERISTICS_VALUE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(DeviceSAPInfoDomainExtension.FieldNames.CHARACTERISTICS_VALUE.javaName())
                    .since(Version.version(10, 9, 15))
                    .add();
            table.column(DeviceSAPInfoDomainExtension.FieldNames.REGISTERED.databaseName())
                    .bool()
                    .map(DeviceSAPInfoDomainExtension.FieldNames.REGISTERED.javaName())
                    .since(Version.version(10, 7, 2))
                    .installValue("'N'")
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
