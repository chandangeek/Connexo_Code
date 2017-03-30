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

@Component(name = "com.energyict.mdc.device.config.cps.LoadProfileTwoVersionedCustomPropertySet", service = CustomPropertySet.class, immediate = true)
@SuppressWarnings("unused")
public class LoadProfileTwoVersionedCustomPropertySet implements CustomPropertySet<ChannelSpec, LoadProfileTwoVersionedDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_CHANNEL_VER_TWO";
    public static final String FK_CPS_DEVICE_VER = "FK_CPS_CHANNEL_VER_TWO";

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

    public LoadProfileTwoVersionedCustomPropertySet() {
        super();
    }

    @Inject
    public LoadProfileTwoVersionedCustomPropertySet(PropertySpecService propertySpecService, DeviceService deviceService) {
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
        return LoadProfileTwoVersionedCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ChannelSpec, LoadProfileTwoVersionedDomainExtension> getPersistenceSupport() {
        return new LoadProfileTwoVersionedPeristenceSupport();
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
                        .named(LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                        .describedAs("A")
                        .addValues(BigDecimal.valueOf(7L), BigDecimal.valueOf(77L), BigDecimal.valueOf(777L))
                        .setDefaultValue(BigDecimal.valueOf(77L))
                        .markExhaustive()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                        .describedAs("infoEnumString")
                        .addValues("alfa", "beta", "gamma")
                        .setDefaultValue("gamma")
                        .markExhaustive()
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                        .describedAs("flag")
                        .setDefaultValue(false)
                        .finish());
    }

    private static class LoadProfileTwoVersionedPeristenceSupport implements PersistenceSupport<ChannelSpec, LoadProfileTwoVersionedDomainExtension> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "RK7";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return LoadProfileTwoVersionedDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_VER;
        }

        @Override
        public Class<LoadProfileTwoVersionedDomainExtension> persistenceClass() {
            return LoadProfileTwoVersionedDomainExtension.class;
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
                .column(LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                .number()
                .map(LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                .add();
            table
                .column(LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.databaseName())
                .varChar()
                .map(LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                .add();
            table
                .column(LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                .bool()
                .map(LoadProfileTwoVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .add();
        }
    }
}