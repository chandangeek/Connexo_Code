/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import javax.validation.ConstraintValidatorContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/5/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TraceFileNameValidatorTest {
    @Test
    public void testSimpleCase() throws Exception {
        boolean valid = isValidTraceFile("tracefile", true);
        assertThat(valid).isTrue();
    }

    @Test
    public void testUpDir() throws Exception {
        boolean valid = isValidTraceFile("../tracefile", true);
        assertThat(valid).isFalse();
    }

    @Test
    public void testNoTracing() throws Exception {
        boolean valid = isValidTraceFile("../tracefile", false);
        assertThat(valid).isTrue();
    }

    @Test
    public void testEmptyTracing() throws Exception {
        boolean valid = isValidTraceFile("", true);
        assertThat(valid).isFalse();
    }

    @Test
    public void testWhitespaceTracing() throws Exception {
        boolean valid = isValidTraceFile("     ", true);
        assertThat(valid).isFalse();
    }

    @Test
    public void testTraceFileSubDir() throws Exception {
        boolean valid = isValidTraceFile("dir/tracefile", true);
        assertThat(valid).isFalse();
    }

    private boolean isValidTraceFile(String tracefile, boolean traceing) {
        TraceFileNameValidator validator = new TraceFileNameValidator();
        EndPointConfigurationImpl mock = mock(EndPointConfigurationImpl.class);
        when(mock.isTracing()).thenReturn(traceing);
        when(mock.getTraceFile()).thenReturn(tracefile);
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderCustomizableContext = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        when(nodeBuilderCustomizableContext.addConstraintViolation()).thenReturn(constraintValidatorContext);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        return validator.isValid(mock, constraintValidatorContext);
    }
}
