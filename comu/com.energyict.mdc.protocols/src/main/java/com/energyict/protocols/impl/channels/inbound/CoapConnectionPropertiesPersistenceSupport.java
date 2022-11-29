/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.common.protocol.ConnectionProvider;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocols.naming.CustomPropertySetComponentName;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface
 * to support the persistence of {@link CoapConnectionProperties}.
 */
public class CoapConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionProvider, CoapConnectionProperties> {
    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P42.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_COAP_CONNECTION_TYPE";
    }

    @Override
    public String domainColumnName() {
        return CoapConnectionProperties.Fields.CONNECTION_PROVIDER.databaseName();
    }

    @Override
    public String domainFieldName() {
        return CoapConnectionProperties.Fields.CONNECTION_PROVIDER.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_COAP_CT";
    }

    @Override
    public Class<CoapConnectionProperties> persistenceClass() {
        return CoapConnectionProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        // None of the custom properties are part of the primary key
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        table
                .column(CoapConnectionProperties.Fields.IP_ADDRESS.databaseName())
                .varChar()
                .map(CoapConnectionProperties.Fields.IP_ADDRESS.javaName())
                .add();
    }

    @Override
    public String application() {
        return "MultiSense";
    }

}