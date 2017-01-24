package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;

import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UniqueUsagePointMRIDValidatorTest {
    private static final String ALREADY_PRESENT_MRID = "MRID";
    private static final long ALREADY_PRESENT_ID = 1;
    private static final String MESSAGE = "Bazinga!";

    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext customizableContext;
    @Mock
    private UsagePoint usagePoint, anotherUsagePoint;
    @Mock
    private MeteringService meteringService;
    @Mock
    private UniqueMRID label;
    private UniqueUsagePointMRIDValidator validator;

    @Before
    public void setUp() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(customizableContext);
        when(customizableContext.addConstraintViolation()).thenReturn(context);
        when(label.message()).thenReturn(MESSAGE);
        validator = new UniqueUsagePointMRIDValidator(meteringService);
        validator.initialize(label);
        when(meteringService.findUsagePointByMRID(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByMRID(ALREADY_PRESENT_MRID)).thenReturn(Optional.of(anotherUsagePoint));
        when(anotherUsagePoint.getId()).thenReturn(ALREADY_PRESENT_ID);
        when(anotherUsagePoint.getMRID()).thenReturn(ALREADY_PRESENT_MRID);
        when(usagePoint.getId()).thenReturn(0L);
        when(usagePoint.getMRID()).thenReturn(ALREADY_PRESENT_MRID);
    }

    @Test
    public void testValidMRID() {
        when(usagePoint.getMRID()).thenReturn("anotherMRID");
        assertThat(validator.isValid(usagePoint, context)).isTrue();
        verifyZeroInteractions(context);
    }

    @Test
    public void testMRIDConflict() {
        assertThat(validator.isValid(usagePoint, context)).isFalse();
        verifyFail();
    }

    @Test
    public void testNullUsagePoint() {
        assertThat(validator.isValid(null, context)).isTrue();
        verifyZeroInteractions(context);
    }

    @Test
    public void testFullCoincidence() {
        when(usagePoint.getId()).thenReturn(ALREADY_PRESENT_ID);
        assertThat(validator.isValid(usagePoint, context)).isTrue();
        verifyZeroInteractions(context);
    }

    private void verifyFail() {
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(MESSAGE);
        verify(builder).addPropertyNode("mRID");
        verify(customizableContext).addConstraintViolation();
    }
}
