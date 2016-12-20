package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Encrypter;
import com.elster.jupiter.orm.MacException;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MacColumnTest {

    static class Movie1 {
        private long id;
        private String title;
    }

    static class Movie2 {
        private long id;
        private Reference<DVDCollection> collection = ValueReference.absent();
        private String title;
    }

    static class DVDCollection {
        private long id;
        private List<Movie2> movies = new ArrayList<>();
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
    public void testMapMac() throws SQLException {
        DataModel dataModel = dbSetup();

        try (TransactionContext context = transactionService.getContext()) {
            Movie1 movie1 = new Movie1();
            movie1.title = "Jaws";
            dataModel.mapper(Movie1.class).persist(movie1);

            context.commit();
        }

        long id;

        try (
                Connection connection = dataModel.getConnection(false);
                PreparedStatement statement = connection.prepareStatement("select * from TST_MOVIE1");
                ResultSet resultSet = statement.executeQuery()
        ) {
            assertThat(resultSet.next()).isTrue();
            String mac = resultSet.getString("mac");

            id = resultSet.getLong("ID");

            assertThat(mac)
                    .isNotNull()
                    .isNotEmpty();
        }

        Movie1 loadedMovie = dataModel.mapper(Movie1.class).getExisting(id);
        assertThat(loadedMovie.title).isEqualTo("Jaws");
    }

    @Test
    public void testUpdateMac() throws SQLException {
        DataModel dataModel = dbSetup();

        long id;

        DataMapper<Movie1> mapper = dataModel.mapper(Movie1.class);
        try (TransactionContext context = transactionService.getContext()) {
            Movie1 movie1 = new Movie1();
            movie1.title = "Jaws";
            mapper.persist(movie1);
            id = movie1.id;

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            Movie1 loadedMovie = mapper.getExisting(id);
            loadedMovie.title = "Jaws II";
            mapper.update(loadedMovie);
            context.commit();
        }

        {
            Movie1 loadedMovie = dataModel.mapper(Movie1.class).getExisting(id);
            assertThat(loadedMovie.title).isEqualTo("Jaws II");
        }
    }

    @Test
    public void testMultiUpdateSingleColumnMac() throws SQLException {
        DataModel dataModel = dbSetup();

        long id1, id2;

        DataMapper<Movie1> mapper = dataModel.mapper(Movie1.class);
        try (TransactionContext context = transactionService.getContext()) {
            Movie1 movie1 = new Movie1();
            movie1.title = "Jaws";
            mapper.persist(movie1);
            id1 = movie1.id;

            Movie1 movie2 = new Movie1();
            movie2.title = "Jaws II";
            mapper.persist(movie2);
            id2 = movie2.id;

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            List<Movie1> movie1s = mapper.find();

            movie1s.get(0).title = "Predator";
            movie1s.get(1).title = "Predator 2";

            mapper.update(movie1s.subList(0, 2), "title");

            context.commit();
        }

        {
            Movie1 loadedMovie = dataModel.mapper(Movie1.class).getExisting(id1);
            assertThat(loadedMovie).isInstanceOf(Movie1.class);
            assertThat(loadedMovie.title).isEqualTo("Predator");
            Movie1 loadedMovie2 = dataModel.mapper(Movie1.class).getExisting(id2);
            assertThat(loadedMovie2).isInstanceOf(Movie1.class);
            assertThat(loadedMovie2.title).isEqualTo("Predator 2");
        }
    }

    DataModel dbSetup() {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        {
            Table<Movie1> movieTable = dataModel.addTable("TST_MOVIE1", Movie1.class);
            movieTable.map(Movie1.class);
            Column idColumn = movieTable.addAutoIdColumn();
            movieTable.column("TITLE").map("title").varChar(80).add();
            movieTable.addMessageAuthenticationCodeColumn(MyEncrypter.INSTANCE);
            movieTable.primaryKey("TST_MOVIE1_PK").on(idColumn).add();
        }
        {
            Table<DVDCollection> dvdCollectionTable = dataModel.addTable("TST_DVDS", DVDCollection.class);
            dvdCollectionTable.map(DVDCollection.class);
            Column idColumn = dvdCollectionTable.addAutoIdColumn();
            dvdCollectionTable.primaryKey("TST_PK_DVD")
                    .on(idColumn)
                    .add();
        }
        {
            Table<Movie2> movieTable = dataModel.addTable("TST_MOVIE2", Movie2.class);
            movieTable.map(Movie2.class);
            Column idColumn = movieTable.addAutoIdColumn();
            movieTable.column("TITLE").map("title").varChar(80).add();
            movieTable.addMessageAuthenticationCodeColumn(MyEncrypter.INSTANCE);
            Column dvdColumn = movieTable.column("TST_DVD").number().add();
            movieTable.primaryKey("TST_MOVIE2_PK").on(idColumn).add();
            movieTable.foreignKey("TST_FK_MOV_DVD")
                    .on(dvdColumn)
                    .references(DVDCollection.class)
                    .composition()
                    .map("collection")
                    .reverseMap("movies")
                    .add();
        }
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
            }
        });

        DataModelUpgrader dataModelUpgrader = ormService.getDataModelUpgrader(Logger.getAnonymousLogger());
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        return dataModel;
    }

    @Test
    public void testCatchDBMeddlingForIdRead() throws SQLException {
        DataModel dataModel = dbSetup();

        long id;

        try (TransactionContext context = transactionService.getContext()) {
            Movie1 movie1 = new Movie1();
            movie1.title = "Jaws";
            dataModel.mapper(Movie1.class).persist(movie1);

            id = movie1.id;

            context.commit();
        }

        // meddle with DB
        try (
                Connection connection = dataModel.getConnection(false);
                PreparedStatement statement = connection.prepareStatement("update TST_MOVIE1 set TITLE = 'Jaws II' where id = " + id);
        ) {
            statement.executeUpdate();
        }

        try {
            Movie1 loadedMovie = dataModel.mapper(Movie1.class).getExisting(id);
            fail("expected MacException");
        } catch (MacException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCatchDBMeddlingForListRead() throws SQLException {
        DataModel dataModel = dbSetup();

        long id;

        try (TransactionContext context = transactionService.getContext()) {
            Movie1 movie1 = new Movie1();
            movie1.title = "Jaws";
            dataModel.mapper(Movie1.class).persist(movie1);

            id = movie1.id;

            context.commit();
        }

        // meddle with DB
        try (
                Connection connection = dataModel.getConnection(false);
                PreparedStatement statement = connection.prepareStatement("update TST_MOVIE1 set TITLE = 'Jaws II' where id = " + id);
        ) {
            statement.executeUpdate();
        }

        try {
            Movie1 loadedMovie = dataModel.mapper(Movie1.class).find().get(0);
            fail("expected MacException");
        } catch (MacException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCatchDBMeddlingForManagedList() throws SQLException {
        DataModel dataModel = dbSetup();
        DataMapper<DVDCollection> mapper = dataModel.mapper(DVDCollection.class);

        long id;

        try (TransactionContext context = transactionService.getContext()) {
            DVDCollection dvdCollection = new DVDCollection();
            Movie2 movie2 = new Movie2();
            movie2.title = "Jaws";
            movie2.collection.set(dvdCollection);
            dvdCollection.movies.add(movie2);
            mapper.persist(dvdCollection);
            id = movie2.id;

            context.commit();
        }

        // meddle with DB
        try (
                Connection connection = dataModel.getConnection(false);
                PreparedStatement statement = connection.prepareStatement("update TST_MOVIE2 set TITLE = 'Jaws II' where id = " + id);
        ) {
            statement.executeUpdate();
        }

        try {
            DVDCollection collection = mapper.find().get(0);
            Movie2 movie2 = collection.movies.get(0);
            fail("expected MacException");
        } catch (MacException e) {
            e.printStackTrace();
        }

    }

    private enum MyEncrypter implements Encrypter {
        INSTANCE;

        @Override
        public String encrypt(byte[] decrypted) {
            return new String(decrypted);
        }
    }

}
