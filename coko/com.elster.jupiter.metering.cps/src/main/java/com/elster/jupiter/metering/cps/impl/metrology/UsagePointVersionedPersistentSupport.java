package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

import javax.validation.MessageInterpolator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UsagePointVersionedPersistentSupport implements PersistenceSupport<UsagePoint, UsagePointVersionedPersistentDomainExtension> {

    private Thesaurus thesaurus;

    public UsagePointVersionedPersistentSupport(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String componentName() {
        return "UC2";
    }

    @Override
    public String tableName() {
        return componentName() + "_CPS_TEST";
    }

    @Override
    public String domainFieldName() {
        return UsagePointSimplePersistentDomainExtension.Fields.USAGE_POINT.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return componentName() + "_FK_TO_UP";
    }

    @Override
    public Class<UsagePointVersionedPersistentDomainExtension> persistenceClass() {
        return UsagePointVersionedPersistentDomainExtension.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.of(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        });
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        table.column(UsagePointSimplePersistentDomainExtension.Fields.NAME.databaseName())
                .varChar(Table.NAME_LENGTH)
                .map(UsagePointSimplePersistentDomainExtension.Fields.NAME.javaName())
                .notNull()
                .add();
        table.column(UsagePointSimplePersistentDomainExtension.Fields.ENHANCED_SUPPORT.databaseName())
                .bool()
                .map(UsagePointSimplePersistentDomainExtension.Fields.ENHANCED_SUPPORT.javaName())
                .add();
    }
}