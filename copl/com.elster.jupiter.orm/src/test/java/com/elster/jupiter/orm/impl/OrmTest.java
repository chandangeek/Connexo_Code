package com.elster.jupiter.orm.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.log.LogService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.internal.Bus;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(MockitoJUnitRunner.class)
public class OrmTest {

    private Injector injector;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    @Mock
	private Principal principal;
    @Mock
    private LogService logService;

    @Before
    public void setUp() {   
		injector = Guice.createInjector(
					inMemoryBootstrapModule,
        			new UtilModule(), 
        			new ThreadSecurityModule(principal), 
        			new PubSubModule(logService),
        			new TransactionModule(),        			        		
        			new OrmModule());
		when(principal.getName()).thenReturn("Test");		
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
			@Override
			public Void perform() {
				injector.getInstance(OrmService.class);
				return null;
			}
		});
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testDataModel() {
    	assertTrue(Bus.getOrmClient().getDataModelFactory().find().size() == 1);
    	assertTrue(Bus.getOrmClient().getTableFactory().find().size() > 4);
    	assertTrue(Bus.getOrmClient().getColumnFactory().find().size() > 10);
    	assertTrue(Bus.getOrmClient().getTableConstraintFactory().find().size() > 0);
    	assertTrue(Bus.getOrmClient().getColumnInConstraintFactory().find().size() > 0);
    }


}
