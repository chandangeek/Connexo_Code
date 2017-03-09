/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationCrudTest {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();

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

    private ServerMetrologyConfigurationService getMetrologyConfigurationService() {
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
        getMetrologyConfigurationService().newMetrologyConfiguration("naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaame", getServiceCategory())
                .withDescription("Long description")
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
    public void testCreateMetrologyConfigurationWithNotUniqueName() {
        getMetrologyConfigurationService().newMetrologyConfiguration("dup1", getServiceCategory()).create();
        getMetrologyConfigurationService().newMetrologyConfiguration("dup1", getServiceCategory()).create();
    }

    @Test
    @Transactional
    public void testCanAddMetrologyContract() {
        MetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newMetrologyConfiguration("config", getServiceCategory()).create();
        MetrologyPurpose metrologyPurpose = getMetrologyConfigurationService().createMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        MetrologyContract metrologyContract = metrologyConfiguration.addMetrologyContract(metrologyPurpose);

        assertThat(metrologyContract.isMandatory()).isFalse();
        assertThat(metrologyContract.getMetrologyConfiguration()).isEqualTo(metrologyConfiguration);
        assertThat(metrologyContract.getMetrologyPurpose()).isEqualTo(metrologyPurpose);
        List<MetrologyContract> metrologyContracts = inMemoryBootstrapModule.getMeteringService().getDataModel().query(MetrologyContract.class)
                .select(where(MetrologyContractImpl.Fields.METROLOGY_CONFIG.fieldName()).isEqualTo(metrologyConfiguration)
                        .and(where(MetrologyContractImpl.Fields.METROLOGY_PURPOSE.fieldName()).isEqualTo(metrologyPurpose)));
        assertThat(metrologyContracts).hasSize(1);
        assertThat(metrologyContracts.get(0)).isEqualTo(metrologyContract);
    }

    @Test
    @Transactional
    public void testDoNotAddTheSameMetrologyContractTwice() {
        MetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newMetrologyConfiguration("config", getServiceCategory()).create();
        MetrologyPurpose metrologyPurpose = getMetrologyConfigurationService().createMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        metrologyConfiguration.addMetrologyContract(metrologyPurpose);
        metrologyConfiguration.addMetrologyContract(metrologyPurpose);

        assertThat(metrologyConfiguration.getContracts()).hasSize(1);
    }

    @Test
    @Transactional
    public void testCanRemoveMetrologyContract() {
        MetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newMetrologyConfiguration("config", getServiceCategory()).create();
        MetrologyPurpose metrologyPurpose = getMetrologyConfigurationService().createMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        MetrologyContract metrologyContract = metrologyConfiguration.addMetrologyContract(metrologyPurpose);
        assertThat(metrologyConfiguration.getContracts()).hasSize(1);

        metrologyConfiguration.removeMetrologyContract(metrologyContract);
        assertThat(metrologyConfiguration.getContracts()).hasSize(0);
        List<MetrologyContract> metrologyContracts = inMemoryBootstrapModule.getMeteringService().getDataModel().query(MetrologyContract.class)
                .select(where(MetrologyContractImpl.Fields.METROLOGY_CONFIG.fieldName()).isEqualTo(metrologyConfiguration)
                        .and(where(MetrologyContractImpl.Fields.METROLOGY_PURPOSE.fieldName()).isEqualTo(metrologyPurpose)));
        assertThat(metrologyContracts).hasSize(0);
    }

    @Test
    @Transactional
    public void testUpdateMetrologyConfiguration() {
        // Business method
        MetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newMetrologyConfiguration("Name", getServiceCategory()).withDescription("Description").create();

        metrologyConfiguration.startUpdate()
                .setName("New name")
                .setDescription("New description")
                .setServiceCategory(inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get())
                .complete();

        //Asserts
        Optional<MetrologyConfiguration> found = getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfiguration.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("New name");
        assertThat(found.get().getDescription()).isEqualTo("New description");
        assertThat(found.get().getServiceCategory().getKind()).isEqualTo(ServiceKind.ELECTRICITY);
        assertThat(found.get().getStatus()).isEqualTo(MetrologyConfigurationStatus.INACTIVE);
    }

    @Test(expected = CannotDeactivateMetrologyConfiguration.class)
    @Transactional
    public void testDeactivateLinkedMetrologyConfiguration() {
        // Business method
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("Name", getServiceCategory())
                .withDescription("Description")
                .create();
        UsagePoint usagePoint = getServiceCategory().newUsagePoint("Usage point", Instant.now()).create();

        metrologyConfiguration.activate();
        usagePoint.apply(metrologyConfiguration);
        metrologyConfiguration.deactivate();
    }
}