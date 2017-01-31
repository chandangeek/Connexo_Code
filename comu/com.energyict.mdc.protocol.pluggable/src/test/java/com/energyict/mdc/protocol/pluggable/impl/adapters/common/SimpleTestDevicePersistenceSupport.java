/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link SimpleTestDeviceSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (10:56)
 */
public class SimpleTestDevicePersistenceSupport implements PersistenceSupport<BaseDevice, SimpleTestDeviceSecurityProperties> {
    @Override
    public String application() {
        return "Example";
    }

    @Override
    public String componentName() {
        return "T12";
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_SIMPLE_TEST_SECURITY";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_SIMPLE_TEST_DEV";
    }

    @Override
    public Class<SimpleTestDeviceSecurityProperties> persistenceClass() {
        return SimpleTestDeviceSecurityProperties.class;
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
        Stream
            .of(SimpleTestDeviceSecurityProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}