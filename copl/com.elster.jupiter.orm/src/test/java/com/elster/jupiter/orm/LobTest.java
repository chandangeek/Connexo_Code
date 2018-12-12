/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LobTest {

    private static final int LOB_LENGTH = 100000;

    private static Injector injector;
    private static InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
//    private static OracleBootstrapModule bootstrapModule = new OracleBootstrapModule();
    private static BundleContext bundleContext;
    private static Principal principal;
    private static FileSystem fileSystem;
    private static FileSystemProvider fileSystemProvider;

    @BeforeClass
    public static void setUp() {
        bundleContext = mock(BundleContext.class);
        principal = mock(Principal.class);
// Uncomment to work with OracleBootstrapModule
//    	when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcurl")).thenReturn("jdbc:oracle:thin:@doraps003.eict.vpdc:7137:DEVRD");
//    	when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcuser")).thenReturn("RVK_DATA_AGGREGATION");
//    	when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcpassword")).thenReturn("zorro");
    	Module module = new AbstractModule() {
    		@Override
    		public void configure() {
    			this.bind(BundleContext.class).toInstance(bundleContext);
    		}
        };
        injector = Guice.createInjector(
        			module,
					bootstrapModule,
        			new UtilModule(),
        			new ThreadSecurityModule(principal),
        			new PubSubModule(),
        			new TransactionModule(false),
        			new OrmModule());
// Uncomment to work with OracleBootstrapModule
//        			new OrmModule(new OracleSchemaInfo()));
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	injector.getInstance(OrmService.class);
        	ctx.commit();
        }
        fileSystem = mock(FileSystem.class);
        fileSystemProvider = mock(FileSystemProvider.class);
        when(fileSystem.provider()).thenReturn(fileSystemProvider);
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        dropTables();
    	bootstrapModule.deactivate();
    }

    private static void dropTables() throws SQLException {
// Uncomment to work with OracleBootstrapModule
//        OrmService service = injector.getInstance(OrmService.class);
//        DataModel dataModel = findOrCreateDataModel(service);
//        dropTables(dataModel);
    }

    private static void dropTables(DataModel dataModel) throws SQLException {
        try (Connection connection = dataModel.getConnection(false)) {
            try (PreparedStatement statement = connection.prepareStatement("DROP TABLE TST_LOBS")) {
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try (PreparedStatement statement = connection.prepareStatement("DROP SEQUENCE TST_LOBSID")) {
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testInsert() throws SQLException {
    	OrmService service = injector.getInstance(OrmService.class);
    	DataModel dataModel = findOrCreateDataModel(service);
    	try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
    		LobTestTuple tuple = new LobTestTuple(dataModel);
    		for (int i = 0 ; i < LOB_LENGTH ; i++) {
    			tuple.charLob += "x";
    		}
    		tuple.byteLob = tuple.charLob.getBytes();
    		tuple.realBlob = SimpleBlob.fromString(tuple.charLob);
    		// round to second
    		Instant now = Instant.ofEpochSecond(System.currentTimeMillis()/1000L);
    		tuple.realDate = now;
    		dataModel.persist(tuple);

    		LobTestTuple alias = dataModel.mapper(LobTestTuple.class).getExisting(tuple.id);
    		assertThat(alias.charLob).isEqualTo(tuple.charLob);
    		assertThat(alias.byteLob).isEqualTo(tuple.byteLob);
    		assertThat(alias.realBlob).isNotNull();
    		assertThat(alias.realBlob.length()).isEqualTo(LOB_LENGTH);
    		assertThat(alias.realDate).isEqualTo(now);
    		assertThat(alias.nullDate).isNull();
    		ctx.commit();
    	}
    }

    @Test
    public void testInsertNull() throws SQLException {
    	OrmService service = injector.getInstance(OrmService.class);
    	DataModel dataModel = findOrCreateDataModel(service);
    	try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
    		LobTestTuple tuple = new LobTestTuple(dataModel);
    		// round to second
    		Instant now = Instant.ofEpochSecond(System.currentTimeMillis()/1000L);
    		tuple.realDate = now;
    		dataModel.persist(tuple);

    		LobTestTuple alias = dataModel.mapper(LobTestTuple.class).getExisting(tuple.id);
    		assertThat(alias.charLob).isNullOrEmpty();
    		assertThat(alias.byteLob).isNullOrEmpty();
    		assertThat(alias.realBlob).isNotNull();
    		assertThat(alias.realBlob.length()).isZero();
    		assertThat(alias.realDate).isEqualTo(now);
    		assertThat(alias.nullDate).isNull();
    		ctx.commit();
    	}
    }

    @Test
    public void testInsertFromFileClosesFileInputStream() throws SQLException, IOException {
    	OrmService service = injector.getInstance(OrmService.class);
    	DataModel dataModel = findOrCreateDataModel(service);
        Path path = mock(Path.class);
        when(path.getFileSystem()).thenReturn(fileSystem);
        InputStream pathIS = mock(InputStream.class);
        when(pathIS.available()).thenReturn(0);
        when(pathIS.read()).thenReturn(-1);
        when(pathIS.read(any(byte[].class))).thenReturn(-1);
        when(pathIS.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        when(fileSystemProvider.newInputStream(path)).thenReturn(pathIS);
    	try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
    		LobTestTuple tuple = new LobTestTuple(dataModel, path);
            tuple.charLob = "testInsertFromFileClosesFileInputStream";
            tuple.byteLob = tuple.charLob.getBytes();
    		tuple.realDate = Instant.now();

            // Business method
            dataModel.persist(tuple);

            // Asserts
    		verify(pathIS).close();
    	}
    }

    // Ignore when working with InMemoryBootstrapModule because H2 does not have support for updating BLOB columns
    @Ignore
    @Test
    public void testUpdateInitialNullValue() throws SQLException, IOException {
        OrmService service = injector.getInstance(OrmService.class);
        DataModel dataModel = findOrCreateDataModel(service);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            LobTestTuple tuple = new LobTestTuple(dataModel);
            // round to second
            Instant now = Instant.ofEpochSecond(System.currentTimeMillis()/1000L);
            tuple.realDate = now;
            dataModel.persist(tuple);

            LobTestTuple forUpdatePurposes = dataModel.mapper(LobTestTuple.class).getExisting(tuple.id);
            forUpdatePurposes.updateRealBlob("Hello world!");

            LobTestTuple loadedAfterUpdate = dataModel.mapper(LobTestTuple.class).getExisting(tuple.id);
            assertThat(loadedAfterUpdate.realBlob).isNotNull();
            assertThat(loadedAfterUpdate.realBlob.length()).isEqualTo(12L);
            ctx.commit();
        }
    }

    // Ignore when working with InMemoryBootstrapModule because H2 does not have support for updating BLOB columns
    @Ignore
    @Test
    public void testUpdate() throws IOException {
    	OrmService service = injector.getInstance(OrmService.class);
        DataModel dataModel = findOrCreateDataModel(service);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            LobTestTuple tuple = new LobTestTuple(dataModel);
    		for (int i = 0 ; i < LOB_LENGTH ; i++) {
    			tuple.charLob += "x";
    		}
    		tuple.byteLob = tuple.charLob.getBytes();
    		tuple.realBlob = SimpleBlob.fromString(tuple.charLob);
    		// round to second
    		Instant now = Instant.ofEpochSecond(System.currentTimeMillis()/1000L);
    		tuple.realDate = now;
    		dataModel.persist(tuple);

    		LobTestTuple forUpdatePurposes = dataModel.mapper(LobTestTuple.class).getExisting(tuple.id);
            forUpdatePurposes.updateRealBlob("Hello world!");

            LobTestTuple loadedAfterUpdate = dataModel.mapper(LobTestTuple.class).getExisting(tuple.id);
    		assertThat(loadedAfterUpdate.realBlob).isNotNull();
    		assertThat(loadedAfterUpdate.realBlob.length()).isEqualTo(12L);
    		ctx.commit();
    	}
    }

    private static DataModel findOrCreateDataModel(OrmService service) {
        return service.getDataModel("TST").orElseGet(() -> createDataModel(service));
    }

    private static DataModel createDataModel(OrmService service) {
        DataModel dataModel = service.newDataModel("TST", "Lob Test");
        Table<LobTestTuple> table = dataModel.addTable("TST_LOBS", LobTestTuple.class);
        table.map(LobTestTuple.class);
        Column idColumn = table.addAutoIdColumn();
        table.column("CHARLOB").type("CLOB").map("charLob").conversion(ColumnConversion.CLOB2STRING).add();
        table.column("BYTELOB").type("BLOB").map("byteLob").conversion(ColumnConversion.BLOB2BYTE).add();
        table.column("REALBLOB").blob().map("realBlob").add();
        table.column("REALDATE").number().map("realDate").conversion(ColumnConversion.NUMBERINUTCSECONDS2INSTANT).add();
        table.column("NULLDATE").number().map("nullDate").conversion(ColumnConversion.NUMBERINUTCSECONDS2INSTANT).add();
        table.primaryKey("TST_PK_LOBS").on(idColumn).add();
        dataModel.register();
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            service.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, Version.latest());
        }
        return dataModel;
    }

    private static class LobTestTuple {
        private final DataModel dataModel;

    	@SuppressWarnings("unused")
		private long id;
    	private String charLob = "";
    	private byte[] byteLob;
    	private Blob realBlob = SimpleBlob.empty();
    	private Instant realDate;
    	private Instant nullDate;

        @Inject
        private LobTestTuple(DataModel dataModel) {
            this.dataModel = dataModel;
        }

        private LobTestTuple(DataModel dataModel, Path path) {
            this(dataModel);
            this.realBlob = FileBlob.from(path);
        }

        void updateRealBlob(String updatedValue) throws IOException {
            this.realBlob.clear();
            BufferedWriter realBlobWriter = new BufferedWriter(new OutputStreamWriter(this.realBlob.setBinaryStream()));
            realBlobWriter.append(updatedValue);
            realBlobWriter.close();
            this.dataModel.update(this, "realBlob");
        }
    }

}