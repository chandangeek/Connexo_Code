package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyPurposeImplTestIT {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testHasDefaultBillingMetrologyPurpose() {
        Optional<MetrologyPurpose> metrologyPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        assertThat(metrologyPurpose).isPresent();
    }

    @Test
    public void testHasDefaultInformationMetrologyPurpose() {
        Optional<MetrologyPurpose> metrologyPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION);
        assertThat(metrologyPurpose).isPresent();
    }

    @Test
    public void testHasDefaultVoltageMonitoringMetrologyPurpose() {
        Optional<MetrologyPurpose> metrologyPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyPurpose(DefaultMetrologyPurpose.VOLTAGE_MONITORING);
        assertThat(metrologyPurpose).isPresent();
    }

    @Test
    @Transactional
    public void testCreateMetrologyPurpose() {
        MetrologyPurpose metrologyPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService().createMetrologyPurpose()
                .withName("name")
                .withDescription("description")
                .create();
        assertThat(metrologyPurpose.getId()).isGreaterThan(0);
        assertThat(metrologyPurpose.getName()).isEqualTo("name");
        assertThat(metrologyPurpose.getDescription()).isEqualTo("description");
    }

    @Test
    @Transactional
    public void testCreateDefaultMetrologyPurposeReturnsTheSameInstance() {
        MetrologyPurpose billingPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
        MetrologyPurpose createdPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createMetrologyPurpose()
                .fromDefaultMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        assertThat(createdPurpose).isEqualTo(billingPurpose);
    }
}
