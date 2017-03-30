/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.DeviceService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.config.cps.RegisterTypeTwoVersionedCustomPropertySet", service = CustomPropertySet.class, immediate = true)
@SuppressWarnings("unused")
public class RegisterTypeTwoVersionedCustomPropertySet implements CustomPropertySet<RegisterSpec, RegisterTypeTwoVersionedDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_REGISTER_VER_TWO";
    public static final String FK_CPS_DEVICE_VER = "FK_CPS_REGISTER_VER_TWO";

    public volatile PropertySpecService propertySpecService;
    public volatile DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public RegisterTypeTwoVersionedCustomPropertySet() {
        super();
    }

    @Inject
    public RegisterTypeTwoVersionedCustomPropertySet(PropertySpecService propertySpecService, DeviceService deviceService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setDeviceService(deviceService);
    }

    @Activate
    public void activate() {
        System.out.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return RegisterTypeTwoVersionedCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<RegisterSpec> getDomainClass() {
        return RegisterSpec.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return "Register";
    }

    @Override
    public PersistenceSupport<RegisterSpec, RegisterTypeTwoVersionedDomainExtension> getPersistenceSupport() {
        return new RegisterTypeTwoVersionedPeristenceSupport();
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isVersioned() {
        return true;
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
                        .bigDecimalSpec()
                        .named(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                        .describedAs("kw")
                        .setDefaultValue(BigDecimal.ZERO)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName(), RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                        .describedAs("infoString")
                        .setDefaultValue("description")
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                        .describedAs("A")
                        .addValues(BigDecimal.valueOf(7L), BigDecimal.valueOf(77L), BigDecimal.valueOf(777L))
                        .setDefaultValue(BigDecimal.valueOf(77L))
                        .markExhaustive()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                        .describedAs("infoEnumString")
                        .addValues("alfa", "beta", "gamma")
                        .setDefaultValue("gamma")
                        .markExhaustive()
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                        .describedAs("flag")
                        .setDefaultValue(false)
                        .finish());
    }

    private static class RegisterTypeTwoVersionedPeristenceSupport implements PersistenceSupport<RegisterSpec, RegisterTypeTwoVersionedDomainExtension> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "RKD";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return RegisterTypeTwoVersionedDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_VER;
        }

        @Override
        public Class<RegisterTypeTwoVersionedDomainExtension> persistenceClass() {
            return RegisterTypeTwoVersionedDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.singletonList(
                    table
                        .column(LoadProfileTypeOneDomainExtension.FieldNames.DEVICE.databaseName())
                        .number()
                        .map(LoadProfileTypeOneDomainExtension.FieldNames.DEVICE.javaName())
                        .notNull()
                        .add());
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table
                .column(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.databaseName())
                .number()
                .map(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                .notNull()
                .add();
            table
                .column(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.databaseName())
                .varChar()
                .map(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                .notNull()
                .add();
            table
                .column(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                .number()
                .map(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                .add();
            table
                .column(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.databaseName())
                .varChar()
                .map(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                .add();
            table
                .column(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                .bool()
                .map(RegisterTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .add();
        }
    }
}