package com.elster.jupiter.orm.impl;

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
import com.elster.jupiter.orm.RefAny;
import com.elster.jupiter.orm.internal.Bus;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.assertj.guava.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class RefAnyTest {

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
    	Optional<Object> tableHolder = Bus.getTable("ORM","ORM_TABLE").get("ORM","ORM_TABLE");
    	assertThat(tableHolder).isPresent();
    	RefAny refAny = RefAny.of(tableHolder.get());
    	assertThat(refAny.get()).isPresent();
    }


}
