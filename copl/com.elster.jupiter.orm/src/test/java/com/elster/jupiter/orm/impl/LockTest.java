package com.elster.jupiter.orm.impl;

import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import com.elster.jupiter.bootstrap.oracle.impl.OracleBootstrapModule;
import com.elster.jupiter.orm.DataModel;
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
import com.google.inject.Module;


@RunWith(MockitoJUnitRunner.class)
@Ignore
public class LockTest {

    private Injector injector;
    private OracleBootstrapModule bootstrapModule = new OracleBootstrapModule();
    //private InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();

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
        			new TransactionModule(true),        			        		
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
    public void testLockNoWait() throws InterruptedException {
    	OrmService ormService = injector.getInstance(OrmService.class);
    	final DataModel dataModel = ((OrmServiceImpl) ormService).getDataModels().get(0);
    	try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
    		assertThat(dataModel.mapper(DataModel.class).lockNoWait("ORM")).isPresent();
    		Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
						assertThat(dataModel.mapper(DataModel.class).lockNoWait("ORM")).isAbsent();
						assertThat(dataModel.mapper(Table.class).lockNoWait("ORM","ORM_DATAMODEL")).isPresent();
					}
				}
    		});
    		thread.start();
    		thread.join();
    	}
    }

}
