/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
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

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;

public class BlobTest {

    private Injector injector;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
        }
    }

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
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testRegisterNonBlobType() {
        OrmService service = injector.getInstance(OrmService.class);
        DataModel dataModel;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            dataModel = service.newDataModel("TST", "forTest");
            Arrays.stream(TableSpecs.values())
                    .forEach(tableSpecs -> tableSpecs.addTo(dataModel, false));
            dataModel.register();
            dataModel.install(true, true);
            ctx.commit();
        }
    }

    @Test
    @Expected(IllegalTableMappingException.class)
    public void testRegisterBlobType() {
        OrmService service = injector.getInstance(OrmService.class);
        DataModel dataModel;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            dataModel = service.newDataModel("TST", "forTest");
            Arrays.stream(TableSpecs.values())
                    .forEach(tableSpecs -> tableSpecs.addTo(dataModel, true));
            dataModel.register();
            dataModel.install(true, true);
            ctx.commit();
        }
    }

    private static class Data {
        private long id;
        private byte[] bytes;
    }

    private enum TableSpecs {
        DATA {
            @Override
            void addTo(DataModel model, boolean blobType) {
                Table<Data> table = model.addTable(name(), Data.class);
                table.map(Data.class);
                Column idColumn = table.addAutoIdColumn();
                Column.Builder blobBuilder = table.column("BYTES")
                        .blob()
                        .map("bytes");
                if (!blobType) {
                    blobBuilder.conversion(ColumnConversion.BLOB2BYTE);
                }
                blobBuilder.add();
                table.primaryKey("DATA_PK").on(idColumn).add();
            }
        };


        abstract void addTo(DataModel model, boolean blobType);
    }
}
