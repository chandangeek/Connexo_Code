package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;

public class MetrologyContractChannelsContainerImplTestIT {

    private static final String MAIN_READING_TYPE_MRID = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule(MAIN_READING_TYPE_MRID);
    private static MeterRole meterRole;
    private static MetrologyPurpose metrologyPurpose;
    private static ServiceCategory serviceCategory;
    private static ReadingType readingType;

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        meterRole = inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.MAIN);
        metrologyPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
        serviceCategory.addMeterRole(meterRole);
        readingType = inMemoryBootstrapModule.getMeteringService().getReadingType(MAIN_READING_TYPE_MRID).get();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testCanCreateMetrologyConfigurationChannelsContainer() {
        UsagePointMetrologyConfiguration metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("MC", serviceCategory).create();
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("RTD", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable readingTypeDeliverable = builder.build(builder.constant(1080L));
        MetrologyContract metrologyContract = metrologyConfiguration.addMandatoryMetrologyContract(metrologyPurpose);
        metrologyContract.addDeliverable(readingTypeDeliverable);
        MetrologyPurpose metrologyPurpose2 = inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION).get();
        MetrologyContract metrologyContract2 = metrologyConfiguration.addMandatoryMetrologyContract(metrologyPurpose2);

        UsagePoint usagePoint = serviceCategory.newUsagePoint("UP", inMemoryBootstrapModule.getClock().instant()).create();
        usagePoint.apply(metrologyConfiguration);

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class)
                .select(where("metrologyConfiguration").isEqualTo(metrologyConfiguration).and(where("usagePoint").isEqualTo(usagePoint)))
                .get(0);

        assertThat(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).isPresent()).isTrue();
        assertThat(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract2).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testMetrologyConfigurationChannelsContainerHasChannelsAfterCreation() {
        UsagePointMetrologyConfiguration metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("MC", serviceCategory).create();
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("RTD1", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable readingTypeDeliverable1 = builder.build(builder.constant(1080L));

        ReadingType readingType2 = inMemoryBootstrapModule.getMeteringService().getReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", ""));
        builder = metrologyConfiguration.newReadingTypeDeliverable("RTD2", readingType2, Formula.Mode.AUTO);
        ReadingTypeDeliverable readingTypeDeliverable2 = builder.build(builder.constant(270L));

        ReadingType readingType3 = inMemoryBootstrapModule.getMeteringService().getReadingType("0.0.2.1.1.1.19.0.0.0.0.0.0.0.0.3.72.0")
                .orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.1.1.1.19.0.0.0.0.0.0.0.0.3.72.0", ""));
        builder = metrologyConfiguration.newReadingTypeDeliverable("RTD3", readingType3, Formula.Mode.AUTO);
        ReadingTypeDeliverable readingTypeDeliverable3 = builder.build(builder.constant(100L));

        ReadingType readingType4 = inMemoryBootstrapModule.getMeteringService().getReadingType("0.0.2.1.1.1.19.0.0.0.0.0.0.0.0.0.72.0")
                .orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.1.1.1.19.0.0.0.0.0.0.0.0.0.72.0", ""));
        builder = metrologyConfiguration.newReadingTypeDeliverable("RTD4", readingType4, Formula.Mode.AUTO);
        builder.build(builder.constant(80L)); // Need for check that for RTD4 there is no channel

        MetrologyContract metrologyContract = metrologyConfiguration.addMandatoryMetrologyContract(metrologyPurpose);
        metrologyContract.addDeliverable(readingTypeDeliverable1);
        metrologyContract.addDeliverable(readingTypeDeliverable2);

        MetrologyPurpose metrologyPurpose2 = inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION).get();
        MetrologyContract metrologyContract2 = metrologyConfiguration.addMandatoryMetrologyContract(metrologyPurpose2);
        metrologyContract2.addDeliverable(readingTypeDeliverable3);

        UsagePoint usagePoint = serviceCategory.newUsagePoint("UP", inMemoryBootstrapModule.getClock().instant()).create();
        usagePoint.apply(metrologyConfiguration);

        AmrSystem amrSystem = inMemoryBootstrapModule.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = amrSystem.newMeter("").setMRID("meter1").create();
        usagePoint.linkMeters().activate(meter, meterRole);

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class)
                .select(where("metrologyConfiguration").isEqualTo(metrologyConfiguration).and(where("usagePoint").isEqualTo(usagePoint)))
                .get(0);

        ChannelsContainer channelsContainers = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).get();
        assertThat(channelsContainers.getChannels()).hasSize(2);
        assertThat(channelsContainers.getChannels().get(0).getMainReadingType()).isEqualTo(readingType);
        assertThat(channelsContainers.getChannels().get(1).getMainReadingType()).isEqualTo(readingType2);

        channelsContainers = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract2).get();
        assertThat(channelsContainers.getChannels()).hasSize(1);
        assertThat(channelsContainers.getChannels().get(0).getMainReadingType()).isEqualTo(readingType3);
    }

    @Test
    @Transactional
    @Ignore
    public void testGetReadingsFromMetrologyConfigurationChannelsContainer() {
        UsagePointMetrologyConfiguration metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("MC", serviceCategory).create();
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType2 = inMemoryBootstrapModule.getMeteringService().getReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", ""));
        FullySpecifiedReadingTypeRequirement readingTypeRequirement = metrologyConfiguration.newReadingTypeRequirement("RTR").withMeterRole(meterRole).withReadingType(readingType2);
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("RTD", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable readingTypeDeliverable = builder.build(builder.divide(builder.requirement(readingTypeRequirement), builder.constant(1000L)));
        MetrologyContract metrologyContract = metrologyConfiguration.addMandatoryMetrologyContract(metrologyPurpose);
        metrologyContract.addDeliverable(readingTypeDeliverable);

        UsagePoint usagePoint = serviceCategory.newUsagePoint("UP", inMemoryBootstrapModule.getClock().instant()).create();
        usagePoint.apply(metrologyConfiguration);

        AmrSystem amrSystem = inMemoryBootstrapModule.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = amrSystem.newMeter("").setMRID("meter1").create();
        usagePoint.linkMeters().activate(meter, meterRole).complete();
        meter = inMemoryBootstrapModule.getMeteringService().findMeter(meter.getId()).get();

        ZonedDateTime readingTime = ZonedDateTime.ofInstant(inMemoryBootstrapModule.getClock().instant(), meter.getZoneId());
        IntervalReading reading = IntervalReadingImpl.of(readingTime.plus(1, ChronoUnit.DAYS).with(ChronoField.MILLI_OF_DAY, 0).toInstant(), new BigDecimal(870000L), ProfileStatus.of());
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(readingType2.getMRID());
        intervalBlock.addIntervalReading(reading);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(meterReading);

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class)
                .select(where("metrologyConfiguration").isEqualTo(metrologyConfiguration).and(where("usagePoint").isEqualTo(usagePoint)))
                .get(0);

        Channel channel = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).get()
                .getChannel(readingType).get();

        List<BaseReadingRecord> readings = channel.getReadings(effectiveMetrologyConfiguration.getRange());
        assertThat(readings).hasSize(1);
    }
}
