/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.validation.ValidationResult.NOT_VALIDATED;
import static com.elster.jupiter.validation.ValidationResult.SUSPECT;
import static com.elster.jupiter.validation.ValidationResult.VALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests integration of all internal components involved in validation.
 * Only interfaces of outside the bundle and implementations of Validator (since there are no internal implementations by design) have been mocked.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationEvaluatorIT {

    private static final String MIN_MAX = DefaultValidatorFactory.THRESHOLD_VALIDATOR;
    private static final String MISSING = DefaultValidatorFactory.MISSING_VALUES_VALIDATOR;
    private static final String MY_RULE_SET = "MyRuleSet";
    private static final String MDM_RULE_SET = "MdmRuleSet";
    private static final String MIN = "minimum";
    private static final String MAX = "maximum";
    private static final Instant DATE_1 = ZonedDateTime.of(1983, 5, 31, 14, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant ACTIVATION_DATE = DATE_1.minusSeconds(900);

    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static String readingType;
    private static String bulkReadingType;

    private static ValidationService validationService;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));


    private Meter meter;

    private static class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(TimeService.class).toInstance(mock(TimeService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(PropertyValueInfoService.class).toInstance(mock(PropertyValueInfoService.class));
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(TokenService.class).toInstance(mock(TokenService.class));
        }
    }

    @BeforeClass
    public static void beforeClass() {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new TaskModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new CalendarModule(),
                    new MeteringModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new H2OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new KpiModule(),
                    new ValidationModule(),
                    new NlsModule(),
                    new EventsModule(),
                    new UserModule(),
                    new BpmModule(),
                    new BasicPropertiesModule(),
                    new DataVaultModule(),
                    new CustomPropertySetsModule(),
                    new AuditServiceModule(),
                    new WebServicesModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(AuditService.class);
            injector.getInstance(FiniteStateMachineService.class);
            readingType = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                    .flow(FlowDirection.FORWARD)
                    .period(TimeAttribute.MINUTE15)
                    .accumulate(Accumulation.DELTADELTA)
                    .code();
            bulkReadingType = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                    .flow(FlowDirection.FORWARD)
                    .period(TimeAttribute.MINUTE15)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .code();
            validationService = injector.getInstance(ValidationService.class);
            validationService.addValidatorFactory(injector.getInstance(DefaultValidatorFactory.class));
            final ValidationRuleSet mdcValidationRuleSet = validationService.createValidationRuleSet(MY_RULE_SET, QualityCodeSystem.MDC);
            final ValidationRuleSet mdmValidationRuleSet = validationService.createValidationRuleSet(MDM_RULE_SET, QualityCodeSystem.MDM);
            ValidationRuleSetVersion mdcValidationRuleSetVersion = mdcValidationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
            ValidationRuleSetVersion mdmValidationRuleSetVersion = mdmValidationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
            mdcValidationRuleSetVersion.addRule(ValidationAction.FAIL, MIN_MAX, "maxValue")
                    .withReadingType(readingType)
                    .havingProperty(MIN).withValue(BigDecimal.valueOf(0))
                    .havingProperty(MAX).withValue(BigDecimal.valueOf(100))
                    .active(true)
                    .create();
            mdcValidationRuleSetVersion.addRule(ValidationAction.FAIL, MISSING, "missing")
                    .withReadingType(readingType)
                    .active(true)
                    .create();
            mdmValidationRuleSetVersion.addRule(ValidationAction.FAIL, MIN_MAX, "minMaxValue")
                    .withReadingType(readingType)
                    .havingProperty(MIN).withValue(BigDecimal.valueOf(50))
                    .havingProperty(MAX).withValue(BigDecimal.valueOf(150))
                    .active(true)
                    .create();
            validationService.addValidationRuleSetResolver(new ValidationRuleSetResolver() {
                @Override
                public Map<ValidationRuleSet, RangeSet<Instant>> resolve(ValidationContext validationContext) {
                    RangeSet<Instant> rangeSet = TreeRangeSet.create();
                    rangeSet.add(Range.atLeast(ACTIVATION_DATE));
                    return Stream.of(mdcValidationRuleSet, mdmValidationRuleSet)
                            .collect(Collectors.toMap(e -> e, e -> rangeSet));
                }

                @Override
                public boolean isValidationRuleSetInUse(ValidationRuleSet ruleSet) {
                    return false;
                }

                @Override
                public boolean isValidationRuleSetActiveOnDeviceConfig(long validationRuleSetId, long deviceConfigId) {
                    return true;
                }

                @Override
                public boolean canHandleRuleSetStatus() {
                    return true;
                }
            });
            return null;
        });
    }

    @Before
    public void setUp() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        meter = amrSystem.newMeter("2331", "myName").create();
        meter.activate(ACTIVATION_DATE);
        //meterActivation.createChannel(readingType1);
        ValidationService validationService = injector.getInstance(ValidationService.class);

        validationService.activateValidation(meter);
        validationService.enableValidationOnStorage(meter);
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testValidationFromDifferentApplications() {
        ValidationService validationService = injector.getInstance(ValidationService.class);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(50), ACTIVATION_DATE));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(102L), DATE_1));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(205L), DATE_1.plusSeconds(900)));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(205L), DATE_1.plusSeconds(900 * 2)));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(311L), DATE_1.plusSeconds(900 * 3)));
        meter.store(QualityCodeSystem.MDC, meterReading);

        ValidationEvaluator evaluator = validationService.getEvaluator();
        Channel channel = meter.getMeterActivations().get(0).getChannelsContainer().getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(DATE_1.plusSeconds(900 * 3));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)));
        assertThat(validationStates).hasSize(4);
        List<ValidationResult> validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, SUSPECT, VALID, SUSPECT));
        assertThat(evaluator.getValidationStatus(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.OTHER),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE))).stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList())).isEqualTo(validationResults);
        validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDM),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)));
        assertThat(validationStates).hasSize(4);
        validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(NOT_VALIDATED, NOT_VALIDATED, NOT_VALIDATED, NOT_VALIDATED));
        validationStates = evaluator.getValidationStatus(Collections.emptySet(),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)));
        assertThat(validationStates).hasSize(4);
        validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, SUSPECT, VALID, SUSPECT));
        assertThat(evaluator.getValidationStatus(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE))).stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList())).isEqualTo(validationResults);

        channel.removeReadings(QualityCodeSystem.MDC, ImmutableList.of(channel.getReadings(Range.greaterThan(ACTIVATION_DATE)).get(2)));
        revalidate(channel, DATE_1);
        validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)));
        assertThat(validationStates).hasSize(4);
        validationStates.sort(Comparator.comparing(DataValidationStatus::getReadingTimestamp));
        validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, SUSPECT, SUSPECT, SUSPECT));
        validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDM),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)));
        assertThat(validationStates).hasSize(3);
        validationStates.sort(Comparator.comparing(DataValidationStatus::getReadingTimestamp));
        validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(NOT_VALIDATED, NOT_VALIDATED, NOT_VALIDATED));
        validationStates = evaluator.getValidationStatus(Collections.emptySet(),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)));
        assertThat(validationStates).hasSize(4);
        validationStates.sort(Comparator.comparing(DataValidationStatus::getReadingTimestamp));
        validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, SUSPECT, SUSPECT, SUSPECT));
    }

    @Test
    @Transactional
    public void testRemoveDeactivateValidation() {
        ValidationService validationService = injector.getInstance(ValidationService.class);

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(50), ACTIVATION_DATE));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(102), DATE_1));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(152), DATE_1.plusSeconds(900)));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(255), DATE_1.plusSeconds(900 * 2)));
        meter.store(QualityCodeSystem.MDC, meterReading);
        validationService.deactivateValidation(meter);
        meter.store(QualityCodeSystem.MDC, MeterReadingImpl.of(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(359), DATE_1.plusSeconds(900 * 3))));
        ValidationEvaluator evaluator = validationService.getEvaluator();
        Channel channel = meter.getMeterActivations().get(0).getChannelsContainer().getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(DATE_1.plusSeconds(900 * 2));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)), Range.greaterThan(ACTIVATION_DATE));
        assertThat(validationStates).hasSize(4);
        List<ValidationResult> validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, VALID, SUSPECT, NOT_VALIDATED));
        channel.removeReadings(QualityCodeSystem.MDC, ImmutableList.of(channel.getReadings(Range.greaterThan(ACTIVATION_DATE)).get(1)));
        validationService.moveLastCheckedBefore(channel, DATE_1.plusSeconds(900));
        validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)), Range.greaterThan(ACTIVATION_DATE));
        assertThat(validationStates).hasSize(4);
        validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, NOT_VALIDATED, NOT_VALIDATED, NOT_VALIDATED));
    }

    @Test
    @Transactional
    public void testRemoveValidation() {
        ValidationService validationService = injector.getInstance(ValidationService.class);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(50), ACTIVATION_DATE));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(102L), DATE_1));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(152L), DATE_1.plusSeconds(900)));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(255L), DATE_1.plusSeconds(900 * 2)));
        meter.store(QualityCodeSystem.MDC, meterReading);
        ValidationEvaluator evaluator = validationService.getEvaluator();
        Channel channel = meter.getMeterActivations().get(0).getChannelsContainer().getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(DATE_1.plusSeconds(900 * 2));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)), Range.greaterThan(ACTIVATION_DATE));
        assertThat(validationStates).hasSize(3);
        List<ValidationResult> validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, VALID, SUSPECT));

        channel.removeReadings(QualityCodeSystem.MDC, ImmutableList.of(channel.getReadings(Range.greaterThan(ACTIVATION_DATE)).get(1)));
        revalidate(channel, DATE_1);
        validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)), Range.greaterThan(ACTIVATION_DATE));
        validationStates.sort(Comparator.comparing(DataValidationStatus::getReadingTimestamp));
        assertThat(validationStates).hasSize(3);
        validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, SUSPECT, SUSPECT));
        Set<QualityCodeIndex> qualityCodes = validationStates.get(1).getReadingQualities().stream()
                .map(q -> q.getType().qualityIndex().orElse(null))
                .collect(Collectors.toSet());
        assertThat(qualityCodes).contains(QualityCodeIndex.SUSPECT);
    }

    @Test
    @Transactional
    public void testEditDeactivateValidation() {
        ValidationService validationService = injector.getInstance(ValidationService.class);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(50), ACTIVATION_DATE));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(102L), DATE_1));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(152L), DATE_1.plusSeconds(900)));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(255L), DATE_1.plusSeconds(900 * 2)));
        meter.store(QualityCodeSystem.MDC, meterReading);
        validationService.deactivateValidation(meter);
        meter.store(QualityCodeSystem.MDC, MeterReadingImpl.of(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(359L), DATE_1.plusSeconds(900 * 3))));

        ValidationEvaluator evaluator = validationService.getEvaluator();
        Channel channel = meter.getMeterActivations().get(0).getChannelsContainer().getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(DATE_1.plusSeconds(900 * 2));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)), Range.greaterThan(ACTIVATION_DATE));
        assertThat(validationStates).hasSize(4);
        List<ValidationResult> validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, VALID, SUSPECT, NOT_VALIDATED));

        channel.editReadings(QualityCodeSystem.MDM, ImmutableList.of(ReadingImpl.of(readingType, BigDecimal.valueOf(70L), DATE_1.plusSeconds(900))));
        validationService.moveLastCheckedBefore(channel, DATE_1.plusSeconds(900));
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(DATE_1);
        validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)), Range.greaterThan(ACTIVATION_DATE));
        assertThat(validationStates).hasSize(4);
        validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, NOT_VALIDATED, NOT_VALIDATED, NOT_VALIDATED));
    }

    @Test
    @Transactional
    public void testEditValidation() {
        ValidationService validationService = injector.getInstance(ValidationService.class);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(50), ACTIVATION_DATE));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(102L), DATE_1));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(152L), DATE_1.plusSeconds(900)));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(255L), DATE_1.plusSeconds(900 * 2)));
        meter.store(QualityCodeSystem.MDC, meterReading);

        ValidationEvaluator evaluator = validationService.getEvaluator(meter);
        Channel channel = meter.getMeterActivations().get(0).getChannelsContainer().getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(DATE_1.plusSeconds(900 * 2));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)), Range.greaterThan(ACTIVATION_DATE));
        assertThat(validationStates).hasSize(3);
        List<ValidationResult> validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, VALID, SUSPECT));

        channel.editReadings(QualityCodeSystem.MDM, ImmutableList.of(ReadingImpl.of(readingType, BigDecimal.valueOf(70L), DATE_1.plusSeconds(900))));
        revalidate(channel, DATE_1);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(DATE_1.plusSeconds(900 * 2));
        validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)), Range.greaterThan(ACTIVATION_DATE));
        assertThat(validationStates).hasSize(3);
        validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, VALID, SUSPECT));
    }

    @Test
    @Transactional
    public void testDataOverruleValidation() {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(50), ACTIVATION_DATE));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(102L), DATE_1));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(152L), DATE_1.plusSeconds(900)));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(255L), DATE_1.plusSeconds(900 * 2)));
        meter.store(QualityCodeSystem.MDC, meterReading);

        ValidationEvaluator evaluator = validationService.getEvaluator();
        Channel channel = meter.getMeterActivations().get(0).getChannelsContainer().getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(DATE_1.plusSeconds(900 * 2));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)), Range.greaterThan(ACTIVATION_DATE));
        assertThat(validationStates).hasSize(3);
        List<ValidationResult> validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, VALID, SUSPECT));

        meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(252L), DATE_1.plusSeconds(900)));
        meterReading.addReading(ReadingImpl.of(bulkReadingType, BigDecimal.valueOf(302L), DATE_1.plusSeconds(900 * 2)));
        meter.store(QualityCodeSystem.MDC, meterReading);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(DATE_1.plusSeconds(900 * 2));
        validationStates = evaluator.getValidationStatus(Collections.singleton(QualityCodeSystem.MDC),
                channel, channel.getReadings(Range.greaterThan(ACTIVATION_DATE)), Range.greaterThan(ACTIVATION_DATE));
        assertThat(validationStates).hasSize(3);
        validationResults = validationStates.stream()
                .map(DataValidationStatus::getValidationResult)
                .collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, SUSPECT, VALID));
    }

    private void revalidate(Channel channel, Instant from) {
        validationService.validate(new ValidationContextImpl(ImmutableSet.of(QualityCodeSystem.MDC), channel.getChannelsContainer()), from);
    }
}
