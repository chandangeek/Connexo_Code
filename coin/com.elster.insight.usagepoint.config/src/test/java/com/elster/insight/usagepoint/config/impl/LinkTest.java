package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LinkTest {
    private static MetrologyInMemoryBootstrapModule inMemoryBootstrapModule = new MetrologyInMemoryBootstrapModule();

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    private UsagePointConfigurationService getUsagePointConfigurationService() {
        return inMemoryBootstrapModule.getUsagePointConfigurationService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }

    private ServerMeteringService getMeteringService() {
        return inMemoryBootstrapModule.getMeteringService();
    }

    private ValidationService getValidationService() {
        return inMemoryBootstrapModule.getValidationService();
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
}
