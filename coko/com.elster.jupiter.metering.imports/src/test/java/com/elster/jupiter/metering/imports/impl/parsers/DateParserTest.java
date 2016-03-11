package com.elster.jupiter.metering.imports.impl.parsers;


import com.elster.jupiter.metering.imports.impl.exceptions.ValueParserException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DateParserTest {

    @Test
    public void testNominalCase() {
        DateParser dateParser = new DateParser("dd/MM/yyyy HH:mm", "GMT-03:00");
        Instant parsedDateTime = dateParser.parse("28/07/2015 14:52");
        assertThat(parsedDateTime).isEqualTo(ZonedDateTime.of(2015, 7, 28, 14, 52, 0, 0, ZoneOffset.ofHours(-3))
                .toInstant());
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
}
