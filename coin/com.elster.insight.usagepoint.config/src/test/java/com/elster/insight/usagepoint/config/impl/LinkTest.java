package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
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
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class LinkTest {
    private static final boolean printSql = false;
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

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
                new NlsModule(),
                new CustomPropertySetsModule()
        );

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
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

    private ValidationService getValidationService() {
        return injector.getInstance(ValidationService.class);
    }

    @Test
    public void testLinkUPtoMC() {
        long upId;
        UsagePoint up;
        long mcId;

        try (TransactionContext context = getTransactionService().getContext()) {
            MeteringService mtrService = getMeteringService();
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            ServiceCategory serviceCategory = mtrService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            up = serviceCategory.newUsagePoint("mrID").create();
            upId = up.getId();
            MetrologyConfiguration mc = upcService.newMetrologyConfiguration("Residential");
            mcId = mc.getId();
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            MeteringService mtrService = getMeteringService();
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<UsagePoint> up2 = mtrService.findUsagePoint(upId);
            Optional<MetrologyConfiguration> mc = upcService.findMetrologyConfiguration(mcId);
            assertThat(up2).isPresent();
            assertThat(mc).isPresent();
            assertThat(mc.get().getName()).isEqualTo("Residential");
            assertThat(up2.get().getMRID()).isEqualTo("mrID");
            upcService.link(up2.get(), mc.get());
            context.commit();
        }
        UsagePointConfigurationService upcService = getUsagePointConfigurationService();
        Optional<MetrologyConfiguration> mc = upcService.findMetrologyConfigurationForUsagePoint(up);
        assertThat(mc).isPresent();
        assertThat(mc.get().getName()).isEqualTo("Residential");
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
            upcService.link(up, mc1);
            context.commit();
            Optional<MetrologyConfiguration> mcx = upcService.findMetrologyConfigurationForUsagePoint(up);
            assertThat(mcx).isPresent();
            assertThat(mcx.get().getName()).isEqualTo("First");
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            upcService.link(up, mc2);
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

    @Test
    public void testMCValRuleSetLink() {
        MetrologyConfiguration mc;
        ValidationRuleSet vrs1;
        ValidationRuleSet vrs2;
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            ValidationService valService = getValidationService();
            mc = upcService.newMetrologyConfiguration("MC1");
            vrs1 = valService.createValidationRuleSet("Rule #1");
            mc.addValidationRuleSet(vrs1);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<MetrologyConfiguration> mc2 = upcService.findMetrologyConfiguration(mc.getId());
            assertThat(mc2).isPresent();
            assertThat(mc2.get().getValidationRuleSets().size()).isEqualTo(1);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            ValidationService valService = getValidationService();
            vrs2 = valService.createValidationRuleSet("Rule #2");
            vrs2.save();
            mc.addValidationRuleSet(vrs2);
            Optional<MetrologyConfiguration> mc2 = upcService.findMetrologyConfiguration(mc.getId());
            assertThat(mc2).isPresent();
            assertThat(mc2.get().getValidationRuleSets().size()).isEqualTo(2);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<MetrologyConfiguration> mc2 = upcService.findMetrologyConfiguration(mc.getId());
            assertThat(mc2).isPresent();
            mc2.get().removeValidationRuleSet(vrs1);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            Optional<MetrologyConfiguration> mc2 = upcService.findMetrologyConfiguration(mc.getId());
            assertThat(mc2).isPresent();
            assertThat(mc2.get().getValidationRuleSets().size()).isEqualTo(1);
            context.commit();
        }
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(SearchService.class).toInstance(mock(SearchService.class));
        }
    }
}
