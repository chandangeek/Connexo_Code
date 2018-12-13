/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.common.dialects;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocols.naming.CustomPropertySetComponentName;

import com.energyict.protocolimplv2.edmi.dialects.ModemDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.UdpDeviceProtocolDialect;
import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link TcpDeviceProtocolDialect}, {@link UdpDeviceProtocolDialect}
  * and {@link ModemDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (11:47)
 */
public class EdmiDeviceProtocolDialectPropertyPersistenceSupport implements PersistenceSupport<DeviceProtocolDialectPropertyProvider, EdmiDeviceProtocolDialectProperties> {
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
        return DeviceProtocolService.COMPONENT_NAME + "_EDMI_DIALECT";
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_EDMI_DIALECT_PROPS";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P39.name();
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public Class<EdmiDeviceProtocolDialectProperties> persistenceClass() {
        return EdmiDeviceProtocolDialectProperties.class;
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        // No custom primary key columns
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        Stream
            .of(EdmiDeviceProtocolDialectProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}