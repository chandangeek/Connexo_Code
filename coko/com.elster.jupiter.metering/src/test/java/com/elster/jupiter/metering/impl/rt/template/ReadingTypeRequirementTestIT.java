package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeTemplate;
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
}
