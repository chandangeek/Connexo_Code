/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.sms;

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
 * to support the persistence of {@link InboundProximusConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (17:43)
 */
public class InboundProximusConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionProvider, InboundProximusConnectionProperties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P09.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_PROXIMUS_SMS_IN_CT";
    }

    @Override
    public String domainFieldName() {
        return InboundProximusConnectionProperties.Fields.CONNECTION_PROVIDER.javaName();
    }

    @Override
    public String domainColumnName() {
        return InboundProximusConnectionProperties.Fields.CONNECTION_PROVIDER.databaseName();
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_PROXIMUS_SMS_IN_CT";
    }

    @Override
    public Class<InboundProximusConnectionProperties> persistenceClass() {
        return InboundProximusConnectionProperties.class;
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
            .of(InboundProximusConnectionProperties.Fields.values())
            .forEach(fieldName -> fieldName.addTo(table));
    }

}