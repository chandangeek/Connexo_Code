/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocols.naming.CustomPropertySetComponentName;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link SDKTimeProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (09:37)
 */
class SDKTimeDialectPropertyPersistenceSupport implements PersistenceSupport<DeviceProtocolDialectPropertyProvider, SDKTimeDialectProperties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String domainFieldName() {
        return CommonDeviceProtocolDialectProperties.Fields.DIALECT_PROPERTY_PROVIDER.javaName();
    }

    @Override
    public String domainColumnName() {
        return CommonDeviceProtocolDialectProperties.Fields.DIALECT_PROPERTY_PROVIDER.databaseName();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_SDKTIME_DIALECT";
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_SDKTIME_DIALECT_PROPS";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P06.name();
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public Class<SDKTimeDialectProperties> persistenceClass() {
        return SDKTimeDialectProperties.class;
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        // No custom primary key columns
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        Stream
            .of(SDKTimeDialectProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}