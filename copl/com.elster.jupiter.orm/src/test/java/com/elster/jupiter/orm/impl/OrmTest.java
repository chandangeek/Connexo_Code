package com.elster.jupiter.orm.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Principal;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;


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
        			new TransactionModule(true),        			        		
        			new OrmModule());
		try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
			injector.getInstance(OrmService.class);
			ctx.commit();
		}
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testDataModel() {
    	OrmService ormService = injector.getInstance(OrmService.class);
    	DataModel dataModel = ((OrmServiceImpl) ormService).getDataModels().get(0);
    	assertThat(dataModel.mapper(DataModel.class).find()).hasSize(1);
    	assertThat(dataModel.mapper(Table.class).find().size()).isGreaterThan(4);
    	assertThat(dataModel.mapper(Column.class).find().size()).isGreaterThan(10);
    	assertThat(dataModel.mapper(TableConstraint.class).find()).isNotEmpty();
    	assertThat(dataModel.mapper(ColumnInConstraintImpl.class).find()).isNotEmpty();
    }
    
    @Test
    public void testEagerQuery() {
    	try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
    		OrmService ormService = injector.getInstance(OrmService.class);
        	DataModel dataModel = ((OrmServiceImpl) ormService).getDataModels().get(0);
    		Optional<DataModel> copy = dataModel.mapper(DataModel.class).getEager("ORM");
    		for (Table<?> each : copy.get().getTables()) {
    			each.getColumns().size();
    			each.getConstraints().size();
    			for ( TableConstraint constraint : each.getConstraints()) {
    				constraint.getColumns().size();
    			}
    		}
    		ctx.commit();
    		assertThat(ctx.getStats().getSqlCount()).isEqualTo(1);
    	}
    }


}
