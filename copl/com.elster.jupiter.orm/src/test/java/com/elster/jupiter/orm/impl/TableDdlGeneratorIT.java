package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryPersistence;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.PrimaryKeyConstraint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UniqueConstraint;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;
import com.elster.jupiter.orm.schema.h2.H2SchemaInfo;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;

import com.google.common.collect.Range;
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
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
import static org.assertj.core.api.Assertions.fail;

@RunWith(MockitoJUnitRunner.class)
public class TableDdlGeneratorIT {

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
        private long movie;
        private String name;
    }

    public static class Actor3 {
        private long id;
        private long movie;
        private String name;
        private boolean lead;
    }

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
            bind(SchemaInfoProvider.class).to(H2SchemaInfo.class);
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
    public void testInstallFirstVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        firstVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(2);
            }
        }
    }

    @Test
    public void testInstallSecondVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        secondVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
        }
    }

    @Test
    public void testInstallFirstVersionThenUpgradeToSecond() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        secondVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(1, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(2);
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, version(2, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
        }
    }

    @Test
    public void testInstallThirdVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        thirdVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_ACTOR")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("NAME")).isEqualTo(2);
                assertThat(resultSet.findColumn("MOVIE")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
        }
    }

    @Test
    public void testInstallFourthVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        fourthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("MAIN_DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_ACTOR")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("NAME")).isEqualTo(2);
                assertThat(resultSet.findColumn("MOVIE")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
        }
    }

    @Test
    public void testInstallFifthVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        fifthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("MAIN_DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_ACTOR")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("NAME")).isEqualTo(2);
                assertThat(resultSet.findColumn("MOVIE")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
            try (ResultSet resultSet = connection.getMetaData()
                    .getCrossReference(null, null, "TST_MOVIE", null, null, "TST_ACTOR")) {
                if (resultSet.next()) {
                    assertThat(resultSet.getString("FK_NAME")).isEqualTo("TST_FK_ACTS_IN_MOVIE");
                } else {
                    fail("No foreign keys found.");
                }
                assertThat(resultSet.next()).isFalse();
            }
        }
    }

    @Test
    public void testInstallSeventhVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        seventhVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "TST_MOVIE", true, true)) {
                List<String> indexNames = new ArrayList<>();
                while (resultSet.next()) {
                    indexNames.add(resultSet.getString("INDEX_NAME"));
                }
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_MOVIETITLE"))).isTrue();
            }
        }
    }

    @Test
    public void testInstallEighthVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        eighthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "TST_MOVIE", true, true)) {
                List<String> indexNames = new ArrayList<>();
                while (resultSet.next()) {
                    indexNames.add(resultSet.getString("INDEX_NAME"));
                }
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_MOVIETITLE"))).isFalse();
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_TITLE"))).isTrue();
            }
        }
    }

    @Test
    public void testInstallNinthVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        ninthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "TST_MOVIE", true, true)) {
                List<String> indexNames = new ArrayList<>();
                while (resultSet.next()) {
                    indexNames.add(resultSet.getString("INDEX_NAME"));
                }
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_TITLE"))).isFalse();
            }
        }
    }

    @Test
    public void testInstallTenthVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        tenthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData()
                    .getCrossReference(null, null, "TST_MOVIE", null, null, "TST_ACTOR")) {
                assertThat(resultSet.next()).isFalse();
            }
        }
    }

    @Test
    public void testInstallEleventhVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        eleventhVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_ACTOR")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("NAME")).isEqualTo(2);
                assertThat(resultSet.findColumn("LEAD")).isEqualTo(3);
                assertThat(resultSet.findColumn("MOVIE")).isEqualTo(4);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(4);
            }
        }
    }

    @Test
    public void testInstallTWelfthVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        twelfthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("MAIN_DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
        }
    }

    @Test
    public void testInstallSixthVersionFromScratch() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        sixthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("MAIN_DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_ACTOR")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("NAME")).isEqualTo(2);
                assertThat(resultSet.findColumn("MOVIE")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
            try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(null, null, "TST_MOVIE")) {
                if (resultSet.next()) {
                    assertThat(resultSet.getString("PK_NAME")).isEqualTo("TST_MOVIE_PK_1");
                } else {
                    fail("No primary keys found.");
                }
                assertThat(resultSet.next()).isFalse();
            }
        }
    }

    @Test
    public void testUpgradeToThirdVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        thirdVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(2, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, Version.latest());

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_ACTOR")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("NAME")).isEqualTo(2);
                assertThat(resultSet.findColumn("MOVIE")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
        }
    }

    @Test
    public void testUpgradeToFourthVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        fourthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(3, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_ACTOR")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("NAME")).isEqualTo(2);
                assertThat(resultSet.findColumn("MOVIE")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, version(4, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("MAIN_DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_ACTOR")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("NAME")).isEqualTo(2);
                assertThat(resultSet.findColumn("MOVIE")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
            }
        }
    }

    @Test
    public void testUpgradeToFifthVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        fifthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(4, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData()
                    .getCrossReference(null, null, "TST_MOVIE", null, null, "TST_ACTOR")) {
                if (resultSet.next()) {
                    assertThat(resultSet.getString("FK_NAME")).isEqualTo("TST_FK_ACTOR_IN_MOVIE");
                } else {
                    fail("No foreign keys found.");
                }
                assertThat(resultSet.next()).isFalse();
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, version(5, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData()
                    .getCrossReference(null, null, "TST_MOVIE", null, null, "TST_ACTOR")) {
                if (resultSet.next()) {
                    assertThat(resultSet.getString("FK_NAME")).isEqualTo("TST_FK_ACTS_IN_MOVIE");
                } else {
                    fail("No foreign keys found.");
                }
                assertThat(resultSet.next()).isFalse();
            }
        }
    }

    @Test
    public void testUpgradeToSixthVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        sixthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(5, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData()
                    .getPrimaryKeys(null, null, "TST_MOVIE")) {
                if (resultSet.next()) {
                    assertThat(resultSet.getString("PK_NAME")).isEqualTo("TST_MOVIE_PK");
                } else {
                    fail("No primary keys found.");
                }
                assertThat(resultSet.next()).isFalse();
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, version(6, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData()
                    .getPrimaryKeys(null, null, "TST_MOVIE")) {
                if (resultSet.next()) {
                    assertThat(resultSet.getString("PK_NAME")).isEqualTo("TST_MOVIE_PK_1");
                } else {
                    fail("No primary keys found.");
                }
                assertThat(resultSet.next()).isFalse();
            }
        }
    }

    @Test
    public void testUpgradeToSeventhVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        seventhVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(6, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "TST_MOVIE", true, true)) {
                List<String> indexNames = new ArrayList<>();
                while (resultSet.next()) {
                    indexNames.add(resultSet.getString("INDEX_NAME"));
                }
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_MOVIETITLE"))).isFalse();
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, version(7, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "TST_MOVIE", true, true)) {
                List<String> indexNames = new ArrayList<>();
                while (resultSet.next()) {
                    indexNames.add(resultSet.getString("INDEX_NAME"));
                }
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_MOVIETITLE"))).isTrue();
            }
        }
    }

    @Test
    public void testUpgradeToEighthVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        eighthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(7, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "TST_MOVIE", true, true)) {
                List<String> indexNames = new ArrayList<>();
                while (resultSet.next()) {
                    indexNames.add(resultSet.getString("INDEX_NAME"));
                }
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_MOVIETITLE"))).isTrue();
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_TITLE"))).isFalse();
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, version(8, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "TST_MOVIE", true, true)) {
                List<String> indexNames = new ArrayList<>();
                while (resultSet.next()) {
                    indexNames.add(resultSet.getString("INDEX_NAME"));
                }
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_MOVIETITLE"))).isFalse();
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_TITLE"))).isTrue();
            }
        }
    }

    @Test
    public void testUpgradeToNinthVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        ninthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(8, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "TST_MOVIE", true, true)) {
                List<String> indexNames = new ArrayList<>();
                while (resultSet.next()) {
                    indexNames.add(resultSet.getString("INDEX_NAME"));
                }
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_TITLE"))).isTrue();
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, version(9, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "TST_MOVIE", true, true)) {
                List<String> indexNames = new ArrayList<>();
                while (resultSet.next()) {
                    indexNames.add(resultSet.getString("INDEX_NAME"));
                }
                assertThat(indexNames.stream().anyMatch(s -> s.startsWith("TST_U_TITLE"))).isFalse();
            }
        }
    }

    @Test
    public void testUpgradeToTenthVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        tenthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(9, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData()
                    .getCrossReference(null, null, "TST_MOVIE", null, null, "TST_ACTOR")) {
                if (resultSet.next()) {
                    assertThat(resultSet.getString("FK_NAME")).isEqualTo("TST_FK_ACTS_IN_MOVIE");
                } else {
                    fail("No foreign keys found.");
                }
                assertThat(resultSet.next()).isFalse();
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, version(10, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData()
                    .getCrossReference(null, null, "TST_MOVIE", null, null, "TST_ACTOR")) {
                assertThat(resultSet.next()).isFalse();
            }
        }
    }

    @Test
    public void testUpgradeToEleventhVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        eleventhVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(10, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                int i = statement.executeUpdate("insert into TST_ACTOR values (1, 'John Wayne', 5)");
                assertThat(i).isEqualTo(1);
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, version(11, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_ACTOR")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("NAME")).isEqualTo(2);
                assertThat(resultSet.findColumn("MOVIE")).isEqualTo(3);
                assertThat(resultSet.findColumn("LEAD")).isEqualTo(4);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(4);
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("LEAD")).isEqualTo("N");
            }
        }
    }

    @Test
    public void testUpgradeToTwelfthVersion() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        twelfthVersionsCode(dataModel);
        dataModel.register();

        ormService.getDataModelUpgrader().upgrade(dataModel, version(11, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                int i = statement.executeUpdate("insert into TST_MOVIE values (1, 'True Grit', null)");
                assertThat(i).isEqualTo(1);
            }
        }

        ormService.getDataModelUpgrader().upgrade(dataModel, version(12, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TST_MOVIE")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.findColumn("ID")).isEqualTo(1);
                assertThat(resultSet.findColumn("TITLE")).isEqualTo(2);
                assertThat(resultSet.findColumn("MAIN_DIRECTOR")).isEqualTo(3);
                assertThat(resultSet.getMetaData().getColumnCount()).isEqualTo(3);
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("MAIN_DIRECTOR")).isEqualTo("<unknown>");
            }
        }
    }

    private void firstVersionsCode(DataModel dataModel) {
        Table<Movie1> movieTable = dataModel.addTable("TST_MOVIE", Movie1.class);
        movieTable.map(Movie1.class);
        Column idColumn = movieTable.addAutoIdColumn();
        movieTable.column("TITLE").map("title").varChar(80).add();
        movieTable.primaryKey("TST_MOVIE_PK").on(idColumn).add();
    }

    // adds a column
    private void secondVersionsCode(DataModel dataModel) {
        Table<Movie2> movieTable = dataModel.addTable("TST_MOVIE", Movie2.class);
        movieTable.map(Movie2.class);
        Column idColumn = movieTable.addAutoIdColumn();
        movieTable.column("TITLE").map("title").varChar(80).add();
        movieTable.column("DIRECTOR").map("director").varChar(80).since(version(2, 0)).add();
        movieTable.primaryKey("TST_MOVIE_PK").on(idColumn).add();
    }

    // adds an actor table
    private void thirdVersionsCode(DataModel dataModel) {
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

    // renames a column
    private void fourthVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            Column idColumn = movieTable.addAutoIdColumn();
            movieTable.column("TITLE").map("title").varChar(80).add();
            Column directorColumn1 = movieTable.column("DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .during(Range.closedOpen(version(2, 0), version(4, 0)))
                    .add();
            movieTable.column("MAIN_DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .since(version(4, 0))
                    .previously(directorColumn1)
                    .add();
            movieTable.primaryKey("TST_MOVIE_PK").on(idColumn).add();
        }
        {
            Table<Actor1> actorTable = dataModel.addTable("TST_ACTOR", Actor1.class);
            actorTable.map(Actor1.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").notNull().varChar(80).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().add();
            actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE").on(movieColumn).references(Movie3.class).map("movie").add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

    // renames a foreign key constraint
    private void fifthVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            Column idColumn = movieTable.addAutoIdColumn();
            movieTable.column("TITLE").map("title").varChar(80).add();
            Column directorColumn1 = movieTable.column("DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .during(Range.closedOpen(version(2, 0), version(4, 0)))
                    .add();
            movieTable.column("MAIN_DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .since(version(4, 0))
                    .previously(directorColumn1)
                    .add();
            movieTable.primaryKey("TST_MOVIE_PK").on(idColumn).add();
        }
        {
            Table<Actor1> actorTable = dataModel.addTable("TST_ACTOR", Actor1.class);
            actorTable.map(Actor1.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").notNull().varChar(80).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().add();
            ForeignKeyConstraint actorInMovie = actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .upTo(version(5, 0))
                    .add();
            actorTable.foreignKey("TST_FK_ACTS_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .since(version(5, 0))
                    .previously(actorInMovie)
                    .add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

    // renames a PK constraint
    private void sixthVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            Column idColumn = movieTable.addAutoIdColumn();
            movieTable.column("TITLE").map("title").varChar(80).add();
            Column directorColumn1 = movieTable.column("DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .during(Range.closedOpen(version(2, 0), version(4, 0)))
                    .add();
            movieTable.column("MAIN_DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .since(version(4, 0))
                    .previously(directorColumn1)
                    .add();
            PrimaryKeyConstraint primaryKey1 = movieTable.primaryKey("TST_MOVIE_PK")
                    .on(idColumn)
                    .upTo(version(6, 0))
                    .add();
            movieTable.primaryKey("TST_MOVIE_PK_1").on(idColumn).since(version(6, 0)).previously(primaryKey1).add();
        }
        {
            Table<Actor1> actorTable = dataModel.addTable("TST_ACTOR", Actor1.class);
            actorTable.map(Actor1.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").notNull().varChar(80).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().add();
            ForeignKeyConstraint actorInMovie = actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .upTo(version(5, 0))
                    .add();
            actorTable.foreignKey("TST_FK_ACTS_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .since(version(5, 0))
                    .previously(actorInMovie)
                    .add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

    // adds a Unique constraint
    private void seventhVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            Column idColumn = movieTable.addAutoIdColumn();
            Column titleColumn = movieTable.column("TITLE").map("title").varChar(80).add();
            Column directorColumn1 = movieTable.column("DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .during(Range.closedOpen(version(2, 0), version(4, 0)))
                    .add();
            movieTable.column("MAIN_DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .since(version(4, 0))
                    .previously(directorColumn1)
                    .add();
            PrimaryKeyConstraint primaryKey1 = movieTable.primaryKey("TST_MOVIE_PK")
                    .on(idColumn)
                    .upTo(version(6, 0))
                    .add();
            movieTable.unique("TST_U_MOVIETITLE").on(titleColumn).since(version(7, 0)).add();
            movieTable.primaryKey("TST_MOVIE_PK_1").on(idColumn).since(version(6, 0)).previously(primaryKey1).add();
        }
        {
            Table<Actor1> actorTable = dataModel.addTable("TST_ACTOR", Actor1.class);
            actorTable.map(Actor1.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").notNull().varChar(80).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().add();
            ForeignKeyConstraint actorInMovie = actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .upTo(version(5, 0))
                    .add();
            actorTable.foreignKey("TST_FK_ACTS_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .since(version(5, 0))
                    .previously(actorInMovie)
                    .add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

    // renames a Unique constraint
    private void eighthVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            Column idColumn = movieTable.addAutoIdColumn();
            Column titleColumn = movieTable.column("TITLE").map("title").varChar(80).add();
            Column directorColumn1 = movieTable.column("DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .during(Range.closedOpen(version(2, 0), version(4, 0)))
                    .add();
            movieTable.column("MAIN_DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .since(version(4, 0))
                    .previously(directorColumn1)
                    .add();
            PrimaryKeyConstraint primaryKey1 = movieTable.primaryKey("TST_MOVIE_PK")
                    .on(idColumn)
                    .upTo(version(6, 0))
                    .add();
            UniqueConstraint uniqeTitle = movieTable.unique("TST_U_MOVIETITLE")
                    .on(titleColumn)
                    .during(Range.closedOpen(version(7, 0), version(8, 0)))
                    .add();
            movieTable.unique("TST_U_TITLE")
                    .on(titleColumn)
                    .since(version(8, 0))
                    .previously(uniqeTitle)
                    .add();
            movieTable.primaryKey("TST_MOVIE_PK_1").on(idColumn).since(version(6, 0)).previously(primaryKey1).add();
        }
        {
            Table<Actor1> actorTable = dataModel.addTable("TST_ACTOR", Actor1.class);
            actorTable.map(Actor1.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").notNull().varChar(80).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().add();
            ForeignKeyConstraint actorInMovie = actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .upTo(version(5, 0))
                    .add();
            actorTable.foreignKey("TST_FK_ACTS_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .since(version(5, 0))
                    .previously(actorInMovie)
                    .add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

    // drops a Unique constraint
    private void ninthVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            Column idColumn = movieTable.addAutoIdColumn();
            Column titleColumn = movieTable.column("TITLE").map("title").varChar(80).add();
            Column directorColumn1 = movieTable.column("DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .during(Range.closedOpen(version(2, 0), version(4, 0)))
                    .add();
            movieTable.column("MAIN_DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .since(version(4, 0))
                    .previously(directorColumn1)
                    .add();
            PrimaryKeyConstraint primaryKey1 = movieTable.primaryKey("TST_MOVIE_PK")
                    .on(idColumn)
                    .upTo(version(6, 0))
                    .add();
            UniqueConstraint uniqeTitle = movieTable.unique("TST_U_MOVIETITLE")
                    .on(titleColumn)
                    .during(Range.closedOpen(version(7, 0), version(8, 0)))
                    .add();
            movieTable.unique("TST_U_TITLE")
                    .on(titleColumn)
                    .during(Range.closedOpen(version(8, 0), version(9, 0)))
                    .previously(uniqeTitle)
                    .add();
            movieTable.primaryKey("TST_MOVIE_PK_1").on(idColumn).since(version(6, 0)).previously(primaryKey1).add();
        }
        {
            Table<Actor1> actorTable = dataModel.addTable("TST_ACTOR", Actor1.class);
            actorTable.map(Actor1.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").notNull().varChar(80).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().add();
            ForeignKeyConstraint actorInMovie = actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .upTo(version(5, 0))
                    .add();
            actorTable.foreignKey("TST_FK_ACTS_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .since(version(5, 0))
                    .previously(actorInMovie)
                    .add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

    // drops a FK constraint
    private void tenthVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            Column idColumn = movieTable.addAutoIdColumn();
            Column titleColumn = movieTable.column("TITLE").map("title").varChar(80).add();
            Column directorColumn1 = movieTable.column("DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .during(Range.closedOpen(version(2, 0), version(4, 0)))
                    .add();
            movieTable.column("MAIN_DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .since(version(4, 0))
                    .previously(directorColumn1)
                    .add();
            PrimaryKeyConstraint primaryKey1 = movieTable.primaryKey("TST_MOVIE_PK")
                    .on(idColumn)
                    .upTo(version(6, 0))
                    .add();
            UniqueConstraint uniqeTitle = movieTable.unique("TST_U_MOVIETITLE")
                    .on(titleColumn)
                    .during(Range.closedOpen(version(7, 0), version(8, 0)))
                    .add();
            movieTable.unique("TST_U_TITLE")
                    .on(titleColumn)
                    .during(Range.closedOpen(version(8, 0), version(9, 0)))
                    .previously(uniqeTitle)
                    .add();
            movieTable.primaryKey("TST_MOVIE_PK_1").on(idColumn).since(version(6, 0)).previously(primaryKey1).add();
        }
        {
            Table<Actor2> actorTable = dataModel.addTable("TST_ACTOR", Actor2.class);
            actorTable.map(Actor2.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").notNull().varChar(80).add();
            actorTable.column("MOVIE").number().notNull().upTo(version(10, 0)).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().map("movie").since(version(10, 0)).add();
            ForeignKeyConstraint actorInMovie = actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .upTo(version(5, 0))
                    .add();
            actorTable.foreignKey("TST_FK_ACTS_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .during(Range.closedOpen(version(5, 0), version(10, 0)))
                    .previously(actorInMovie)
                    .add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

    // adds a non null column
    private void eleventhVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            Column idColumn = movieTable.addAutoIdColumn();
            Column titleColumn = movieTable.column("TITLE").map("title").varChar(80).add();
            Column directorColumn1 = movieTable.column("DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .during(Range.closedOpen(version(2, 0), version(4, 0)))
                    .add();
            movieTable.column("MAIN_DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .since(version(4, 0))
                    .previously(directorColumn1)
                    .add();
            PrimaryKeyConstraint primaryKey1 = movieTable.primaryKey("TST_MOVIE_PK")
                    .on(idColumn)
                    .upTo(version(6, 0))
                    .add();
            UniqueConstraint uniqeTitle = movieTable.unique("TST_U_MOVIETITLE")
                    .on(titleColumn)
                    .during(Range.closedOpen(version(7, 0), version(8, 0)))
                    .add();
            movieTable.unique("TST_U_TITLE")
                    .on(titleColumn)
                    .during(Range.closedOpen(version(8, 0), version(9, 0)))
                    .previously(uniqeTitle)
                    .add();
            movieTable.primaryKey("TST_MOVIE_PK_1").on(idColumn).since(version(6, 0)).previously(primaryKey1).add();
        }
        {
            Table<Actor3> actorTable = dataModel.addTable("TST_ACTOR", Actor3.class);
            actorTable.map(Actor3.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").notNull().varChar(80).add();
            actorTable.column("MOVIE").number().notNull().upTo(version(10, 0)).add();
            actorTable.column("LEAD").bool().installValue("'N'").map("lead").since(version(11, 0)).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().map("movie").since(version(10, 0)).add();
            ForeignKeyConstraint actorInMovie = actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .upTo(version(5, 0))
                    .add();
            actorTable.foreignKey("TST_FK_ACTS_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .during(Range.closedOpen(version(5, 0), version(10, 0)))
                    .previously(actorInMovie)
                    .add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

    // makes a nullable column non null
    private void twelfthVersionsCode(DataModel dataModel) {
        {
            Table<Movie3> movieTable = dataModel.addTable("TST_MOVIE", Movie3.class);
            movieTable.map(Movie3.class);
            Column idColumn = movieTable.addAutoIdColumn();
            Column titleColumn = movieTable.column("TITLE").map("title").varChar(80).add();
            Column directorColumn1 = movieTable.column("DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .during(Range.closedOpen(version(2, 0), version(4, 0)))
                    .add();
            Column directorColumn2 = movieTable.column("MAIN_DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .during(Range.closedOpen(version(4, 0), version(12, 0)))
                    .previously(directorColumn1)
                    .add();
            movieTable.column("MAIN_DIRECTOR")
                    .map("director")
                    .varChar(80)
                    .notNull()
                    .since(version(12, 0))
                    .installValue("'<unknown>'")
                    .previously(directorColumn2)
                    .add();
            PrimaryKeyConstraint primaryKey1 = movieTable.primaryKey("TST_MOVIE_PK")
                    .on(idColumn)
                    .upTo(version(6, 0))
                    .add();
            UniqueConstraint uniqueTitle = movieTable.unique("TST_U_MOVIETITLE")
                    .on(titleColumn)
                    .during(Range.closedOpen(version(7, 0), version(8, 0)))
                    .add();
            movieTable.unique("TST_U_TITLE")
                    .on(titleColumn)
                    .during(Range.closedOpen(version(8, 0), version(9, 0)))
                    .previously(uniqueTitle)
                    .add();
            movieTable.primaryKey("TST_MOVIE_PK_1").on(idColumn).since(version(6, 0)).previously(primaryKey1).add();
        }
        {
            Table<Actor3> actorTable = dataModel.addTable("TST_ACTOR", Actor3.class);
            actorTable.map(Actor3.class);
            actorTable.since(version(3, 0));
            Column idColumn = actorTable.addAutoIdColumn();
            actorTable.column("NAME").map("name").notNull().varChar(80).add();
            actorTable.column("MOVIE").number().notNull().upTo(version(10, 0)).add();
            actorTable.column("LEAD").bool().installValue("'N'").map("lead").since(version(11, 0)).add();
            Column movieColumn = actorTable.column("MOVIE").number().notNull().map("movie").since(version(10, 0)).add();
            ForeignKeyConstraint actorInMovie = actorTable.foreignKey("TST_FK_ACTOR_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .upTo(version(5, 0))
                    .add();
            actorTable.foreignKey("TST_FK_ACTS_IN_MOVIE")
                    .on(movieColumn)
                    .references(Movie3.class)
                    .map("movie")
                    .during(Range.closedOpen(version(5, 0), version(10, 0)))
                    .previously(actorInMovie)
                    .add();
            actorTable.primaryKey("TST_ACTOR_PK").on(idColumn).add();
        }
    }

}
