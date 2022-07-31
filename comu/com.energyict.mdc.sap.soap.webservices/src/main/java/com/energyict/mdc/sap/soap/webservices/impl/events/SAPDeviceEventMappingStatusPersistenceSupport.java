/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.events;

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

public class SAPDeviceEventMappingStatusPersistenceSupport implements PersistenceSupport<ServiceCall, SAPDeviceEventMappingStatusDomainExtension> {

    private static final String COMPONENT_NAME = "SDE";
    private static final String TABLE_NAME = COMPONENT_NAME + "_EVENT_MAPPING_STATUS";
    private static final String FK_NAME = "FK_" + TABLE_NAME + "_SC";

    @Override
    public String componentName() {
        return COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String domainColumnName() {
        return SAPDeviceEventMappingStatusDomainExtension.FieldNames.SERVICE_CALL.databaseName();
    }

    @Override
    public String domainFieldName() {
        return SAPDeviceEventMappingStatusDomainExtension.FieldNames.SERVICE_CALL.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return FK_NAME;
    }

    @Override
    public Class<SAPDeviceEventMappingStatusDomainExtension> persistenceClass() {
        return SAPDeviceEventMappingStatusDomainExtension.class;
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
        table.column(SAPDeviceEventMappingStatusDomainExtension.FieldNames.LOADED_ENTRIES_NUMBER.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INTWRAPPER)
                .map(SAPDeviceEventMappingStatusDomainExtension.FieldNames.LOADED_ENTRIES_NUMBER.javaName())
                .notNull()
                .add();
        table.column(SAPDeviceEventMappingStatusDomainExtension.FieldNames.FAILED_ENTRIES_NUMBER.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INTWRAPPER)
                .map(SAPDeviceEventMappingStatusDomainExtension.FieldNames.FAILED_ENTRIES_NUMBER.javaName())
                .notNull()
                .add();
        table.column(SAPDeviceEventMappingStatusDomainExtension.FieldNames.SKIPPED_ENTRIES_NUMBER.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INTWRAPPER)
                .map(SAPDeviceEventMappingStatusDomainExtension.FieldNames.SKIPPED_ENTRIES_NUMBER.javaName())
                .notNull()
                .add();
        table.column(SAPDeviceEventMappingStatusDomainExtension.FieldNames.PATH.databaseName())
                .varChar()
                .map(SAPDeviceEventMappingStatusDomainExtension.FieldNames.PATH.javaName())
                .notNull()
                .add();
        table.column(SAPDeviceEventMappingStatusDomainExtension.FieldNames.SEPARATOR.databaseName())
                .varChar()
                .map(SAPDeviceEventMappingStatusDomainExtension.FieldNames.SEPARATOR.javaName())
                .notNull()
                .add();
        table.column(SAPDeviceEventMappingStatusDomainExtension.FieldNames.COLUMN_VALUE_SEPARATOR.databaseName())
                .varChar()
                .map(SAPDeviceEventMappingStatusDomainExtension.FieldNames.COLUMN_VALUE_SEPARATOR.javaName())
                .since(Version.version(10, 7, 17))
                .installValue("'/'")
                .notNull()
                .add();
    }

    @Override
    public String application() {
        return WebServiceActivator.APPLICATION_NAME;
    }
}
