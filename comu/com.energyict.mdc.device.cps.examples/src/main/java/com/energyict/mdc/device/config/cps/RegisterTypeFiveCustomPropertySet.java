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

@Component(name = "com.energyict.mdc.device.config.cps.RegisterTypeFiveCustomPropertySet", service = CustomPropertySet.class, immediate = true)
@SuppressWarnings("unused")
public class RegisterTypeFiveCustomPropertySet implements CustomPropertySet<RegisterSpec, RegisterTypeFiveDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_REGISTER_FIVE";
    public static final String FK_CPS_REGISTER_FIVE = "FK_CPS_REGISTER_FIVE";

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

    public RegisterTypeFiveCustomPropertySet() {
        super();
    }

    @Inject
    public RegisterTypeFiveCustomPropertySet(PropertySpecService propertySpecService, DeviceService deviceService) {
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
        return RegisterTypeFiveCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<RegisterSpec, RegisterTypeFiveDomainExtension> getPersistenceSupport() {
        return new RegisterTypeFivePeristenceSupport();
    }

    @Override
    public boolean isRequired() {
        return false;
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
                        .bigDecimalSpec()
                        .named(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                        .describedAs("kw")
                        .setDefaultValue(BigDecimal.ZERO)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName(), RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                        .describedAs("infoString")
                        .setDefaultValue("description")
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                        .describedAs("flag")
                        .setDefaultValue(false)
                        .finish());
    }

    private static class RegisterTypeFivePeristenceSupport implements PersistenceSupport<RegisterSpec, RegisterTypeFiveDomainExtension> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "RKA";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return RegisterTypeFiveDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_REGISTER_FIVE;
        }

        @Override
        public Class<RegisterTypeFiveDomainExtension> persistenceClass() {
            return RegisterTypeFiveDomainExtension.class;
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
                .column(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.databaseName())
                .number()
                .map(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                .notNull()
                .add();
            table
                .column(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.databaseName())
                .varChar()
                .map(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                .notNull()
                .add();
            table
                .column(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                .bool()
                .map(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .add();
        }
    }
}