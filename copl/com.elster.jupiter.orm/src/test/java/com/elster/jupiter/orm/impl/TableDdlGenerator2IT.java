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
public class TableDdlGenerator2IT {

    public static class Movie1 {
        private long id;
        private String title;
    }

    public static class Production1 {
        private long id;
        private String title;
    }

    public static class Actor1 {
        private long id;
        private Reference<Movie1> movie = ValueReference.absent();
        private String name;
    }

    public static class Actor2 {
        private long id;
        private Reference<Production1> production = ValueReference.absent();
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
    public void testUpgradeTo2ndVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        the2ndVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(1, 0));

//        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
//            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
//                ResultSet resultSet = preparedStatement.executeQuery();
//                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
//                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
//                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(2);
//            }
//            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO TST_MOVIE (ID, TITLE) values (?, ?)")) {
//                statement.setLong(1, 5000);
//                statement.setString(2, "The Shawshank Redemption");
//                statement.executeUpdate();
//            }
//        }

        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(2, 0));

//        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
//            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
//                ResultSet resultSet = preparedStatement.executeQuery();
//                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
//                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
//                assertThat(resultSet.findColumn("DIRECTOR")).isEqualTo(3);
//                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
//            }
//        }
//
//        DataMapper<Movie2> movieDao = dataModel.mapper(Movie2.class);
//
//        List<Movie2> movies = movieDao.find();
//        assertThat(movies).hasSize(1);
//        Movie2 movie = movies.get(0);
//        assertThat(movie.title).isEqualTo("The Shawshank Redemption");
//        assertThat(movie.director).isNull();
//
//        try (TransactionContext context = transactionService.getContext()) {
//            movieDao.remove(movie);
//            movie = new Movie2();
//            movie.title = "The Graduate";
//            movie.director = "Mike Nichols";
//            movieDao.persist(movie);
//            context.commit();
//        }
//
//        movies = movieDao.find();
//        assertThat(movies).hasSize(1);
//        movie = movies.get(0);
//        assertThat(movie.title).isEqualTo("The Graduate");
//        assertThat(movie.director).isEqualTo("Mike Nichols");
//
//        try (TransactionContext context = transactionService.getContext()) {
//            movie.title = "The Nutty Professor";
//            movie.director = "Tom Shadyac";
//            movieDao.update(movie);
//            context.commit();
//        }
//
//        movies = movieDao.find();
//        assertThat(movies).hasSize(1);
//        movie = movies.get(0);
//        assertThat(movie.title).isEqualTo("The Nutty Professor");
//        assertThat(movie.director).isEqualTo("Tom Shadyac");
    }

    private void the1stVersionsCode(DataModel dataModel) {
        {
            Table<Movie1> movieTable = dataModel.addTable("TST_MOVIE", Movie1.class);
            movieTable.map(Movie1.class);
            Column idColumn = movieTable.addAutoIdColumn();
            movieTable.column("TITLE").map("title").varChar(80).add();
            movieTable.primaryKey("TST_MOVIE_PK").on(idColumn).add();
        }
        {
            Table<Actor1> actorTable = dataModel.addTable("TST_ACTOR", Actor1.class);
            actorTable.map(Actor1.class);
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").varChar(80).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().add();
            actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie1.class)
                    .map("movie")
                    .add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

    // adds a column
    private void the2ndVersionsCode(DataModel dataModel) {
        {
            Table<Movie1> movieTable = dataModel.addTable("TST_MOVIE", Movie1.class);
            movieTable.map(Movie1.class);
            Column idColumn = movieTable.addAutoIdColumn();
            movieTable.column("TITLE").map("title").varChar(80).add();
            movieTable.primaryKey("TST_MOVIE_PK").on(idColumn).add();
        }
        {
            Table<Production1> productionTable = dataModel.addTable("TST_PRODUCTION", Production1.class);
            productionTable.map(Production1.class);
            productionTable.since(version(2, 0));
            Column idColumn = productionTable.addAutoIdColumn();
            productionTable.column("TITLE").map("title").varChar(80).add();
            productionTable.primaryKey("TST_PRODUCTION_PK").on(idColumn).add();
        }
        {
            Table<Actor2> actorTable = dataModel.addTable("TST_ACTOR", Actor2.class);
            actorTable.map(Actor2.class);
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").varChar(80).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().upTo(version(2, 0)).add();
            Column productionColumn = actorTable.column("PRODUCTION").number().notNull().since(version(2, 0)).add();
            actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie1.class)
                    .map("movie")
                    .upTo(version(2, 0))
                    .add();
            actorTable.foreignKey("TST_FK_ACTOR_IN_PRODUCTION")
                    .on(productionColumn)
                    .references(Production1.class)
                    .map("production")
                    .since(version(2, 0))
                    .add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

}
