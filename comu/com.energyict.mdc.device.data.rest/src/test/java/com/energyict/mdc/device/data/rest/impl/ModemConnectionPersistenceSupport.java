/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

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
public class ModemConnectionPersistenceSupport implements PersistenceSupport<ConnectionProvider, ModemConnectionPropertyValues> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return "T08";
    }

    @Override
    public String tableName() {
        return "TST_MODEM_PROPS";
    }

    @Override
    public String domainFieldName() {
        return "connectionProvider";
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_TST_MODEM_PROPS_CT";
    }

    @Override
    public Class<ModemConnectionPropertyValues> persistenceClass() {
        return ModemConnectionPropertyValues.class;
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
            .column(ModemConnectionProperties.PHONE_NUMBER.propertyName())
            .notNull()
            .varChar()
            .map(ModemConnectionProperties.PHONE_NUMBER.propertyName())
            .add();
    }

}