package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeTemplateImplTestIT {
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            DataModel dataModel = inMemoryBootstrapModule.getMeteringService().getDataModel();
            dataModel.mapper(ReadingTypeTemplate.class).remove(dataModel.query(ReadingTypeTemplate.class).select(Condition.TRUE));
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

    private List<ReadingTypeTemplateAttribute> getPersistedAttributes() {
        return inMemoryBootstrapModule.getMeteringService().getDataModel().query(ReadingTypeTemplateAttribute.class).select(Condition.TRUE);
    }

    @Test
    @Transactional
    public void createEmptyTemplate() {
        String name = "Empty";
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate(name);
        System.out.println("Template: " + template);

        assertThat(template.getId()).isGreaterThan(0);
        assertThat(template.getName()).isEqualTo(name);
        assertThat(getPersistedAttributes().isEmpty()).isTrue();
    }

    @Test
    @Transactional
    public void createTemplateAttributeWithNullCodeWhichDoesNotSupportWildcard() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("No wildcard");
        assertThat(ReadingTypeTemplateAttributeName.ARGUMENT_DENOMINATOR.getDefinition().canBeWildcard()).isFalse()
                .as("This test need attribute which can not accept wildcards");
        template.updater().setAttribute(ReadingTypeTemplateAttributeName.ARGUMENT_DENOMINATOR, null).done();
        System.out.println("Template: " + template);

        ReadingTypeTemplateAttribute argDenom = template.getAttributes()
                .stream()
                .filter(attr -> attr.getName().equals(ReadingTypeTemplateAttributeName.ARGUMENT_DENOMINATOR))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No attribute ARGUMENT_DENOMINATOR"));
        assertThat(argDenom.getCode()).isPresent();
        assertThat(argDenom.getCode().get()).isEqualTo(0);
        assertThat(getPersistedAttributes().isEmpty()).isTrue();
    }

    @Test
    @Transactional
    public void createTemplateAttributeWithNullCodeWhichSupportsWildcard() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("With wildcard");
        assertThat(ReadingTypeTemplateAttributeName.TIME.getDefinition().canBeWildcard()).isTrue()
                .as("This test need attribute which can accept wildcards");
        template.updater().setAttribute(ReadingTypeTemplateAttributeName.TIME, null).done();
        System.out.println("Template: " + template);

        ReadingTypeTemplateAttribute timeAttr = template.getAttributes()
                .stream()
                .filter(attr -> attr.getName().equals(ReadingTypeTemplateAttributeName.TIME))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No attribute TIME"));
        assertThat(timeAttr.getCode().isPresent()).isFalse();
        assertThat(getPersistedAttributes().isEmpty()).isTrue();
    }

    @Test
    @Transactional
    public void createTemplateAttributeWithCode() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("With code");
        assertThat(ReadingTypeTemplateAttributeName.TIME.getDefinition().canBeWildcard()).isTrue()
                .as("This test need attribute which can accept wildcards");
        int timeAttrCode = TimeAttribute.HOUR24.getId();
        template.updater().setAttribute(ReadingTypeTemplateAttributeName.TIME, timeAttrCode).done();
        System.out.println("Template: " + template);

        ReadingTypeTemplateAttribute timeAttr = template.getAttributes()
                .stream()
                .filter(attr -> attr.getName().equals(ReadingTypeTemplateAttributeName.TIME))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No attribute TIME"));
        assertThat(timeAttr.getCode()).isPresent();
        assertThat(timeAttr.getCode().get()).isEqualTo(timeAttrCode);
        assertThat(getPersistedAttributes()).hasSize(1);
    }

    @Test
    @Transactional
    public void createTemplateAttributeWithPossibleValues() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("With possible values");
        assertThat(ReadingTypeTemplateAttributeName.COMMODITY.getDefinition().canBeWildcard()).isFalse()
                .as("This test need attribute which can not accept wildcards");
        template.updater().setAttribute(ReadingTypeTemplateAttributeName.COMMODITY, null, Commodity.CARBON.getId(), Commodity.C2H2.getId(), Commodity.CO.getId()).done();
        System.out.println("Template: " + template);

        ReadingTypeTemplateAttribute timeAttr = template.getAttributes()
                .stream()
                .filter(attr -> attr.getName().equals(ReadingTypeTemplateAttributeName.COMMODITY))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No attribute COMMODITY"));
        assertThat(timeAttr.getCode().isPresent()).isFalse();
        assertThat(getPersistedAttributes()).hasSize(1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "code", messageId = "{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}", strict = true)
    public void createAttributeWithCodeIsNotWithinPossibleValues() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Bad time code");
        template.updater().setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE15.getId(), TimeAttribute.MINUTE1.getId()).done();
    }

    @Test
    @Transactional
    public void redefineForAttributeDoesNotIncreasePersistedAttributes() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Time");
        template.updater().setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE15.getId()).done();
        assertThat(getPersistedAttributes()).hasSize(1);
        template.updater().setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE1.getId()).done();
        assertThat(getPersistedAttributes()).hasSize(1);
    }

    @Test
    @Transactional
    public void redefineForDefaultAttributeRemovesOldPersisted() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Time");
        template.updater().setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE15.getId(), TimeAttribute.MINUTE15.getId(), TimeAttribute.MINUTE1.getId()).done();
        assertThat(getPersistedAttributes()).hasSize(1);
        template.updater().setAttribute(ReadingTypeTemplateAttributeName.TIME, null).done();
        assertThat(getPersistedAttributes()).hasSize(0);
    }

    @Test
    public void testBatchUpdate() {
        ReadingTypeTemplate template;
        // Manual transaction handling to be sure that attributes will be persisted only after the #done() call
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Batch");
            context.commit();
        }
        ReadingTypeTemplate.ReadingTypeTemplateUpdater updater = template.updater()
                .setAttribute(ReadingTypeTemplateAttributeName.MACRO_PERIOD, MacroPeriod.DAILY.getId())
                .setAttribute(ReadingTypeTemplateAttributeName.AGGREGATE, Aggregate.AVERAGE.getId())
                .setAttribute(ReadingTypeTemplateAttributeName.TIME, null) // default
                .setAttribute(ReadingTypeTemplateAttributeName.ACCUMULATION, Accumulation.DELTADELTA.getId());
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            long oldVersion = template.getVersion();
            updater.done();
            assertThat(template.getVersion()).isEqualTo(oldVersion + 1);
            assertThat(getPersistedAttributes()).hasSize(3);
            inMemoryBootstrapModule.getMeteringService().getDataModel().remove(template);
            context.commit();
        }
    }
}
