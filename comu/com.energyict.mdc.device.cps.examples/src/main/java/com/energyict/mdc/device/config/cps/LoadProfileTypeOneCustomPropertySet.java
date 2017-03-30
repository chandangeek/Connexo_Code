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
import com.energyict.mdc.device.config.ChannelSpec;
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

@Component(name = "com.energyict.mdc.device.config.cps.LoadProfileTypeOneCustomPropertySet", service = CustomPropertySet.class, immediate = true)
@SuppressWarnings("unused")
public class LoadProfileTypeOneCustomPropertySet implements CustomPropertySet<ChannelSpec, LoadProfileTypeOneDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_CHANNEL_ONE";
    public static final String FK_CPS_CHANNEL_ONE = "FK_CPS_CHANNEL_ONE";

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

    public LoadProfileTypeOneCustomPropertySet() {
        super();
    }

    @Inject
    public LoadProfileTypeOneCustomPropertySet(PropertySpecService propertySpecService, DeviceService deviceService) {
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
        return LoadProfileTypeOneCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ChannelSpec> getDomainClass() {
        return ChannelSpec.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return "Channel";
    }

    @Override
    public PersistenceSupport<ChannelSpec, LoadProfileTypeOneDomainExtension> getPersistenceSupport() {
        return new LoadProfileTypeOnePeristenceSupport();
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
                        .named(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                        .describedAs("A")
                        .addValues(BigDecimal.valueOf(7L), BigDecimal.valueOf(77L), BigDecimal.valueOf(777L))
                        .setDefaultValue(BigDecimal.valueOf(77L))
                        .markExhaustive()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                        .describedAs("infoEnumString")
                        .addValues("alfa", "beta", "gamma")
                        .setDefaultValue("gamma")
                        .markExhaustive()
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                        .describedAs("flag")
                        .setDefaultValue(false)
                        .finish());
    }

    private static class LoadProfileTypeOnePeristenceSupport implements PersistenceSupport<ChannelSpec, LoadProfileTypeOneDomainExtension> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "RK8";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return LoadProfileTypeOneDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_CHANNEL_ONE;
        }

        @Override
        public Class<LoadProfileTypeOneDomainExtension> persistenceClass() {
            return LoadProfileTypeOneDomainExtension.class;
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
                .column(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                .number()
                .map(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                .add();
            table
                .column(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.databaseName())
                .varChar()
                .map(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                .add();
            table
                .column(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                .bool()
                .map(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .add();
        }
    }
}