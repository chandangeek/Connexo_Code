package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MetrologyTestPersistentSupport implements PersistenceSupport<MetrologyConfiguration, MetrologyTestPersistentDomainExtension> {

    @Override
    public String componentName() {
        return "MC1";
    }

    @Override
    public String tableName() {
        return componentName() + "_CPS_TEST";
    }

    @Override
    public String domainFieldName() {
        return MetrologyTestPersistentDomainExtension.Fields.METROLOGY_CONFIG.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return componentName() + "FK_TO_M_CONFIG";
    }

    @Override
    public Class<MetrologyTestPersistentDomainExtension> persistenceClass() {
        return MetrologyTestPersistentDomainExtension.class;
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
        table.column(MetrologyTestPersistentDomainExtension.Fields.NAME.databaseName())
                .varChar(Table.NAME_LENGTH)
                .map(MetrologyTestPersistentDomainExtension.Fields.NAME.javaName())
                .notNull()
                .add();
        table.column(MetrologyTestPersistentDomainExtension.Fields.ENHANCED_SUPPORT.databaseName())
                .bool()
                .map(MetrologyTestPersistentDomainExtension.Fields.ENHANCED_SUPPORT.javaName())
                .add();
    }
}