package com.elster.jupiter.validation.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.base.Optional;
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

import static org.assertj.guava.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSetImplIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private String MAX_NUMBER_IN_SEQUENCE = "maxNumberInSequence";
    private String MIN_MAX = "minMax";
    private String MIN = "min";
    private String MAX = "max";
    private ReadingType readingType;


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
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(ValidationService.class);
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
                ValidationRule zeroesRule = validationRuleSet.addRule(ValidationAction.FAIL, "consecutiveZeroes");
                zeroesRule.addReadingType(readingType);
                zeroesRule.addProperty(MAX_NUMBER_IN_SEQUENCE, Unit.UNITLESS.amount(BigDecimal.valueOf(20)));
                zeroesRule.activate();
                ValidationRule minMaxRule = validationRuleSet.addRule(ValidationAction.WARN_ONLY, MIN_MAX);
                minMaxRule.addReadingType(readingType);
                minMaxRule.addProperty(MIN, Unit.WATT_HOUR.amount(BigDecimal.valueOf(1), 3));
                minMaxRule.addProperty(MAX, Unit.WATT_HOUR.amount(BigDecimal.valueOf(100), 3));
                minMaxRule.activate();
                validationRuleSet.save();

                Optional<ValidationRuleSet> found = injector.getInstance(ValidationService.class).getValidationRuleSet(validationRuleSet.getId());
                assertThat(found).isPresent();
                assertThat(found.get().getRules()).hasSize(2);
            }
        });
    }

    @Test
    public void testAddSecondRuleSeeIfReadingTypesArentLost() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        ValidationRuleSet validationRuleSet;
        try(TransactionContext context = transactionService.getContext()) {
            readingType = injector.getInstance(MeteringService.class).getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            validationRuleSet = injector.getInstance(ValidationService.class).createValidationRuleSet("myRuleSet");
            ValidationRule zeroesRule = validationRuleSet.addRule(ValidationAction.FAIL, "consecutiveZeroes");
            zeroesRule.addReadingType(readingType);
            zeroesRule.addProperty(MAX_NUMBER_IN_SEQUENCE, Unit.UNITLESS.amount(BigDecimal.valueOf(20)));
            zeroesRule.activate();
            validationRuleSet.save();
            context.commit();
        }
        try(TransactionContext context = transactionService.getContext()) {
            readingType = injector.getInstance(MeteringService.class).getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            validationRuleSet = injector.getInstance(ValidationService.class).getValidationRuleSet(validationRuleSet.getId()).get();
            ValidationRule minMaxRule = validationRuleSet.addRule(ValidationAction.WARN_ONLY, MIN_MAX);
            minMaxRule.addReadingType(readingType);
            minMaxRule.addProperty(MIN, Unit.WATT_HOUR.amount(BigDecimal.valueOf(1), 3));
            minMaxRule.addProperty(MAX, Unit.WATT_HOUR.amount(BigDecimal.valueOf(100), 3));
            minMaxRule.activate();
            validationRuleSet.save();
            context.commit();
        }
        validationRuleSet = injector.getInstance(ValidationService.class).getValidationRuleSet(validationRuleSet.getId()).get();
        assertThat(validationRuleSet.getRules()).hasSize(2);
        ValidationRule validationRule = validationRuleSet.getRules().get(0);
        assertThat(validationRule.getReadingTypes()).hasSize(1);
    }

    @Test
    public void testAddSecondRuleSeeIfPropertiesArentLost() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        ValidationRuleSet validationRuleSet;
        try(TransactionContext context = transactionService.getContext()) {
            readingType = injector.getInstance(MeteringService.class).getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            validationRuleSet = injector.getInstance(ValidationService.class).createValidationRuleSet("myRuleSet");
            ValidationRule zeroesRule = validationRuleSet.addRule(ValidationAction.FAIL, "consecutiveZeroes");
            zeroesRule.addReadingType(readingType);
            zeroesRule.addProperty(MAX_NUMBER_IN_SEQUENCE, Unit.UNITLESS.amount(BigDecimal.valueOf(20)));
            zeroesRule.activate();
            validationRuleSet.save();
            context.commit();
        }
        try(TransactionContext context = transactionService.getContext()) {
            readingType = injector.getInstance(MeteringService.class).getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            validationRuleSet = injector.getInstance(ValidationService.class).getValidationRuleSet(validationRuleSet.getId()).get();
            ValidationRule minMaxRule = validationRuleSet.addRule(ValidationAction.WARN_ONLY, MIN_MAX);
            minMaxRule.addReadingType(readingType);
            minMaxRule.addProperty(MIN, Unit.WATT_HOUR.amount(BigDecimal.valueOf(1), 3));
            minMaxRule.addProperty(MAX, Unit.WATT_HOUR.amount(BigDecimal.valueOf(100), 3));
            minMaxRule.activate();
            validationRuleSet.save();
            context.commit();
        }
        validationRuleSet = injector.getInstance(ValidationService.class).getValidationRuleSet(validationRuleSet.getId()).get();
        assertThat(validationRuleSet.getRules()).hasSize(2);
        ValidationRule validationRule = validationRuleSet.getRules().get(0);
        assertThat(validationRule.getProperties()).hasSize(1);
    }


}
