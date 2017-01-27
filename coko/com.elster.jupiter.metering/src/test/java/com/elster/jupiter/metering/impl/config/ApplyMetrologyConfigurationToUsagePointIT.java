package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    ReadingType fifteenMinuteskWhForward;
    ReadingType fifteenMinuteskWhReverse;

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
        fifteenMinuteskWhForward = getMeteringService().getReadingType("0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> getMeteringService().createReadingType("0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
        fifteenMinuteskWhReverse = getMeteringService().getReadingType("0.0.2.4.19.2.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> getMeteringService().createReadingType("0.0.2.4.19.2.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
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

    @Test
    @Transactional
    public void applyFirstMetrologyConfigurationToUsagePointWithLinkedMeters() {
        fifteenMinuteskWhForward = getMeteringService().getReadingType("0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0")
                .orElseGet(() -> getMeteringService().createReadingType("0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
        fifteenMinuteskWhReverse = getMeteringService().getReadingType("0.0.2.4.19.2.12.0.0.0.0.0.0.0.0.3.72.0")
                .orElseGet(() -> getMeteringService().createReadingType("0.0.2.4.19.2.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        inMemoryBootstrapModule.getMeteringDataModelService().addHeadEndInterface(new TestHeadEndInterface(fifteenMinuteskWhForward, fifteenMinuteskWhReverse));
        NlsKey name = mock(NlsKey.class);
        when(name.getKey()).thenReturn(ApplyMetrologyConfigurationToUsagePointIT.class.getSimpleName());
        when(name.getDefaultMessage()).thenReturn(ApplyMetrologyConfigurationToUsagePointIT.class.getSimpleName());
        when(name.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
        when(name.getLayer()).thenReturn(Layer.DOMAIN);

        UsagePointMetrologyConfiguration metrologyConfiguration = createMetrologyConfiguration(METROLOGY_CONFIGURATION_NAME);

        ServiceCategory serviceCategory = getElectricityServiceCategory();
        UsagePoint usagePoint = serviceCategory.newUsagePoint(USAGE_POINT_NAME, INSTALLATION_TIME).create();

        Meter meterConsunption = setupMeter("meterConsunption");
        activateMeter(meterConsunption, usagePoint, findMeterRole(DefaultMeterRole.CONSUMPTION), fifteenMinuteskWhForward);

        Meter meterProduction = setupMeter("meterProduction");
        activateMeter(meterProduction, usagePoint, findMeterRole(DefaultMeterRole.PRODUCTION), fifteenMinuteskWhReverse);

        MetrologyContract contractInformation = metrologyConfiguration.getContracts()
                .stream()
                .filter(mcr -> mcr.getMetrologyPurpose().equals(findPurpose(DefaultMetrologyPurpose.INFORMATION)))
                .findFirst()
                .get();

        // Business method
        usagePoint.apply(metrologyConfiguration, INSTALLATION_TIME, Stream.of(contractInformation).collect(Collectors.toSet()));

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
        assertThat(currentEffectiveMetrologyConfiguration.get().getMetrologyConfiguration().getContracts().get(0).getStatus(usagePoint).isComplete()).isTrue();

        MetrologyContract contract1 = metrologyConfiguration.getContracts()
                .stream()
                .filter(mcr -> mcr.getMetrologyPurpose().equals(findPurpose(DefaultMetrologyPurpose.INFORMATION)))
                .findFirst()
                .get();
        MetrologyContract contract2 = metrologyConfiguration.getContracts()
                .stream()
                .filter(mcr -> mcr.getMetrologyPurpose().equals(findPurpose(DefaultMetrologyPurpose.BILLING)))
                .findFirst()
                .get();

        assertThat(currentEffectiveMetrologyConfiguration.get().getChannelsContainer(contract1).isPresent()).isTrue();
        assertThat(currentEffectiveMetrologyConfiguration.get().getChannelsContainer(contract2).isPresent()).isTrue();


        // Asserts that usage point has no linked metrology configuration before installation time
        effectiveMetrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration(INSTALLATION_TIME.minusMillis(1));
        assertThat(effectiveMetrologyConfiguration).isEmpty();
    }

    private Meter setupMeter(String amrIdBase) {
        AmrSystem mdc = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        return mdc.newMeter(amrIdBase, amrIdBase).create();
    }

    private void activateMeter(Meter meter, UsagePoint usagePoint, MeterRole meterRole, ReadingType... readingTypes) {
        MeterActivation meterActivationOnUsagePoint = usagePoint.activate(meter, meterRole, INSTALLATION_TIME);
        for (ReadingType readingType : readingTypes) {
            meterActivationOnUsagePoint.getChannelsContainer().createChannel(readingType);
        }
        usagePoint.update();
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

        FullySpecifiedReadingTypeRequirement consumption = metrologyConfiguration.newReadingTypeRequirement("A-", findMeterRole(DefaultMeterRole.CONSUMPTION))
                .withReadingType(fifteenMinuteskWhForward);
        FullySpecifiedReadingTypeRequirement production = metrologyConfiguration.newReadingTypeRequirement("A+", findMeterRole(DefaultMeterRole.PRODUCTION))
                .withReadingType(fifteenMinuteskWhReverse);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("consumption", fifteenMinuteskWhForward, Formula.Mode.AUTO);
        ReadingTypeDeliverable dConsumption = builder.build(builder.requirement(consumption));
        ReadingTypeDeliverableBuilder builderProduction = metrologyConfiguration.newReadingTypeDeliverable("production", fifteenMinuteskWhReverse, Formula.Mode.AUTO);
        ReadingTypeDeliverable dProduction = builderProduction.build(builderProduction.requirement(production));

        // add deliverables with requirements to information contract
        MetrologyContract informationMetrologyContract = metrologyConfiguration.addMetrologyContract(findPurpose(DefaultMetrologyPurpose.INFORMATION));
        informationMetrologyContract.addDeliverable(dConsumption);
        informationMetrologyContract.update();

        // add deliverables with requirements to billing contract
        MetrologyContract billingMetrologyContract = metrologyConfiguration.addMandatoryMetrologyContract(findPurpose(DefaultMetrologyPurpose.BILLING));
        billingMetrologyContract.addDeliverable(dProduction);
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
