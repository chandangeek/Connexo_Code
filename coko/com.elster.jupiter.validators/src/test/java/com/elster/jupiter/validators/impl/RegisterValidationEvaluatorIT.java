package com.elster.jupiter.validators.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
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
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.validation.ValidationResult.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests integration of all internal components involved in validation.
 * Only interfaces of outside the bundle and implementations of Validator (since there are no internal implementations by design) have been mocked.
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterValidationEvaluatorIT {

    private static final String REGISTERINCREASE = DefaultValidatorFactory.REGISTER_INCREASE_VALIDATOR;
    private static final String FAILEQUALDATA = "failEqualData";
    private static final String MY_RULE_SET = "MyRuleSet";
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
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new FiniteStateMachineModule(),
                new MeteringModule("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                new MeteringGroupsModule(),
                new TaskModule(),
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
        injector.getInstance(TransactionService.class).execute(() -> {
        	MeteringService meteringService = injector.getInstance(MeteringService.class);
            readingType = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
            	.measure(MeasurementKind.ENERGY)
            	.in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
            	.flow(FlowDirection.FORWARD)
                .accumulate(Accumulation.BULKQUANTITY)
            	.code();
            ReadingType readingType1 = meteringService.getReadingType(readingType).get();
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("2331");
            meter.save();
            meterActivation = meter.activate(date1);
            ValidationService validationService = injector.getInstance(ValidationService.class);
            validationService.addValidatorFactory(injector.getInstance(DefaultValidatorFactory.class));
            final ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(MY_RULE_SET);
            ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("Version 1", Instant.EPOCH);
            ValidationRule registerIncreaseRule = validationRuleSetVersion.addRule(ValidationAction.FAIL, REGISTERINCREASE, "minmax");
            registerIncreaseRule.addReadingType(readingType1);
            registerIncreaseRule.addProperty(FAILEQUALDATA, false);
            registerIncreaseRule.activate();
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
    public void testEditLastValue() {
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
        assertThat(validationResults).isEqualTo(ImmutableList.of(VALID,VALID,VALID));
        injector.getInstance(TransactionService.class).execute(() -> {
            channel.editReadings(ImmutableList.of(ReadingImpl.of(readingType,BigDecimal.valueOf(10L),date1.plusSeconds(900*3))));
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

}
