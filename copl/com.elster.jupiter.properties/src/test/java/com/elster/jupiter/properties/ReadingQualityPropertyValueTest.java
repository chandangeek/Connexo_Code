/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ReadingQualityPropertyValueTest {

    @Test
    public void testEqual() {
        ReadingQualityPropertyValue rq1 = new ReadingQualityPropertyValue("1.1.1");
        ReadingQualityPropertyValue rq2 = new ReadingQualityPropertyValue("1.1.1");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.1");
        rq2 = new ReadingQualityPropertyValue("1.1.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.1");
        rq2 = new ReadingQualityPropertyValue("1.*.1");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.1");
        rq2 = new ReadingQualityPropertyValue("*.1.1");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.1");
        rq2 = new ReadingQualityPropertyValue("*.1.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.1");
        rq2 = new ReadingQualityPropertyValue("*.*.1");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.1");
        rq2 = new ReadingQualityPropertyValue("1.*.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.1");
        rq2 = new ReadingQualityPropertyValue("*.*.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);
    }

    @Test
    public void testNotEqual() {
        ReadingQualityPropertyValue rq1 = new ReadingQualityPropertyValue("1.1.2");
        ReadingQualityPropertyValue rq2 = new ReadingQualityPropertyValue("1.1.1");
        assertNotEquals(rq1, rq2);
        assertNotEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.2");
        rq2 = new ReadingQualityPropertyValue("1.1.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.2");
        rq2 = new ReadingQualityPropertyValue("1.*.1");
        assertNotEquals(rq1, rq2);
        assertNotEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.2");
        rq2 = new ReadingQualityPropertyValue("*.1.1");
        assertNotEquals(rq1, rq2);
        assertNotEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.2");
        rq2 = new ReadingQualityPropertyValue("*.1.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.2");
        rq2 = new ReadingQualityPropertyValue("*.*.1");
        assertNotEquals(rq1, rq2);
        assertNotEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.2");
        rq2 = new ReadingQualityPropertyValue("1.*.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.1.2");
        rq2 = new ReadingQualityPropertyValue("*.*.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);
    }

    @Test
    public void testInterestingCases() {
        ReadingQualityPropertyValue rq1 = new ReadingQualityPropertyValue("*.1.*");
        ReadingQualityPropertyValue rq2 = new ReadingQualityPropertyValue("1.4.1");
        assertNotEquals(rq1, rq2);
        assertNotEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("3.1.3");
        rq2 = new ReadingQualityPropertyValue("*.1.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("1.5.2");
        rq2 = new ReadingQualityPropertyValue("1.*.1");
        assertNotEquals(rq1, rq2);
        assertNotEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("5.1.1");
        rq2 = new ReadingQualityPropertyValue("*.1.1");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("5.1.21");
        rq2 = new ReadingQualityPropertyValue("*.1.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("5.1.212");
        rq2 = new ReadingQualityPropertyValue("*.1.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("55.1.212");
        rq2 = new ReadingQualityPropertyValue("*.1.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("55.11.212");
        rq2 = new ReadingQualityPropertyValue("*.1.*");
        assertNotEquals(rq1, rq2);
        assertNotEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("55.1.212");
        rq2 = new ReadingQualityPropertyValue("*.11.*");
        assertNotEquals(rq1, rq2);
        assertNotEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("55.*.212");
        rq2 = new ReadingQualityPropertyValue("*.11.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("55.*.212");
        rq2 = new ReadingQualityPropertyValue("*.*.*");
        assertEquals(rq1, rq2);
        assertEquals(rq2, rq1);

        rq1 = new ReadingQualityPropertyValue("55.*.212");
        rq2 = new ReadingQualityPropertyValue("5.*.*");
        assertNotEquals(rq1, rq2);
        assertNotEquals(rq2, rq1);
    }
}
