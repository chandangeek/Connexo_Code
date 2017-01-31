/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocols.naming.CustomPropertySetComponentName;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface
 * to support the persistence of {@link EIWebConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:46)
 */
public class EIWebConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionProvider, EIWebConnectionProperties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P14.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_EIWEB_CONNECTION_TYPE";
    }

    @Override
    public String domainFieldName() {
        return EIWebConnectionProperties.Fields.CONNECTION_PROVIDER.javaName();
    }

    @Override
    public String domainColumnName() {
        return EIWebConnectionProperties.Fields.CONNECTION_PROVIDER.databaseName();
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_EIWEB_CT";
    }

    @Override
    public Class<EIWebConnectionProperties> persistenceClass() {
        return EIWebConnectionProperties.class;
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
            .column(EIWebConnectionProperties.Fields.IP_ADDRESS.databaseName())
            .varChar()
            .map(EIWebConnectionProperties.Fields.IP_ADDRESS.javaName())
            .add();
        table
            .column(EIWebConnectionProperties.Fields.MAC_ADDRESS.databaseName())
            .varChar()
            .map(EIWebConnectionProperties.Fields.MAC_ADDRESS.javaName())
            .add();
    }

}