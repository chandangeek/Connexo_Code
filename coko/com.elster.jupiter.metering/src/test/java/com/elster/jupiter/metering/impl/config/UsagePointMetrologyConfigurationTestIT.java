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
import com.elster.jupiter.metering.config.FullySpecifiedReadingType;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointRequirement;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.Collections;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointMetrologyConfigurationTestIT {

    private static final String DEFAULT_SEARCH_PROPERTY = "usagePointRequirementSearchableProperty";
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

    private SearchablePropertyValue.ValueBean getSearchablePropertyValueBean() {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = DEFAULT_SEARCH_PROPERTY;
        valueBean.operator = SearchablePropertyOperator.EQUAL;
        valueBean.values = Collections.singletonList("value");
        return valueBean;
    }

    @Test
    @Transactional
    public void canCreateUsagePointMetrologyConfiguration() {
        String name = "UsagePoint metrology configuration";
        String description = "Description";
        UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = getMetrologyConfigurationService()
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", getServiceCategory()).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        metrologyConfiguration.addMeterRole(meterRole);
    }

    @Test
    @Transactional
    public void testAddMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", getServiceCategory()).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        metrologyConfiguration.addReadingTypeRequirement("Reading type requirement")
                .withMeterRole(meterRole)
                .withReadingType(readingType);
    }

    @Test
    @Transactional
    public void testCanCreateReadingTypeRequirementWithMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        String name = "Reading type requirement";
        FullySpecifiedReadingType readingTypeRequirement = metrologyConfiguration.addReadingTypeRequirement(name)
                .withMeterRole(meterRole)
                .withReadingType(readingType);
        assertThat(readingTypeRequirement.getName()).isEqualTo(name);
        assertThat(readingTypeRequirement.getMetrologyConfiguration()).isEqualTo(metrologyConfiguration);
    }

    @Test
    @Transactional
    public void testCanAddUsagePointRequirement() {
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config", getServiceCategory())
                .create();
        UsagePointRequirement usagePointRequirement = metrologyConfiguration.addUsagePointRequirement(getSearchablePropertyValueBean());

        SearchablePropertyValue.ValueBean valueBean = usagePointRequirement.toValueBean();
        assertThat(valueBean.operator).isEqualTo(SearchablePropertyOperator.EQUAL);
        assertThat(valueBean.propertyName).isEqualTo(DEFAULT_SEARCH_PROPERTY);
        assertThat(valueBean.values).containsExactly("value");
    }

    @Test
    @Transactional
    public void testCanNotAddUsagePointRequirementWithTheSamePropertyTwice() {
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config", getServiceCategory())
                .create();
        metrologyConfiguration.addUsagePointRequirement(getSearchablePropertyValueBean());
        metrologyConfiguration.addUsagePointRequirement(getSearchablePropertyValueBean());

        assertThat(metrologyConfiguration.getUsagePointRequirements()).hasSize(1);
    }

    @Test
    @Transactional
    public void testCanUpdateUsagePointRequirement() {
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config", getServiceCategory())
                .create();
        SearchablePropertyValue.ValueBean valueBean = getSearchablePropertyValueBean();
        metrologyConfiguration.addUsagePointRequirement(valueBean);
        valueBean.values = Collections.singletonList("changed");
        valueBean.operator = SearchablePropertyOperator.NOT_EQUAL;
        metrologyConfiguration.addUsagePointRequirement(valueBean);

        metrologyConfiguration = getMetrologyConfigurationService()
                .findUsagePointMetrologyConfiguration(metrologyConfiguration.getId())
                .get();
        assertThat(metrologyConfiguration.getUsagePointRequirements()).hasSize(1);
        valueBean = metrologyConfiguration.getUsagePointRequirements().get(0).toValueBean();
        assertThat(valueBean.propertyName).isEqualTo(DEFAULT_SEARCH_PROPERTY);
        assertThat(valueBean.values).contains("changed");
        assertThat(valueBean.operator).isEqualTo(SearchablePropertyOperator.NOT_EQUAL);
    }

    @Test
    @Transactional
    public void testCanRemoveUsagePointRequirement() {
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config", getServiceCategory())
                .create();

        SearchablePropertyValue.ValueBean valueBean = getSearchablePropertyValueBean();
        UsagePointRequirement requirement1 = metrologyConfiguration.addUsagePointRequirement(valueBean);

        valueBean.propertyName = "requirement2";
        valueBean.values = Collections.singletonList("some");
        valueBean.operator = SearchablePropertyOperator.NOT_EQUAL;
        metrologyConfiguration.addUsagePointRequirement(valueBean);
        assertThat(metrologyConfiguration.getUsagePointRequirements()).hasSize(2);

        metrologyConfiguration = getMetrologyConfigurationService()
                .findUsagePointMetrologyConfiguration(metrologyConfiguration.getId())
                .get();
        metrologyConfiguration.removeUsagePointRequirement(requirement1);

        assertThat(metrologyConfiguration.getUsagePointRequirements()).hasSize(1);
        valueBean = metrologyConfiguration.getUsagePointRequirements().get(0).toValueBean();
        assertThat(valueBean.propertyName).isEqualTo("requirement2");
        assertThat(valueBean.values).containsExactly("some");
        assertThat(valueBean.operator).isEqualTo(SearchablePropertyOperator.NOT_EQUAL);
    }
}
