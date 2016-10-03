package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointMeterActivationException;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.transaction.TransactionContext;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointMeterActivatorImplManageActivationsIT {

    public static final String READING_TYPE_MRID = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule(READING_TYPE_MRID);
    private static Instant INSTALLATION_TIME = ZonedDateTime.of(2016, 7, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static Instant THREE_DAYS_BEFORE = INSTALLATION_TIME.minus(3, ChronoUnit.DAYS);
    private static Instant TWO_DAYS_BEFORE = INSTALLATION_TIME.minus(2, ChronoUnit.DAYS);
    private static Instant ONE_DAY_BEFORE = INSTALLATION_TIME.minus(1, ChronoUnit.DAYS);
    private static Instant ONE_DAY_AFTER = INSTALLATION_TIME.plus(1, ChronoUnit.DAYS);
    private static Instant TWO_DAYS_AFTER = INSTALLATION_TIME.plus(2, ChronoUnit.DAYS);
    private static Instant THREE_DAYS_AFTER = INSTALLATION_TIME.plus(3, ChronoUnit.DAYS);

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    private static Meter meter;
    private static MeterRole meterRole;
    private static UsagePoint usagePoint;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
            AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
            meter = system.newMeter("Meter", "myName").create();
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            usagePoint = serviceCategory.newUsagePoint("UsagePoint", INSTALLATION_TIME).create();
            meterRole = inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
            context.commit();
        }
    }

    private static void reloadObjects() {
        meter = inMemoryBootstrapModule.getMeteringService().findMeterById(meter.getId()).get();
        usagePoint = inMemoryBootstrapModule.getMeteringService().findUsagePointById(usagePoint.getId()).get();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void beforeTest() {
        reloadObjects();
    }

    @After
    public void afterTest() {
        reloadObjects();
    }

    @Test
    @Transactional
    public void testHasNoActivations() {
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(1);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(1);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));
    }

    @Test
    @Transactional
    public void testHasSingleActivationBefore() {
        meter.activate(Range.closedOpen(TWO_DAYS_BEFORE, ONE_DAY_BEFORE));
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(2);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.closedOpen(TWO_DAYS_BEFORE, INSTALLATION_TIME));
        assertThat(meterActivations.get(1).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(1);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));
    }

    @Test
    @Transactional
    public void testHasSingleEffectiveActivation() {
        meter.activate(Range.closedOpen(TWO_DAYS_BEFORE, TWO_DAYS_AFTER));
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(2);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.closedOpen(TWO_DAYS_BEFORE, INSTALLATION_TIME));
        assertThat(meterActivations.get(1).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(1);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));
    }

    @Test
    @Transactional
    public void testHasSingleActivationAfter() {
        meter.activate(Range.closedOpen(ONE_DAY_AFTER, TWO_DAYS_AFTER));
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(1);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(1);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));
    }

    @Test
    @Transactional
    public void testHasTwoActivationsBefore() {
        meter.activate(Range.closedOpen(THREE_DAYS_BEFORE, TWO_DAYS_BEFORE));
        meter.activate(Range.closedOpen(TWO_DAYS_BEFORE, ONE_DAY_BEFORE));
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(3);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.closedOpen(THREE_DAYS_BEFORE, TWO_DAYS_BEFORE));
        assertThat(meterActivations.get(1).getRange()).isEqualTo(Range.closedOpen(TWO_DAYS_BEFORE, INSTALLATION_TIME));
        assertThat(meterActivations.get(2).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(1);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));
    }

    @Test
    @Transactional
    public void testHasActivationsBeforeAndEffective() {
        meter.activate(Range.closedOpen(TWO_DAYS_BEFORE, ONE_DAY_BEFORE));
        meter.activate(Range.closedOpen(ONE_DAY_BEFORE, ONE_DAY_AFTER));
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(3);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.closedOpen(TWO_DAYS_BEFORE, ONE_DAY_BEFORE));
        assertThat(meterActivations.get(1).getRange()).isEqualTo(Range.closedOpen(ONE_DAY_BEFORE, INSTALLATION_TIME));
        assertThat(meterActivations.get(2).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(1);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));
    }

    @Test
    @Transactional
    public void testHasTwoActivationsAfter() {
        meter.activate(Range.closedOpen(ONE_DAY_AFTER, TWO_DAYS_AFTER));
        meter.activate(Range.closedOpen(TWO_DAYS_AFTER, THREE_DAYS_AFTER));
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(2);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.closedOpen(INSTALLATION_TIME, TWO_DAYS_AFTER));
        assertThat(meterActivations.get(1).getRange()).isEqualTo(Range.atLeast(TWO_DAYS_AFTER));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(2);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.closedOpen(INSTALLATION_TIME, TWO_DAYS_AFTER));
        assertThat(usagePointActivations.get(1).getRange()).isEqualTo(Range.atLeast(TWO_DAYS_AFTER));
    }

    @Test
    @Transactional
    public void testHasActivationsTheSameAsDesired() {
        meter.activate(Range.atLeast(INSTALLATION_TIME));
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(1);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(1);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));
    }

    @Test
    @Transactional
    public void testHasActivationBeforeAndActivationWithInstallationStartTime() {
        meter.activate(Range.closedOpen(TWO_DAYS_BEFORE, INSTALLATION_TIME));
        meter.activate(Range.closedOpen(INSTALLATION_TIME, TWO_DAYS_AFTER));
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(2);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.closedOpen(TWO_DAYS_BEFORE, INSTALLATION_TIME));
        assertThat(meterActivations.get(1).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(1);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.atLeast(INSTALLATION_TIME));
    }

    @Test
    @Transactional
    public void testHasEffectiveActivationAndActivationAfter() {
        meter.activate(Range.closedOpen(TWO_DAYS_BEFORE, TWO_DAYS_AFTER));
        meter.activate(Range.closedOpen(TWO_DAYS_AFTER, THREE_DAYS_AFTER));
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(3);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.closedOpen(TWO_DAYS_BEFORE, INSTALLATION_TIME));
        assertThat(meterActivations.get(1).getRange()).isEqualTo(Range.closedOpen(INSTALLATION_TIME, TWO_DAYS_AFTER));
        assertThat(meterActivations.get(2).getRange()).isEqualTo(Range.atLeast(TWO_DAYS_AFTER));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(2);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.closedOpen(INSTALLATION_TIME, TWO_DAYS_AFTER));
        assertThat(usagePointActivations.get(1).getRange()).isEqualTo(Range.atLeast(TWO_DAYS_AFTER));
    }

    @Test
    @Transactional
    public void testCopyDataFromEffectiveConfiguration() {
        MeterActivation activationWithReadings = meter.activate(Range.closedOpen(TWO_DAYS_BEFORE, TWO_DAYS_AFTER));
        meter.activate(Range.closedOpen(TWO_DAYS_AFTER, THREE_DAYS_AFTER));
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().getReadingType(READING_TYPE_MRID).get();
        activationWithReadings.getChannelsContainer().createChannel(readingType);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(READING_TYPE_MRID);
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(INSTALLATION_TIME.minus(45, ChronoUnit.MINUTES), BigDecimal.valueOf(1045, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(INSTALLATION_TIME.minus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(1030, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(INSTALLATION_TIME.minus(15, ChronoUnit.MINUTES), BigDecimal.valueOf(1015, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(INSTALLATION_TIME.plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(2030, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(INSTALLATION_TIME.plus(45, ChronoUnit.MINUTES), BigDecimal.valueOf(2045, 2)));
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);
        inMemoryBootstrapModule.getMeteringDataModelService().addHeadEndInterface(new TestHeadEndInterface(readingType));
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        reloadObjects();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(3);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.closedOpen(TWO_DAYS_BEFORE, INSTALLATION_TIME));
        List<BaseReadingRecord> readings = meterActivations.get(0).getChannelsContainer()
                .getChannel(readingType).get().getReadings(Range.closedOpen(TWO_DAYS_BEFORE, INSTALLATION_TIME));
        assertThat(readings).hasSize(3);
        assertThat(readings.stream().map(reading -> reading.getQuantity(0).getValue()).collect(Collectors.toList()))
                .containsExactly(BigDecimal.valueOf(1045, 2), BigDecimal.valueOf(1030, 2), BigDecimal.valueOf(1015, 2));
        assertThat(meterActivations.get(1).getRange()).isEqualTo(Range.closedOpen(INSTALLATION_TIME, TWO_DAYS_AFTER));
        readings = meterActivations.get(1).getChannelsContainer().getChannel(readingType).get()
                .getReadings(Range.closedOpen(INSTALLATION_TIME, TWO_DAYS_AFTER));
        assertThat(readings).hasSize(2);
        assertThat(readings.stream().map(reading -> reading.getQuantity(0).getValue()).collect(Collectors.toList()))
                .containsExactly(BigDecimal.valueOf(2030, 2), BigDecimal.valueOf(2045, 2));
        assertThat(meterActivations.get(2).getRange()).isEqualTo(Range.atLeast(TWO_DAYS_AFTER));
    }

    @Test
    @Transactional
    public void testClearActivationsInTheMiddle() {
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint2 = serviceCategory.newUsagePoint("UsagePoint2", ONE_DAY_BEFORE).create();
        AmrSystem system = inMemoryBootstrapModule.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter2 = system.newMeter("Meter2", "myName2").create();
        MeterRole meterRole2 = inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.MAIN);

        usagePoint2.linkMeters().activate(meter, meterRole).complete();
        usagePoint2.linkMeters().clear(ONE_DAY_AFTER, meterRole).complete();
        reloadObjects();
        usagePoint.linkMeters().activate(ONE_DAY_AFTER, meter, meterRole)
                .activate(INSTALLATION_TIME, meter2, meterRole2).complete();
        UsagePointMeterActivatorImpl activator = (UsagePointMeterActivatorImpl) usagePoint.linkMeters();
        activator.clear(Range.closedOpen(INSTALLATION_TIME, THREE_DAYS_AFTER), meterRole);
        activator.clear(Range.closedOpen(INSTALLATION_TIME, THREE_DAYS_AFTER), meterRole2).complete();
        reloadObjects();
        usagePoint2 = inMemoryBootstrapModule.getMeteringService().findUsagePointById(usagePoint2.getId()).get();
        meter2 = inMemoryBootstrapModule.getMeteringService().findMeterById(meter2.getId()).get();

        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        assertThat(meterActivations).hasSize(3);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.closedOpen(ONE_DAY_BEFORE, ONE_DAY_AFTER));
        assertThat(meterActivations.get(1).getRange()).isEqualTo(Range.closedOpen(ONE_DAY_AFTER, THREE_DAYS_AFTER));
        assertThat(meterActivations.get(2).getRange()).isEqualTo(Range.atLeast(THREE_DAYS_AFTER));

        List<MeterActivation> usagePointActivations = usagePoint.getMeterActivations();
        assertThat(usagePointActivations).hasSize(2);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.atLeast(THREE_DAYS_AFTER));
        assertThat(usagePointActivations.get(0).getMeter().get()).isEqualTo(meter);
        assertThat(usagePointActivations.get(1).getRange()).isEqualTo(Range.atLeast(THREE_DAYS_AFTER));
        assertThat(usagePointActivations.get(1).getMeter().get()).isEqualTo(meter2);

        meterActivations = meter2.getMeterActivations();
        assertThat(meterActivations).hasSize(2);
        assertThat(meterActivations.get(0).getRange()).isEqualTo(Range.closedOpen(INSTALLATION_TIME, THREE_DAYS_AFTER));
        assertThat(meterActivations.get(1).getRange()).isEqualTo(Range.atLeast(THREE_DAYS_AFTER));

        usagePointActivations = usagePoint2.getMeterActivations();
        assertThat(usagePointActivations).hasSize(1);
        assertThat(usagePointActivations.get(0).getRange()).isEqualTo(Range.closedOpen(ONE_DAY_BEFORE, ONE_DAY_AFTER));
        assertThat(usagePointActivations.get(0).getMeter().get()).isEqualTo(meter);
    }

    @Test(expected = UsagePointMeterActivationException.UsagePointHasMeterOnThisRole.class)
    @Transactional
    public void testCanNotLinkTwoMetersOnTheSameMeterRole() {
        AmrSystem system = inMemoryBootstrapModule.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter2 = system.newMeter("Meter2", "myName2").create();

        usagePoint.linkMeters()
                .activate(meter, meterRole)
                .activate(meter2, meterRole)
                .throwingValidation()
                .complete();
    }

    private static class TestHeadEndInterface implements HeadEndInterface {
        private List<ReadingType> supportedReadingTypes;

        public TestHeadEndInterface(ReadingType... supported) {
            this.supportedReadingTypes = Arrays.stream(supported).collect(Collectors.toList());
        }

        @Override
        public Optional<URL> getURLForEndDevice(EndDevice endDevice) {
            return Optional.empty();
        }

        @Override
        public EndDeviceCapabilities getCapabilities(EndDevice endDevice) {
            return new EndDeviceCapabilities(this.supportedReadingTypes, Collections.emptyList());
        }

        @Override
        public CommandFactory getCommandFactory() {
            return null;
        }

        @Override
        public CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant) {
            return null;
        }

        @Override
        public CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant, ServiceCall serviceCall) {
            return null;
        }

        @Override
        public CompletionOptions readMeter(Meter meter, List<ReadingType> readingTypes) {
            return null;
        }

        @Override
        public CompletionOptions readMeter(Meter meter, List<ReadingType> readingTypes, ServiceCall serviceCall) {
            return null;
        }

        @Override
        public CompletionOptions sendCommand(EndDeviceCommand endDeviceCommand, Instant releaseDate) {
            return null;
        }

        @Override
        public CompletionOptions sendCommand(EndDeviceCommand endDeviceCommand, Instant releaseDate, ServiceCall parentServiceCall) {
            return null;
        }

        @Override
        public String getAmrSystem() {
            return KnownAmrSystem.MDC.getName();
        }
    }
}
