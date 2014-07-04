package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidReadingTypeValidatorTest {

    @Mock
    private MeteringService meteringService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;
    @Mock
    private ReadingType readingType;

    @Test
    public void testIsValid() throws Exception {
        when(meteringService.getReadingType("valid")).thenReturn(Optional.of(readingType));

        assertThat(new ValidReadingTypeValidator(meteringService).isValid("valid", context)).isTrue();
    }

    @Test
    public void testInvalid() throws Exception {
        when(meteringService.getReadingType("valid")).thenReturn(Optional.<ReadingType>absent());

        assertThat(new ValidReadingTypeValidator(meteringService).isValid("valid", context)).isFalse();
    }

}