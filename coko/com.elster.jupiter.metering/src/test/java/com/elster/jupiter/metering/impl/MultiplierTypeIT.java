package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test for {@link MultiplierType}s.
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiplierTypeIT {

    private static final String MULTIPLIER_TYPE_NAME = "Pulse";

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private User user;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;
    private MeteringService meteringService;
    private TransactionService transactionService;

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws SQLException {
        when(this.threadPrincipalService.getLocale()).thenReturn(Locale.getDefault());
        when(this.threadPrincipalService.getPrincipal()).thenReturn(this.user);
        when(this.user.getName()).thenReturn(MultiplierTypeIT.class.getSimpleName());
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new NlsModule(),
                    new CustomPropertySetsModule(),
                    new BasicPropertiesModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(FiniteStateMachineService.class);
            meteringService = injector.getInstance(MeteringService.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void getDefaultMultiplierTypes() {
        // Business method
        List<MultiplierType> multiplierTypes = this.meteringService.getMultiplierTypes();

        // Asserts
        assertThat(multiplierTypes).hasSize(MultiplierType.StandardType.values().length);
    }

    @Test
    public void findCTMultiplierType() {
        // Business method
        MultiplierType ct = this.meteringService.getMultiplierType(MultiplierType.StandardType.CT);

        // Asserts
        assertThat(ct).isNotNull();
    }

    @Test
    public void findVTMultiplierType() {
        // Business method
        MultiplierType vt = this.meteringService.getMultiplierType(MultiplierType.StandardType.VT);

        // Asserts
        assertThat(vt).isNotNull();
    }

    @Test
    public void findPulseMultiplierType() {
        // Business method
        MultiplierType pulse = this.meteringService.getMultiplierType(MultiplierType.StandardType.Pulse);

        // Asserts
        assertThat(pulse).isNotNull();
    }

    @Test
    public void findTransformerMultiplierType() {
        // Business method
        MultiplierType transformer = this.meteringService.getMultiplierType(MultiplierType.StandardType.Transformer);

        // Asserts
        assertThat(transformer).isNotNull();
    }

    @Test
    public void createMultiplierTypeWithFixedName() {
        MultiplierType multiplierType;

        // Business method
        try (TransactionContext context = transactionService.getContext()) {
            multiplierType = meteringService.createMultiplierType(MULTIPLIER_TYPE_NAME);
            context.commit();
        }

        // Asserts
        assertThat(meteringService.getMultiplierType(MULTIPLIER_TYPE_NAME)).contains(multiplierType);
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void createMultiplierTypeWithNullName() {
        // Business method
        try (TransactionContext context = transactionService.getContext()) {
            meteringService.createMultiplierType((String) null);
            context.commit();
        }

        // Asserts: see expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void createMultiplierTypeWithEmptyName() {
        // Business method
        try (TransactionContext context = transactionService.getContext()) {
            meteringService.createMultiplierType("");
            context.commit();
        }

        // Asserts: see expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    public void createMultiplierTypeWithLongName() {
        // Business method
        try (TransactionContext context = transactionService.getContext()) {
            meteringService.createMultiplierType(Strings.repeat("name", 200));
            context.commit();
        }

        // Asserts: see expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
    public void createMultiplierTypeWithDuplicateFixedName() {
        try (TransactionContext context = transactionService.getContext()) {
            meteringService.createMultiplierType(MULTIPLIER_TYPE_NAME);
            context.commit();
        }

        // Business method
        try (TransactionContext context = transactionService.getContext()) {
            meteringService.createMultiplierType(MULTIPLIER_TYPE_NAME);
            context.commit();
        }

        // Asserts: see expected constraint violation rule
    }

    @Test
    public void createMultiplierTypeWithNlsSupport() {
        String sourceNlsComponent = "SRC";
        String expectedEnglishName = "Test multiplier type";
        String expectedFrenchName = "Type de multiplication de teste";
        String expectedGermanName = "Test Multiplikator Typ";
        NlsService nlsService = injector.getInstance(NlsService.class);
        NlsKey nlsKey = injector.getInstance(TransactionService.class).execute(() -> {
            NlsKey nlsKey1 = SimpleNlsKey.key(sourceNlsComponent, Layer.DOMAIN, "test.multiplier.type").defaultMessage(expectedEnglishName);
            nlsService
                    .translate(nlsKey1)
                    .to(Locale.GERMAN, expectedGermanName)
                    .to(Locale.FRENCH, expectedFrenchName)
                    .add();
            return nlsKey1;
        });
        List<MultiplierType> existingMultiplierTypes = this.meteringService.getMultiplierTypes();
        MultiplierType multiplierType;

        // Business method
        try (TransactionContext context = transactionService.getContext()) {
            multiplierType = meteringService.createMultiplierType(nlsKey);
            context.commit();
        }

        // Asserts
        List<MultiplierType> currentMultiplierTypes = this.meteringService.getMultiplierTypes();
        assertThat(currentMultiplierTypes.size()).isEqualTo(existingMultiplierTypes.size() + 1);
        when(this.threadPrincipalService.getLocale()).thenReturn(Locale.ENGLISH);
        assertThat(multiplierType.getName()).isEqualTo(expectedEnglishName);
        when(this.threadPrincipalService.getLocale()).thenReturn(Locale.GERMAN);
        assertThat(multiplierType.getName()).isEqualTo(expectedGermanName);
        when(this.threadPrincipalService.getLocale()).thenReturn(Locale.FRENCH);
        assertThat(multiplierType.getName()).isEqualTo(expectedFrenchName);
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
    public void createDuplicateMultiplierTypeWithNlsSupport() {
        String sourceNlsComponent = "SRC";
        String expectedEnglishName = "Test multiplier type";
        String expectedFrenchName = "Type de multiplication de teste";
        String expectedGermanName = "Test Multiplikator Typ";
        NlsService nlsService = injector.getInstance(NlsService.class);
        NlsKey nlsKey = injector.getInstance(TransactionService.class).execute(() -> {
            NlsKey nlsKey1 = SimpleNlsKey.key(sourceNlsComponent, Layer.DOMAIN, "test.multiplier.type").defaultMessage(expectedEnglishName);
            nlsService
                    .translate(nlsKey1)
                    .to(Locale.GERMAN, expectedGermanName)
                    .to(Locale.FRENCH, expectedFrenchName)
                    .add();
            return nlsKey1;
        });
        try (TransactionContext context = transactionService.getContext()) {
            meteringService.createMultiplierType(nlsKey);
            context.commit();
        }

        // Business method
        try (TransactionContext context = transactionService.getContext()) {
            meteringService.createMultiplierType(nlsKey);
            context.commit();
        }

        // Asserts: see expected constraint violation rule
    }

}