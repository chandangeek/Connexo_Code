/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryPersistence;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DdlDifference;
import com.elster.jupiter.orm.Difference;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;
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
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import javax.validation.constraints.Size;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TableDdlGeneratorJournalingDiffsIT {

    public static class Movie1 {
        @SuppressWarnings("unused")
        private long id;
        @Size(max = 150)
        @SuppressWarnings("unused")
        private String title;
    }

    // Movie with name of directory
    public static class Movie2 {
        @SuppressWarnings("unused")
        private long id;
        @Size(max = 150)
        @SuppressWarnings("unused")
        private String title;
        @SuppressWarnings("unused")
        @Size(max = 80)
        private String director;
    }

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

    @Test
    public void noJournalTableChanges() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");
        the1stVersionsCode(dataModel);
        dataModel.register();
        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(1, 0));
        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getTables(null, null, "TST_MOVIEJRNL", null)) {
                assertThat(resultSet.next()).isFalse();
            }
        }

        // Business method
        List<Difference> differences = ormService.getDataModelDifferences(Logger.getAnonymousLogger())
                .findDifferences();

        // Asserts
        assertThat(differences).isEmpty();
    }

    @Test
    public void addJournalTableOnly() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");
        the2ndVersionsCode(dataModel);
        dataModel.register();
        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(1, 0));
        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getTables(null, null, "TST_MOVIEJRNL", null)) {
                assertThat(resultSet.next()).isFalse();
            }
        }

        // Business method
        List<Difference> differences = ormService.getDataModelDifferences(Logger.getAnonymousLogger())
                .findDifferences();

        // Asserts

        assertThat(differences).hasSize(1);
        Difference difference = differences.get(0);
        assertThat(((DdlDifference)difference).ddl()).hasSize(1);
        String createTableDdl = ((DdlDifference)difference).ddl().get(0);
        assertThat(createTableDdl.toUpperCase()).startsWith("create table TST_MOVIEJRNL ".toUpperCase());

    }

    @Test
    public void addColumnAndJournalTable() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");
        the3rdVersionsCode(dataModel);
        dataModel.register();
        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(1, 0));
        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getTables(null, null, "TST_MOVIEJRNL", null)) {
                assertThat(resultSet.next()).isFalse();
            }
        }

        // Business method
        List<Difference> differences = ormService.getDataModelDifferences(Logger.getAnonymousLogger())
                .findDifferences();

        // Asserts
        assertThat(differences).hasSize(2);
        DdlDifference difference = (DdlDifference) differences.get(1);
        assertThat(difference.ddl()).hasSize(1);
        String createTableDdl = difference.ddl().get(0);
        assertThat(createTableDdl.toUpperCase()).startsWith("create table TST_MOVIEJRNL ".toUpperCase());
        assertThat(createTableDdl.toUpperCase()).contains("DIRECTOR");

    }

    @Test
    public void renameColumnWithJournalTable() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");
        the4thVersionsCode(dataModel);
        dataModel.register();
        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(1, 0));
        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getTables(null, null, "TST_MOVIEJRNL", null)) {
                assertThat(resultSet.next()).isFalse();
            }
        }

        // Business method
        List<Difference> differences = ormService.getDataModelDifferences(Logger.getAnonymousLogger())
                .findDifferences();

        // Asserts
        assertThat(differences).hasSize(2);
        DdlDifference difference = (DdlDifference) differences.get(1);
        assertThat(difference.ddl()).hasSize(1);
        String createTableDdl = difference.ddl().get(0);
        assertThat(createTableDdl.toUpperCase()).startsWith("create table TST_MOVIEJRNL ".toUpperCase());
        assertThat(createTableDdl.toUpperCase()).contains("DIRECTOR_NAME");
    }

    @Test
    public void removeJournalTable() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");
        the5thVersionsCode(dataModel);
        dataModel.register();
        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(4, 0));
        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getTables(null, null, "TST_MOVIEJRNL", null)) {
                assertThat(resultSet.next()).isTrue();
            }
        }
        // Create Movie that we will modify after the upgrade
        Movie2 movie = new Movie2();
        movie.id = 1;
        movie.title = "Attack of the killer tomatoes";
        try (TransactionContext context = transactionService.getContext()) {
            dataModel.persist(movie);
            context.commit();
        }

        // Business method
        List<Difference> differences = ormService.getDataModelDifferences(Logger.getAnonymousLogger())
                .findDifferences();

        // Asserts
        assertThat(differences).hasSize(1);
        DdlDifference difference = (DdlDifference) differences.get(0);
        assertThat(difference.ddl()).hasSize(1);
        String dropTableDdl = difference.ddl().get(0);
        assertThat(dropTableDdl.toUpperCase()).startsWith("drop table TST_MOVIEJRNL".toUpperCase());

    }

    // Adds journal table on both tables.
    private void the1stVersionsCode(DataModel dataModel) {
        Table<Movie1> table = dataModel.addTable("TST_MOVIE", Movie1.class);
        table.map(Movie1.class);
        Column id = table.addAutoIdColumn();
        table.column("TITLE").varChar().map("title").add();
        table.primaryKey("TST_MOVIE_PK").on(id).add();
    }
     // Adds journal table on both tables.
    private void the2ndVersionsCode(DataModel dataModel) {
        Table<Movie1> table = dataModel.addTable("TST_MOVIE", Movie1.class);
        table.map(Movie1.class);
        table.setJournalTableName("TST_MOVIEJRNL").since(version(2, 0));
        Column id = table.addAutoIdColumn();
        table.column("TITLE").varChar().map("title").add();
        table.primaryKey("TST_MOVIE_PK").on(id).add();
    }

     // Adds director column.
    private void the3rdVersionsCode(DataModel dataModel) {
        Table<Movie2> table = dataModel.addTable("TST_MOVIE", Movie2.class);
        table.map(Movie2.class);
        table.setJournalTableName("TST_MOVIEJRNL").since(version(3, 0));
        Column id = table.addAutoIdColumn();
        table.column("TITLE").varChar().map("title").add();
        table.column("DIRECTOR").varChar().map("director").since(version(3, 0)).add();
        table.primaryKey("TST_MOVIE_PK").on(id).add();
    }

     // Renames director column while journal is active.
    private void the4thVersionsCode(DataModel dataModel) {
        Table<Movie2> table = dataModel.addTable("TST_MOVIE", Movie2.class);
        table.map(Movie2.class);
        table.setJournalTableName("TST_MOVIEJRNL").since(version(3, 0));
        Column id = table.addAutoIdColumn();
        table.column("TITLE").varChar().map("title").add();
        Column director = table.column("DIRECTOR").varChar().map("director").upTo(version(4, 0)).add();
        table.column("DIRECTOR_NAME").varChar().map("director").since(version(4, 0)).previously(director).add();
        table.primaryKey("TST_MOVIE_PK").on(id).add();
    }

     // Removes journal table.
    private void the5thVersionsCode(DataModel dataModel) {
        Table<Movie2> table = dataModel.addTable("TST_MOVIE", Movie2.class);
        table.map(Movie2.class);
        table.setJournalTableName("TST_MOVIEJRNL").upTo(version(5, 0));
        Column id = table.addAutoIdColumn();
        table.column("TITLE").varChar().map("title").add();
        table.column("DIRECTOR").varChar().map("director").add();
        table.primaryKey("TST_MOVIE_PK").on(id).add();
    }

}