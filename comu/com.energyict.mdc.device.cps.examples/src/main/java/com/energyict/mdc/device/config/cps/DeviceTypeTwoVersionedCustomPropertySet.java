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

@Component(name = "com.energyict.mdc.device.config.cps.DeviceTypeTwoVersionedCustomPropertySet", service = CustomPropertySet.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceTypeTwoVersionedCustomPropertySet implements CustomPropertySet<Device, DeviceTypeTwoVersionedDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_DEVICE_VER_TWO";
    public static final String FK_CPS_DEVICE_VER = "FK_CPS_DEVICE_VER_TWO";

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

    public DeviceTypeTwoVersionedCustomPropertySet() {
        super();
    }

    @Inject
    public DeviceTypeTwoVersionedCustomPropertySet(PropertySpecService propertySpecService, DeviceService deviceService) {
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
        return DeviceTypeTwoVersionedCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<Device, DeviceTypeTwoVersionedDomainExtension> getPersistenceSupport() {
        return new DeviceTypeTwoVersionedPeristenceSupport();
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
                        .booleanSpec()
                        .named(DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                        .describedAs("cccccccccc")
                        .setDefaultValue(false)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                        .describedAs("aaaaaa")
                        .setDefaultValue(BigDecimal.ZERO)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                        .describedAs("bbbbbbbb")
                        .addValues(BigDecimal.valueOf(8L), BigDecimal.valueOf(88L), BigDecimal.valueOf(888L))
                        .setDefaultValue(BigDecimal.valueOf(88L))
                        .markExhaustive()
                        .finish());
    }

    private static class DeviceTypeTwoVersionedPeristenceSupport implements PersistenceSupport<Device, DeviceTypeTwoVersionedDomainExtension> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "RK5";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return DeviceTypeTwoVersionedDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_VER;
        }

        @Override
        public Class<DeviceTypeTwoVersionedDomainExtension> persistenceClass() {
            return DeviceTypeTwoVersionedDomainExtension.class;
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
            table
                .column(DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.databaseName())
                .number()
                .map(DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                .notNull()
                .add();
            table
                .column(DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                .number()
                .map(DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                .add();
            table
                .column(DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                .bool()
                .map(DeviceTypeTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .add();
        }
    }
}