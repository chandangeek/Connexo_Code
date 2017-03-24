/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;

public class InheritanceMappingTest {

    private Injector injector;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();

    @Before
    public void setUp() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new UtilModule(),
                new ThreadSecurityModule(() -> "test"),
                new PubSubModule(),
                new TransactionModule(false),
                new OrmModule());

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            OrmService service = injector.getInstance(OrmService.class);
            DataModel ormDataModel = service.newDataModel("ORM", "forTest");
            Arrays.stream(com.elster.jupiter.orm.internal.TableSpecs.values())
                    .forEach(tableSpecs -> tableSpecs.addTo(ormDataModel));
            ormDataModel.register();
            ormDataModel.install(true, true);
            ctx.commit();
        }
    }

    @After
    public void cleanUp() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void registerModelWithDiscriminator() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            OrmService service = injector.getInstance(OrmService.class);
            DataModel dataModel = service.newDataModel("TST", "forTest");
            Arrays.stream(TableSpecs.values())
                    .forEach(tableSpecs -> tableSpecs.addTo(dataModel, true));
            dataModel.register();
            dataModel.install(true, true);
            ctx.commit();
        }
    }

    @Test
    @Expected(IllegalTableMappingException.class)
    public void registerModelWithoutDiscriminator() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            OrmService service = injector.getInstance(OrmService.class);
            DataModel dataModel = service.newDataModel("TST", "forTest");
            Arrays.stream(TableSpecs.values())
                    .forEach(tableSpecs -> tableSpecs.addTo(dataModel, false));
            dataModel.register();
            dataModel.install(true, true);
            ctx.commit();
        }
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
        }
    }

    private enum TableSpecs {
        NAMER {
            @Override
            public void addTo(DataModel dataModel, boolean addDiscriminator) {
                Table<Namer> testBTable = dataModel.addTable(name(), Namer.class);
                testBTable.map(ImmutableMap.of(TestB.class.getName(), TestB.class, TestC.class.getName(), TestC.class));
                Column idColumn = testBTable.addAutoIdColumn();
                if (addDiscriminator) {
                    testBTable.addDiscriminatorColumn("discriminator", "varchar(200)");
                }
                testBTable.column("NAME").varChar(80).notNull().map("name").add();
                testBTable.primaryKey("TEST_B_PK_CONSTRAINT").on(idColumn).add();
            }
        };

        public abstract void addTo(DataModel dataModel, boolean addDiscriminator);
    }

    private interface Namer {
        String name();

    }

    private static class TestC implements Namer {
        private long id;
        private String name;

        public void init(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

    }

    private static class TestB implements Namer {
        private long id;
        private String name;

        public void init(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

    }
}
