/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.parsers;


import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DateParserTest {


    @Test
    public void testNominalCase() {
        DateParser dateParser = new DateParser("dd/MM/yyyy HH:mm", "GMT-03:00");
        ZonedDateTime parsedDateTime = dateParser.parse("28/07/2015 14:52");
        assertThat(parsedDateTime).isEqualTo(ZonedDateTime.of(2015, 7, 28, 14, 52, 0, 0, ZoneOffset.ofHours(-3)));
    }

    @Test
    public void testParseEmptyValue() {
        DateParser dateParser = new DateParser("dd/MM/yyyy HH:mm", "GMT-03:00");
        assertThat(dateParser.parse(null)).isNull();
        assertThat(dateParser.parse("")).isNull();
    }

    @Test
    public void testValueParserException() {
        DateParser dateParser = new DateParser("dd/MM/yyyy HH:mm", "GMT-03:00");
        assertThatThrownBy(() -> dateParser.parse("36/25/10055")).isInstanceOf(ValueParserException.class);
    }

    @Test
    public void testTimeChangeCase() {
        DateParser dateParser = new DateParser("dd/MM/yyyy HH:mm Z", "GMT+05:00");
        ZonedDateTime parsedDateTime1 = dateParser.parse("25/03/2018 00:00 +0100");
        Assert.assertTrue(parsedDateTime1.getHour() == 0 && parsedDateTime1.getMinute() == 0 && parsedDateTime1.getSecond() == 0 && parsedDateTime1.getNano() == 0);
        assertThat(parsedDateTime1).isEqualTo(ZonedDateTime.of(2018, 3, 25, 0, 0, 0, 0, ZoneOffset.ofHours(5)));

        ZonedDateTime parsedDateTime2 = dateParser.parse("26/03/2018 00:00 +0100");
        Assert.assertTrue(parsedDateTime2.getHour() == 0 && parsedDateTime2.getMinute() == 0 && parsedDateTime2.getSecond() == 0 && parsedDateTime2.getNano() == 0);
        assertThat(parsedDateTime2).isEqualTo(ZonedDateTime.of(2018, 3, 26, 0, 0, 0, 0, ZoneOffset.ofHours(5)));
    }
}
