package com.elster.jupiter.validation.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests integration of all internal components involved in validation.
 * Only interfaces of outside the bundle and implementations of Validator (since there are no internal implementations by design) have been mocked.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationOnStoreIT {

    private static final String MIN_MAX = "minMax";
    private static final String CONSECUTIVE_ZEROES = "consecutiveZeroes";
    private static final String MY_RULE_SET = "MyRuleSet";
    private static final String MAX_NUMBER_IN_SEQUENCE = "maxNumberInSequence";
    private static final String MIN = "min";
    private static final String MAX = "max";
    public static final ZonedDateTime ZONED_DATE_TIME = ZonedDateTime.of(1983, 5, 31, 14, 0, 0, 0, ZoneId.systemDefault());
    private static final Instant date1 = ZONED_DATE_TIME.toInstant();

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator minMax;
    @Mock
    private Validator conseqZero;
    @Mock
    private PropertySpec min, max, conZero;

    private BigDecimalFactory valueFactory = new BigDecimalFactory();
    private MeterActivation meterActivation;
    private TransactionService transactionService;
    private Meter meter;
    private ReadingType deltaReadingType;
    private ReadingType bulkReadingType;
    private ValidationRule minMaxRule;
    private ValidationRule zeroesRule;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new MeteringModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new TaskModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new KpiModule(),
                    new ValidationModule(),
                    new NlsModule(),
                    new DataVaultModule(),
                    new CustomPropertySetsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(userService.findGroup(anyString())).thenReturn(Optional.empty());
        when(validatorFactory.available()).thenReturn(Arrays.asList(MIN_MAX, CONSECUTIVE_ZEROES));
        when(validatorFactory.createTemplate(eq(MIN_MAX))).thenReturn(minMax);
        when(validatorFactory.createTemplate(eq(CONSECUTIVE_ZEROES))).thenReturn(conseqZero);
        when(validatorFactory.create(eq(CONSECUTIVE_ZEROES), anyMapOf(String.class, Object.class))).thenReturn(conseqZero);
        when(validatorFactory.create(eq(MIN_MAX), anyMapOf(String.class, Object.class))).thenReturn(minMax);
        when(minMax.getReadingQualityCodeIndex()).thenReturn(Optional.empty());
        when(minMax.getPropertySpecs()).thenReturn(Arrays.asList(min, max));
        when(minMax.validate(any(IntervalReadingRecord.class))).thenReturn(ValidationResult.SUSPECT);
        when(min.getName()).thenReturn(MIN);
        when(min.getValueFactory()).thenReturn(valueFactory);
        when(max.getName()).thenReturn(MAX);
        when(max.getValueFactory()).thenReturn(valueFactory);
        when(conseqZero.getReadingQualityCodeIndex()).thenReturn(Optional.empty());
        when(conseqZero.getPropertySpecs()).thenReturn(Collections.singletonList(conZero));
        when(conZero.getName()).thenReturn(MAX_NUMBER_IN_SEQUENCE);
        when(conZero.getValueFactory()).thenReturn(valueFactory);

        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(FiniteStateMachineService.class);
                MeteringService meteringService = injector.getInstance(MeteringService.class);
                deltaReadingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
                bulkReadingType = meteringService.getReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
                AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
                meter = amrSystem.newMeter("2331", "myName").create();
                meterActivation = meter.activate(date1);
                meterActivation.getChannelsContainer().createChannel(bulkReadingType);

                ValidationServiceImpl validationService = (ValidationServiceImpl) injector.getInstance(ValidationService.class);
                validationService.addResource(validatorFactory);

                final ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(MY_RULE_SET, QualityCodeSystem.MDC);
                ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
                zeroesRule = validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, CONSECUTIVE_ZEROES, "consecutivezeros")
                        .withReadingType(deltaReadingType)
                        .withReadingType(bulkReadingType)
                        .havingProperty(MAX_NUMBER_IN_SEQUENCE).withValue(BigDecimal.valueOf(20))
                        .active(true)
                        .create();
                minMaxRule = validationRuleSetVersion.addRule(ValidationAction.FAIL, MIN_MAX, "minmax")
                        .withReadingType(bulkReadingType)
                        .havingProperty(MIN).withValue(BigDecimal.valueOf(1))
                        .havingProperty(MAX).withValue(BigDecimal.valueOf(100))
                        .active(true)
                        .create();

                validationService.addValidationRuleSetResolver(new ValidationRuleSetResolver() {
                    @Override
                    public List<ValidationRuleSet> resolve(ValidationContext validationContext) {
                        return Collections.singletonList(validationRuleSet);
                    }

                    @Override
                    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
                        return false;
                    }
                });

                validationService.activateValidation(meter);
                validationService.enableValidationOnStorage(meter);
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testValidationFailsOnMinMax() {
        transactionService.execute(VoidTransaction.of(() -> {
            IntervalReadingImpl intervalReading = IntervalReadingImpl.of(ZONED_DATE_TIME.plusHours(1).toInstant(), BigDecimal.valueOf(400), Collections.emptySet());
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());
            intervalBlock.addIntervalReading(intervalReading);
            meterReading.addIntervalBlock(intervalBlock);
            meter.store(QualityCodeSystem.MDC, meterReading);
        }));

        List<ReadingQualityType> readingQualityTypes = meter.getReadingQualities(Range.singleton(ZONED_DATE_TIME.plusHours(1).toInstant())).stream()
                .map(ReadingQualityRecord::getType)
                .collect(Collectors.toList());

        assertThat(readingQualityTypes).containsOnly(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT),
                ReadingQualityType.defaultCodeForRuleId(QualityCodeSystem.MDC, minMaxRule.getId()));
    }
}
