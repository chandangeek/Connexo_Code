package com.elster.insight.usagepoint.config.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.validation.ConstraintViolationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationCrudTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(SearchService.class).toInstance(mock(SearchService.class));
        }
    }

    private static final boolean printSql = false;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(
        			new MockModule(),
        			inMemoryBootstrapModule,
        			new UsagePointConfigModule(),
                    new IdsModule(),
                    new MeteringModule(),
                    new PartyModule(),
                    new FiniteStateMachineModule(),
        			new UserModule(),
        			new EventsModule(),
        			new InMemoryMessagingModule(),
        			new DomainUtilModule(),
        			new OrmModule(),
        			new UtilModule(),
        			new ThreadSecurityModule(),
        			new DataVaultModule(),
        			new PubSubModule(),
        			new TransactionModule(printSql),
                    new NlsModule(),
                    new ValidationModule(),
                    new MeteringGroupsModule(),
                    new TaskModule(),
                    new BasicPropertiesModule(),
                    new TimeModule()
                );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(ThreadPrincipalService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(ValidationService.class);
        	injector.getInstance(UsagePointConfigurationService.class);
        	ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }

    private UsagePointConfigurationService getUsagePointConfigurationService() {
        return injector.getInstance(UsagePointConfigurationService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    @Test
    public void testCrud()  {
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            MetrologyConfiguration mc1 = upcService.newMetrologyConfiguration("Residenshull");
            assertThat(mc1.getName()).isEqualTo("Residenshull");
            MetrologyConfiguration mc2 = upcService.newMetrologyConfiguration("Commercial 1");
            assertThat(mc2.getName()).isEqualTo("Commercial 1");
        	context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
        	Optional<MetrologyConfiguration> mc1 = upcService.findMetrologyConfiguration(1);
        	assertThat(mc1).isPresent();
        	assertThat(mc1.get().getName()).isEqualTo("Residenshull");
            Optional<MetrologyConfiguration> mc2 = getUsagePointConfigurationService().findMetrologyConfiguration(2);
            assertThat(mc2.isPresent());
            assertThat(mc2.get().getName()).isEqualTo("Commercial 1");    
            List<MetrologyConfiguration> all = upcService.findAllMetrologyConfigurations();
            assertThat(all.size()).isEqualTo(2);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<MetrologyConfiguration> mc1 = upcService.findMetrologyConfiguration(1);
            assertThat(mc1).isPresent();
            assertThat(mc1.get().getName()).isEqualTo("Residenshull");
            mc1.get().updateName("Residential");
            mc1 = upcService.findMetrologyConfiguration(1);            
            assertThat(mc1).isPresent();
            assertThat(mc1.get().getName()).isEqualTo("Residential");
            mc1 = upcService.findMetrologyConfiguration("Residential");            
            assertThat(mc1).isPresent();
            assertThat(mc1.get().getName()).isEqualTo("Residential");
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            List<MetrologyConfiguration> all = upcService.findAllMetrologyConfigurations();
        	for (MetrologyConfiguration mc : all) {
        		mc.delete();
        	}
        	context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            List<MetrologyConfiguration> all = upcService.findAllMetrologyConfigurations();
            assertThat(all).isEmpty();
            context.commit();
        }
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void testEmptyName()  {
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.newMetrologyConfiguration("");
            context.commit();
        }
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void testNullName()  {
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.newMetrologyConfiguration(null);
            context.commit();
        }
    }    
    
    @Test(expected = ConstraintViolationException.class)
    public void testDuplicateNameCreate() {
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.newMetrologyConfiguration("dup1");
            upcService.newMetrologyConfiguration("dup1");
            context.commit();
        }
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void testDuplicateNameRename() {
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.newMetrologyConfiguration("dup2");
            MetrologyConfiguration x = upcService.newMetrologyConfiguration("x");
            x.updateName("dup2");
            context.commit();
        }
    }
}
