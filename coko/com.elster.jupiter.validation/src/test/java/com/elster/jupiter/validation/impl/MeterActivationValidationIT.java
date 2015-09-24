package com.elster.jupiter.validation.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationValidationIT {

    private Injector injector;

    @Rule
    public TestRule timeZone = Using.timZone("GMT");

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private IValidationRuleSet validationRuleSet;
    private ValidationServiceImpl validationService;
    private TransactionService transactionService;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private ValidationRuleSetResolver ruleSetResolver;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() throws SQLException {
        when(ruleSetResolver.resolve(any())).thenAnswer(invocation -> Arrays.asList(validationRuleSet));
        when(validatorFactory.available()).thenReturn(Collections.singletonList("autoPass"));
        when(validatorFactory.create("autoPass", Collections.emptyMap())).thenReturn(validator);
        when(validatorFactory.createTemplate("autoPass")).thenReturn(validator);
        when(validator.validate(any(IntervalReadingRecord.class))).thenReturn(ValidationResult.VALID);
        when(validator.getPropertySpecs()).thenReturn(Collections.emptyList());

        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new ValidationModule(),
                new FiniteStateMachineModule(),
                new MeteringGroupsModule(),
                new TaskModule()
        );
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(VoidTransaction.of(() -> validationService = (ValidationServiceImpl) injector.getInstance(ValidationService.class)));
        ValidationEventHandler validationEventHandler = new ValidationEventHandler();
        validationEventHandler.setValidationService(validationService);
        Publisher publisher = injector.getInstance(Publisher.class);
        publisher.addSubscriber(validationEventHandler);

    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersistence() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem system = meteringService.findAmrSystem(1).get();
            Meter meter = system.newMeter("1").create();
            MeterActivation meterActivation = meter.activate(ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault()).toInstant());
            ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            Channel channel = meterActivation.createChannel(readingType);
            MeterActivation loaded = meteringService.findMeterActivation(meterActivation.getId()).get();
            assertThat(loaded.getChannels()).hasSize(1).contains(channel);
        }
    }

    @Test
    public void testAdvanceWithReadings() {
        validationService.addValidationRuleSetResolver(ruleSetResolver);
        validationService.addValidatorFactory(validatorFactory);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        createRuleSet(readingType);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem system = meteringService.findAmrSystem(1).get();
            Meter meter = system.newMeter("1").create();
            ZonedDateTime startTime = ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault());
            ZonedDateTime originalCutOff = ZonedDateTime.of(2012, 12, 25, 0, 0, 0, 0, ZoneId.systemDefault());
            ZonedDateTime newCutOff = ZonedDateTime.of(2012, 12, 20, 0, 0, 0, 0, ZoneId.systemDefault());
            MeterActivation meterActivation = meter.activate(startTime.toInstant());
            meterActivation.endAt(originalCutOff.toInstant());
            Channel channel = meterActivation.createChannel(readingType);
            MeterActivation currentActivation = meter.activate(originalCutOff.toInstant());
            Channel currentChannel = currentActivation.createChannel(readingType);
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.minusMinutes(15).toInstant(), BigDecimal.valueOf(4025, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.toInstant(), BigDecimal.valueOf(4175, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4225, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.toInstant(), BigDecimal.valueOf(4725, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4825, 2)));
            meterReading.addIntervalBlock(intervalBlock);
            meter.store(meterReading);
            validationService.activateValidation(meter);
            validationService.validate(meterActivation);
            validationService.validate(currentActivation);

            currentActivation.advanceStartDate(newCutOff.toInstant());

            assertThat(meter.getMeterActivations()).hasSize(2);
            MeterActivation first = meter.getMeterActivations().get(0);
            MeterActivation second = meter.getMeterActivations().get(1);
            assertThat(first.getRange()).isEqualTo(Range.closedOpen(startTime.toInstant(), newCutOff.toInstant()));
            assertThat(second.getRange()).isEqualTo(Range.atLeast(newCutOff.toInstant()));

            Instant lastCheckedOfPrior = validationService.getIMeterActivationValidations(meterActivation).get(0)
                    .getChannelValidation(meterActivation.getChannels().get(0)).get()
                    .getLastChecked();
            assertThat(lastCheckedOfPrior).isEqualTo(newCutOff.toInstant());
            Instant lastCheckedOfCurrent = validationService.getIMeterActivationValidations(currentActivation).get(0)
                    .getChannelValidation(currentActivation.getChannels().get(0)).get()
                    .getLastChecked();
            assertThat(lastCheckedOfCurrent).isEqualTo(newCutOff.toInstant());
        }
    }

    private void createRuleSet(ReadingType readingType) {
        try (TransactionContext context = transactionService.getContext()) {
            validationRuleSet = (IValidationRuleSet) validationService.createValidationRuleSet("forTest");
            ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("First, Last and Always", Instant.EPOCH);
            ValidationRule validationRule = validationRuleSetVersion.addRule(ValidationAction.FAIL, "autoPass", "autoPass");
            validationRule.addReadingType(readingType);
            validationRule.activate();
            validationRuleSet.save();
            context.commit();
        }
    }


}
