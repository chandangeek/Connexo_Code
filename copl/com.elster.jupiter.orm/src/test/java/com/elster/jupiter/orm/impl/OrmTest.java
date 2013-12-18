package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.OrmService;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.Principal;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class OrmTest {

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
    public void testDataModel() {
    	assertThat(Bus.getOrmClient().getDataModelFactory().find()).hasSize(1);
    	assertThat(Bus.getOrmClient().getTableFactory().find().size()).isGreaterThan(4);
    	assertThat(Bus.getOrmClient().getColumnFactory().find().size()).isGreaterThan(10);
    	assertThat(Bus.getOrmClient().getTableConstraintFactory().find()).isNotEmpty();
    	assertThat(Bus.getOrmClient().getColumnInConstraintFactory().find()).isNotEmpty();
    	assertThat(Bus.getTable("ORM","ORM_TABLE").get("ORM","ORM_TABLES")).isAbsent();
    	Optional<Object> tableHolder = Bus.getTable("ORM","ORM_TABLE").get("ORM","ORM_TABLE");
    	assertThat(tableHolder).isPresent();
    	Column column = Bus.getOrmClient().getColumnFactory().find().get(0);
    	assertThat(column.getTable().getName()).isNotEmpty();
    }


}
