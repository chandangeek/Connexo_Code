package com.elster.jupiter.validation.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.*;
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
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSetImplIT {

    public static final String CONSEC_ZEROS_VALIDATOR_CLASS = "com.elster.jupiter.validators.ConsecutiveZerosValidator";
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
    private Validator minMax, consecZero;
    @Mock
    private PropertySpec min, max, consZero;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private String MAX_NUMBER_IN_SEQUENCE = "maxNumberInSequence";
    private String MIN_MAX = "minMax";
    private String MIN = "min";
    private String MAX = "max";
    private ReadingType readingType;
    private BigDecimalFactory valueFactory = new BigDecimalFactory();


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
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new MeteringModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
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
                    new NlsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(validatorFactory.available()).thenReturn(Arrays.asList(MIN_MAX, CONSEC_ZEROS_VALIDATOR_CLASS));
        when(validatorFactory.createTemplate(eq(MIN_MAX))).thenReturn(minMax);
        when(validatorFactory.createTemplate(eq(CONSEC_ZEROS_VALIDATOR_CLASS))).thenReturn(consecZero);
        when(minMax.getPropertySpecs()).thenReturn(Arrays.asList(min, max));
        when(min.getName()).thenReturn(MIN);
        when(min.getValueFactory()).thenReturn(valueFactory);
        when(max.getName()).thenReturn(MAX);
        when(max.getValueFactory()).thenReturn(valueFactory);
        when(consecZero.getPropertySpecs()).thenReturn(Arrays.asList(consZero));
        when(consZero.getName()).thenReturn(MAX_NUMBER_IN_SEQUENCE);
        when(consZero.getValueFactory()).thenReturn(valueFactory);
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                ValidationServiceImpl instance = (ValidationServiceImpl) injector.getInstance(ValidationService.class);
                instance.addResource(validatorFactory);
                return null;
            }
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersist() {
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                readingType = injector.getInstance(MeteringService.class).getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
                ValidationRuleSet validationRuleSet = injector.getInstance(ValidationService.class).createValidationRuleSet("myRuleSet");
                ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
                ValidationRule zeroesRule = validationRuleSetVersion.addRule(ValidationAction.FAIL, CONSEC_ZEROS_VALIDATOR_CLASS, "consecutiveZeroes");
                zeroesRule.addReadingType(readingType);
                zeroesRule.addProperty(MAX_NUMBER_IN_SEQUENCE, BigDecimal.valueOf(20));
                zeroesRule.activate();
                ValidationRule minMaxRule = validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, MIN_MAX, "minmax");
                minMaxRule.addReadingType(readingType);
                minMaxRule.addProperty(MIN, BigDecimal.valueOf(1));
                minMaxRule.addProperty(MAX, BigDecimal.valueOf(100));
                minMaxRule.activate();
                validationRuleSet.save();

                Optional<? extends ValidationRuleSet> found = injector.getInstance(ValidationService.class).getValidationRuleSet(validationRuleSet.getId());
                assertThat(found.isPresent()).isTrue();
                assertThat(found.get().getRuleSetVersions().get(0).getRules()).hasSize(2);
            }
        });
    }

    @Test
    public void testAddSecondRuleSeeIfReadingTypesArentLost() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        ValidationRuleSet validationRuleSet;
        try (TransactionContext context = transactionService.getContext()) {
            readingType = injector.getInstance(MeteringService.class).getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            validationRuleSet = injector.getInstance(ValidationService.class).createValidationRuleSet("myRuleSet");
            ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
            ValidationRule zeroesRule = validationRuleSetVersion.addRule(ValidationAction.FAIL, CONSEC_ZEROS_VALIDATOR_CLASS, "consecutiveZeroes");
            zeroesRule.addReadingType(readingType);
            zeroesRule.addProperty(MAX_NUMBER_IN_SEQUENCE, BigDecimal.valueOf(20));
            zeroesRule.activate();
            validationRuleSet.save();
            context.commit();
        }
        try (TransactionContext context = transactionService.getContext()) {
            readingType = injector.getInstance(MeteringService.class).getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            validationRuleSet = injector.getInstance(ValidationService.class).getValidationRuleSet(validationRuleSet.getId()).get();
            ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.getRuleSetVersions().get(0);
            ValidationRule minMaxRule = validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, MIN_MAX, "minmax");
            minMaxRule.addReadingType(readingType);
            minMaxRule.addProperty(MIN, BigDecimal.valueOf(1));
            minMaxRule.addProperty(MAX, BigDecimal.valueOf(100));
            minMaxRule.activate();
            validationRuleSet.save();
            context.commit();
        }
        validationRuleSet = injector.getInstance(ValidationService.class).getValidationRuleSet(validationRuleSet.getId()).get();
        assertThat(validationRuleSet.getRuleSetVersions().get(0).getRules()).hasSize(2);
        ValidationRule validationRule = validationRuleSet.getRuleSetVersions().get(0).getRules().get(0);
        assertThat(validationRule.getReadingTypes()).hasSize(1);
    }

    @Test
    public void testAddSecondRuleSeeIfPropertiesArentLost() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        ValidationRuleSet validationRuleSet;
        try (TransactionContext context = transactionService.getContext()) {
            readingType = injector.getInstance(MeteringService.class).getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            validationRuleSet = injector.getInstance(ValidationService.class).createValidationRuleSet("myRuleSet");
            ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
            ValidationRule zeroesRule = validationRuleSetVersion.addRule(ValidationAction.FAIL, CONSEC_ZEROS_VALIDATOR_CLASS, "consecutiveZeroes");
            zeroesRule.addReadingType(readingType);
            zeroesRule.addProperty(MAX_NUMBER_IN_SEQUENCE, BigDecimal.valueOf(20));
            zeroesRule.activate();
            validationRuleSet.save();
            context.commit();
        }
        try (TransactionContext context = transactionService.getContext()) {
            readingType = injector.getInstance(MeteringService.class).getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            validationRuleSet = injector.getInstance(ValidationService.class).getValidationRuleSet(validationRuleSet.getId()).get();
            ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.getRuleSetVersions().get(0);
            ValidationRule minMaxRule = validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, MIN_MAX, "minmax");
            minMaxRule.addReadingType(readingType);
            minMaxRule.addProperty(MIN, BigDecimal.valueOf(1));
            minMaxRule.addProperty(MAX, BigDecimal.valueOf(100));
            minMaxRule.activate();
            validationRuleSet.save();
            context.commit();
        }
        validationRuleSet = injector.getInstance(ValidationService.class).getValidationRuleSet(validationRuleSet.getId()).get();
        assertThat(validationRuleSet.getRuleSetVersions().get(0).getRules()).hasSize(2);
        ValidationRule validationRule = validationRuleSet.getRuleSetVersions().get(0).getRules().get(0);
        assertThat(validationRule.getProperties()).hasSize(1);
    }


}
