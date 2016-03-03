package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the methods that apply a {@link MetrologyConfiguration}
 * to a {@link UsagePoint} like:
 * <ul>
 * <li>{@link UsagePoint#apply(MetrologyConfiguration)}</li>
 * <li>{@link UsagePoint#apply(MetrologyConfiguration, Instant)}</li>
 * <li>{@link UsagePoint#removeMetrologyConfiguration(Instant)}</li>
 * <li>{@link UsagePoint#getMetrologyConfiguration()}</li>
 * <li>{@link UsagePoint#getMetrologyConfiguration(Instant)}</li>
 * </ul>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplyMetrologyConfigurationToUsagePointTest {
    private static Clock clock = mock(Clock.class);
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule(clock);

    @BeforeClass
    public static void setUp() {
        when(clock.instant()).thenReturn(Instant.now());
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private MetrologyConfigurationService getMetrologyConfigurationService() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }

    private ServerMeteringService getMeteringService() {
        return inMemoryBootstrapModule.getMeteringService();
    }

    @Test
    public void testLinkUPtoMC() {
        long upId;
        UsagePoint up;
        long mcId;
        Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
        when(clock.instant()).thenReturn(jan1st2016);

        try (TransactionContext context = getTransactionService().getContext()) {
            MeteringService mtrService = getMeteringService();
            MetrologyConfigurationService service = getMetrologyConfigurationService();
            ServiceCategory serviceCategory = mtrService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            up = serviceCategory.newUsagePoint("mrID").withInstallationTime(Instant.EPOCH).create();
            upId = up.getId();
            MetrologyConfiguration mc = service.newMetrologyConfiguration("Residential");
            mcId = mc.getId();
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            MeteringService mtrService = getMeteringService();
            MetrologyConfigurationService service = getMetrologyConfigurationService();
            Optional<UsagePoint> usagePoint = mtrService.findUsagePoint(upId);
            Optional<MetrologyConfiguration> mc = service.findMetrologyConfiguration(mcId);
            assertThat(usagePoint).isPresent();
            assertThat(mc).isPresent();
            usagePoint.get().apply(mc.get());
            context.commit();
        }
        MeteringService mtrService = getMeteringService();
        UsagePoint usagePoint = mtrService.findUsagePoint(upId).get();
        Optional<MetrologyConfiguration> metrologyConfiguration = usagePoint.getMetrologyConfiguration();
        assertThat(metrologyConfiguration).isPresent();
        assertThat(metrologyConfiguration.get().getId()).isEqualTo(mcId);
        Optional<MetrologyConfiguration> expectedSameMC = usagePoint.getMetrologyConfiguration(jan1st2016.plusSeconds(86400L));
        assertThat(expectedSameMC).isPresent();
        assertThat(expectedSameMC.get().getId()).isEqualTo(mcId);
        Optional<MetrologyConfiguration> expectedEmpty = usagePoint.getMetrologyConfiguration(jan1st2016.minusSeconds(3600L));
        assertThat(expectedEmpty).isEmpty();
    }

    @Test
    public void changeConfiguration() {
        UsagePoint up;
        MetrologyConfiguration mc1;
        MetrologyConfiguration mc2;
        Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
        Instant feb1st2016 = Instant.ofEpochMilli(1454281200000L);
        when(clock.instant()).thenReturn(jan1st2016, feb1st2016);

        long mc1Id = 0;
        long mc2Id = 0;
        try (TransactionContext context = getTransactionService().getContext()) {
            MeteringService mtrService = getMeteringService();
            MetrologyConfigurationService service = getMetrologyConfigurationService();
            ServiceCategory serviceCategory = mtrService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            up = serviceCategory.newUsagePoint("UpdateMe").withInstallationTime(Instant.EPOCH).create();
            mc1 = service.newMetrologyConfiguration("First");
            mc2 = service.newMetrologyConfiguration("Second");
            context.commit();
            mc1Id = mc1.getId();
            mc2Id = mc2.getId();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            up.apply(mc1, jan1st2016);
            up.apply(mc2, feb1st2016);
            context.commit();
        }
        Optional<MetrologyConfiguration> janConfiguration = up.getMetrologyConfiguration(feb1st2016.minusSeconds(3600L));
        assertThat(janConfiguration).isPresent();
        assertThat(janConfiguration.get().getId()).isEqualTo(mc1Id);
        Optional<MetrologyConfiguration> febConfiguration = up.getMetrologyConfiguration(feb1st2016.plusSeconds(3600L));
        assertThat(febConfiguration).isPresent();
        assertThat(febConfiguration.get().getId()).isEqualTo(mc2Id);
    }

}