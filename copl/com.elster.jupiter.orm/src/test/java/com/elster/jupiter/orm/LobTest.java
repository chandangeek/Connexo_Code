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

import java.security.Principal;
import java.sql.SQLException;
import java.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LobTest {

    private static final int LOB_LENGTH = 100000;

    private Injector injector;
    private InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();

    @Mock
    private Principal principal;
    @Mock
    private BundleContext bundleContext;

    @Before
    public void setUp() {
    	when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcurl")).thenReturn("jdbc:oracle:thin:@localhost:1521:orcl");
    	when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcuser")).thenReturn("kore");
    	when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcpassword")).thenReturn("kore");
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
    public void test() {
    	OrmService service = injector.getInstance(OrmService.class);
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
    		dataModel.install(true, true);
    		LobTestTuple tuple = new LobTestTuple();
    		for (int i = 0 ; i < LOB_LENGTH ; i++) {
    			tuple.charLob += "x";
    		}
    		tuple.byteLob = tuple.charLob.getBytes();
    		tuple.realBlob = SimpleBlob.fromString(tuple.charLob);
    		// round to second
    		Instant now = Instant.ofEpochSecond(System.currentTimeMillis()/1000L);
    		tuple.realDate = now;
    		dataModel.persist(tuple);
    		LobTestTuple alias = dataModel.mapper(LobTestTuple.class).getExisting(1);
    		assertThat(alias.charLob).isEqualTo(tuple.charLob);
    		assertThat(alias.byteLob).isEqualTo(tuple.byteLob);
    		assertThat(alias.realBlob).isNotNull();
    		assertThat(alias.realBlob.length()).isEqualTo(LOB_LENGTH);
    		assertThat(alias.realDate).isEqualTo(now);
    		assertThat(alias.nullDate).isNull();
    		ctx.commit();
    	}
    }

    private static class LobTestTuple {
    	@SuppressWarnings("unused")
		private long id;
    	private String charLob = "";
    	private byte[] byteLob;
    	private Blob realBlob = SimpleBlob.empty();
    	private Instant realDate;
    	private Instant nullDate;
    }

}