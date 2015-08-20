package com.elster.jupiter.data.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import java.time.Instant;
import java.time.Period;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.data.lifecycle.impl.DataLifeCycleModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(MockitoJUnitRunner.class)
public class LifeCycleServiceTest {

	private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
	private LifeCycleService lifeCycleService;
	private Injector injector;

	private static class MockModule extends AbstractModule {
		@Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
        }
    }

    private static final boolean printSql = false;

	@Before
	public void setUp() throws SQLException {
		injector = Guice.createInjector(
				new MockModule(),
    			inMemoryBootstrapModule,
    			new OrmModule(),
    			new DomainUtilModule(),
    			new NlsModule(),
    			new UserModule(),
    			new UtilModule(),
    			new ThreadSecurityModule(),
    			new PubSubModule(),
    			new TransactionModule(printSql),
    			new InMemoryMessagingModule(),
    			new TaskModule(),
    			new IdsModule(),
    			new EventsModule(),
    			new PartyModule(),
                new FiniteStateMachineModule(),
    			new MeteringModule(),
    			new DataLifeCycleModule()
		);
		try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
			injector.getInstance(FiniteStateMachineService.class);
			lifeCycleService = injector.getInstance(LifeCycleService.class);
			ctx.commit();
		}
	}

	@After
	public void tearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }

	@Test
    public void test()  {
		Instant afterInstall = Instant.now();
		List<LifeCycleCategory> categories = lifeCycleService.getCategories();
		assertThat(categories).hasSize(LifeCycleCategoryKind.values().length);
		for (LifeCycleCategory category : categories) {
			assertThat(categories.indexOf(category)).isEqualTo(	category.getKind().ordinal());
		}
		lifeCycleService.getTask();
		injector.getInstance(TransactionService.class).execute(() -> { lifeCycleService.runNow(); return null; });
		Period period = categories.get(0).getRetention();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		injector.getInstance(TransactionService.class).execute(() -> { categories.get(0).setRetentionDays(100); return null;});
		assertThat(lifeCycleService.getCategories().get(0).getRetention()).isEqualTo(Period.ofDays(100));
		assertThat(lifeCycleService.getCategoriesAsOf(afterInstall).get(0).getRetention()).isEqualTo(period);
		assertThat(lifeCycleService.getCategoriesAsOf(afterInstall).get(0).getKind()).isEqualTo(lifeCycleService.getCategories().get(0).getKind());
		assertThat(lifeCycleService.getCategoriesAsOf(afterInstall).size()).isEqualTo(lifeCycleService.getCategories().size());
	}

}
