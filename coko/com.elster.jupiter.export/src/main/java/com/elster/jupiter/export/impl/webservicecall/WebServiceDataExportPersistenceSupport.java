/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.ServiceCall;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WebServiceDataExportPersistenceSupport implements PersistenceSupport<ServiceCall, WebServiceDataExportDomainExtension> {
    static final String APPLICATION_NAME = "Pulse";

    private static final String FK_NAME = DataExportService.COMPONENTNAME + "_FK_WEB_SERVICE_CALL_CPS";
    private static final String TABLE_NAME = DataExportService.COMPONENTNAME + "_WEB_SERVICE_CALL_CPS";

    @Override
    public String componentName() {
        return DataExportService.COMPONENTNAME;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String domainFieldName() {
        return WebServiceDataExportDomainExtension.FieldNames.DOMAIN.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return FK_NAME;
    }

    @Override
    public Class<WebServiceDataExportDomainExtension> persistenceClass() {
        return WebServiceDataExportDomainExtension.class;
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
        table.column(WebServiceDataExportDomainExtension.FieldNames.UUID.databaseName())
                .varChar()
                .map(WebServiceDataExportDomainExtension.FieldNames.UUID.javaName())
                .notNull()
                .add();
        table.column(WebServiceDataExportDomainExtension.FieldNames.TIMEOUT.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2LONG)
                .map(WebServiceDataExportDomainExtension.FieldNames.TIMEOUT.javaName())
                .notNull()
                .add();
        table.column(WebServiceDataExportDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                .varChar()
                .map(WebServiceDataExportDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                .add();
    }

    @Override
    public String application() {
        return APPLICATION_NAME;
    }
}
