/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class ConnectionStatusChangePersistenceSupport implements PersistenceSupport<ServiceCall, ConnectionStatusChangeDomainExtension> {

    public static final String COMPONENT_NAME = "C01";
    public static final String TABLE_NAME = "T01_" + WebServiceActivator.COMPONENT_NAME + "_" + COMPONENT_NAME;
    private static final String FK_NAME = "FK_" + TABLE_NAME;

    @Override
    public String componentName() {
        return COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String domainFieldName() {
        return ConnectionStatusChangeDomainExtension.FieldNames.DOMAIN.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return FK_NAME;
    }

    @Override
    public Class<ConnectionStatusChangeDomainExtension> persistenceClass() {
        return ConnectionStatusChangeDomainExtension.class;
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
        table.column(ConnectionStatusChangeDomainExtension.FieldNames.ID.databaseName())
                .varChar()
                .map(ConnectionStatusChangeDomainExtension.FieldNames.ID.javaName())
                .notNull()
                .add();
        table.column(ConnectionStatusChangeDomainExtension.FieldNames.CATEGORY_CODE.databaseName())
                .varChar()
                .map(ConnectionStatusChangeDomainExtension.FieldNames.CATEGORY_CODE.javaName())
                .notNull()
                .add();
        table.column(ConnectionStatusChangeDomainExtension.FieldNames.CONFIRMATION_URL.databaseName())
                .varChar()
                .map(ConnectionStatusChangeDomainExtension.FieldNames.CONFIRMATION_URL.javaName())
                .notNull()
                .upTo(Version.version(10, 7))
                .add();
        table.column(ConnectionStatusChangeDomainExtension.FieldNames.REASON_CODE.databaseName())
                .varChar()
                .map(ConnectionStatusChangeDomainExtension.FieldNames.REASON_CODE.javaName())
                .add();
        table.column(ConnectionStatusChangeDomainExtension.FieldNames.PROCESS_DATE.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INSTANT)
                .map(ConnectionStatusChangeDomainExtension.FieldNames.PROCESS_DATE.javaName())
                .add();
    }

    @Override
    public String application() {
        return APPLICATION_NAME;
    }
}