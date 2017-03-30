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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// TODO: This class only for demo purposes
@Component(name = "com.energyict.mdc.device.config.cps.DeviceTypeDemoCustomValidationPropertySet", service = CustomPropertySet.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceTypeDemoCustomValidationPropertySet implements CustomPropertySet<Device, DeviceTypeDemoCustomValidationDomainExtension> {

    public static final String TABLE_NAME = "AKG_CPS_DEMO_VALIDATION";
    public static final String FK_CPS_DEVICE_ONE = "FK_CPS_DEMO_VALIDATION";

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

    public DeviceTypeDemoCustomValidationPropertySet() {
        super();
    }

    @Inject
    public DeviceTypeDemoCustomValidationPropertySet(PropertySpecService propertySpecService, DeviceService deviceService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setDeviceService(deviceService);
    }

    @Override
    public String getName() {
        return "MeterSpecs";
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
    public PersistenceSupport<Device, DeviceTypeDemoCustomValidationDomainExtension> getPersistenceSupport() {
        return new DeviceTypeDemoCustomValidationPeristenceSupport();
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
                        .stringSpec()
                        .named(DeviceTypeDemoCustomValidationDomainExtension.FieldNames.METER_MECHANISM.javaName(), DeviceTypeDemoCustomValidationDomainExtension.FieldNames.METER_MECHANISM.javaName())
                        .describedAs("Meter mechanism")
                        .addValues("Credit", "Mechanical Token", "Electronic Token", "Coin", "Prepayment")
                        .setDefaultValue("Credit")
                        .markExhaustive()
                        .finish());
    }

    private static class DeviceTypeDemoCustomValidationPeristenceSupport implements PersistenceSupport<Device, DeviceTypeDemoCustomValidationDomainExtension> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "AKG";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return DeviceTypeOneDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_ONE;
        }

        @Override
        public Class<DeviceTypeDemoCustomValidationDomainExtension> persistenceClass() {
            return DeviceTypeDemoCustomValidationDomainExtension.class;
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
                    .column(DeviceTypeDemoCustomValidationDomainExtension.FieldNames.METER_MECHANISM.databaseName())
                    .varChar()
                    .map(DeviceTypeDemoCustomValidationDomainExtension.FieldNames.METER_MECHANISM.javaName())
                    .add();
        }
    }
}