package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.transaction.TransactionContext;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FindReadingQualitiesIT {
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());
    private static final String READING_TYPE_DELTA = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String READING_TYPE_BULK = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule(READING_TYPE_DELTA, READING_TYPE_BULK);
    private static final Instant START_TIME = ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant FIRST_TIME = ZonedDateTime.of(2012, 12, 20, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant SECOND_TIME = ZonedDateTime.of(2012, 12, 25, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    private static Channel channel1, channel2;
    private static CimChannel deltaCimChannel1, bulkCimChannel1;
    private static ReadingQualityRecord watchdog, batteryLow, added, suspect, overflow, estimatedGeneric, estimated;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Meter meter;
        MeterActivation meterActivation;
        ReadingType deltaReadingType, bulkReadingType;
        try (TransactionContext ctx = inMemoryBootstrapModule.getTransactionService().getContext()) {
            AmrSystem system = meteringService.findAmrSystem(1).get();
            meter = system.newMeter("FindReadingQualitiesIT", "myName").create();
            meterActivation = meter.activate(START_TIME);
            deltaReadingType = meteringService.getReadingType(READING_TYPE_DELTA).get();
            bulkReadingType = meteringService.getReadingType(READING_TYPE_BULK).get();
            channel1 = meterActivation.getChannelsContainer().createChannel(deltaReadingType, bulkReadingType);
            channel2 = meterActivation.getChannelsContainer().createChannel(deltaReadingType);
            deltaCimChannel1 = channel1.getCimChannel(deltaReadingType).get();
            bulkCimChannel1 = channel1.getCimChannel(bulkReadingType).get();

            batteryLow = channel1.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.BATTERYLOW), bulkReadingType, FIRST_TIME);
            watchdog = channel1.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.WATCHDOGFLAG), deltaReadingType, START_TIME);

            added = channel2.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED), deltaReadingType, START_TIME);
            suspect = channel2.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), deltaReadingType, SECOND_TIME);
            estimatedGeneric = channel2.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.EXTERNAL, QualityCodeIndex.ESTIMATEGENERIC), deltaReadingType, FIRST_TIME);
            estimated = channel2.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.OTHER, QualityCodeCategory.ESTIMATED, 1000), deltaReadingType, FIRST_TIME);
            overflow = channel2.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.ENDDEVICE, QualityCodeIndex.OVERFLOWCONDITIONDETECTED), deltaReadingType, SECOND_TIME);
            overflow.makePast();
            ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testBasics() {
        List<ReadingQualityRecord> list = channel1.findReadingQualities().collect();
        assertThat(list).containsOnly(batteryLow, watchdog);
        assertThat(channel1.findReadingQualities().findFirst()).contains(list.get(0));
        assertThat(deltaCimChannel1.findReadingQualities().collect()).containsOnly(watchdog);
        assertThat(deltaCimChannel1.findReadingQualities().findFirst()).contains(watchdog);
        assertThat(bulkCimChannel1.findReadingQualities().collect()).containsOnly(batteryLow);
        assertThat(bulkCimChannel1.findReadingQualities().findFirst()).contains(batteryLow);
        assertThat(channel2.findReadingQualities().collect()).containsOnly(added, estimated, estimatedGeneric, overflow, suspect);
    }

    @Test
    public void testSorted() {
        assertThat(channel1.findReadingQualities().sorted().collect()).containsExactly(watchdog, batteryLow);
        assertThat(channel1.findReadingQualities().sorted().findFirst()).contains(watchdog);
        List<ReadingQualityRecord> list = channel2.findReadingQualities().sorted().collect();
        assertThat(list).hasSize(5);
        assertThat(list.get(0)).isEqualTo(added);
        assertThat(list.subList(1, 3)).containsOnly(estimated, estimatedGeneric);
        assertThat(list.subList(3, 5)).containsOnly(suspect, overflow);
        assertThat(channel2.findReadingQualities().sorted()
                .ofQualityIndex(QualityCodeIndex.ADDED).ofQualitySystems(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.EXTERNAL))
                .orOfAnotherTypeInSameSystems().ofAnyQualityIndexInCategory(QualityCodeCategory.ESTIMATED)
                .orOfAnotherType().ofQualitySystems(ImmutableSet.of(QualityCodeSystem.MDM, QualityCodeSystem.ENDDEVICE))
                .actual()
                .inTimeInterval(Range.lessThan(SECOND_TIME.plusMillis(1)))
                .collect())
                .containsExactly(added, estimatedGeneric, suspect);
    }

    @Test
    public void testTimestamp() {
        assertThat(channel1.findReadingQualities().atTimestamp(FIRST_TIME).collect())
                .containsOnly(batteryLow);
        assertThat(deltaCimChannel1.findReadingQualities().atTimestamp(FIRST_TIME).collect())
                .isEmpty();
        assertThat(bulkCimChannel1.findReadingQualities().atTimestamp(FIRST_TIME).collect())
                .containsOnly(batteryLow);
        assertThat(channel2.findReadingQualities().atTimestamp(FIRST_TIME).collect())
                .containsOnly(estimatedGeneric, estimated);
    }

    @Test
    public void testInTimeInterval() {
        assertThat(channel1.findReadingQualities().inTimeInterval(Range.all()).collect())
                .containsOnly(batteryLow, watchdog);
        assertThat(channel2.findReadingQualities().inTimeInterval(Range.all()).collect())
                .containsOnly(added, estimated, estimatedGeneric, overflow, suspect);
        assertThat(channel2.findReadingQualities().inTimeInterval(Range.closed(FIRST_TIME, SECOND_TIME)).collect())
                .containsOnly(estimated, estimatedGeneric, overflow, suspect);
        assertThat(channel2.findReadingQualities().inTimeInterval(Range.closedOpen(FIRST_TIME, SECOND_TIME)).collect())
                .containsOnly(estimated, estimatedGeneric);
        assertThat(channel2.findReadingQualities().inTimeInterval(Range.openClosed(FIRST_TIME, SECOND_TIME)).collect())
                .containsOnly(overflow, suspect);
        assertThat(channel2.findReadingQualities().inTimeInterval(Range.open(FIRST_TIME, SECOND_TIME)).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities().inTimeInterval(Range.atMost(FIRST_TIME)).collect())
                .containsOnly(added, estimated, estimatedGeneric);
    }

    @Test
    public void testActual() {
        assertThat(channel2.findReadingQualities().atTimestamp(SECOND_TIME).actual().collect())
                .containsOnly(suspect);
        assertThat(channel2.findReadingQualities().inTimeInterval(Range.closed(FIRST_TIME, SECOND_TIME)).actual().collect())
                .containsOnly(estimated, estimatedGeneric, suspect);
        assertThat(channel2.findReadingQualities().actual().collect())
                .containsOnly(added, estimated, estimatedGeneric, suspect);
    }

    @Test
    public void testOfQualitySystem() {
        assertThat(deltaCimChannel1.findReadingQualities().ofQualitySystem(QualityCodeSystem.MDC).collect())
                .containsOnly(watchdog);
        assertThat(deltaCimChannel1.findReadingQualities().ofQualitySystem(QualityCodeSystem.MDM).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities().ofQualitySystem(QualityCodeSystem.MDC).collect())
                .containsOnly(added);
        assertThat(channel2.findReadingQualities().ofQualitySystem(QualityCodeSystem.MDM).collect())
                .containsOnly(suspect);
    }

    @Test
    public void testOfQualitySystems() {
        assertThat(deltaCimChannel1.findReadingQualities().ofQualitySystems(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM)).collect())
                .containsOnly(watchdog);
        assertThat(deltaCimChannel1.findReadingQualities().ofQualitySystems(ImmutableSet.of(QualityCodeSystem.EXTERNAL, QualityCodeSystem.OTHER)).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities().ofQualitySystems(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM)).collect())
                .containsOnly(added, suspect);
        assertThat(channel2.findReadingQualities().ofQualitySystems(ImmutableSet.of(QualityCodeSystem.ENDDEVICE)).collect())
                .containsOnly(overflow);
        assertThat(channel2.findReadingQualities().ofQualitySystems(ImmutableSet.of(QualityCodeSystem.EXTERNAL, QualityCodeSystem.OTHER)).collect())
                .containsOnly(estimated, estimatedGeneric);
        assertThat(channel1.findReadingQualities().ofQualitySystems(Collections.emptySet()).collect())
                .containsOnly(batteryLow, watchdog);
        assertThat(channel2.findReadingQualities().ofQualitySystems(Collections.emptySet()).collect())
                .containsOnly(added, estimated, estimatedGeneric, overflow, suspect);
    }

    @Test
    public void testCombinationOfQualitySystemActualAndTime() {
        assertThat(channel1.findReadingQualities().ofQualitySystem(QualityCodeSystem.MDC)
                .atTimestamp(FIRST_TIME).collect())
                .containsOnly(batteryLow);
        assertThat(channel2.findReadingQualities().inTimeInterval(Range.closed(START_TIME, FIRST_TIME))
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM)).collect())
                .containsOnly(added);
        assertThat(channel2.findReadingQualities().actual()
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.EXTERNAL, QualityCodeSystem.OTHER, QualityCodeSystem.ENDDEVICE)).collect())
                .containsOnly(estimated, estimatedGeneric);
        assertThat(channel2.findReadingQualities().ofQualitySystems(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.ENDDEVICE))
                .actual().collect())
                .containsOnly(added);
        assertThat(channel2.findReadingQualities().actual()
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.EXTERNAL, QualityCodeSystem.OTHER, QualityCodeSystem.ENDDEVICE))
                .inTimeInterval(Range.closed(SECOND_TIME, SECOND_TIME)).collect())
                .isEmpty();
    }

    @Test
    public void testOfQualityIndex() {
        assertThat(bulkCimChannel1.findReadingQualities().ofQualityIndex(QualityCodeIndex.BATTERYLOW).collect())
                .containsOnly(batteryLow);
        assertThat(bulkCimChannel1.findReadingQualities().ofQualityIndex(QualityCodeIndex.WATCHDOGFLAG).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities().ofQualityIndex(QualityCodeIndex.ESTIMATEGENERIC).collect())
                .containsOnly(estimatedGeneric);
        assertThat(channel2.findReadingQualities().ofQualityIndex(QualityCodeCategory.ESTIMATED, 1000).collect())
                .containsOnly(estimated);
        assertThat(channel2.findReadingQualities().ofQualityIndex(QualityCodeCategory.REASONABILITY, 258).collect())
                .containsOnly(suspect);
    }

    @Test
    public void testOfQualityIndices() {
        assertThat(channel1.findReadingQualities().ofQualityIndices(ImmutableSet.of(QualityCodeIndex.BATTERYLOW, QualityCodeIndex.ALARMFLAG)).collect())
                .containsOnly(batteryLow);
        assertThat(channel1.findReadingQualities().ofQualityIndices(QualityCodeCategory.DIAGNOSTICS, ImmutableSet.of(1, 4)).collect())
                .containsOnly(batteryLow, watchdog);
        assertThat(channel2.findReadingQualities().ofQualityIndices(ImmutableSet.of(QualityCodeIndex.ESTIMATEGENERIC, QualityCodeIndex.EDITGENERIC)).collect())
                .containsOnly(estimatedGeneric);
        assertThat(channel2.findReadingQualities().ofQualityIndices(QualityCodeCategory.ESTIMATED, ImmutableSet.of(0, 1, 2)).collect())
                .containsOnly(estimatedGeneric);
        assertThat(channel2.findReadingQualities().ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.OVERFLOWCONDITIONDETECTED, QualityCodeIndex.ADDED)).collect())
                .containsOnly(suspect, overflow, added);
        assertThat(channel1.findReadingQualities().ofQualityIndices(Collections.emptySet()).collect())
                .containsOnly(batteryLow, watchdog);
        assertThat(channel1.findReadingQualities().ofQualityIndices(QualityCodeCategory.DIAGNOSTICS, Collections.emptySet()).collect())
                .containsOnly(batteryLow, watchdog);
        assertThat(channel2.findReadingQualities().ofQualityIndices(Collections.emptySet()).collect())
                .containsOnly(added, estimated, estimatedGeneric, overflow, suspect);
        assertThat(channel2.findReadingQualities().ofQualityIndices(QualityCodeCategory.ESTIMATED, Collections.emptySet()).collect())
                .containsOnly(estimated, estimatedGeneric);
    }

    @Test
    public void testCombinationOfQualityIndexActualAndTime() {
        assertThat(bulkCimChannel1.findReadingQualities().ofQualityIndex(QualityCodeIndex.BATTERYLOW)
                .inTimeInterval(Range.greaterThan(FIRST_TIME)).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities().atTimestamp(FIRST_TIME).actual()
                .ofQualityIndex(QualityCodeCategory.ESTIMATED, 1000).collect())
                .containsOnly(estimated);
        assertThat(channel1.findReadingQualities().ofQualityIndices(QualityCodeCategory.DIAGNOSTICS, Collections.emptySet())
                .atTimestamp(START_TIME).collect())
                .containsOnly(watchdog);
        assertThat(channel2.findReadingQualities().actual()
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.ESTIMATEGENERIC, QualityCodeIndex.EDITGENERIC)).collect())
                .containsOnly(estimatedGeneric);
        assertThat(channel2.findReadingQualities().ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.OVERFLOWCONDITIONDETECTED, QualityCodeIndex.ADDED))
                .actual().collect())
                .containsOnly(suspect, added);
        assertThat(channel2.findReadingQualities().ofQualityIndices(Collections.emptySet())
                .inTimeInterval(Range.atMost(SECOND_TIME)).collect())
                .containsOnly(added, estimated, estimatedGeneric, overflow, suspect);
        assertThat(channel2.findReadingQualities().ofQualityIndices(Collections.emptySet())
                .inTimeInterval(Range.atLeast(SECOND_TIME)).collect())
                .containsOnly(overflow, suspect);
        assertThat(channel2.findReadingQualities().actual()
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.ADDED, QualityCodeIndex.ACCEPTED, QualityCodeIndex.ESTIMATEGENERIC,
                        QualityCodeIndex.OVERFLOWCONDITIONDETECTED, QualityCodeIndex.SUSPECT, QualityCodeIndex.REVERSEROTATION))
                .inTimeInterval(Range.atLeast(SECOND_TIME)).collect())
                .containsOnly(suspect);
    }

    @Test
    public void testOfQualityCategory() {
        assertThat(channel1.findReadingQualities().ofAnyQualityIndexInCategory(QualityCodeCategory.DIAGNOSTICS).collect())
                .containsOnly(batteryLow, watchdog);
        assertThat(channel1.findReadingQualities().ofAnyQualityIndexInCategory(QualityCodeCategory.DERIVED).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities().ofAnyQualityIndexInCategory(QualityCodeCategory.REASONABILITY).collect())
                .containsOnly(suspect);
        assertThat(channel2.findReadingQualities().ofAnyQualityIndexInCategory(QualityCodeCategory.ESTIMATED).collect())
                .containsOnly(estimated, estimatedGeneric);
    }

    @Test
    public void testOfQualityCategories() {
        assertThat(channel1.findReadingQualities().ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.REASONABILITY, QualityCodeCategory.DIAGNOSTICS)).collect())
                .containsOnly(batteryLow, watchdog);
        assertThat(channel1.findReadingQualities().ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.REASONABILITY, QualityCodeCategory.VALIDATION,
                QualityCodeCategory.DATACOLLECTION, QualityCodeCategory.PROJECTED, QualityCodeCategory.ESTIMATED, QualityCodeCategory.TAMPER, QualityCodeCategory.EDITED)).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities().ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.ESTIMATED)).collect())
                .containsOnly(estimated, estimatedGeneric);
        assertThat(channel2.findReadingQualities().ofAnyQualityIndexInCategories(Collections.emptySet()).collect())
                .containsOnly(added, estimated, estimatedGeneric, overflow, suspect);
    }

    @Test
    public void testCombinationOfQualityCategoryActualAndTime() {
        assertThat(channel1.findReadingQualities().ofAnyQualityIndexInCategory(QualityCodeCategory.DIAGNOSTICS)
                .atTimestamp(FIRST_TIME).collect())
                .containsOnly(batteryLow);
        assertThat(channel1.findReadingQualities().actual().inTimeInterval(Range.all())
                .ofAnyQualityIndexInCategory(QualityCodeCategory.DERIVED).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities().inTimeInterval(Range.closed(FIRST_TIME, FIRST_TIME))
                .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.ESTIMATED))
                .actual().collect())
                .containsOnly(estimated, estimatedGeneric);
        assertThat(channel2.findReadingQualities().ofAnyQualityIndexInCategories(Collections.emptySet())
                .actual().collect())
                .containsOnly(added, estimated, estimatedGeneric, suspect);
    }

    @Test
    public void testOrOfAnotherType() {
        assertThat(channel1.findReadingQualities()
                .ofQualityIndex(QualityCodeIndex.WATCHDOGFLAG)
                .orOfAnotherType().ofQualityIndex(QualityCodeIndex.BATTERYLOW).collect())
                .containsOnly(watchdog, batteryLow);
        assertThat(channel1.findReadingQualities()
                .ofAnyQualityIndexInCategory(QualityCodeCategory.DIAGNOSTICS).ofQualitySystem(QualityCodeSystem.MDM)
                .orOfAnotherType().ofQualitySystem(QualityCodeSystem.MDC).ofQualityIndex(QualityCodeIndex.SUSPECT).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.ENDDEVICE, QualityCodeSystem.MDM))
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.OVERFLOWCONDITIONDETECTED)).collect())
                .containsOnly(suspect, overflow);
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.ENDDEVICE, QualityCodeSystem.MDM))
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.OVERFLOWCONDITIONDETECTED))
                .orOfAnotherType().ofQualitySystem(QualityCodeSystem.MDC).ofAnyQualityIndexInCategory(QualityCodeCategory.EDITED).collect())
                .containsOnly(suspect, overflow, added);
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.ENDDEVICE, QualityCodeSystem.MDM))
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.OVERFLOWCONDITIONDETECTED))
                .orOfAnotherType().ofQualitySystem(QualityCodeSystem.MDC).ofAnyQualityIndexInCategory(QualityCodeCategory.EDITED)
                .orOfAnotherType().ofQualitySystems(ImmutableSet.of(QualityCodeSystem.EXTERNAL, QualityCodeSystem.NOTAPPLICABLE)).collect())
                .containsOnly(suspect, overflow, added, estimatedGeneric);
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.ENDDEVICE, QualityCodeSystem.MDM))
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.OVERFLOWCONDITIONDETECTED))
                .orOfAnotherType().ofQualitySystem(QualityCodeSystem.MDC).ofAnyQualityIndexInCategory(QualityCodeCategory.EDITED)
                .orOfAnotherType().ofQualitySystems(ImmutableSet.of(QualityCodeSystem.EXTERNAL, QualityCodeSystem.NOTAPPLICABLE))
                .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.REASONABILITY, QualityCodeCategory.EDITED)).collect())
                .containsOnly(suspect, overflow, added);
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.ENDDEVICE, QualityCodeSystem.MDM))
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.OVERFLOWCONDITIONDETECTED))
                .orOfAnotherType().ofQualitySystem(QualityCodeSystem.MDC).ofAnyQualityIndexInCategory(QualityCodeCategory.EDITED)
                .orOfAnotherType().ofQualityIndex(QualityCodeCategory.ESTIMATED, 1000).collect())
                .containsOnly(suspect, overflow, added, estimated);
    }

    @Test
    public void testOrOfAnotherTypeInSameSystems() {
        assertThat(channel1.findReadingQualities()
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.ENDDEVICE))
                .ofQualityIndex(QualityCodeIndex.WATCHDOGFLAG)
                .orOfAnotherTypeInSameSystems().ofAnyQualityIndexInCategory(QualityCodeCategory.DIAGNOSTICS).collect())
                .containsOnly(watchdog, batteryLow);
        assertThat(channel1.findReadingQualities()
                .ofQualityIndex(QualityCodeIndex.WATCHDOGFLAG)
                .ofQualitySystem(QualityCodeSystem.MDC)
                .orOfAnotherTypeInSameSystems().ofQualityIndex(QualityCodeIndex.WATCHDOGFLAG).collect())
                .containsOnly(watchdog);
        assertThat(channel2.findReadingQualities()
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.REJECTED))
                .orOfAnotherTypeInSameSystems().ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED)).collect())
                .containsOnly(added, suspect);
        assertThat(channel2.findReadingQualities().ofQualitySystem(QualityCodeSystem.MDM)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.REJECTED))
                .orOfAnotherTypeInSameSystems().ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED)).collect())
                .containsOnly(suspect);
        assertThat(channel2.findReadingQualities().ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.REJECTED))
                .ofQualitySystem(QualityCodeSystem.MDM)
                .orOfAnotherTypeInSameSystems().ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED)).collect())
                .containsOnly(suspect);
        assertThat(channel2.findReadingQualities().ofQualitySystems(ImmutableSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC))
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.REJECTED))
                .orOfAnotherTypeInSameSystems().ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED)).collect())
                .containsOnly(added, suspect);
        assertThat(channel2.findReadingQualities().ofQualitySystem(QualityCodeSystem.MDC)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.REJECTED))
                .orOfAnotherTypeInSameSystems().ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED)).collect())
                .containsOnly(added);
        assertThat(channel2.findReadingQualities().ofQualitySystem(QualityCodeSystem.MDM)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.ADDED, QualityCodeIndex.REJECTED))
                .orOfAnotherTypeInSameSystems().ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED)).collect())
                .isEmpty();
    }

    @Test
    public void testCombinations() {
        assertThat(channel1.findReadingQualities()
                .ofQualitySystems(Collections.emptySet())
                .ofQualityIndex(QualityCodeIndex.ADDED)
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(Collections.emptySet())
                .orOfAnotherType()
                .ofQualitySystem(QualityCodeSystem.OTHER)
                .orOfAnotherType()
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.OVERFLOWCONDITIONDETECTED))
                .atTimestamp(FIRST_TIME)
                .actual().collect())
                .containsOnly(batteryLow);
        assertThat(channel1.findReadingQualities().inTimeInterval(Range.all())
                .ofQualityIndex(QualityCodeIndex.CRCERROR)
                .orOfAnotherType()
                .ofQualitySystems(Collections.emptySet())
                .ofQualityIndices(Collections.emptySet())
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategory(QualityCodeCategory.VALIDATION)
                .orOfAnotherType()
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.EXTERNAL, QualityCodeSystem.OTHER))
                .actual().collect())
                .containsOnly(watchdog, batteryLow);
        assertThat(channel2.findReadingQualities().actual()
                .ofQualitySystems(Collections.emptySet())
                .ofQualityIndices(Collections.emptySet())
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(Collections.emptySet())
                .orOfAnotherType()
                .ofQualitySystems(Collections.emptySet())
                .orOfAnotherType()
                .ofQualityIndices(Collections.emptySet())
                .inTimeInterval(Range.closed(START_TIME, FIRST_TIME))
                .collect())
                .containsOnly(added, estimatedGeneric, estimated);
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(Collections.emptySet())
                .ofQualityIndices(Collections.emptySet())
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(Collections.emptySet())
                .orOfAnotherType()
                .ofQualitySystems(Collections.emptySet())
                .orOfAnotherType()
                .ofQualityIndices(Collections.emptySet())
                .inTimeInterval(Range.atLeast(START_TIME))
                .actual().collect())
                .containsOnly(added, estimatedGeneric, estimated, suspect);
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(Collections.emptySet())
                .orOfAnotherType()
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.CLOCKERROR, QualityCodeIndex.CLOCKSETBACKWARD, QualityCodeIndex.CLOCKSETFORWARD))
                .orOfAnotherType()
                .ofQualitySystem(QualityCodeSystem.NOTAPPLICABLE)
                .ofQualityIndices(Collections.emptySet())
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(Collections.emptySet())
                .inTimeInterval(Range.open(START_TIME.minusMillis(1), SECOND_TIME.plusMillis(1)))
                .collect())
                .containsOnly(added, estimatedGeneric, estimated, overflow, suspect);
        assertThat(channel2.findReadingQualities()
                .inTimeInterval(Range.atMost(SECOND_TIME))
                .ofAnyQualityIndexInCategories(Collections.emptySet())
                .ofQualitySystem(QualityCodeSystem.NOTAPPLICABLE)
                .orOfAnotherTypeInSameSystems()
                .ofQualityIndices(Collections.emptySet())
                .orOfAnotherType()
                .ofQualityIndices(Collections.emptySet())
                .collect())
                .containsOnly(added, estimatedGeneric, estimated, overflow, suspect);
    }

    @Test
    public void testOverriddenTimeCriteria() {
        assertThat(channel1.findReadingQualities()
                .atTimestamp(START_TIME)
                .atTimestamp(FIRST_TIME).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities()
                .inTimeInterval(Range.closed(FIRST_TIME, SECOND_TIME))
                .atTimestamp(FIRST_TIME).collect())
                .containsOnly(estimated, estimatedGeneric);
        assertThat(channel2.findReadingQualities()
                .inTimeInterval(Range.closed(START_TIME, SECOND_TIME))
                .inTimeInterval(Range.atLeast(FIRST_TIME))
                .inTimeInterval(Range.atMost(FIRST_TIME)).collect())
                .containsOnly(estimated, estimatedGeneric);
        assertThat(channel2.findReadingQualities()
                .inTimeInterval(Range.lessThan(FIRST_TIME))
                .inTimeInterval(Range.greaterThan(FIRST_TIME)).collect())
                .isEmpty();
    }

    @Test
    public void testOverriddenTypeCriteria() {
        assertThat(channel1.findReadingQualities()
                .ofAnyQualityIndexInCategory(QualityCodeCategory.DIAGNOSTICS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT).collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities()
                .ofQualitySystem(QualityCodeSystem.MDM)
                .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.OTHER, QualityCodeSystem.EXTERNAL)).collect())
                .containsOnly(estimatedGeneric, estimated);
        assertThat(channel2.findReadingQualities()
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.ESTIMATEGENERIC, QualityCodeIndex.OVERFLOWCONDITIONDETECTED))
                .ofQualityIndices(QualityCodeCategory.ESTIMATED, ImmutableSet.of(1000, 1001, 1002)).collect())
                .containsOnly(estimated);
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(Collections.emptySet())
                .ofQualitySystem(QualityCodeSystem.NOTAPPLICABLE)
                .orOfAnotherType()
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.CLOCKERROR, QualityCodeIndex.CLOCKSETBACKWARD, QualityCodeIndex.CLOCKSETFORWARD))
                .orOfAnotherType()
                .ofQualitySystem(QualityCodeSystem.NOTAPPLICABLE)
                .ofQualityIndices(Collections.emptySet())
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(Collections.emptySet())
                .inTimeInterval(Range.open(START_TIME.minusMillis(1), SECOND_TIME.plusMillis(1)))
                .collect())
                .isEmpty();
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(Collections.emptySet())
                .ofQualitySystem(QualityCodeSystem.NOTAPPLICABLE)
                .orOfAnotherType()
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.CLOCKERROR, QualityCodeIndex.CLOCKSETBACKWARD, QualityCodeIndex.CLOCKSETFORWARD))
                .orOfAnotherType()
                .ofQualitySystem(QualityCodeSystem.NOTAPPLICABLE)
                .ofQualityIndices(Collections.emptySet())
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(Collections.emptySet())
                .ofQualitySystem(QualityCodeSystem.MDM)
                .inTimeInterval(Range.open(START_TIME.minusMillis(1), SECOND_TIME.plusMillis(1)))
                .collect())
                .containsOnly(suspect);
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(Collections.emptySet())
                .ofQualitySystem(QualityCodeSystem.NOTAPPLICABLE)
                .orOfAnotherType()
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.CLOCKERROR, QualityCodeIndex.CLOCKSETBACKWARD, QualityCodeIndex.CLOCKSETFORWARD))
                .orOfAnotherType()
                .ofQualitySystem(QualityCodeSystem.NOTAPPLICABLE)
                .ofQualityIndices(Collections.emptySet())
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(Collections.emptySet())
                .ofQualitySystem(QualityCodeSystem.MDM)
                .ofQualityIndex(QualityCodeIndex.ADDED)
                .inTimeInterval(Range.open(START_TIME.minusMillis(1), SECOND_TIME.plusMillis(1)))
                .collect())
                .containsOnly(suspect);
        assertThat(channel2.findReadingQualities()
                .ofQualitySystems(Collections.emptySet())
                .ofQualitySystem(QualityCodeSystem.NOTAPPLICABLE)
                .orOfAnotherType()
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.CLOCKERROR, QualityCodeIndex.CLOCKSETBACKWARD, QualityCodeIndex.CLOCKSETFORWARD))
                .orOfAnotherType()
                .ofQualitySystem(QualityCodeSystem.NOTAPPLICABLE)
                .ofQualityIndices(Collections.emptySet())
                .ofQualityIndex(QualityCodeIndex.REJECTED)
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(Collections.emptySet())
                .ofQualitySystem(QualityCodeSystem.MDM)
                .ofQualityIndex(QualityCodeIndex.ADDED)
                .inTimeInterval(Range.open(START_TIME.minusMillis(1), SECOND_TIME.plusMillis(1)))
                .collect())
                .isEmpty();
    }
}
