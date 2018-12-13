/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (13:24)
 */
public class IpConnectionPersistenceSupport implements PersistenceSupport<ConnectionProvider, IpConnectionPropertyValues> {

    @Override
    public String application() {
        return "Example";
    }

    @Override
    public String componentName() {
        return "T07";
    }

    @Override
    public String tableName() {
        return "TST_IP_PROPS";
    }

    @Override
    public String domainFieldName() {
        return "connectionProvider";
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_TST_IP_PROPS_CT";
    }

    @Override
    public Class<IpConnectionPropertyValues> persistenceClass() {
        return IpConnectionPropertyValues.class;
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
            .column(IpConnectionProperties.IP_ADDRESS.propertyName())
            .varChar()
            .map(IpConnectionProperties.IP_ADDRESS.propertyName())
            .add();
        table
            .column(IpConnectionProperties.PORT.propertyName())
            .number()
            .map(IpConnectionProperties.PORT.propertyName())
            .add();
    }

}