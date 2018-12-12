/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
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
import java.util.ArrayList;
import java.util.List;
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
public class TableDdlGenerator3IT {

    public static class Movie1 {
        private long id;
        private String title;
    }

    public static class Movie2 {
        private long id;
        private String title;
        private String director;
    }

    public static class Movie3 {
        private long id;
        private String title;
        private String director;
        private List<Actor1> actors = new ArrayList<>();
    }

    public static class Actor1 {
        private long id;
        private Reference<Movie3> movie = ValueReference.absent();
        private String name;
    }

    public static class Actor2 {
        private long id;
        private String name;
    }

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
//            bind(ServiceCallTypeOneCustomPropertySet.class).to(ServiceCallTypeOneCustomPropertySet.class);
        }
    }

    private static final String IMPORTER_NAME = "someImporter";

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

    @Test
    public void testInstall4thVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        the4thVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, Version.latest());

        // just test that the old foreign key mapping doesn't raise exceptions
    }



    private void the1stVersionsCode(DataModel dataModel) {
        Table<Movie1> movieTable = dataModel.addTable("TST_MOVIE", Movie1.class);
        movieTable.map(Movie1.class);
        Column idColumn = movieTable.addAutoIdColumn();
        movieTable.column("TITLE").map("title").varChar(80).add();
        movieTable.primaryKey("TST_MOVIE_PK").on(idColumn).add();
    }

    // adds a column
    private void the2ndVersionsCode(DataModel dataModel) {
        Table<Movie2> movieTable = dataModel.addTable("TST_MOVIE", Movie2.class);
        movieTable.map(Movie2.class);
        Column idColumn = movieTable.addAutoIdColumn();
        movieTable.column("TITLE").map("title").varChar(80).add();
        movieTable.column("DIRECTOR").map("director").varChar(80).since(version(2, 0)).add();
        movieTable.primaryKey("TST_MOVIE_PK").on(idColumn).add();
    }

    // adds an actor table
    private void the3rdVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            Column idColumn = movieTable.addAutoIdColumn();
            movieTable.column("TITLE").map("title").notNull().varChar(80).add();
            movieTable.column("DIRECTOR").map("director").varChar(80).since(version(2, 0)).add();
            movieTable.primaryKey("TST_MOVIE_PK").on(idColumn).add();
        }
        {
            Table<Actor1> actorTable = dataModel.addTable("TST_ACTOR", Actor1.class);
            actorTable.map(Actor1.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").varChar(80).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().add();
            actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE").on(movieColumn).references(Movie3.class).map("movie").add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

    // remove a table
    private void the4thVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            movieTable.upTo(version(4, 0));
            Column idColumn = movieTable.addAutoIdColumn();
            movieTable.column("TITLE").map("title").notNull().varChar(80).add();
            movieTable.column("DIRECTOR").map("director").varChar(80).since(version(2, 0)).add();
            movieTable.primaryKey("TST_MOVIE_PK").on(idColumn).add();
        }
        {
            Table<Actor2> actorTable = dataModel.addTable("TST_ACTOR", Actor2.class);
            actorTable.map(Actor2.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").varChar(80).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().upTo(version(4, 0)).add();
            actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE").on(movieColumn).references(Movie3.class).map("movie").upTo(version(4, 0)).add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

}
