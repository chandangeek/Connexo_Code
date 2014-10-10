package com.elster.jupiter.orm.impl;

import static org.assertj.core.api.Assertions.assertThat;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.Guice;
import com.google.inject.Injector;


@RunWith(MockitoJUnitRunner.class)
public class RefAnyTest {

    private Injector injector;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Mock
    private Principal principal;

    @Before
    public void setUp() {   
		injector = Guice.createInjector(
					inMemoryBootstrapModule,
        			new UtilModule(), 
        			new ThreadSecurityModule(principal),
        			new PubSubModule(),
        			new TransactionModule(),        			        		
        			new OrmModule());	
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
    public void testRefAny() {   
    	OrmServiceImpl service = (OrmServiceImpl) injector.getInstance(OrmService.class);
    	Optional<?> tableHolder = service.getDataModel("ORM").get().getTable("ORM_TABLE").getOptional("ORM","ORM_TABLE");
    	assertThat(tableHolder.isPresent()).isTrue();
    	RefAny refAny = injector.getInstance(OrmService.class).getDataModels().get(0).asRefAny(tableHolder.get());
    	assertThat(refAny.isPresent()).isTrue();
    }


}
