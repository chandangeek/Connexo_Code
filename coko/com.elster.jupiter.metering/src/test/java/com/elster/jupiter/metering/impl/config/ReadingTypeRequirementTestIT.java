package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.FullySpecifiedReadingType;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingType;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.transaction.TransactionContext;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeRequirementTestIT {
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();
    private static MetrologyConfiguration metrologyConfiguration;

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
            metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService().newMetrologyConfiguration("Test", serviceCategory).create();
            context.commit();
        }
    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());
    @Rule
    public ExpectedConstraintViolationRule violationRule = new ExpectedConstraintViolationRule();

    @Before
    public void before() {
        metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findMetrologyConfiguration(metrologyConfiguration.getId()).get();
    }

    @Test
    @Transactional
    public void testCreateFullySpecifiedReadingTypeRequirement() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        String name = "Fully specified";
        FullySpecifiedReadingType fullySpecifiedReadingType = metrologyConfiguration.newReadingTypeRequirement(name)
                .withReadingType(readingType);
        assertThat(fullySpecifiedReadingType.getId()).isGreaterThan(0);
        assertThat(fullySpecifiedReadingType.getName()).isEqualTo(name);
        assertThat(fullySpecifiedReadingType.getReadingType()).isEqualTo(readingType);
        assertThat(fullySpecifiedReadingType.getMetrologyConfiguration()).isEqualTo(metrologyConfiguration);
        assertThat(metrologyConfiguration.getRequirements()).contains(fullySpecifiedReadingType);
    }

    @Test
    @Transactional
    public void testCreatePartiallySpecifiedReadingTypeRequirement() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Zero reading type template")
                .done();
        String name = "Partially specified";
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.newReadingTypeRequirement(name)
                .withReadingTypeTemplate(readingTypeTemplate);
        assertThat(partiallySpecifiedReadingType.getId()).isGreaterThan(0);
        assertThat(partiallySpecifiedReadingType.getName()).isEqualTo(name);
        assertThat(partiallySpecifiedReadingType.getReadingTypeTemplate()).isEqualTo(readingTypeTemplate);
        assertThat(partiallySpecifiedReadingType.getMetrologyConfiguration()).isEqualTo(metrologyConfiguration);
        assertThat(metrologyConfiguration.getRequirements()).contains(partiallySpecifiedReadingType);
    }

    @Test
    @Transactional
    public void testCreatePartiallySpecifiedReadingTypeRequirementWithOverriddenValue() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Zero reading type template")
                .done();
        String name = "Partially specified";
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.newReadingTypeRequirement(name)
                .withReadingTypeTemplate(readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.COMMODITY, 50);
        assertThat(partiallySpecifiedReadingType.getId()).isGreaterThan(0);
        assertThat(partiallySpecifiedReadingType.getVersion()).isEqualTo(2);
    }

    @Test
    @Transactional
    public void validCorrectCodeFromPossibleValuesForPartiallySpecified() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Zero reading type template")
                .done();
        readingTypeTemplate.startUpdate().setAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, null, 4, 10);
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.newReadingTypeRequirement("Partially specified")
                .withReadingTypeTemplate(readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 4);
        // assert no exception
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}", property = "code", strict = true)
    public void validBadCodeFromPossibleValuesForPartiallySpecified() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Zero reading type template")
                .done();
        readingTypeTemplate.startUpdate().setAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, null, 4, 10).done();
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.newReadingTypeRequirement("Partially specified")
                .withReadingTypeTemplate(readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 6);
    }

    @Test
    @Transactional
    public void validCorrectCodeFromSystemValuesForPartiallySpecified() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Zero reading type template")
                .done();
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.newReadingTypeRequirement("Partially specified")
                .withReadingTypeTemplate(readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.DAILY.getId());
        // assert no exception
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}", property = "code", strict = true)
    public void validBadCodeFromSystemValuesForPartiallySpecified() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Zero reading type template")
                .done();
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.newReadingTypeRequirement("Partially specified")
                .withReadingTypeTemplate(readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.WEEKLYS.getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED + "}", property = "name", strict = true)
    public void validNameForPartiallySpecified() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Zero reading type template")
                .done();
        metrologyConfiguration.newReadingTypeRequirement(null)
                .withReadingTypeTemplate(readingTypeTemplate);
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED + "}", property = "readingTypeTemplate", strict = true)
    public void validReadingTypeTemplateTemplateForPartiallySpecified() {
        metrologyConfiguration.newReadingTypeRequirement("Some name").withReadingTypeTemplate(null);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED + "}", property = "name", strict = true)
    public void validNameForFullySpecified() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        metrologyConfiguration.newReadingTypeRequirement(null).withReadingType(readingType);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED + "}", property = "readingType", strict = true)
    public void validReadingTypeForFullySpecified() {
        metrologyConfiguration.newReadingTypeRequirement("Some name").withReadingType(null);
    }

    @Test
    @Transactional
    public void testMatchFullySpecifiedReadingType() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        FullySpecifiedReadingTypeImpl fullySpecifiedReadingType =
                (FullySpecifiedReadingTypeImpl) metrologyConfiguration
                        .newReadingTypeRequirement("Zero fully specified")
                        .withReadingType(readingType);
        assertThat(fullySpecifiedReadingType.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testDoesNotMatchFullySpecifiedReadingType() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        ReadingType readingTypeWithToU = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.20.0.0.0.0.0", "ToU reading type");
        FullySpecifiedReadingTypeImpl fullySpecifiedReadingType =
                (FullySpecifiedReadingTypeImpl) metrologyConfiguration
                        .newReadingTypeRequirement("Zero fully specified")
                        .withReadingType(readingType);
        assertThat(fullySpecifiedReadingType.matches(readingTypeWithToU)).isFalse();
    }

    @Test
    @Transactional
    public void testMatchPartiallySpecifiedReadingType() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Zero reading type template")
                .done();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.4.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "DeltaDelta reading type");
        PartiallySpecifiedReadingTypeImpl partiallySpecifiedReadingType =
                (PartiallySpecifiedReadingTypeImpl) metrologyConfiguration
                        .newReadingTypeRequirement("Zero partially specified")
                        .withReadingTypeTemplate(readingTypeTemplate);
        assertThat(partiallySpecifiedReadingType.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testAPlusMatchesPrimaryMeteredChannel() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("APlus")
                .setAttribute(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE.getId())
                .setAttribute(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD.getId())
                .setAttribute(
                        ReadingTypeTemplateAttributeName.COMMODITY,
                        null,
                        Commodity.ELECTRICITY_PRIMARY_METERED.getId(),
                        Commodity.ELECTRICITY_SECONDARY_METERED.getId())
                .setAttribute(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY.getId())
                .setAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATTHOUR.getId())
                .done();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "A+ primary metered");
        PartiallySpecifiedReadingTypeImpl partiallySpecifiedReadingType =
                (PartiallySpecifiedReadingTypeImpl) metrologyConfiguration
                        .newReadingTypeRequirement("A+")
                        .withReadingTypeTemplate(readingTypeTemplate);

        assertThat(partiallySpecifiedReadingType.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testAPlusMatchesSecondaryMeteredChannel() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("APlus")
                .setAttribute(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.NOTAPPLICABLE.getId())
                .setAttribute(ReadingTypeTemplateAttributeName.FLOW_DIRECTION, FlowDirection.FORWARD.getId())
                .setAttribute(
                        ReadingTypeTemplateAttributeName.COMMODITY,
                        null,
                        Commodity.ELECTRICITY_PRIMARY_METERED.getId(),
                        Commodity.ELECTRICITY_SECONDARY_METERED.getId())
                .setAttribute(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, MeasurementKind.ENERGY.getId())
                .setAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.WATTHOUR.getId())
                .done();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0", "A+ secondary metered");
        PartiallySpecifiedReadingTypeImpl partiallySpecifiedReadingType =
                (PartiallySpecifiedReadingTypeImpl) metrologyConfiguration
                        .newReadingTypeRequirement("A+")
                        .withReadingTypeTemplate(readingTypeTemplate);

        assertThat(partiallySpecifiedReadingType.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testMatchOverriddenValuePartiallySpecifiedReadingType() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Accumulation reading type template")
                .setAttribute(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.DELTADELTA.getId())
                .done();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.9.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "DeltaDelta reading type");
        PartiallySpecifiedReadingTypeImpl partiallySpecifiedReadingType =
                (PartiallySpecifiedReadingTypeImpl) metrologyConfiguration
                        .newReadingTypeRequirement("Zero partially specified")
                        .withReadingTypeTemplate(readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.SUMMATION.getId());
        assertThat(partiallySpecifiedReadingType.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testMatchWildCardPartiallySpecifiedReadingType() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Zero reading type template")
                .done();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("24.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Weekly reading type");
        PartiallySpecifiedReadingTypeImpl partiallySpecifiedReadingType =
                (PartiallySpecifiedReadingTypeImpl) metrologyConfiguration
                        .newReadingTypeRequirement("Zero partially specified")
                        .withReadingTypeTemplate(readingTypeTemplate);
        // System possible values for macro period should be applied
        assertThat(partiallySpecifiedReadingType.matches(readingType)).isFalse();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}", property = "name", strict = true)
    public void testCanNotCreateRequirementsWithTheSameName() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        ReadingType weeklyReadingType = inMemoryBootstrapModule.getMeteringService().createReadingType("24.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Weekly reading type");
        metrologyConfiguration.newReadingTypeRequirement("Name").withReadingType(readingType);
        metrologyConfiguration.newReadingTypeRequirement("Name").withReadingType(weeklyReadingType);
    }

    @Test
    @Transactional
    public void testCanCreateRequirementsWithTheSameNameButOnDifferentConfigurations() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");

        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        MetrologyConfiguration metrologyConfiguration2 = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .newMetrologyConfiguration("Configuration 2", serviceCategory).create();
        ReadingType readingType2 = inMemoryBootstrapModule.getMeteringService().createReadingType("24.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Weekly reading type");

        FullySpecifiedReadingType rtr1 = metrologyConfiguration.newReadingTypeRequirement("Name").withReadingType(readingType);
        FullySpecifiedReadingType rtr2 = metrologyConfiguration2.newReadingTypeRequirement("Name").withReadingType(readingType2);

        assertThat(rtr1).isNotEqualTo(rtr2);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIREMENT_MUST_HAVE_UNIQUE_RT + "}", property = "readingType", strict = true)
    public void testCanNotCreateRequirementsWithTheSameReadingType() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        metrologyConfiguration.newReadingTypeRequirement("Name").withReadingType(readingType);
        metrologyConfiguration.newReadingTypeRequirement("Name 2").withReadingType(readingType);
    }

    @Test
    @Transactional
    public void testCanCreateRequirementsWithTheSameReadingTypeButOnDifferentConfigurations() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        MetrologyConfiguration metrologyConfiguration2 = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .newMetrologyConfiguration("Configuration 2", serviceCategory).create();

        FullySpecifiedReadingType rtr1 = metrologyConfiguration.newReadingTypeRequirement("Name").withReadingType(readingType);
        FullySpecifiedReadingType rtr2 = metrologyConfiguration2.newReadingTypeRequirement("Name 2").withReadingType(readingType);

        assertThat(rtr1).isNotEqualTo(rtr2);
    }
}