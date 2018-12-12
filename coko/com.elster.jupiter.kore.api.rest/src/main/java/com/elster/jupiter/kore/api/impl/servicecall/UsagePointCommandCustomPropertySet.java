/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;

import com.google.inject.Module;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UsagePointCommandCustomPropertySet implements CustomPropertySet<ServiceCall, UsagePointCommandDomainExtension> {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    public UsagePointCommandCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return UsagePointCommandCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.CPS_DOMAIN_NAME_SERVICE_CALL).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, UsagePointCommandDomainExtension> getPersistenceSupport() {
        return new UsagePointCommandPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(UsagePointCommandDomainExtension.FieldNames.COMMANDS_EXPECTED.javaName(), UsagePointCommandDomainExtension.FieldNames.COMMANDS_EXPECTED.javaName())
                        .describedAs(UsagePointCommandDomainExtension.FieldNames.COMMANDS_EXPECTED.javaName())
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(UsagePointCommandDomainExtension.FieldNames.COMMANDS_SUCCESS.javaName(), UsagePointCommandDomainExtension.FieldNames.COMMANDS_SUCCESS.javaName())
                        .describedAs(UsagePointCommandDomainExtension.FieldNames.COMMANDS_SUCCESS.javaName())
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(UsagePointCommandDomainExtension.FieldNames.COMMANDS_FAILED.javaName(), UsagePointCommandDomainExtension.FieldNames.COMMANDS_FAILED.javaName())
                        .describedAs(UsagePointCommandDomainExtension.FieldNames.COMMANDS_FAILED.javaName())
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UsagePointCommandDomainExtension.FieldNames.CALLBACK_METHOD.javaName(), UsagePointCommandDomainExtension.FieldNames.CALLBACK_METHOD.javaName())
                        .describedAs(UsagePointCommandDomainExtension.FieldNames.CALLBACK_METHOD.javaName())
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UsagePointCommandDomainExtension.FieldNames.CALLBACK_SUCCESS.javaName(), UsagePointCommandDomainExtension.FieldNames.CALLBACK_SUCCESS.javaName())
                        .describedAs(UsagePointCommandDomainExtension.FieldNames.CALLBACK_SUCCESS.javaName())
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UsagePointCommandDomainExtension.FieldNames.CALLBACK_PART_SUCCESS.javaName(), UsagePointCommandDomainExtension.FieldNames.CALLBACK_PART_SUCCESS.javaName())
                        .describedAs(UsagePointCommandDomainExtension.FieldNames.CALLBACK_PART_SUCCESS.javaName())
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UsagePointCommandDomainExtension.FieldNames.CALLBACK_FAILURE.javaName(), UsagePointCommandDomainExtension.FieldNames.CALLBACK_FAILURE.javaName())
                        .describedAs(UsagePointCommandDomainExtension.FieldNames.CALLBACK_FAILURE.javaName())
                        .finish()
        );
    }

    private class UsagePointCommandPersistenceSupport implements PersistenceSupport<ServiceCall, UsagePointCommandDomainExtension> {
        private final String TABLE_NAME = "PRI_CPS_UP_COMMAND";
        private final String FK_MTR_CPS_UP_COMMAND = "FK_PRI_CPS_UP_COMMAND";

        @Override
        public String componentName() {
            return "PRS";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return UsagePointCommandDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_MTR_CPS_UP_COMMAND;
        }

        @Override
        public Class<UsagePointCommandDomainExtension> persistenceClass() {
            return UsagePointCommandDomainExtension.class;
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
            table
                    .column(UsagePointCommandDomainExtension.FieldNames.COMMANDS_EXPECTED.databaseName())
                    .number()
                    .map(UsagePointCommandDomainExtension.FieldNames.COMMANDS_EXPECTED.javaName())
                    .notNull()
                    .add();
            table
                    .column(UsagePointCommandDomainExtension.FieldNames.COMMANDS_SUCCESS.databaseName())
                    .number()
                    .map(UsagePointCommandDomainExtension.FieldNames.COMMANDS_SUCCESS.javaName())
                    .notNull()
                    .add();
            table
                    .column(UsagePointCommandDomainExtension.FieldNames.COMMANDS_FAILED.databaseName())
                    .number()
                    .map(UsagePointCommandDomainExtension.FieldNames.COMMANDS_FAILED.javaName())
                    .notNull()
                    .add();
            table
                    .column(UsagePointCommandDomainExtension.FieldNames.CALLBACK_METHOD.databaseName())
                    .varChar(Table.NAME_LENGTH)
                    .map(UsagePointCommandDomainExtension.FieldNames.CALLBACK_METHOD.javaName())
                    .notNull()
                    .add();
            table
                    .column(UsagePointCommandDomainExtension.FieldNames.CALLBACK_SUCCESS.databaseName())
                    .varChar(Table.MAX_STRING_LENGTH)
                    .map(UsagePointCommandDomainExtension.FieldNames.CALLBACK_SUCCESS.javaName())
                    .notNull()
                    .add();
            table
                    .column(UsagePointCommandDomainExtension.FieldNames.CALLBACK_PART_SUCCESS.databaseName())
                    .varChar(Table.MAX_STRING_LENGTH)
                    .map(UsagePointCommandDomainExtension.FieldNames.CALLBACK_PART_SUCCESS.javaName())
                    .notNull()
                    .add();
            table
                    .column(UsagePointCommandDomainExtension.FieldNames.CALLBACK_FAILURE.databaseName())
                    .varChar(Table.MAX_STRING_LENGTH)
                    .map(UsagePointCommandDomainExtension.FieldNames.CALLBACK_FAILURE.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return "Pulse";
        }
    }

}
