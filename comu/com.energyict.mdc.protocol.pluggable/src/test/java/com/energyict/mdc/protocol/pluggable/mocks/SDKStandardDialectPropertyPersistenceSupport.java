package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link SDKStandardDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-30 (12:26)
 */
class SDKStandardDialectPropertyPersistenceSupport implements PersistenceSupport<DeviceProtocolDialectPropertyProvider, SDKStandardDialectProperties> {
    @Override
    public String application() {
        return "Example";
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
        return componentName() + "_SDKSTANDARD_DIALECT";
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_SDKSTANDARD_DIALECT_PROPS";
    }

    @Override
    public String componentName() {
        return "P29";
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public Class<SDKStandardDialectProperties> persistenceClass() {
        return SDKStandardDialectProperties.class;
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        // No custom primary key columns
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        Stream
            .of(SDKStandardDialectProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}