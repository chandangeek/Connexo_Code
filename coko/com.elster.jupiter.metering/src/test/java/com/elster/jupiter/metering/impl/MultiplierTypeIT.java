package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

import com.google.common.base.Strings;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Integration test for {@link MultiplierType}s.
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiplierTypeIT {
    private static final String MULTIPLIER_TYPE_NAME = "Pulse";
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());
    private ServerMeteringService meteringService;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void before() throws SQLException {
        meteringService = inMemoryBootstrapModule.getMeteringService();
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
    @Transactional
    public void createMultiplierTypeWithFixedName() {
        MultiplierType multiplierType;

        // Business method
        multiplierType = meteringService.createMultiplierType(MULTIPLIER_TYPE_NAME);

        // Asserts
        assertThat(meteringService.getMultiplierType(MULTIPLIER_TYPE_NAME)).contains(multiplierType);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void createMultiplierTypeWithNullName() {
        // Business method
        meteringService.createMultiplierType((String) null);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void createMultiplierTypeWithEmptyName() {
        // Business method
        meteringService.createMultiplierType("");

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    public void createMultiplierTypeWithLongName() {
        // Business method
        meteringService.createMultiplierType(Strings.repeat("name", 200));

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
    public void createMultiplierTypeWithDuplicateFixedName() {
        meteringService.createMultiplierType(MULTIPLIER_TYPE_NAME);

        // Business method
        meteringService.createMultiplierType(MULTIPLIER_TYPE_NAME);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    public void createMultiplierTypeWithNlsSupport() {
        String sourceNlsComponent = "SRC";
        String expectedEnglishName = "Test multiplier type";
        String expectedFrenchName = "Type de multiplication de teste";
        String expectedGermanName = "Test Multiplikator Typ";
        NlsService nlsService = inMemoryBootstrapModule.getNlsService();
        NlsKey nlsKey = SimpleNlsKey.key(sourceNlsComponent, Layer.DOMAIN, "test.multiplier.type").defaultMessage(expectedEnglishName);
        nlsService
                .translate(nlsKey)
                .to(Locale.GERMAN, expectedGermanName)
                .to(Locale.FRENCH, expectedFrenchName)
                .add();
        List<MultiplierType> existingMultiplierTypes = this.meteringService.getMultiplierTypes();
        MultiplierType multiplierType;

        // Business method
        multiplierType = meteringService.createMultiplierType(nlsKey);

        // Asserts
        ThreadPrincipalService threadPrincipalService = inMemoryBootstrapModule.getThreadPrincipalService();
        List<MultiplierType> currentMultiplierTypes = this.meteringService.getMultiplierTypes();
        assertThat(currentMultiplierTypes.size()).isEqualTo(existingMultiplierTypes.size() + 1);
        threadPrincipalService.set(threadPrincipalService.getPrincipal(), threadPrincipalService.getModule(), threadPrincipalService.getAction(), Locale.ENGLISH);
        assertThat(multiplierType.getName()).isEqualTo(expectedEnglishName);
        threadPrincipalService.set(threadPrincipalService.getPrincipal(), threadPrincipalService.getModule(), threadPrincipalService.getAction(), Locale.GERMAN);
        assertThat(multiplierType.getName()).isEqualTo(expectedGermanName);
        threadPrincipalService.set(threadPrincipalService.getPrincipal(), threadPrincipalService.getModule(), threadPrincipalService.getAction(), Locale.FRENCH);
        assertThat(multiplierType.getName()).isEqualTo(expectedFrenchName);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
    public void createDuplicateMultiplierTypeWithNlsSupport() {
        String sourceNlsComponent = "SRC";
        String expectedEnglishName = "Test multiplier type";
        String expectedFrenchName = "Type de multiplication de teste";
        String expectedGermanName = "Test Multiplikator Typ";
        NlsService nlsService = inMemoryBootstrapModule.getNlsService();
        NlsKey nlsKey = SimpleNlsKey.key(sourceNlsComponent, Layer.DOMAIN, "test.multiplier.type").defaultMessage(expectedEnglishName);
        nlsService
                .translate(nlsKey)
                .to(Locale.GERMAN, expectedGermanName)
                .to(Locale.FRENCH, expectedFrenchName)
                .add();

        meteringService.createMultiplierType(nlsKey);

        // Business method
        meteringService.createMultiplierType(nlsKey);

        // Asserts: see expected constraint violation rule
    }
}