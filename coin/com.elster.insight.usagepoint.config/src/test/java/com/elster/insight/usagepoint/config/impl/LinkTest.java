package com.elster.insight.usagepoint.config.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(MockitoJUnitRunner.class)
public class LinkTest {
    private static final Instant START = ZonedDateTime.of(2013, 4, 14, 17, 20, 4, 0,ZoneId.systemDefault()).toInstant();
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
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
                    new FiniteStateMachineModule(),
                    new NlsModule()
                );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
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
    
    private ServerMeteringService getMeteringService() {
        return injector.getInstance(ServerMeteringService.class);
    }
    
    @Test
    public void testLink()  {
        long upId;
        long mcId;

        try (TransactionContext context = getTransactionService().getContext()) {
            MeteringService mtrService = getMeteringService();
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            ServiceCategory serviceCategory = mtrService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            UsagePoint up = serviceCategory.newUsagePoint("mrID").create();
            upId = up.getId();
            MetrologyConfiguration mc = upcService.newMetrologyConfiguration("Residential");
            mcId = mc.getId();
        	context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            MeteringService mtrService = getMeteringService();
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<UsagePoint> up = mtrService.findUsagePoint(upId);
        	Optional<MetrologyConfiguration> mc = upcService.findMetrologyConfiguration(mcId);
        	assertThat(up).isPresent();
        	assertThat(mc).isPresent();
        	assertThat(mc.get().getName()).isEqualTo("Residential");
        	assertThat(up.get().getMRID()).isEqualTo("mrID");
        	UsagePointMetrologyConfiguration upmc = upcService.link(up.get(),  mc.get(), Interval.of(Range.atLeast(START)));
            assertThat(upmc).isNotNull();            
            assertThat(upmc.getMetrologyConfiguration().getName()).isEqualTo("Residential");
            assertThat(upmc.getUsagePoint().getMRID()).isEqualTo("mrID");
            context.commit();
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConflict() {
        UsagePoint up;
        MetrologyConfiguration mc;

        try (TransactionContext context = getTransactionService().getContext()) {
            MeteringService mtrService = getMeteringService();
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            ServiceCategory serviceCategory = mtrService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            up = serviceCategory.newUsagePoint("Duplicate").create();
            mc = upcService.newMetrologyConfiguration("Duplicate");
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.link(up,  mc, Interval.of(Range.atLeast(START)));
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.link(up,  mc, Interval.of(Range.atLeast(START)));
            context.commit();
        }
    }
}
