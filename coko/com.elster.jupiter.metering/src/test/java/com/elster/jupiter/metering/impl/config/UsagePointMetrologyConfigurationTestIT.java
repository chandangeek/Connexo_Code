/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointRequirement;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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

    private static final String DEFAULT_SEARCH_PROPERTY = "SERVICEKIND";
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
        valueBean.values = Collections.singletonList("GAS");
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
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.PRODUCTION.getKey()).get();
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
        metrologyConfiguration
                .newReadingTypeRequirement("Reading type requirement", meterRole)
                .withReadingType(readingType);

        metrologyConfiguration.removeMeterRole(meterRole);
    }

    @Test(expected = UnsupportedOperationException.class)
    @Transactional
    public void testCanNotCreateFullySpecifiedReadingTypeRequirementWithoutMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");

        // Business method
        metrologyConfiguration
                .newReadingTypeRequirement("Reading type requirement")
                .withReadingType(readingType);
    }

    @Test(expected = UnsupportedOperationException.class)
    @Transactional
    public void testCanNotCreatePartiallySpecifiedReadingTypeRequirementWithoutMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingTypeTemplate readingTypeTemplate = getMetrologyConfigurationService()
                .createReadingTypeTemplate("Zero reading type template")
                .done();

        // Business method
        metrologyConfiguration
                .newReadingTypeRequirement("Reading type requirement")
                .withReadingTypeTemplate(readingTypeTemplate);
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void testCanNotCreateReadingTypeRequirementWithUnassignedMeterRole() {
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", getServiceCategory()).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        try {
            metrologyConfiguration
                    .newReadingTypeRequirement("Reading type requirement", meterRole)
                    .withReadingType(readingType);
        } catch (ConstraintViolationException e) {
            // Asserts
            assertThat(e.getConstraintViolations()).hasSize(1);
            ConstraintViolation<?> violation = e.getConstraintViolations().iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("meterRole");

            // Rethrow for the expected exception rule
            throw e;
        }
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
        FullySpecifiedReadingTypeRequirement readingTypeRequirement =
                metrologyConfiguration
                        .newReadingTypeRequirement(name, meterRole)
                        .withReadingType(readingType);
        assertThat(readingTypeRequirement.getName()).isEqualTo(name);
        assertThat(readingTypeRequirement.getMetrologyConfiguration()).isEqualTo(metrologyConfiguration);
    }

    @Test
    @Transactional
    public void testCanFindMeterRoleForRequirement() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        String name = "Reading type requirement";
        FullySpecifiedReadingTypeRequirement readingTypeRequirement =
                metrologyConfiguration
                        .newReadingTypeRequirement(name, meterRole)
                        .withReadingType(readingType);

        UsagePointMetrologyConfiguration mc = (UsagePointMetrologyConfiguration) getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfiguration.getId()).get();
        Optional<MeterRole> meterRoleRef = mc.getMeterRoleFor(readingTypeRequirement);

        assertThat(meterRoleRef).isPresent();
        assertThat(meterRoleRef.get()).isEqualTo(meterRole);
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
        assertThat(valueBean.values).containsExactly("GAS");
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
        valueBean.values = Collections.singletonList("ELECTRICITY");
        valueBean.operator = SearchablePropertyOperator.NOT_EQUAL;
        metrologyConfiguration.addUsagePointRequirement(valueBean);

        metrologyConfiguration = (UsagePointMetrologyConfiguration) getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfiguration.getId()).get();
        assertThat(metrologyConfiguration.getUsagePointRequirements()).hasSize(1);
        valueBean = metrologyConfiguration.getUsagePointRequirements().get(0).toValueBean();
        assertThat(valueBean.propertyName).isEqualTo(DEFAULT_SEARCH_PROPERTY);
        assertThat(valueBean.values).contains("ELECTRICITY");
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
        valueBean.values = Collections.singletonList("ELECTRICITY");
        valueBean.operator = SearchablePropertyOperator.NOT_EQUAL;
        metrologyConfiguration.addUsagePointRequirement(valueBean);
        assertThat(metrologyConfiguration.getUsagePointRequirements()).hasSize(2);

        metrologyConfiguration = (UsagePointMetrologyConfiguration) getMetrologyConfigurationService()
                .findMetrologyConfiguration(metrologyConfiguration.getId())
                .get();
        metrologyConfiguration.removeUsagePointRequirement(requirement1);

        assertThat(metrologyConfiguration.getUsagePointRequirements()).hasSize(1);
        valueBean = metrologyConfiguration.getUsagePointRequirements().get(0).toValueBean();
        assertThat(valueBean.propertyName).isEqualTo("requirement2");
        assertThat(valueBean.values).containsExactly("ELECTRICITY");
        assertThat(valueBean.operator).isEqualTo(SearchablePropertyOperator.NOT_EQUAL);
    }

    @Test
    @Transactional
    public void testCanGetReadingTypeRequirementsForMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        String name = "Reading type requirement";
        FullySpecifiedReadingTypeRequirement readingTypeRequirement =
                metrologyConfiguration
                        .newReadingTypeRequirement(name, meterRole)
                        .withReadingType(readingType);

        List<ReadingTypeRequirement> requirements = metrologyConfiguration.getRequirements(meterRole);

        assertThat(requirements).hasSize(1);
        assertThat(requirements.get(0)).isEqualTo(readingTypeRequirement);
    }

    @Test
    @Transactional
    public void testCanFindLinkableMetrologyConfigurations() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
        SearchablePropertyValue.ValueBean serviceKindBean = new SearchablePropertyValue.ValueBean();
        serviceKindBean.propertyName = "SERVICEKIND";
        serviceKindBean.operator = SearchablePropertyOperator.EQUAL;
        serviceKindBean.values = Collections.singletonList("ELECTRICITY");
        metrologyConfiguration.addUsagePointRequirement(serviceKindBean);
        metrologyConfiguration.activate();

        metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config 2", serviceCategory)
                .create();
        serviceKindBean = new SearchablePropertyValue.ValueBean();
        serviceKindBean.propertyName = "SERVICEKIND";
        serviceKindBean.operator = SearchablePropertyOperator.EQUAL;
        serviceKindBean.values = Collections.singletonList("GAS");
        metrologyConfiguration.addUsagePointRequirement(serviceKindBean);
        metrologyConfiguration.activate();

        UsagePoint usagePoint = serviceCategory.newUsagePoint("UsagePoint1", inMemoryBootstrapModule.getClock().instant()).create();
        List<UsagePointMetrologyConfiguration> metrologyConfigurations = getMetrologyConfigurationService().findLinkableMetrologyConfigurations(usagePoint);

        assertThat(metrologyConfigurations).hasSize(1);
        assertThat(metrologyConfigurations.get(0)).isEqualTo(metrologyConfiguration);
    }


    @Test
    @Transactional
    public void testNoLinkableMetrologyConfigurationsForAllInactiveMetrologyConfigurations() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
        SearchablePropertyValue.ValueBean serviceKindBean = new SearchablePropertyValue.ValueBean();
        serviceKindBean.propertyName = "SERVICEKIND";
        serviceKindBean.operator = SearchablePropertyOperator.EQUAL;
        serviceKindBean.values = Collections.singletonList("GAS");
        metrologyConfiguration.addUsagePointRequirement(serviceKindBean);

        UsagePoint usagePoint = serviceCategory.newUsagePoint("UsagePoint1", inMemoryBootstrapModule.getClock().instant()).create();
        List<UsagePointMetrologyConfiguration> metrologyConfigurations = getMetrologyConfigurationService().findLinkableMetrologyConfigurations(usagePoint);

        assertThat(metrologyConfigurations).hasSize(0);
    }

    @Test
    @Transactional
    public void testCanRemoveMetrologyConfigurationWithReadingTypeRequirements() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory).create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        metrologyConfiguration
                .newReadingTypeRequirement("Reading type requirement", meterRole)
                .withReadingType(readingType);

        metrologyConfiguration.delete();

        assertThat(getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfiguration.getId())
                .isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testCanRemoveMetrologyConfigurationWithContracts() {
        ServiceCategory serviceCategory = getServiceCategory();
        MetrologyPurpose metrologyPurpose = getMetrologyConfigurationService().findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                .get();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService()
                .createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        FullySpecifiedReadingTypeRequirement requirement = metrologyConfiguration
                .newReadingTypeRequirement("Reading type requirement", meterRole)
                .withReadingType(readingType);
        ReadingTypeDeliverableBuilder deliverableBuilder = metrologyConfiguration.newReadingTypeDeliverable("Reading type deliverable", readingType, Formula.Mode.EXPERT);
        ReadingTypeDeliverable readingTypeDeliverable = deliverableBuilder.build(deliverableBuilder.divide(deliverableBuilder
                .requirement(requirement), deliverableBuilder.constant(10L)));
        MetrologyContract metrologyContract = metrologyConfiguration.addMetrologyContract(metrologyPurpose);
        metrologyContract.addDeliverable(readingTypeDeliverable);

        // Business method
        metrologyConfiguration.delete();

        // Asserts
        assertThat(getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfiguration.getId())).isEmpty();
    }

    @Test
    @Transactional
    public void testMakeMetrologyConfigurationObsolete() {
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config", getServiceCategory()).create();

        // Business method
        metrologyConfiguration.makeObsolete();

        // Asserts
        assertThat(metrologyConfiguration.getObsoleteTime()).isPresent();
    }

    @Test
    @Transactional
    public void testGetObsoleteTime() {
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config", getServiceCategory()).create();

        // Business method
        Optional<Instant> obsoleteTime = metrologyConfiguration.getObsoleteTime();

        // Asserts
        assertThat(obsoleteTime).isEmpty();
    }

    @Test(expected = CannotDeactivateMetrologyConfiguration.class)
    @Transactional
    public void testCanNotMakeActiveMetrologyConfigurationObsolete() {
        // Business method
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("Name", getServiceCategory()).create();
        UsagePoint usagePoint = getServiceCategory().newUsagePoint("Usage point", Instant.now()).create();

        metrologyConfiguration.activate();
        usagePoint.apply(metrologyConfiguration);

        // Business method
        metrologyConfiguration.makeObsolete();

        //Asserts
        // Exception is thrown
    }
}
