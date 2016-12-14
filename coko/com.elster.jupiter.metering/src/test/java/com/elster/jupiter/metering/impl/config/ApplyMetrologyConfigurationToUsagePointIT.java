package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the methods that apply a {@link UsagePointMetrologyConfiguration}
 * to a {@link UsagePoint} like:
 * <ul>
 * <li>{@link UsagePoint#apply(UsagePointMetrologyConfiguration)}</li>
 * <li>{@link UsagePoint#apply(UsagePointMetrologyConfiguration, Instant)}</li>
 * <li>{@link UsagePoint#removeMetrologyConfiguration(Instant)}</li>
 * <li>{@link UsagePoint#getCurrentEffectiveMetrologyConfiguration()}</li>
 * <li>{@link UsagePoint#getEffectiveMetrologyConfiguration(Instant)}</li>
 * </ul>
 */
public class ApplyMetrologyConfigurationToUsagePointIT {

    private static final String USAGE_POINT_NAME = "UP0001";
    private static final String METROLOGY_CONFIGURATION_NAME = "Metrology configuration";
    private static final Instant INSTALLATION_TIME = ZonedDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(0, 0), ZoneId.systemDefault()).toInstant();

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    private static Clock clock = mock(Clock.class);
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withClock(clock);

    @BeforeClass
    public static void setUp() {
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(Clock.systemUTC().getZone());
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private MetrologyConfigurationService getMetrologyConfigurationService() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService();
    }

    private ServerMeteringService getMeteringService() {
        return inMemoryBootstrapModule.getMeteringService();
    }

    @Test
    @Transactional
    public void changeConfiguration() {
        UsagePoint up;
        UsagePointMetrologyConfiguration mc1;
        UsagePointMetrologyConfiguration mc2;
        Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
        Instant feb1st2016 = Instant.ofEpochMilli(1454281200000L);
        when(clock.instant()).thenReturn(jan1st2016, feb1st2016);

        long mc1Id = 0;
        long mc2Id = 0;
        MeteringService mtrService = getMeteringService();
        MetrologyConfigurationService service = getMetrologyConfigurationService();
        ServiceCategory serviceCategory = mtrService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        up = serviceCategory.newUsagePoint("UpdateMe", Instant.EPOCH).create();
        mc1 = service.newUsagePointMetrologyConfiguration("First", serviceCategory).create();
        mc2 = service.newUsagePointMetrologyConfiguration("Second", serviceCategory).create();

        mc1Id = mc1.getId();
        mc2Id = mc2.getId();

        up.apply(mc1, jan1st2016);
        up.apply(mc2, feb1st2016);

        Optional<MetrologyConfiguration> janConfiguration = up.getEffectiveMetrologyConfiguration(feb1st2016.minusSeconds(3600L))
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration);
        assertThat(janConfiguration).isPresent();
        assertThat(janConfiguration.get().getId()).isEqualTo(mc1Id);
        Optional<MetrologyConfiguration> febConfiguration = up.getEffectiveMetrologyConfiguration(feb1st2016.plusSeconds(3600L))
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration);
        assertThat(febConfiguration).isPresent();
        assertThat(febConfiguration.get().getId()).isEqualTo(mc2Id);
    }

    @Test
    @Transactional
    public void applyFirstMetrologyConfigurationToUsagePointNoLinkedMeters() {
        UsagePointMetrologyConfiguration metrologyConfiguration = createMetrologyConfiguration(METROLOGY_CONFIGURATION_NAME);
        ServiceCategory serviceCategory = getElectricityServiceCategory();
        UsagePoint usagePoint = serviceCategory.newUsagePoint(USAGE_POINT_NAME, INSTALLATION_TIME).create();

        // Business method
        usagePoint.apply(metrologyConfiguration, INSTALLATION_TIME);

        // Asserts that usage point is now linked to metrology configuration
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> currentEffectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration();
        assertThat(currentEffectiveMetrologyConfiguration).isPresent();
        assertThat(currentEffectiveMetrologyConfiguration.get().getMetrologyConfiguration()).isEqualTo(metrologyConfiguration);
        assertThat(currentEffectiveMetrologyConfiguration.get().getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));

        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfiguration;

        // Asserts that usage point has linked metrology configuration in the future
        effectiveMetrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration(INSTALLATION_TIME.plusMillis(1));
        assertThat(effectiveMetrologyConfiguration).isPresent();
        assertThat(currentEffectiveMetrologyConfiguration.get().getMetrologyConfiguration()).isEqualTo(metrologyConfiguration);
        assertThat(currentEffectiveMetrologyConfiguration.get().getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));

        // Asserts that usage point has no linked metrology configuration before installation time
        effectiveMetrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration(INSTALLATION_TIME.minusMillis(1));
        assertThat(effectiveMetrologyConfiguration).isEmpty();
    }

    private ServiceCategory getElectricityServiceCategory() {
        return getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new IllegalStateException("No such service category: " + ServiceKind.ELECTRICITY.getKey() + ", but should be installed already"));
    }

    private UsagePointMetrologyConfiguration createMetrologyConfiguration(String name) {
        UsagePointMetrologyConfiguration metrologyConfiguration =
                getMetrologyConfigurationService().newUsagePointMetrologyConfiguration(name, getElectricityServiceCategory()).create();
        metrologyConfiguration.addMeterRole(findMeterRole(DefaultMeterRole.CONSUMPTION));
        metrologyConfiguration.addMeterRole(findMeterRole(DefaultMeterRole.PRODUCTION));
        MetrologyContract informationMetrologyContract = metrologyConfiguration.addMetrologyContract(findPurpose(DefaultMetrologyPurpose.INFORMATION));
        // add deliverables with requirements to information contract
        informationMetrologyContract.update();
        MetrologyContract billingMetrologyContract = metrologyConfiguration.addMandatoryMetrologyContract(findPurpose(DefaultMetrologyPurpose.BILLING));
        // add deliverables with requirements to billing contract
        billingMetrologyContract.update();
        return metrologyConfiguration;
    }

    private MeterRole findMeterRole(DefaultMeterRole meterRole) {
        return getMetrologyConfigurationService().findMeterRole(meterRole.getKey())
                .orElseThrow(() -> new IllegalStateException("No such default meter role: " + meterRole.getKey() + ", but should be installed already"));
    }

    private MetrologyPurpose findPurpose(DefaultMetrologyPurpose purpose) {
        return getMetrologyConfigurationService().findMetrologyPurpose(purpose)
                .orElseThrow(() -> new IllegalStateException("No such default metrology purpose: " + purpose.getName().getKey() + ", but should be installed already"));
    }
}
