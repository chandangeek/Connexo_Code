/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.orm.Version.version;

@RunWith(MockitoJUnitRunner.class)
public class TableDdlGenerator4IT {

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
        }
    }

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private final Instant now = ZonedDateTime.of(2016, 1, 8, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    private Injector injector;

    private TransactionService transactionService;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private LogService logService;
    @Mock
    private MessageInterpolator messageInterpolator;

    private Clock clock;
    private OrmService ormService;

    @Before
    public void setUp() {
        clock = new ProgrammableClock(ZoneId.of("UTC"), now);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new OrmModule(),
                    new UtilModule(clock),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.run(() -> {
            ormService = injector.getInstance(OrmService.class);
            SchemaInfoProvider schemaInfoProvider = injector.getInstance(SchemaInfoProvider.class);
            ((OrmServiceImpl) ormService).setSchemaInfoProvider(schemaInfoProvider);
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    /** Prior version 10.2 we store book's price in a separate table, but in version 10.2 we included price in that table */
    public static class Book {
        public long id;
        public long price;
    }

    /** Let's assume that this class has a plenty of cool methods for price manipulation and we don't want embed all of them into the Book class,
        so we still use this class. */
    public static class BookPrice {
        /** com.elster.jupiter.orm.IllegalTableMappingException: Table BOOK_PRICE : Column BOOK has no mapping */
        // public Reference<Book> book = ValueReference.absent(); // This field was replaced by Book book
        public Book book;
        /** com.elster.jupiter.orm.MappingException: No mapping found for field price on class com.elster.jupiter.orm.impl.TableDdlGenerator4IT$BookPrice */
        // public long price; // this field was removed, now we use book.price
    }

    @Test
    public void testCreateDataModelWithRemovedFields() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        Table<Book> bookTable = dataModel.addTable("BOOK", Book.class);
        bookTable.map(Book.class);
        Column idColumn = bookTable.column("ID").map("id").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
        bookTable.column("PRICE").map("price").number().conversion(ColumnConversion.NUMBER2LONG).since(version(10, 2)).notNull().add();
        bookTable.primaryKey("PK_BOOK").on(idColumn).add();

        Table<BookPrice> bookPrice = dataModel.addTable("BOOK_PRICE", BookPrice.class);
        bookPrice.map(BookPrice.class);
        bookPrice.upTo(version(10, 2));
        Column bookColumn = bookPrice.column("BOOK").number().notNull().add();
        bookPrice.column("PRICE").map("price").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
        bookPrice.primaryKey("PK_BOOK_PRICE").on(bookColumn).add();
        bookPrice.foreignKey("FK_BOOK_PRICE").on(bookColumn).map("book").references("BOOK").add();

        dataModel.register();
        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(2, 0));
    }
}