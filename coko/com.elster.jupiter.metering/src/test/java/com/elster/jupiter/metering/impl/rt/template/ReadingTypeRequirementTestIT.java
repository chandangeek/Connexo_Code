package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
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
            metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService().newMetrologyConfiguration("Test",
                    inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get()).create();
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
        FullySpecifiedReadingType fullySpecifiedReadingType = metrologyConfiguration.addFullySpecifiedReadingTypeRequirement(name, readingType);
        assertThat(fullySpecifiedReadingType.getId()).isGreaterThan(0);
        assertThat(fullySpecifiedReadingType.getName()).isEqualTo(name);
        assertThat(fullySpecifiedReadingType.getReadingType()).isEqualTo(readingType);
        assertThat(fullySpecifiedReadingType.getMetrologyConfiguration()).isEqualTo(metrologyConfiguration);
        assertThat(metrologyConfiguration.getRequirements()).contains(fullySpecifiedReadingType);
    }

    @Test
    @Transactional
    public void testCreatePartiallySpecifiedReadingTypeRequirement() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Zero reading type template");
        String name = "Partially specified";
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement(name, readingTypeTemplate);
        assertThat(partiallySpecifiedReadingType.getId()).isGreaterThan(0);
        assertThat(partiallySpecifiedReadingType.getName()).isEqualTo(name);
        assertThat(partiallySpecifiedReadingType.getReadingTypeTemplate()).isEqualTo(readingTypeTemplate);
        assertThat(partiallySpecifiedReadingType.getMetrologyConfiguration()).isEqualTo(metrologyConfiguration);
        assertThat(metrologyConfiguration.getRequirements()).contains(partiallySpecifiedReadingType);
    }

    @Test
    @Transactional
    public void testCreatePartiallySpecifiedReadingTypeRequirementWithOverriddenValue() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Zero reading type template");
        String name = "Partially specified";
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement(name, readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.COMMODITY, 50);
        assertThat(partiallySpecifiedReadingType.getId()).isGreaterThan(0);
        assertThat(partiallySpecifiedReadingType.getVersion()).isEqualTo(2);
    }

    @Test
    @Transactional
    public void validCorrectCodeFromPossibleValuesForPartiallySpecified() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Zero reading type template");
        readingTypeTemplate.updater().setAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, null, 4, 10);
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement("Partially specified", readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 4);
        // assert no exception
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}", property = "code", strict = true)
    public void validBadCodeFromPossibleValuesForPartiallySpecified() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Zero reading type template");
        readingTypeTemplate.updater().setAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, null, 4, 10).done();
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement("Partially specified", readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 6);
    }

    @Test
    @Transactional
    public void validCorrectCodeFromSystemValuesForPartiallySpecified() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Zero reading type template");
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement("Partially specified", readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.DAILY.getId());
        // assert no exception
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}", property = "code", strict = true)
    public void validBadCodeFromSystemValuesForPartiallySpecified() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Zero reading type template");
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement("Partially specified", readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.WEEKLYS.getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED + "}", property = "name", strict = true)
    public void validNameForPartiallySpecified() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Zero reading type template");
        metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement(null, readingTypeTemplate);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED + "}", property = "readingTypeTemplate", strict = true)
    public void validReadingTypeTemplateTemplateForPartiallySpecified() {
        metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement("Some name", null);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED + "}", property = "name", strict = true)
    public void validNameForFullySpecified() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        metrologyConfiguration.addFullySpecifiedReadingTypeRequirement(null, readingType);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED + "}", property = "readingType", strict = true)
    public void validReadingTypeForFullySpecified() {
        metrologyConfiguration.addFullySpecifiedReadingTypeRequirement("Some name", null);
    }

    @Test
    @Transactional
    public void testMatchFullySpecifiedReadingType() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        FullySpecifiedReadingType fullySpecifiedReadingType = metrologyConfiguration.addFullySpecifiedReadingTypeRequirement("Zero fully specified", readingType);
        assertThat(fullySpecifiedReadingType.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testDoesNotMatchFullySpecifiedReadingType() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Zero reading type");
        ReadingType readingTypeWithToU = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.20.0.0.0.0.0", "ToU reading type");
        FullySpecifiedReadingType fullySpecifiedReadingType = metrologyConfiguration.addFullySpecifiedReadingTypeRequirement("Zero fully specified", readingType);
        assertThat(fullySpecifiedReadingType.matches(readingTypeWithToU)).isFalse();
    }

    @Test
    @Transactional
    public void testMatchPartiallySpecifiedReadingType() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Zero reading type template");
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.4.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "DeltaDelta reading type");
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement("Zero partially specified", readingTypeTemplate);
        assertThat(partiallySpecifiedReadingType.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testMatchOverriddenValuePartiallySpecifiedReadingType() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Accumulation reading type template");
        readingTypeTemplate.updater().setAttribute(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.DELTADELTA.getId());
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.9.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "DeltaDelta reading type");
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement("Zero partially specified", readingTypeTemplate);
        partiallySpecifiedReadingType.overrideAttribute(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.SUMMATION.getId());
        assertThat(partiallySpecifiedReadingType.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testMatchWildCardPartiallySpecifiedReadingType() {
        ReadingTypeTemplate readingTypeTemplate = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Zero reading type template");
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("24.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Weekly reading type");
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = metrologyConfiguration.addPartiallySpecifiedReadingTypeRequirement("Zero partially specified", readingTypeTemplate);
        // System possible values for macro period should be applied
        assertThat(partiallySpecifiedReadingType.matches(readingType)).isFalse();
    }
}
