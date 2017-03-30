/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial;

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
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface
 * to support the persistence of {@link SioSerialConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (09:46)
 */
public class SioSerialConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionProvider, SioSerialConnectionProperties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P15.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_SIOSERIAL_CONNTASK";
    }

    @Override
    public String domainFieldName() {
        return "connectionProvider";
    }

    @Override
    public String domainColumnName() {
        return "CONNECTIONPROVIDER";
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_SIOSERIALCT_CT";
    }

    @Override
    public Class<SioSerialConnectionProperties> persistenceClass() {
        return SioSerialConnectionProperties.class;
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
        Stream
            .of(SioSerialConnectionProperties.Fields.values())
            .forEach(fieldName -> fieldName.addTo(table));
    }

}