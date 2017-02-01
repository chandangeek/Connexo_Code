/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link Strings} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-06 (16:14)
 */
public class StringsTest {

    @Test
    public void testLengthOfSingleNull () {
        assertEquals(0, Strings.length(null));
    }

    @Test
    public void testLengthOfMultipleNulls () {
        assertEquals(0, Strings.length(null, null, null));
    }

    @Test
    public void testLengthOfSingleString () {
        assertEquals(4, Strings.length("data"));
    }

    @Test
    public void testLengthOfMultipleStrings () {
        assertEquals(15, Strings.length("first", "second", "last"));
    }

    @Test
    public void testLengthOfMultipleStringsIncludingNull () {
        assertEquals(9, Strings.length("first", null, "last"));
    }

}