/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeTemplateImplTestIT {
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();

    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());
    @Rule
    public ExpectedConstraintViolationRule violationRule = new ExpectedConstraintViolationRule();

    private List<ReadingTypeTemplateAttribute> getPersistedAttributes() {
        return inMemoryBootstrapModule.getMeteringService().getDataModel().query(ReadingTypeTemplateAttribute.class).select(Condition.TRUE);
    }

    @Test
    @Transactional
    public void createEmptyTemplate() {
        String name = "Empty";
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate(name)
                .done();
        System.out.println("Template: " + template);

        assertThat(template.getId()).isGreaterThan(0);
        assertThat(template.getName()).isEqualTo(name);
    }

    @Test
    @Transactional
    public void createTemplateAttributeWithNullCodeWhichDoesNotSupportWildcard() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("No wildcard")
                .done();
        assertThat(ReadingTypeTemplateAttributeName.ARGUMENT_DENOMINATOR.getDefinition().canBeWildcard()).isFalse()
                .as("This test need attribute which can not accept wildcards");
        template.startUpdate().setAttribute(ReadingTypeTemplateAttributeName.ARGUMENT_DENOMINATOR, null).done();
        System.out.println("Template: " + template);

        ReadingTypeTemplateAttribute argDenom = template.getAttributes()
                .stream()
                .filter(attr -> attr.getName().equals(ReadingTypeTemplateAttributeName.ARGUMENT_DENOMINATOR))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No attribute ARGUMENT_DENOMINATOR"));
        assertThat(argDenom.getCode()).isPresent();
        assertThat(argDenom.getCode().get()).isEqualTo(0);
    }

    @Test
    @Transactional
    public void createTemplateAttributeWithNullCodeWhichSupportsWildcard() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("With wildcard")
                .done();
        assertThat(ReadingTypeTemplateAttributeName.TIME.getDefinition().canBeWildcard()).isTrue()
                .as("This test need attribute which can accept wildcards");
        template.startUpdate().setAttribute(ReadingTypeTemplateAttributeName.TIME, null).done();
        System.out.println("Template: " + template);

        ReadingTypeTemplateAttribute timeAttr = template.getAttributes()
                .stream()
                .filter(attr -> attr.getName().equals(ReadingTypeTemplateAttributeName.TIME))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No attribute TIME"));
        assertThat(timeAttr.getCode().isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void createTemplateAttributeWithCode() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("With code")
                .done();
        assertThat(ReadingTypeTemplateAttributeName.TIME.getDefinition().canBeWildcard()).isTrue()
                .as("This test need attribute which can accept wildcards");
        int timeAttrCode = TimeAttribute.HOUR24.getId();
        template.startUpdate().setAttribute(ReadingTypeTemplateAttributeName.TIME, timeAttrCode).done();
        System.out.println("Template: " + template);

        ReadingTypeTemplateAttribute timeAttr = template.getAttributes()
                .stream()
                .filter(attr -> attr.getName().equals(ReadingTypeTemplateAttributeName.TIME))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No attribute TIME"));
        assertThat(timeAttr.getCode()).isPresent();
        assertThat(timeAttr.getCode().get()).isEqualTo(timeAttrCode);
    }

    @Test
    @Transactional
    public void createTemplateAttributeWithPossibleValues() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("With possible values")
                .done();
        assertThat(ReadingTypeTemplateAttributeName.COMMODITY.getDefinition().canBeWildcard()).isFalse()
                .as("This test need attribute which can not accept wildcards");
        template.startUpdate().setAttribute(ReadingTypeTemplateAttributeName.COMMODITY, null, Commodity.CARBON.getId(), Commodity.C2H2.getId(), Commodity.CO.getId()).done();
        System.out.println("Template: " + template);

        ReadingTypeTemplateAttribute timeAttr = template.getAttributes()
                .stream()
                .filter(attr -> attr.getName().equals(ReadingTypeTemplateAttributeName.COMMODITY))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No attribute COMMODITY"));
        assertThat(timeAttr.getCode().isPresent()).isFalse();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "code", messageId = "{" + PrivateMessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}", strict = true)
    public void createAttributeWithCodeIsNotWithinPossibleValues() {
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Bad time code")
                .setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE15.getId(), TimeAttribute.MINUTE1.getId())
                .done();
    }

    @Test
    @Transactional
    public void redefineForAttributeDoesNotIncreasePersistedAttributes() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Time")
                .setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE15.getId())
                .done();
        int persistedAttributes = getPersistedAttributes().size();
        template.startUpdate().setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE1.getId()).done();
        assertThat(getPersistedAttributes()).hasSize(persistedAttributes);
    }

    @Test
    @Transactional
    public void redefineForDefaultAttributeRemovesOldPersisted() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Time")
                .setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE15.getId(), TimeAttribute.MINUTE15.getId(), TimeAttribute.MINUTE1.getId())
                .done();
        int persistedAttributes = getPersistedAttributes().size();
        template.startUpdate().setAttribute(ReadingTypeTemplateAttributeName.TIME, null).done();
        assertThat(getPersistedAttributes()).hasSize(persistedAttributes - 1);
    }

    @Test
    public void testBatchUpdate() {
        ReadingTypeTemplate template;
        // Manual transaction handling to be sure that attributes will be persisted only after the #done() call
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                    .createReadingTypeTemplate("Batch")
                    .done();
            context.commit();
        }
        ReadingTypeTemplate.ReadingTypeTemplateAttributeSetter updater = template.startUpdate()
                .setAttribute(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.DAILY.getId())
                .setAttribute(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE.getId())
                .setAttribute(ReadingTypeTemplateAttributeName.TIME, null) // default
                .setAttribute(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.DELTADELTA.getId());
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            long oldVersion = template.getVersion();
            updater.done();
            assertThat(template.getVersion()).isEqualTo(oldVersion + 1);
            template.delete();
            context.commit();
        }
    }

    @Test
    @Transactional
    public void testMatchDefaultAttributeInTemplate() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Default template")
                .done();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "ToU reading type");
        assertThat(template.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testMatchCodeAttributeInTemplate() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Code template")
                .setAttribute(ReadingTypeTemplateAttributeName.COMMODITY, 7)
                .done();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.7.0.0.0.0.0.0.0.0.0.0.0.0", "Commodity reading type");
        assertThat(template.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testMatchPossibleValuesAttributeInTemplate() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Possible values template")
                .setAttribute(ReadingTypeTemplateAttributeName.CRITICAL_PEAK_PERIOD, null, 4, 6)
                .done();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.6.0.0.0.0.0", "CPP reading type");
        assertThat(template.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    public void testMatchCodeAndPossibleValuesAttributeInTemplate() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Code and possible values template")
                .setAttribute(ReadingTypeTemplateAttributeName.ARGUMENT_DENOMINATOR, 5, 5, 6)
                .done();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.6.0.0.0.0.0.0.0", "Arg. denom. reading type");
        assertThat(template.matches(readingType)).isFalse();
    }

    @Test
    @Transactional
    public void testMatchWildcardAttributeInTemplate() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeTemplate("Wildcard template")
                .done();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("8.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Macro period reading type");
        assertThat(template.matches(readingType)).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + PrivateMessageSeeds.Constants.READING_TYPE_TEMPLATE_UNITS_SHOULD_HAVE_THE_SAME_DIMENSION + "}", property = "values", strict = true)
    public void testValidPossibleValuesHaveTheSameDimension() {
        inMemoryBootstrapModule.getMetrologyConfigurationService().createReadingTypeTemplate("Valid possible units dimension")
                .setAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, null, ReadingTypeUnit.AMPERE.getId(), ReadingTypeUnit.LITRE.getId())
                .done();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + PrivateMessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}", property = "name", strict = true)
    public void testValidTemplateHasUniqueNameDuringCreation() {
        inMemoryBootstrapModule.getMetrologyConfigurationService().createReadingTypeTemplate("Name").done();
        inMemoryBootstrapModule.getMetrologyConfigurationService().createReadingTypeTemplate("Name").done();
    }

    @Test
    @Transactional
    public void testNoValidTemplateHasUniqueNameDuringUpdate() {
        inMemoryBootstrapModule.getMetrologyConfigurationService().createReadingTypeTemplate("Name")
                .setAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 5)
                .done();
        // assert no exception about non-unique name
    }

    @Test
    @Transactional
    public void testCanNotAddTwoTheSameDefaultTemplates() {
        inMemoryBootstrapModule.getMetrologyConfigurationService().createReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS).done();
        inMemoryBootstrapModule.getMetrologyConfigurationService().createReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS).done();

        List<ReadingTypeTemplate> aPlusTemplates = inMemoryBootstrapModule.getMetrologyConfigurationService().getDataModel()
                .query(ReadingTypeTemplate.class)
                .select(where(ReadingTypeTemplateImpl.Fields.DEFAULT_TEMPLATE.fieldName()).isEqualTo(DefaultReadingTypeTemplate.A_PLUS));

        assertThat(aPlusTemplates).hasSize(1);
    }
}
