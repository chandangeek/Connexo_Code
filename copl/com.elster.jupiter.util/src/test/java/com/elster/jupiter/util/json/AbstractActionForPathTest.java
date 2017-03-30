/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AbstractActionForPathTest {

    private List<String> aPath = Arrays.asList("{", "field1");

    @Mock
    private ValueMatcher valueMatcher;
    private AbstractActionForPath actionForPath;

    @Before
    public void setUp() {
        when(valueMatcher.matches(aPath)).thenReturn(true);

        actionForPath = spy(new AbstractActionForPath(valueMatcher) {
            @Override
            protected void perform(String value) {

            }
        });
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testPerformCalledWithValue() {

        actionForPath.action(aPath, "value");

        verify(actionForPath).perform("value");
    }

    @Test
    public void testPerformNotCalledWithValueIfMatcherReturnsFalse() {
        when(valueMatcher.matches(aPath)).thenReturn(false);

        actionForPath.action(aPath, "value");

        verify(actionForPath, never()).perform("value");
    }

}
