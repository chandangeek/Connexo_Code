/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.parent;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.ServiceCall;

import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.parent.AbstractMasterDomainExtension;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractMasterCustomPropertyPersistenceSupport<E extends AbstractMasterDomainExtension>
        implements PersistenceSupport<ServiceCall, E> {

    @Override
    public abstract String componentName();

    @Override
    public abstract String tableName();

    @Override
    public String domainFieldName() {
        return AbstractMasterDomainExtension.FieldNames.DOMAIN.javaName();
    }

    @Override
    public abstract String domainForeignKeyName();

    @Override
    public abstract Class<E> persistenceClass();

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
        table.column(AbstractMasterDomainExtension.FieldNames.CALLS_SUCCESS.databaseName()).number()
                .map(AbstractMasterDomainExtension.FieldNames.CALLS_SUCCESS.javaName()).notNull().add();
        table.column(AbstractMasterDomainExtension.FieldNames.CALLS_FAILED.databaseName()).number()
                .map(AbstractMasterDomainExtension.FieldNames.CALLS_FAILED.javaName()).notNull().add();
        table.column(AbstractMasterDomainExtension.FieldNames.CALLS_EXPECTED.databaseName()).number()
                .map(AbstractMasterDomainExtension.FieldNames.CALLS_EXPECTED.javaName()).notNull().add();
        table.column(AbstractMasterDomainExtension.FieldNames.CALLBACK_URL.databaseName()).varChar()
                .map(AbstractMasterDomainExtension.FieldNames.CALLBACK_URL.javaName()).notNull(false).add();
        table.column(AbstractMasterDomainExtension.FieldNames.CORRELATION_ID.databaseName()).varChar()
                .map(AbstractMasterDomainExtension.FieldNames.CORRELATION_ID.javaName()).notNull(false).since(Version.version(10,7)).add();

    }

    @Override
    public String application() {
        return "MultiSense";
    }
}
