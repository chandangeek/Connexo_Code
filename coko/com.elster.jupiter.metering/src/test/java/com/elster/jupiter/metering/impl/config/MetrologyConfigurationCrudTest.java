package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationCrudTest {

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

    private MetrologyConfigurationService getMetrologyConfigurationService() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService();
    }

    private ServiceCategory getServiceCategory() {
        return inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.GAS).get();
    }

    @Test
    @Transactional
    public void testCreateMetrologyConfiguration() {
        // Business method
        MetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newMetrologyConfiguration("Name", getServiceCategory()).withDescription("Description").create();

        //Asserts
        Optional<MetrologyConfiguration> found = getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfiguration.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Name");
        assertThat(found.get().getDescription()).isEqualTo("Description");
        assertThat(found.get().getServiceCategory().getKind()).isEqualTo(ServiceKind.GAS);
        assertThat(found.get().getStatus()).isEqualTo(MetrologyConfigurationStatus.INACTIVE);
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void testCreateMetrologyConfigurationWithoutName() {
        getMetrologyConfigurationService().newMetrologyConfiguration(null, getServiceCategory()).withDescription("No name").create();
    }

    @Test
    @ExpectedConstraintViolation(property = "serviceCategory", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void testCreateMetrologyConfigurationWithoutServiceCategory() {
        getMetrologyConfigurationService().newMetrologyConfiguration("Name", null).withDescription("No service category").create();
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void testCreateMetrologyConfigurationWithEmptyName() {
        getMetrologyConfigurationService().newMetrologyConfiguration("", getServiceCategory()).withDescription("Empty name").create();
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    public void testCreateMetrologyConfigurationWithTooLongName() {
        getMetrologyConfigurationService().newMetrologyConfiguration("naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaame", getServiceCategory()).withDescription("Long description").create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
    public void testCreateMetrologyConfigurationWithNotUniqueName() {
        getMetrologyConfigurationService().newMetrologyConfiguration("dup1", getServiceCategory()).create();
        getMetrologyConfigurationService().newMetrologyConfiguration("dup1", getServiceCategory()).create();
    }
}