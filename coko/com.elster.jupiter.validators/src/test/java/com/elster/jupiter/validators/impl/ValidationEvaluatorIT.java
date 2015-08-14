package com.elster.jupiter.validators.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.*;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.*;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.validation.ValidationResult.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests integration of all internal components involved in validation.
 * Only interfaces of outside the bundle and implementations of Validator (since there are no internal implementations by design) have been mocked.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationEvaluatorIT {

    private static final String MIN_MAX = DefaultValidatorFactory.THRESHOLD_VALIDATOR;
    private static final String MISSING = DefaultValidatorFactory.MISSING_VALUES_VALIDATOR;
    private static final String MY_RULE_SET = "MyRuleSet";
    private static final String MIN = "minimum";
    private static final String MAX = "maximum";
    private static final Instant date1 = ZonedDateTime.of(1983, 5, 31, 14, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;

    private MeterActivation meterActivation;
    private Meter meter;
    private String readingType;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
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
                    new MeteringGroupsModule(),
                    new TaskModule(),
                    new MeteringModule(false, "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new ValidationModule(),
                    new NlsModule(),
                    new EventsModule(),
                    new UserModule(),
                    new BasicPropertiesModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
        	MeteringService meteringService = injector.getInstance(MeteringService.class);
            readingType = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
            	.measure(MeasurementKind.ENERGY)
            	.in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
            	.flow(FlowDirection.FORWARD)
            	.period(TimeAttribute.MINUTE15)
            	.accumulate(Accumulation.DELTADELTA)
            	.code();
            ReadingType readingType1 = meteringService.getReadingType(readingType).get();
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("2331");
            meter.save();
            meterActivation = meter.activate(date1);
            //meterActivation.createChannel(readingType1);
            ValidationService validationService = injector.getInstance(ValidationService.class);
            validationService.addValidatorFactory(injector.getInstance(DefaultValidatorFactory.class));
            final ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(MY_RULE_SET);
            ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
            ValidationRule minMaxRule = validationRuleSetVersion.addRule(ValidationAction.FAIL, MIN_MAX, "minmax");
            minMaxRule.addReadingType(readingType1);
            minMaxRule.addProperty(MIN, BigDecimal.valueOf(1));
            minMaxRule.addProperty(MAX, BigDecimal.valueOf(100));
            minMaxRule.activate();
            ValidationRule missingRule = validationRuleSetVersion.addRule(ValidationAction.FAIL, MISSING, "missing");
            missingRule.addReadingType(readingType1);
            missingRule.activate();
            validationRuleSet.save();
            validationService.addValidationRuleSetResolver(new ValidationRuleSetResolver() {
                @Override
                public List<ValidationRuleSet> resolve(MeterActivation meterActivation) {
                    return Arrays.asList(validationRuleSet);
                }

                @Override
                public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
                    return false;
                }
            });
            validationService.activateValidation(meter);
			validationService.enableValidationOnStorage(meter);
            return null;
        });
    }


    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testRemoveDeactivateValidation() {
    	ValidationService validationService = injector.getInstance(ValidationService.class);
        injector.getInstance(TransactionService.class).execute(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(50L), date1.plusSeconds(900 * 1)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(102L), date1.plusSeconds(900 * 2)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(103L), date1.plusSeconds(900 * 3)));
        	meter.store(meterReading);
        	validationService.deactivateValidation(meter);
        	meter.store(MeterReadingImpl.of(ReadingImpl.of(readingType,BigDecimal.valueOf(104L), date1.plusSeconds(900 * 4))));
        	return null;
        });
        ValidationEvaluator evaluator = validationService.getEvaluator();
        Channel channel = meter.getMeterActivations().get(0).getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(date1.plusSeconds(900*3));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(channel, channel.getReadings(Range.all()), Range.all());
        assertThat(validationStates).hasSize(4);
        List<ValidationResult> validationResults =  validationStates.stream()
        		.map(DataValidationStatus::getValidationResult)
        		.collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID,SUSPECT, SUSPECT, NOT_VALIDATED));
        injector.getInstance(TransactionService.class).execute(() -> {
            channel.removeReadings(ImmutableList.of(channel.getReadings(Range.all()).get(1)));
            return null;
        });
        validationStates = evaluator.getValidationStatus(channel, channel.getReadings(Range.all()), Range.all());
        assertThat(validationStates).hasSize(4);
        validationResults =  validationStates.stream()
        		.map(DataValidationStatus::getValidationResult)
        		.collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID,NOT_VALIDATED, NOT_VALIDATED, NOT_VALIDATED));
    }

    @Test
    public void testRemoveValidation() {
    	ValidationService validationService = injector.getInstance(ValidationService.class);
        injector.getInstance(TransactionService.class).execute(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(50L), date1.plusSeconds(900 * 1)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(102L), date1.plusSeconds(900 * 2)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(103L), date1.plusSeconds(900 * 3)));
        	meter.store(meterReading);
        	return null;
        });
        ValidationEvaluator evaluator = validationService.getEvaluator();
        Channel channel = meter.getMeterActivations().get(0).getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(date1.plusSeconds(900*3));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(channel, channel.getReadings(Range.all()), Range.all());
        assertThat(validationStates).hasSize(3);
        List<ValidationResult> validationResults =  validationStates.stream()
        		.map(DataValidationStatus::getValidationResult)
        		.collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, SUSPECT, SUSPECT));
        injector.getInstance(TransactionService.class).execute(() -> {
            channel.removeReadings(ImmutableList.of(channel.getReadings(Range.all()).get(1)));
            return null;
        });
        validationStates = evaluator.getValidationStatus(channel, channel.getReadings(Range.all()), Range.all());
        validationStates.sort(Comparator.comparing(DataValidationStatus::getReadingTimestamp));
        assertThat(validationStates).hasSize(3);
        validationResults =  validationStates.stream()
        		.map(DataValidationStatus::getValidationResult)
        		.collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, VALID, SUSPECT));
        Set<QualityCodeIndex> qualityCodes = validationStates.get(1).getReadingQualities().stream()
        		.map(q -> q.getType().qualityIndex().orElse(null))
        		.collect(Collectors.toSet());
        assertThat(qualityCodes).contains(QualityCodeIndex.REJECTED);
    }

    @Test
    public void testEditDeactivateValidation() {
    	ValidationService validationService = injector.getInstance(ValidationService.class);
        injector.getInstance(TransactionService.class).execute(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(50L), date1.plusSeconds(900 * 1)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(102L), date1.plusSeconds(900 * 2)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(103L), date1.plusSeconds(900 * 3)));
        	meter.store(meterReading);
        	validationService.deactivateValidation(meter);
        	meter.store(MeterReadingImpl.of(ReadingImpl.of(readingType,BigDecimal.valueOf(104L), date1.plusSeconds(900 * 4))));
        	return null;
        });
        ValidationEvaluator evaluator = validationService.getEvaluator();
        Channel channel = meter.getMeterActivations().get(0).getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(date1.plusSeconds(900*3));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(channel, channel.getReadings(Range.all()), Range.all());
        assertThat(validationStates).hasSize(4);
        List<ValidationResult> validationResults =  validationStates.stream()
        		.map(DataValidationStatus::getValidationResult)
        		.collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID,SUSPECT, SUSPECT, NOT_VALIDATED));
        injector.getInstance(TransactionService.class).execute(() -> {
            channel.editReadings(ImmutableList.of(ReadingImpl.of(readingType,BigDecimal.valueOf(70L),date1.plusSeconds(900*2))));
            return null;
        });
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(date1.plusSeconds(900*1));
        validationStates = evaluator.getValidationStatus(channel, channel.getReadings(Range.all()), Range.all());
        assertThat(validationStates).hasSize(4);
        validationResults =  validationStates.stream()
        		.map(DataValidationStatus::getValidationResult)
        		.collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID,NOT_VALIDATED, NOT_VALIDATED, NOT_VALIDATED));
    }

    @Test
    public void testEditValidation() {
    	ValidationService validationService = injector.getInstance(ValidationService.class);
        injector.getInstance(TransactionService.class).execute(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(50L), date1.plusSeconds(900 * 1)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(102L), date1.plusSeconds(900 * 2)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(103L), date1.plusSeconds(900 * 3)));
        	meter.store(meterReading);
        	return null;
        });
        ValidationEvaluator evaluator = validationService.getEvaluator(meter, Range.openClosed(date1, date1.plusSeconds(900*3)));
        Channel channel = meter.getMeterActivations().get(0).getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(date1.plusSeconds(900*3));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(channel, channel.getReadings(Range.all()), Range.all());
        assertThat(validationStates).hasSize(3);
        List<ValidationResult> validationResults =  validationStates.stream()
        		.map(DataValidationStatus::getValidationResult)
        		.collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID,SUSPECT, SUSPECT));
        injector.getInstance(TransactionService.class).execute(() -> {
            channel.editReadings(ImmutableList.of(ReadingImpl.of(readingType,BigDecimal.valueOf(70L),date1.plusSeconds(900*2))));
            return null;
        });
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(date1.plusSeconds(900*3));
        validationStates = evaluator.getValidationStatus(channel, channel.getReadings(Range.all()), Range.all());
        assertThat(validationStates).hasSize(3);
        validationResults =  validationStates.stream()
        		.map(DataValidationStatus::getValidationResult)
        		.collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID, VALID, SUSPECT));
    }

    @Test
    public void testDataOverruleValidation() {
    	ValidationService validationService = injector.getInstance(ValidationService.class);
        injector.getInstance(TransactionService.class).execute(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(50L), date1.plusSeconds(900 * 1)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(102L), date1.plusSeconds(900 * 2)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(103L), date1.plusSeconds(900 * 3)));
        	meter.store(meterReading);
        	return null;
        });
        ValidationEvaluator evaluator = validationService.getEvaluator();
        Channel channel = meter.getMeterActivations().get(0).getChannels().get(0);
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(date1.plusSeconds(900*3));
        List<DataValidationStatus> validationStates = evaluator.getValidationStatus(channel, channel.getReadings(Range.all()), Range.all());
        assertThat(validationStates).hasSize(3);
        List<ValidationResult> validationResults =  validationStates.stream()
        		.map(DataValidationStatus::getValidationResult)
        		.collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID,SUSPECT, SUSPECT));
        injector.getInstance(TransactionService.class).execute(() -> {
        	MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
         	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(150L), date1.plusSeconds(900 * 1)));
         	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(50L), date1.plusSeconds(900 * 2)));
         	meter.store(meterReading);
         	return null;
        });
        assertThat(validationService.getLastChecked(channel).get()).isEqualTo(date1.plusSeconds(900*3));
        validationStates = evaluator.getValidationStatus(channel, channel.getReadings(Range.all()), Range.all());
        assertThat(validationStates).hasSize(3);
        validationResults =  validationStates.stream()
        		.map(DataValidationStatus::getValidationResult)
        		.collect(Collectors.toList());
        assertThat(validationResults).isEqualTo(ImmutableList.of(SUSPECT, VALID, SUSPECT));
    }
}
