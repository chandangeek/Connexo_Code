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
import java.io.OutputStreamWriter;
import java.security.Principal;
import java.sql.SQLException;
import java.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LobTest {

    private static final int LOB_LENGTH = 100000;

    private Injector injector;
    private InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
//    private OracleBootstrapModule bootstrapModule = new OracleBootstrapModule();

    @Mock
    private Principal principal;
    @Mock
    private BundleContext bundleContext;

    @Before
    public void setUp() {
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
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	injector.getInstance(OrmService.class);
        	ctx.commit();
        }
    }

    @After
    public void tearDown() throws SQLException {
    	bootstrapModule.deactivate();
    }

    @Test
    public void testInsert() throws SQLException {
    	OrmService service = injector.getInstance(OrmService.class);
    	DataModel dataModel = this.findOrCreateDataModel(service);
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

// Ignore when working with InMemoryBootstrapModule because H2 does not have support for updating BLOB columns
    @Ignore
    @Test
    public void testUpdate() throws IOException {
    	OrmService service = injector.getInstance(OrmService.class);
        DataModel dataModel = this.findOrCreateDataModel(service);
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

    private DataModel findOrCreateDataModel(OrmService service) {
        return service.getDataModel("TST").orElseGet(() -> this.createDataModel(service));
    }

    private DataModel createDataModel(OrmService service) {
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
            dataModel.install(true, false);
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

        void updateRealBlob(String updatedValue) throws IOException {
            this.realBlob.clear();
            BufferedWriter realBlobWriter = new BufferedWriter(new OutputStreamWriter(this.realBlob.setBinaryStream()));
            realBlobWriter.append(updatedValue);
            realBlobWriter.close();
            this.dataModel.update(this, "realBlob");
        }
    }

}