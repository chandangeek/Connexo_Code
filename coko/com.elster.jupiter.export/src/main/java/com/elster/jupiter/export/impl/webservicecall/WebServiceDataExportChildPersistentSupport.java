package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.ServiceCall;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WebServiceDataExportChildPersistentSupport implements PersistenceSupport<ServiceCall, WebServiceDataExportChildDomainExtension> {
    public static final String COMPONENT_NAME = "DE1";
    static final String APPLICATION_NAME = "Pulse";


    private static final String FK_NAME = COMPONENT_NAME + "_FK_WEB_SC_CPS_CSC";
    private static final String UK_NAME = COMPONENT_NAME + "_UK_WS_CPS_NAME_MRID";
    private static final String TABLE_NAME = COMPONENT_NAME + "_WS_CALL_CHILD_CPS";

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
        return WebServiceDataExportChildDomainExtension.FieldNames.DOMAIN.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return FK_NAME;
    }

    @Override
    public Class<WebServiceDataExportChildDomainExtension> persistenceClass() {
        return WebServiceDataExportChildDomainExtension.class;
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
        Column deviceName = table.column(WebServiceDataExportChildDomainExtension.FieldNames.DEVICE_NAME.databaseName())
                .varChar()
                .map(WebServiceDataExportChildDomainExtension.FieldNames.DEVICE_NAME.javaName())
                .notNull()
                .add();
        Column readingTypeMrId = table.column(WebServiceDataExportChildDomainExtension.FieldNames.READING_TYPE_MRID.databaseName())
                .varChar()
                .map(WebServiceDataExportChildDomainExtension.FieldNames.READING_TYPE_MRID.javaName())
                .notNull()
                .add();
        Column dataSourceId = table.column(WebServiceDataExportChildDomainExtension.FieldNames.DATA_SOURCE_ID.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2LONG)
                .map(WebServiceDataExportChildDomainExtension.FieldNames.DATA_SOURCE_ID.javaName())
                .notNull()
                .add();

    }

    @Override
    public String application() {
        return APPLICATION_NAME;
    }
}
