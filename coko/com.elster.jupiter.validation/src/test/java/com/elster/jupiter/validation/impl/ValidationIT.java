package com.elster.jupiter.validation.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests integration of all internal components involved in validation.
 * Only interfaces of outside the bundle and implementations of Validator (since there are no internal implementations by design) have been mocked.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationIT {

    private static final String MIN_MAX = "minMax";
    private static final String CONSECUTIVE_ZEROES = "consecutiveZeroes";
    private static final String MY_RULE_SET = "MyRuleSet";
    private static final String MAX_NUMBER_IN_SEQUENCE = "maxNumberInSequence";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final long METERACTIVATION_ID = 101L;
    private static final long CHANNEL1_ID = 1001L;
    private static final long CHANNEL2_ID = 1002L;
    private static final Date date1 = new DateTime(1983, 5, 31, 14, 0, 0).toDate();
    private static final Date date2 = new DateTime(1983, 5, 31, 15, 0, 0).toDate();
    private static final Date date3 = new DateTime(1983, 5, 31, 16, 0, 0).toDate();
    private static final Date date4 = new DateTime(1983, 5, 31, 17, 0, 0).toDate();
    private static final Date date5 = new DateTime(1983, 5, 31, 18, 0, 0).toDate();
    private static final Date date6 = new DateTime(1983, 5, 31, 19, 0, 0).toDate();


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
    private Validator validator;
    private MeterActivation meterActivation;


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
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
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new ValidationModule(),
                new NlsModule()
        );
        when(validatorFactory.available()).thenReturn(Arrays.asList(MIN_MAX, CONSECUTIVE_ZEROES));
        when(validatorFactory.createTemplate(eq(MIN_MAX))).thenReturn(validator);
        when(validatorFactory.createTemplate(eq(CONSECUTIVE_ZEROES))).thenReturn(validator);
        when(validatorFactory.create(eq(CONSECUTIVE_ZEROES), any(Map.class))).thenReturn(validator);
        when(validatorFactory.create(eq(MIN_MAX), any(Map.class))).thenReturn(validator);
        when(validator.getReadingQualityTypeCode()).thenReturn(Optional.<ReadingQualityType>absent());

        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                MeteringService meteringService = injector.getInstance(MeteringService.class);
                ReadingType readingType1 = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
                ReadingType readingType2 = meteringService.getReadingType("0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
                ReadingType readingType3 = meteringService.getReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
                AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
                Meter meter = amrSystem.newMeter("2331");
                meterActivation = meter.activate(date1);
                meterActivation.createChannel(readingType1, readingType2);
                meterActivation.createChannel(readingType1, readingType3);

                ValidationServiceImpl validationService = (ValidationServiceImpl) injector.getInstance(ValidationService.class);
                validationService.addResource(validatorFactory);

                final ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(MY_RULE_SET);
                ValidationRule zeroesRule = validationRuleSet.addRule(ValidationAction.FAIL, CONSECUTIVE_ZEROES, "consecutivezeros");
                zeroesRule.addReadingType(readingType1);
                zeroesRule.addReadingType(readingType2);
                zeroesRule.addProperty(MAX_NUMBER_IN_SEQUENCE, Unit.UNITLESS.amount(BigDecimal.valueOf(20)));
                zeroesRule.activate();
                ValidationRule minMaxRule = validationRuleSet.addRule(ValidationAction.WARN_ONLY, MIN_MAX, "minmax");
                minMaxRule.addReadingType(readingType3);
                minMaxRule.addReadingType(readingType2);
                minMaxRule.addProperty(MIN, Unit.WATT_HOUR.amount(BigDecimal.valueOf(1), 3));
                minMaxRule.addProperty(MAX, Unit.WATT_HOUR.amount(BigDecimal.valueOf(100), 3));
                minMaxRule.activate();
                validationRuleSet.save();

                validationService.addValidationRuleSetResolver(new ValidationRuleSetResolver() {
                    @Override
                    public List<ValidationRuleSet> resolve(MeterActivation meterActivation, Interval interval) {
                        return Arrays.asList(validationRuleSet);
                    }
                });
                return null;
            }
        });
    }


    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testValidation() {
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                ValidationService service = injector.getInstance(ValidationService.class);
                service.validate(meterActivation, interval(date1, date5));

                DataModel valDataModel = injector.getInstance(OrmService.class).getDataModel(ValidationService.COMPONENTNAME).get();
                List<IMeterActivationValidation> meterActivationValidations = valDataModel.mapper(IMeterActivationValidation.class).find("meterActivation", meterActivation);
                assertThat(meterActivationValidations).hasSize(1);
                assertThat(meterActivationValidations.get(0).getRuleSet().getName()).isEqualTo(MY_RULE_SET);
                assertThat(meterActivationValidations.get(0).isObsolete()).isFalse();
                assertThat(meterActivationValidations.get(0).getChannelValidations()).hasSize(2);

            }
        });
    }

    private Interval interval(Date from, Date to) {
        return new Interval(from, to);
    }


}
