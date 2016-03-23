package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UPMetrologyConfiguration;
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
public class UPMetrologyConfigurationTestIT {

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
    public void canCreateUsagePointMetrologyConfiguration() {
        String name = "UsagePoint metrology configuration";
        String description = "Description";
        UPMetrologyConfiguration usagePointMetrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration(name, getServiceCategory())
                .withDescription(description)
                .create();

        assertThat(usagePointMetrologyConfiguration.getName()).isEqualTo(name);
        assertThat(usagePointMetrologyConfiguration.getDescription()).isEqualTo(description);
        Optional<MetrologyConfiguration> metrologyConfiguration = getMetrologyConfigurationService().findMetrologyConfiguration(name);
        assertThat(metrologyConfiguration).isPresent();
        assertThat(metrologyConfiguration.get().getId()).isEqualTo(usagePointMetrologyConfiguration.getId());
    }

    @Test(expected = CannotManageMeterRoleOnMetrologyConfigurationException.class)
    @Transactional
    public void testAddMeterRoleWhichIsNotAssignedToServiceCategory() {
        UPMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", getServiceCategory()).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        metrologyConfiguration.addMeterRole(meterRole);
    }

    @Test
    @Transactional
    public void testAddMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UPMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        long version = metrologyConfiguration.getVersion();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);

        assertThat(metrologyConfiguration.getMeterRoles()).contains(meterRole);
        assertThat(metrologyConfiguration.getVersion()).isEqualTo(version + 1);
    }

    @Test
    @Transactional
    public void testCanNotAddMeterRoleTwice() {
        ServiceCategory serviceCategory = getServiceCategory();
        UPMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        long version = metrologyConfiguration.getVersion();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);

        assertThat(metrologyConfiguration.getMeterRoles()).contains(meterRole);
        assertThat(metrologyConfiguration.getMeterRoles()).hasSize(1);
        assertThat(metrologyConfiguration.getVersion()).isEqualTo(version + 1);
    }

    @Test
    @Transactional
    public void testCanRemoveMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UPMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);

        long version = metrologyConfiguration.getVersion();
        metrologyConfiguration.removeMeterRole(meterRole);

        assertThat(metrologyConfiguration.getMeterRoles()).hasSize(0);
        assertThat(metrologyConfiguration.getVersion()).isEqualTo(version + 1);
    }

    @Test
    @Transactional
    public void testCanNotRemoveUnassignedMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UPMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole defaultMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(defaultMeterRole);
        metrologyConfiguration.addMeterRole(defaultMeterRole);

        MeterRole productionRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.PRODUCTION.getKey()).get();
        metrologyConfiguration.removeMeterRole(productionRole);

        assertThat(metrologyConfiguration.getMeterRoles()).contains(defaultMeterRole);
    }


    @Test(expected = CannotManageMeterRoleOnMetrologyConfigurationException.class)
    @Transactional
    public void testCanNotRemoveMeterRoleInUse() {
        ServiceCategory serviceCategory = getServiceCategory();
        UPMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        metrologyConfiguration.addReadingTypeRequirement("Reading type requirement")
                .withMeterRole(meterRole)
                .withReadingType(readingType);

        metrologyConfiguration.removeMeterRole(meterRole);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED + "}", property = "meterRole")
    public void testCanNotCreateFullySpecifiedReadingTypeRequirementWithoutMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UPMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        metrologyConfiguration.addReadingTypeRequirement("Reading type requirement")
                .withReadingType(readingType);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED + "}", property = "meterRole")
    public void testCanNotCreatePartiallySpecifiedReadingTypeRequirementWithoutMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UPMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingTypeTemplate readingTypeTemplate = getMetrologyConfigurationService().createReadingTypeTemplate("Zero reading type template");
        metrologyConfiguration.addReadingTypeRequirement("Reading type requirement")
                .withReadingTypeTemplate(readingTypeTemplate);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CAN_NOT_ADD_REQUIREMENT_WITH_THAT_ROLE + "}", property = "meterRole")
    public void testCanNotCreateReadingTypeRequirementWithUnassignedMeterRole() {
        UPMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", getServiceCategory()).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        metrologyConfiguration.addReadingTypeRequirement("Reading type requirement")
                .withMeterRole(meterRole)
                .withReadingType(readingType);
    }
}
