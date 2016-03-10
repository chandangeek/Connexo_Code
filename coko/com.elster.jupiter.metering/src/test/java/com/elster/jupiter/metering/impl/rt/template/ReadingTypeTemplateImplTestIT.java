package com.elster.jupiter.metering.impl.rt.template;

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
        String name = "Wildcard -";
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate(name);
        template.setAttribute(ReadingTypeTemplateAttributeName.ARGUMENT_DENOMINATOR, null);
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
        String name = "Wildcard +";
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate(name);
        template.setAttribute(ReadingTypeTemplateAttributeName.TIME, null);
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
        String name = "Wildcard +";
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate(name);
        int timeAttrCode = TimeAttribute.HOUR24.getId();
        template.setAttribute(ReadingTypeTemplateAttributeName.TIME, timeAttrCode);
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
    @ExpectedConstraintViolation(property = "code", messageId = "{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}", strict = true)
    public void createAttributeWithBadTimeCode() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Bad time code");
        template.setAttribute(ReadingTypeTemplateAttributeName.TIME, 1024);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "values[0]", messageId = "{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}", strict = true)
    public void createAttributeWithBadPossibleTimeValue() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Bad time possible value");
        template.setAttribute(ReadingTypeTemplateAttributeName.TIME, null, 1024);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "code", messageId = "{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}", strict = true)
    public void createAttributeWithCodeIsNotWithinPossibleValues() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Bad time code");
        template.setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE15.getId(), TimeAttribute.MINUTE1.getId());
    }

    @Test
    @Transactional
    public void redefineForAttributeDoesNotIncreasePersistedAttributes() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Time");
        template.setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE15.getId());
        assertThat(getPersistedAttributes()).hasSize(1);
        template.setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE1.getId());
        assertThat(getPersistedAttributes()).hasSize(1);
    }

    @Test
    @Transactional
    public void redefineForDefaultAttributeRemovesOldPersisted() {
        ReadingTypeTemplate template = inMemoryBootstrapModule.getMeteringService().createReadingTypeTemplate("Time");
        template.setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE15.getId());
        assertThat(getPersistedAttributes()).hasSize(1);
        template.setAttribute(ReadingTypeTemplateAttributeName.TIME, null);
        assertThat(getPersistedAttributes()).hasSize(0);
    }
}
