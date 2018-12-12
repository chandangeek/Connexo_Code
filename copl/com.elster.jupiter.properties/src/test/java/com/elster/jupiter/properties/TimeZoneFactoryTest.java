/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import org.junit.Before;
import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * @author sva
 * @since 5/02/2016 - 14:12
 */
public class TimeZoneFactoryTest {

    private AbstractValueFactory<TimeZone> timeZoneValueFactory;

    @Before
    public void setUp() throws Exception {
        timeZoneValueFactory = new TimeZoneFactory();
    }

    @Test
    public void testFromStringValue() throws Exception {
        TimeZone validTimeZone = timeZoneValueFactory.fromStringValue("Europe/Brussels");
        TimeZone invalidTimeZone = timeZoneValueFactory.fromStringValue("Invalid");

        assertEquals("Europe/Brussels", validTimeZone.getID());
        assertEquals(TimeZoneFactory.InvalidTimeZone.class, invalidTimeZone.getClass());
        assertEquals("Invalid", invalidTimeZone.getID());
    }

    @Test
    public void testToStringValue() throws Exception {
        TimeZone validTimeZone = TimeZone.getTimeZone("Europe/Brussels");
        TimeZone invalidTimeZone = timeZoneValueFactory.fromStringValue("Invalid");

        assertEquals("Europe/Brussels", timeZoneValueFactory.toStringValue(validTimeZone));
        assertEquals("Invalid", timeZoneValueFactory.toStringValue(invalidTimeZone));
    }
}