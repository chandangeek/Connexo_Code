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
        return new SearchablePropertyValue.ValueBean(DEFAULT_SEARCH_PROPERTY, SearchablePropertyOperator.EQUAL, "GAS");
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", getServiceCategory())
                .create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.PRODUCTION.getKey())
                .get();
        metrologyConfiguration.addMeterRole(meterRole);
    }

    @Test
    @Transactional
    public void testAddMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
        MeterRole defaultMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .get();
        serviceCategory.addMeterRole(defaultMeterRole);
        metrologyConfiguration.addMeterRole(defaultMeterRole);

        MeterRole productionRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.PRODUCTION.getKey())
                .get();
        metrologyConfiguration.removeMeterRole(productionRole);

        assertThat(metrologyConfiguration.getMeterRoles()).contains(defaultMeterRole);
    }


    @Test(expected = CannotManageMeterRoleOnMetrologyConfigurationException.class)
    @Transactional
    public void testCanNotRemoveMeterRoleInUse() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService()
                .createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        metrologyConfiguration
                .newReadingTypeRequirement("Reading type requirement", meterRole)
                .withReadingType(readingType);

        metrologyConfiguration.removeMeterRole(meterRole);
    }

    @Test(expected = UnsupportedOperationException.class)
    @Transactional
    public void testCanNotCreateFullySpecifiedReadingTypeRequirementWithoutMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService()
                .createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");

        // Business method
        metrologyConfiguration
                .newReadingTypeRequirement("Reading type requirement")
                .withReadingType(readingType);
    }

    @Test(expected = UnsupportedOperationException.class)
    @Transactional
    public void testCanNotCreatePartiallySpecifiedReadingTypeRequirementWithoutMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", getServiceCategory())
                .create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService()
                .createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService()
                .createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
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
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService()
                .createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        String name = "Reading type requirement";
        FullySpecifiedReadingTypeRequirement readingTypeRequirement =
                metrologyConfiguration
                        .newReadingTypeRequirement(name, meterRole)
                        .withReadingType(readingType);

        UsagePointMetrologyConfiguration mc = (UsagePointMetrologyConfiguration) getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfiguration
                .getId()).get();
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
        assertThat(valueBean.getOperator()).isEqualTo(SearchablePropertyOperator.EQUAL);
        assertThat(valueBean.getPropertyName()).isEqualTo(DEFAULT_SEARCH_PROPERTY);
        assertThat(valueBean.getValues()).containsExactly("GAS");
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
        metrologyConfiguration.addUsagePointRequirement(new SearchablePropertyValue.ValueBean(DEFAULT_SEARCH_PROPERTY, SearchablePropertyOperator.NOT_EQUAL, "ELECTRICITY"));

        metrologyConfiguration = (UsagePointMetrologyConfiguration) getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfiguration
                .getId()).get();
        assertThat(metrologyConfiguration.getUsagePointRequirements()).hasSize(1);
        valueBean = metrologyConfiguration.getUsagePointRequirements().get(0).toValueBean();
        assertThat(valueBean.getPropertyName()).isEqualTo(DEFAULT_SEARCH_PROPERTY);
        assertThat(valueBean.getValues()).contains("ELECTRICITY");
        assertThat(valueBean.getOperator()).isEqualTo(SearchablePropertyOperator.NOT_EQUAL);
    }

    @Test
    @Transactional
    public void testCanRemoveUsagePointRequirement() {
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config", getServiceCategory())
                .create();

        SearchablePropertyValue.ValueBean valueBean = getSearchablePropertyValueBean();
        UsagePointRequirement requirement1 = metrologyConfiguration.addUsagePointRequirement(valueBean);

        metrologyConfiguration.addUsagePointRequirement(new SearchablePropertyValue.ValueBean("requirement2", SearchablePropertyOperator.NOT_EQUAL, "ELECTRICITY"));
        assertThat(metrologyConfiguration.getUsagePointRequirements()).hasSize(2);

        metrologyConfiguration = (UsagePointMetrologyConfiguration) getMetrologyConfigurationService()
                .findMetrologyConfiguration(metrologyConfiguration.getId())
                .get();
        metrologyConfiguration.removeUsagePointRequirement(requirement1);

        assertThat(metrologyConfiguration.getUsagePointRequirements()).hasSize(1);
        valueBean = metrologyConfiguration.getUsagePointRequirements().get(0).toValueBean();
        assertThat(valueBean.getPropertyName()).isEqualTo("requirement2");
        assertThat(valueBean.getValues()).containsExactly("ELECTRICITY");
        assertThat(valueBean.getOperator()).isEqualTo(SearchablePropertyOperator.NOT_EQUAL);
    }

    @Test
    @Transactional
    public void testCanGetReadingTypeRequirementsForMeterRole() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService()
                .createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
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
        SearchablePropertyValue.ValueBean serviceKindBean = new SearchablePropertyValue.ValueBean("SERVICEKIND", SearchablePropertyOperator.EQUAL, "ELECTRICITY");
        metrologyConfiguration.addUsagePointRequirement(serviceKindBean);
        metrologyConfiguration.activate();

        metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("config 2", serviceCategory)
                .create();
        serviceKindBean = new SearchablePropertyValue.ValueBean("SERVICEKIND", SearchablePropertyOperator.EQUAL, Collections
                .singletonList("GAS"));
        metrologyConfiguration.addUsagePointRequirement(serviceKindBean);
        metrologyConfiguration.activate();

        UsagePoint usagePoint = serviceCategory.newUsagePoint("UsagePoint1", inMemoryBootstrapModule.getClock()
                .instant()).create();
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
        SearchablePropertyValue.ValueBean serviceKindBean = new SearchablePropertyValue.ValueBean("SERVICEKIND", SearchablePropertyOperator.EQUAL, "GAS");
        metrologyConfiguration.addUsagePointRequirement(serviceKindBean);

        UsagePoint usagePoint = serviceCategory.newUsagePoint("UsagePoint1", inMemoryBootstrapModule.getClock()
                .instant()).create();
        List<UsagePointMetrologyConfiguration> metrologyConfigurations = getMetrologyConfigurationService().findLinkableMetrologyConfigurations(usagePoint);

        assertThat(metrologyConfigurations).hasSize(0);
    }

    @Test
    @Transactional
    public void testCanRemoveMetrologyConfigurationWithReadingTypeRequirements() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("config", serviceCategory)
                .create();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();
        serviceCategory.addMeterRole(meterRole);
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService()
                .createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
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

    /**
     * This test verifies that {@link UsagePointMetrologyConfiguration} created by using
     * {@link com.elster.jupiter.metering.config.UsagePointMetrologyConfigurationBuilder}
     * will return <code>true</code> on {@link UsagePointMetrologyConfiguration#isGapAllowed()}
     */
    @Test
    @Transactional
    public void testDefaultGapAllowedFlag() {
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("Name", getServiceCategory()).create();
        assertThat(metrologyConfiguration.isGapAllowed()).isEqualTo(true);
    }

    /**
     * Test setting isGapAllowed flag for {@link UsagePointMetrologyConfiguration}
     */
    @Test
    @Transactional
    public void testGapAllowedFlag() {
        verifyGapAllowedFlag(true, "Name1");
        verifyGapAllowedFlag(false, "Name2");
    }

    private void verifyGapAllowedFlag(boolean isGapAllowed, String name) {
        UsagePointMetrologyConfiguration metrologyConfiguration = getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration(name, getServiceCategory())
                .withGapAllowed(isGapAllowed)
                .create();
        assertThat(metrologyConfiguration.isGapAllowed()).isEqualTo(isGapAllowed);

        // verify flag is saved properly
        MetrologyConfiguration mc = getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfiguration
                .getId()).get();
        assertThat(mc.isGapAllowed()).isEqualTo(isGapAllowed);
    }
}
