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
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.*;
import com.elster.jupiter.validation.impl.ValidationModule;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests integration of all internal components involved in validation.
 * Only interfaces of outside the bundle and implementations of Validator (since there are no internal implementations by design) have been mocked.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationPerformanceIT {

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
                    new MeteringModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
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
        	ValidationService validationService = injector.getInstance(ValidationService.class);
            validationService.addValidatorFactory(injector.getInstance(DefaultValidatorFactory.class));
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
    public void testPass() {
    	TransactionService txService = injector.getInstance(TransactionService.class);
    	MeteringService meteringService = injector.getInstance(MeteringService.class);
    	int i = 1;
    	try (TransactionContext context = txService.getContext()) {
    		MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(50L), date1.plusSeconds(900 * i++)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(51L), date1.plusSeconds(900 * i++)));
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(52L), date1.plusSeconds(900 * i++)));
        	meter.store(meterReading);
        	context.commit();
    	}
        // reread meter
        meter = meteringService.findMeter(meter.getId()).get();
        int sqlCount = 0;
        try (TransactionContext context = txService.getContext()) {
        	MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(50L), date1.plusSeconds(900 * i++)));
        	meter.store(meterReading);
        	context.commit();
        	sqlCount = context.getStats().getSqlCount();
        }
        meter = meteringService.findMeter(meter.getId()).get();
        try (TransactionContext context = txService.getContext()) {
        	MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        	for (int j = 0 ; j < 10 ; j++) {
        		meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(50L), date1.plusSeconds(900 * i++)));
        	}
        	meter.store(meterReading);
        	context.commit();
        	assertThat(context.getStats().getSqlCount()).isEqualTo(sqlCount);
        }
    }
 }
