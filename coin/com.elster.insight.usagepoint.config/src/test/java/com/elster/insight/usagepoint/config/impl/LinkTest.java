package com.elster.insight.usagepoint.config.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import java.util.List;
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
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
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
public class LinkTest {
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
        			new ValidationModule(),
        			new MeteringGroupsModule(),
        			new TaskModule(),
        			new BasicPropertiesModule(),
        			new TimeModule(),
        			new TransactionModule(printSql),
                    new FiniteStateMachineModule(),
                    new NlsModule()
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
    
    private ServerMeteringService getMeteringService() {
        return injector.getInstance(ServerMeteringService.class);
    }
    
    @Test
    public void testLinkUPtoMC()  {
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
        	UsagePointMetrologyConfiguration upmc = upcService.link(up.get(),  mc.get());
            assertThat(upmc).isNotNull();            
            assertThat(upmc.getMetrologyConfiguration().getName()).isEqualTo("Residential");
            assertThat(upmc.getUsagePoint().getMRID()).isEqualTo("mrID");
            context.commit();
        }
    }
    
    @Test
    public void testUpdateUPtoMCLink() {
        UsagePoint up;
        MetrologyConfiguration mc1;
        MetrologyConfiguration mc2;

        try (TransactionContext context = getTransactionService().getContext()) {
            MeteringService mtrService = getMeteringService();
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            ServiceCategory serviceCategory = mtrService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            up = serviceCategory.newUsagePoint("UpdateMe").create();
            mc1 = upcService.newMetrologyConfiguration("First");
            mc2 = upcService.newMetrologyConfiguration("Second");
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.link(up,  mc1);
            context.commit();
            Optional<MetrologyConfiguration> mcx = upcService.findMetrologyConfigurationForUsagePoint(up);
            assertThat(mcx).isPresent();
            assertThat(mcx.get().getName()).isEqualTo("First");
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.link(up,  mc2);
            context.commit();
            Optional<MetrologyConfiguration> mcx = upcService.findMetrologyConfigurationForUsagePoint(up);
            assertThat(mcx).isPresent();
            assertThat(mcx.get().getName()).isEqualTo("Second");            
        }
    }
    
    @Test
    public void testMutlipleUPforMC() {
        UsagePoint up1;
        UsagePoint up2;
        MetrologyConfiguration mc;

        try (TransactionContext context = getTransactionService().getContext()) {
            MeteringService mtrService = getMeteringService();
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            ServiceCategory serviceCategory = mtrService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            up1 = serviceCategory.newUsagePoint("First").create();
            up2 = serviceCategory.newUsagePoint("Second").create();
            mc = upcService.newMetrologyConfiguration("HasTwo");
            upcService.link(up1, mc);
            upcService.link(up2, mc);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            List<UsagePoint> upList = upcService.findUsagePointsForMetrologyConfiguration(mc);
            assertThat(upList.size()).isEqualTo(2);
            assertThat(upList.get(0).getMRID()).isEqualTo("First");
            assertThat(upList.get(1).getMRID()).isEqualTo("Second");
            context.commit();
        }
    }
}
