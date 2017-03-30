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
import com.energyict.mdc.device.data.Device;
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

@Component(name = "com.energyict.mdc.device.config.cps.DeviceTypeTwoCustomPropertySet", service = CustomPropertySet.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceTypeTwoCustomPropertySet implements CustomPropertySet<Device, DeviceTypeTwoDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_DEVICE_TWO";
    public static final String FK_CPS_DEVICE_TWO = "FK_CPS_DEVICE_TWO";

    public volatile PropertySpecService propertySpecService;
    public volatile DeviceService deviceService;

    @SuppressWarnings("unused")
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public DeviceTypeTwoCustomPropertySet() {
        super();
    }

    @Inject
    public DeviceTypeTwoCustomPropertySet(PropertySpecService propertySpecService, DeviceService deviceService) {
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
        return DeviceTypeTwoCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return "Device";
    }

    @Override
    public PersistenceSupport<Device, DeviceTypeTwoDomainExtension> getPersistenceSupport() {
        return new DeviceTwoPeristenceSupport();
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
        PropertySpec testNumberPropertySpec = this.propertySpecService
                .bigDecimalSpec()
                .named(DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                .describedAs("aaaaaa")
                .setDefaultValue(BigDecimal.ZERO)
                .markRequired()
                .finish();
        PropertySpec testNumberEnumPropertySpec = this.propertySpecService
                .bigDecimalSpec()
                .named(DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                .describedAs("bbbbbbbb")
                .addValues(BigDecimal.valueOf(8L), BigDecimal.valueOf(88L), BigDecimal.valueOf(888L))
                .setDefaultValue(BigDecimal.valueOf(88L))
                .markExhaustive()
                .finish();
        PropertySpec testBooleanPropertySpec = this.propertySpecService
                .booleanSpec()
                .named(DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .describedAs("cccccccccc")
                .setDefaultValue(false)
                .finish();
        return Arrays.asList(testBooleanPropertySpec, testNumberPropertySpec, testNumberEnumPropertySpec);
    }

    private static class DeviceTwoPeristenceSupport implements PersistenceSupport<Device, DeviceTypeTwoDomainExtension> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "RK4";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return DeviceTypeTwoDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_TWO;
        }

        @Override
        public Class<DeviceTypeTwoDomainExtension> persistenceClass() {
            return DeviceTypeTwoDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table
                    .column(DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.databaseName())
                    .number()
                    .map(DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                    .notNull()
                    .add();
            table
                    .column(DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                    .number()
                    .map(DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                    .add();
            table
                    .column(DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                    .bool()
                    .map(DeviceTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                    .add();
        }
    }
}