/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.transaction.TransactionContext;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.NoSuchElementException;

import org.assertj.core.data.MapEntry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class OutputChannelDependenciesIT {
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();
    private static MeteringService meteringService;
    private static ServerMetrologyConfigurationService metrologyConfigurationService;
    private static MeterRole defaultMeterRole;
    private static ReadingType min15Plus, min15Minus, dailyPlus, monthlyPlus, monthlyMinus, monthlyTotal, monthlyNet, nil;
    private static Channel min15PlusChannel, dailyPlusChannel, monthlyPlusChannel, monthlyMinusChannel, monthlyTotalChannel,
            monthlyNetChannel, nilChannel, min15PlusMeterChannel, min15MinusMeterChannel, meterNilChannel;
    private static UsagePoint usagePoint;
    private static Meter meter;
    private static ChannelsContainer informationContractChannelsContainer, meterChannelsContainer;
    private static ReadingTypeRequirement aPlusRequirement, aMinusRequirement;
    private static ReadingTypeDeliverable min15PlusDeliverable, dailyPlusDeliverable, monthlyPlusDeliverable, monthlyMinusDeliverable,
            monthlyTotalDeliverable, monthlyNetDeliverable, nilDeliverable;
    private static MetrologyContract informationContract;
    private static UsagePointMetrologyConfiguration metrologyConfiguration;
    private static ServiceCategory electricity;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        meteringService = inMemoryBootstrapModule.getMeteringService();
        metrologyConfigurationService = inMemoryBootstrapModule.getMetrologyConfigurationService();
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            min15Minus = getReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0");
            min15Plus = getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            dailyPlus = getReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            monthlyPlus = getReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            monthlyMinus = getReadingType("13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0");
            monthlyTotal = getReadingType("13.0.0.4.20.1.12.0.0.0.0.0.0.0.0.3.72.0");
            monthlyNet = getReadingType("13.0.0.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0");
            nil = getReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
            inMemoryBootstrapModule.getMeteringDataModelService().addHeadEndInterface(new TestHeadEndInterface(min15Minus, min15Plus));
            electricity = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                    .orElseThrow(() -> new NoSuchElementException("Requested service category is not found"));
            MetrologyPurpose information = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                    .orElseThrow(() -> new NoSuchElementException("Information purpose is not found"));
            metrologyConfiguration = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Test", electricity).create();
            informationContract = metrologyConfiguration.addMandatoryMetrologyContract(information);
            defaultMeterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                    .orElseThrow(() -> new NoSuchElementException("Default meter role is not found"));
            aPlusRequirement = addReadingTypeRequirement(metrologyConfiguration, DefaultReadingTypeTemplate.A_PLUS, defaultMeterRole);
            aMinusRequirement = addReadingTypeRequirement(metrologyConfiguration, DefaultReadingTypeTemplate.A_MINUS, defaultMeterRole);
            ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("15 min A+", min15Plus, Formula.Mode.AUTO);
            min15PlusDeliverable = builder.build(builder.requirement(aPlusRequirement));
            informationContract.addDeliverable(min15PlusDeliverable);
            builder = metrologyConfiguration.newReadingTypeDeliverable("Daily A+", dailyPlus, Formula.Mode.AUTO);
            dailyPlusDeliverable = builder.build(builder.minus(builder.deliverable(min15PlusDeliverable), builder.constant(0)));
            informationContract.addDeliverable(dailyPlusDeliverable);
            builder = metrologyConfiguration.newReadingTypeDeliverable("Monthly A+", monthlyPlus, Formula.Mode.AUTO);
            monthlyPlusDeliverable = builder.build(
                    builder.divide(builder.plus(builder.requirement(aPlusRequirement), builder.deliverable(dailyPlusDeliverable)), builder.constant(2)));
            informationContract.addDeliverable(monthlyPlusDeliverable);
            builder = metrologyConfiguration.newReadingTypeDeliverable("Monthly A-", monthlyMinus, Formula.Mode.AUTO);
            monthlyMinusDeliverable = builder.build(builder.multiply(builder.constant(1), builder.requirement(aMinusRequirement)));
            informationContract.addDeliverable(monthlyMinusDeliverable);
            builder = metrologyConfiguration.newReadingTypeDeliverable("Monthly total", monthlyTotal, Formula.Mode.AUTO);
            monthlyTotalDeliverable = builder.build(
                    builder.plus(builder.deliverable(monthlyMinusDeliverable), builder.deliverable(monthlyPlusDeliverable)));
            informationContract.addDeliverable(monthlyTotalDeliverable);
            builder = metrologyConfiguration.newReadingTypeDeliverable("Monthly net", monthlyNet, Formula.Mode.AUTO);
            monthlyNetDeliverable = builder.build(
                    builder.minus(builder.deliverable(monthlyPlusDeliverable), builder.deliverable(monthlyMinusDeliverable)));
            informationContract.addDeliverable(monthlyNetDeliverable);
            builder = metrologyConfiguration.newReadingTypeDeliverable("Nil", nil, Formula.Mode.EXPERT);
            nilDeliverable = builder.build(builder.nullValue());
            informationContract.addDeliverable(nilDeliverable);
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testGetDeliverables() {
        assertThat(informationContract.getDeliverables()).containsOnly(min15PlusDeliverable, dailyPlusDeliverable, monthlyPlusDeliverable,
                monthlyMinusDeliverable, monthlyTotalDeliverable, monthlyNetDeliverable, nilDeliverable);
    }

    @Test
    public void testGetRequirements() {
        assertThat(informationContract.getRequirements()).containsOnly(aPlusRequirement, aMinusRequirement);
    }

    @Test
    public void testSortReadingTypesByDependencyLevel() {
        assertThat(informationContract.sortReadingTypesByDependencyLevel()).containsExactly(
                ImmutableSet.of(nil, min15Plus, monthlyMinus),
                ImmutableSet.of(dailyPlus),
                ImmutableSet.of(monthlyPlus),
                ImmutableSet.of(monthlyTotal, monthlyNet));
    }

    @Test
    @Transactional
    public void testGetDependencyScopeForOutputChannels() {
        setUpUsagePoint();
        Range<Instant> range = informationContractChannelsContainer.getRange();
        assertThat(informationContractChannelsContainer.findDependentChannelScope(Collections.emptyMap())).isEmpty();
        assertThat(informationContractChannelsContainer.findDependentChannelScope(ImmutableMap.of(
                nilChannel, range,
                monthlyTotalChannel, range,
                monthlyNetChannel, range
        ))).isEmpty();
        Range<Instant> fiveSeconds = Range.closedOpen(Instant.EPOCH, Instant.EPOCH.plusSeconds(5));
        assertThat(informationContractChannelsContainer.findDependentChannelScope(ImmutableMap.of(
                monthlyPlusChannel, range
        ))).containsOnly(MapEntry.entry(monthlyTotalChannel, range), MapEntry.entry(monthlyNetChannel, range));
        assertThat(informationContractChannelsContainer.findDependentChannelScope(ImmutableMap.of(
                monthlyPlusChannel, fiveSeconds,
                monthlyMinusChannel, Range.greaterThan(Instant.EPOCH.plusSeconds(5))
        ))).containsOnly(MapEntry.entry(monthlyTotalChannel, range), MapEntry.entry(monthlyNetChannel, range));
        assertThat(informationContractChannelsContainer.findDependentChannelScope(ImmutableMap.of(
                monthlyPlusChannel, range,
                monthlyTotalChannel, Range.all()
        ))).containsOnly(MapEntry.entry(monthlyTotalChannel, range), MapEntry.entry(monthlyNetChannel, range));
        assertThat(informationContractChannelsContainer.findDependentChannelScope(ImmutableMap.of(
                dailyPlusChannel, fiveSeconds,
                monthlyMinusChannel, Range.greaterThan(Instant.EPOCH.plusSeconds(5))
        ))).containsOnly(MapEntry.entry(monthlyPlusChannel, fiveSeconds),
                MapEntry.entry(monthlyTotalChannel, range), MapEntry.entry(monthlyNetChannel, range));
        assertThat(informationContractChannelsContainer.findDependentChannelScope(ImmutableMap.of(
                min15PlusChannel, fiveSeconds
        ))).containsOnly(MapEntry.entry(dailyPlusChannel, fiveSeconds), MapEntry.entry(monthlyPlusChannel, fiveSeconds),
                MapEntry.entry(monthlyTotalChannel, fiveSeconds), MapEntry.entry(monthlyNetChannel, fiveSeconds));
    }

    @Test
    @Transactional
    public void testGetDependencyScopeForInputChannels() {
        setUpUsagePoint();
        setUpMeter();
        Range<Instant> range = informationContractChannelsContainer.getRange();
        assertThat(meterChannelsContainer.findDependentChannelScope(Collections.emptyMap())).isEmpty();
        assertThat(meterChannelsContainer.findDependentChannelScope(ImmutableMap.of(
                meterNilChannel, Range.all()
        ))).isEmpty();
        assertThat(meterChannelsContainer.findDependentChannelScope(ImmutableMap.of(
                min15MinusMeterChannel, Range.all()
        ))).containsOnly(MapEntry.entry(monthlyMinusChannel, range),
                MapEntry.entry(monthlyTotalChannel, range), MapEntry.entry(monthlyNetChannel, range));
        Range<Instant> beforeFiveSeconds = Range.lessThan(Instant.EPOCH.plusSeconds(5));
        Range<Instant> afterFiveSeconds = Range.atLeast(Instant.EPOCH.plusSeconds(5));
        Range<Instant> fiveSeconds = beforeFiveSeconds.intersection(range);
        assertThat(meterChannelsContainer.findDependentChannelScope(ImmutableMap.of(
                min15PlusMeterChannel, beforeFiveSeconds,
                min15MinusMeterChannel, afterFiveSeconds
        ))).containsOnly(MapEntry.entry(min15PlusChannel, fiveSeconds), MapEntry.entry(dailyPlusChannel, fiveSeconds),
                MapEntry.entry(monthlyPlusChannel, fiveSeconds), MapEntry.entry(monthlyMinusChannel, afterFiveSeconds),
                MapEntry.entry(monthlyTotalChannel, range), MapEntry.entry(monthlyNetChannel, range));
    }

    private static void setUpUsagePoint() {
        usagePoint = electricity.newUsagePoint("TniopEgasu", Instant.EPOCH).create();
        usagePoint.apply(metrologyConfiguration, Instant.EPOCH);
        informationContractChannelsContainer = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(() -> new NoSuchElementException("Metrology configuration is assigned to a usage point, " +
                        "but no effective metrology configuration is found afterwards."))
                .getChannelsContainer(informationContract)
                .orElseThrow(() -> new NoSuchElementException("Channels container related to information contract is not found."));
        min15PlusChannel = getChannel(informationContractChannelsContainer, min15Plus);
        dailyPlusChannel = getChannel(informationContractChannelsContainer, dailyPlus);
        monthlyPlusChannel = getChannel(informationContractChannelsContainer, monthlyPlus);
        monthlyMinusChannel = getChannel(informationContractChannelsContainer, monthlyMinus);
        monthlyTotalChannel = getChannel(informationContractChannelsContainer, monthlyTotal);
        monthlyNetChannel = getChannel(informationContractChannelsContainer, monthlyNet);
        nilChannel = getChannel(informationContractChannelsContainer, nil);
    }

    private static void setUpMeter() {
        meter = meteringService.findAmrSystem(1)
                .orElseThrow(() -> new NoSuchElementException("Default AMR system is not found."))
                .newMeter("42", "Retem").create();
        usagePoint.linkMeters().activate(Instant.EPOCH, meter, defaultMeterRole).complete();

        MeterActivation meterActivation = meter.getCurrentMeterActivation()
                .orElseThrow(() -> new NoSuchElementException("Current meter activation is not found."));
        meterChannelsContainer = meterActivation.getChannelsContainer();
        min15PlusMeterChannel = getChannel(meterChannelsContainer, min15Plus);
        min15MinusMeterChannel = getChannel(meterChannelsContainer, min15Minus);
        meterNilChannel = meterChannelsContainer.createChannel(nil);
    }

    private static ReadingType getReadingType(String code) {
        return meteringService.findReadingTypes(Collections.singletonList(code))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType(code, code));
    }

    private static ReadingTypeRequirement addReadingTypeRequirement(UsagePointMetrologyConfiguration metrologyConfiguration,
                                                                    DefaultReadingTypeTemplate template, MeterRole meterRole) {
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingTypeTemplate actualTemplate = metrologyConfigurationService.findReadingTypeTemplate(template.getNameTranslation().getDefaultFormat())
                .orElseThrow(() -> new NoSuchElementException("Default reading type template is not found"));
        return metrologyConfiguration.newReadingTypeRequirement(template.getNameTranslation().getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(actualTemplate);

    }

    private static Channel getChannel(ChannelsContainer container, ReadingType readingType) {
        return container.getChannel(readingType)
                .orElseThrow(() -> new NoSuchElementException("Channel related to reading type '" + readingType.getMRID() + "' is not found"));
    }
}
