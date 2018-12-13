/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public class ActionBasedJsonParserTest {

    private ActionBasedJsonParser parser;

    @Mock
    private ActionForPath actionForPath1, actionForPath2;

    @Before
    public void setUp() {
        parser = new ActionBasedJsonParser(actionForPath1, actionForPath2);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCorrectPaths() throws IOException {
        String json = "{\"field1\":\"value\", \"field2\": [ \"firstValue\", \"secondValue\"], \"field3\": { \"subField\": 2 }}";

        parser.parse(new StringReader(json));

        InOrder inOrder = inOrder(actionForPath1);
        inOrder.verify(actionForPath1).action(Arrays.asList("{", "field1"), "value");
        inOrder.verify(actionForPath1).action(Arrays.asList("{", "field2", "["), "firstValue");
        inOrder.verify(actionForPath1).action(Arrays.asList("{", "field2", "["), "secondValue");
        inOrder.verify(actionForPath1).action(Arrays.asList("{", "field3", "{", "subField"), "2");

        inOrder = inOrder(actionForPath2);
        inOrder.verify(actionForPath2).action(Arrays.asList("{", "field1"), "value");
        inOrder.verify(actionForPath2).action(Arrays.asList("{", "field2", "["), "firstValue");
        inOrder.verify(actionForPath2).action(Arrays.asList("{", "field2", "["), "secondValue");
        inOrder.verify(actionForPath2).action(Arrays.asList("{", "field3", "{", "subField"), "2");
    }


    public interface B {
        void method(List<String> strings, String name);
    }

    @Test
    public void test() {

        B b = Mockito.mock(B.class);

        List<String> strings = new ArrayList<>();
        strings.add("A");
        strings.add("B");

        b.method(strings, "C");

        InOrder inOrder = inOrder(b);
        inOrder.verify(b).method(Arrays.asList("A", "B"), "C");

    }

}
