/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.cps;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UsagePointTestPersistentSupport implements PersistenceSupport<UsagePoint, UsagePointTestPersistentDomainExtension> {
    @Override
    public String application() {
        return "Example";
    }

    @Override
    public String componentName() {
        return "UP1";
    }

    @Override
    public String tableName() {
        return componentName() + "_CPS_TEST";
    }

    @Override
    public String domainFieldName() {
        return "usagePoint";
    }

    @Override
    public String domainForeignKeyName() {
        return componentName() + "FK_TO_USAGE_POINT";
    }

    @Override
    public Class<UsagePointTestPersistentDomainExtension> persistenceClass() {
        return UsagePointTestPersistentDomainExtension.class;
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
        table.column(CustomPropertySetAttributes.NAME.databaseName())
                .varChar(Table.NAME_LENGTH)
                .map(CustomPropertySetAttributes.NAME.propertyKey())
                .notNull()
                .add();
        table.column(CustomPropertySetAttributes.ENHANCED_SUPPORT.databaseName())
                .bool()
                .map(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())
                .add();
    }
}