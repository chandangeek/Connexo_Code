/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.parsers;


import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InstantParserTest {

    @Test
    public void testNominalCase() {
        InstantParser instantParser = new InstantParser("dd/MM/yyyy HH:mm", "GMT-03:00");
        Instant parsedDateTime = instantParser.parse("28/07/2015 14:52");
        assertThat(parsedDateTime).isEqualTo(ZonedDateTime.of(2015, 7, 28, 14, 52, 0, 0, ZoneOffset.ofHours(-3))
                .toInstant());
    }

    @Test
    public void testParseEmptyValue() {
        InstantParser instantParser = new InstantParser("dd/MM/yyyy HH:mm", "GMT-03:00");
        assertThat(instantParser.parse(null)).isNull();
        assertThat(instantParser.parse("")).isNull();
    }

    @Test
    public void testValueParserException() {
        InstantParser instantParser = new InstantParser("dd/MM/yyyy HH:mm", "GMT-03:00");
        assertThatThrownBy(() -> instantParser.parse("36/25/10055")).isInstanceOf(ValueParserException.class);
    }
}
