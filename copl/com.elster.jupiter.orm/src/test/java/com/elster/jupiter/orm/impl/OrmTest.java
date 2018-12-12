/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.internal.TableSpecs;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


@RunWith(MockitoJUnitRunner.class)
public class OrmTest {

    private Injector injector;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Mock
    private Principal principal;

    @Before
    public void setUp() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new UtilModule(),
                new ThreadSecurityModule(principal),
                new PubSubModule(),
                new TransactionModule(false),
                new OrmModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            OrmService service = injector.getInstance(OrmService.class);
            DataModel dataModel = service.newDataModel("ORM", "forTest");
            Arrays.stream(TableSpecs.values())
                    .forEach(tableSpecs -> tableSpecs.addTo(dataModel));
            dataModel.register();
            dataModel.install(true, true);
            ctx.commit();
        }
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testDataModel() {
        OrmService ormService = injector.getInstance(OrmService.class);
        DataModel dataModel = ormService.getDataModels().get(0);
        assertThat(dataModel.mapper(DataModel.class).find()).hasSize(1);
        assertThat(dataModel.mapper(Table.class).find().size()).isGreaterThan(4);
        assertThat(dataModel.mapper(Column.class).find().size()).isGreaterThan(10);
        assertThat(dataModel.mapper(TableConstraint.class).find()).isNotEmpty();
        assertThat(dataModel.mapper(ColumnInConstraintImpl.class).find()).isNotEmpty();
    }

    @Test
    public void testEagerQuery() {
        OrmService ormService = injector.getInstance(OrmService.class);
        DataModel dataModel = ormService.getDataModels().get(0);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            Optional<DataModel> copy = dataModel.mapper(DataModel.class).getEager("ORM");
            for (Table<?> each : copy.get().getTables()) {
                each.getColumns().size();
                each.getConstraints().size();
                for (TableConstraint constraint : each.getConstraints()) {
                    constraint.getColumns().size();
                }
            }
            ctx.commit();
            assertThat(ctx.getStats().getSqlCount()).isEqualTo(1);
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            Optional<Column> column = dataModel.mapper(Column.class).getEager("ORM", "ORM_TABLE", "NAME");
            assertThat(column.isPresent()).isTrue();
        }
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
        }
    }

}
