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

@Component(name = "com.energyict.mdc.device.config.cps.DeviceTypeThreeCustomPropertySet", service = CustomPropertySet.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceTypeThreeCustomPropertySet implements CustomPropertySet<Device, DeviceTypeThreeDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_DEVICE_THREE";
    public static final String FK_CPS_DEVICE_THREE = "FK_CPS_DEVICE_THREE";

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

    public DeviceTypeThreeCustomPropertySet() {
        super();
    }

    @Inject
    public DeviceTypeThreeCustomPropertySet(PropertySpecService propertySpecService, DeviceService deviceService) {
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
        return DeviceTypeThreeCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<Device, DeviceTypeThreeDomainExtension> getPersistenceSupport() {
        return new DeviceThreePeristenceSupport();
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
                        .named(DeviceTypeThreeDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), DeviceTypeThreeDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                        .describedAs("kw")
                        .setDefaultValue(BigDecimal.ZERO)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceTypeThreeDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName(), DeviceTypeThreeDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                        .describedAs("infoString")
                        .setDefaultValue("description")
                        .finish());
    }

    private static class DeviceThreePeristenceSupport implements PersistenceSupport<Device, DeviceTypeThreeDomainExtension> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "RK3";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return DeviceTypeThreeDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_THREE;
        }

        @Override
        public Class<DeviceTypeThreeDomainExtension> persistenceClass() {
            return DeviceTypeThreeDomainExtension.class;
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
                .column(DeviceTypeThreeDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.databaseName())
                .number()
                .map(DeviceTypeThreeDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                .notNull()
                .add();
            table
                .column(DeviceTypeThreeDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.databaseName())
                .varChar()
                .map(DeviceTypeThreeDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                .notNull()
                .add();
        }
    }
}