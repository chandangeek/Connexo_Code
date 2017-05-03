/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.orm.Table;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyPurposeImplTestIT {

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
        MetrologyPurpose metrologyPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService().createMetrologyPurpose(
                SimpleNlsKey.key("TST", Layer.DOMAIN, "custom.purpose.key").defaultMessage("Custom"),
                SimpleNlsKey.key("TST", Layer.DOMAIN, "custom.purpose.descr").defaultMessage("Description"));
        assertThat(metrologyPurpose.getId()).isGreaterThan(0);
        assertThat(metrologyPurpose.getName()).isEqualTo("Custom");
        assertThat(metrologyPurpose.getDescription()).isEqualTo("Description");
    }

    @Test
    @Transactional
    public void testCreateDefaultMetrologyPurposeReturnsTheSameInstance() {
        MetrologyPurpose billingPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
        MetrologyPurpose createdPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        assertThat(createdPurpose).isEqualTo(billingPurpose);
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    @Transactional
    public void testCreateMetrologyPurposeWithEmptyName() {
        inMemoryBootstrapModule.getMetrologyConfigurationService().createMetrologyPurpose(
                SimpleNlsKey.key("TST", Layer.DOMAIN, ""),
                SimpleNlsKey.key("TST", Layer.DOMAIN, "description"));
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + PrivateMessageSeeds.Constants.FIELD_TOO_LONG + "}")
    @Transactional
    public void testCreateMetrologyPurposeWithTooLongName() {
        String[] name = new String[Table.NAME_LENGTH + 1];
        Arrays.fill(name, "a");
        String longName = Stream.of(name).collect(Collectors.joining(""));
        inMemoryBootstrapModule.getMetrologyConfigurationService().createMetrologyPurpose(
                SimpleNlsKey.key("TST", Layer.DOMAIN, longName),
                SimpleNlsKey.key("TST", Layer.DOMAIN, "description"));
    }

    @Test
    @ExpectedConstraintViolation(property = "description", messageId = "{" + PrivateMessageSeeds.Constants.FIELD_TOO_LONG + "}")
    @Transactional
    public void testCreateMetrologyPurposeWithTooLongDescription() {
        String[] description = new String[Table.DESCRIPTION_LENGTH + 1];
        Arrays.fill(description, "a");
        String longDescription = Stream.of(description).collect(Collectors.joining(""));
        inMemoryBootstrapModule.getMetrologyConfigurationService().createMetrologyPurpose(
                SimpleNlsKey.key("TST", Layer.DOMAIN, "name"),
                SimpleNlsKey.key("TST", Layer.DOMAIN, longDescription));
    }

    @Test
    @Transactional
    public void testCanDeleteMetrologyPurpose() {
        MetrologyPurpose metrologyPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService().createMetrologyPurpose(
                SimpleNlsKey.key("TST", Layer.DOMAIN, "name"),
                SimpleNlsKey.key("TST", Layer.DOMAIN, "description"));
        metrologyPurpose.delete();

        assertThat(inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyPurpose(metrologyPurpose.getId()).isPresent()).isFalse();
    }

    @Test(expected = CannotDeleteMetrologyPurposeException.class)
    @Transactional
    public void testCanNotDeleteMetrologyPurposeInUse() {
        MetrologyPurpose metrologyPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService().createMetrologyPurpose(
                SimpleNlsKey.key("TST", Layer.DOMAIN, "name"),
                SimpleNlsKey.key("TST", Layer.DOMAIN, "description"));
        MetrologyConfiguration metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService().newMetrologyConfiguration("config",
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get()).create();
        metrologyConfiguration.addMandatoryMetrologyContract(metrologyPurpose);

        metrologyPurpose.delete();
    }
}
