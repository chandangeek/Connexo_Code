/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DateTimeFormatGeneratorTest {

    @Test
    public void testOne() {
        String dateTimeFormat = DateTimeFormatGenerator.getDateTimeFormat("dd MMM ''yy", "HH:mm", "DT", "SPACE");
        assertEquals(dateTimeFormat, "dd MMM ''yy HH:mm");
    }

    @Test
    public void testTwo() {
        String dateTimeFormat = DateTimeFormatGenerator.getDateTimeFormat("dd MMM ''yy", "HH:mm", "TD", "SPACE");
        assertEquals(dateTimeFormat, "HH:mm dd MMM ''yy");
    }

    @Test
    public void testThree() {
        String dateTimeFormat = DateTimeFormatGenerator.getDateTimeFormat("EEE dd MMM ''yy", "HH:mm", "DT", "-");
        assertEquals(dateTimeFormat, "EEE dd MMM ''yy - HH:mm");
    }

    @Test
    public void testFour() {
        String dateTimeFormat = DateTimeFormatGenerator.getDateTimeFormat("dd MMM ''yy", "HH:mm:ss", "TD", "-");
        assertEquals(dateTimeFormat, "HH:mm:ss - dd MMM ''yy");
    }

    @Test
    public void testFive() {
        String dateTimeFormat = DateTimeFormatGenerator.getDateTimeFormat("dd MMM ''yy", "HH:mm:ss", "TD", "#@#");
        assertEquals(dateTimeFormat, "HH:mm:ss #@# dd MMM ''yy");
    }

}
