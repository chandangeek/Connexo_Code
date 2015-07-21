package com.energyict.mdc.device.data.importers.parsers;

import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;
import com.energyict.mdc.device.data.importers.impl.parsers.BigDecimalParser;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class BigDecimalParserTest {

    BigDecimalParser parserFormat1 = new BigDecimalParser(SupportedNumberFormat.FORMAT1);
    BigDecimalParser parserFormat2 = new BigDecimalParser(SupportedNumberFormat.FORMAT2);
    BigDecimalParser parserFormat3 = new BigDecimalParser(SupportedNumberFormat.FORMAT3);
    BigDecimalParser parserFormat4 = new BigDecimalParser(SupportedNumberFormat.FORMAT4);

    @Test
    public void testNominalCase() {
        BigDecimal value;

        value = parserFormat1.parse("123,456,789.012");
        assertThat(value).isEqualTo(BigDecimal.valueOf(123456789.012));

        value = parserFormat2.parse("123.456.789,012");
        assertThat(value).isEqualTo(BigDecimal.valueOf(123456789.012));

        value = parserFormat3.parse("123456789.012");
        assertThat(value).isEqualTo(BigDecimal.valueOf(123456789.012));

        value = parserFormat4.parse("123456789,012");
        assertThat(value).isEqualTo(BigDecimal.valueOf(123456789.012));
    }

    @Test
    public void testParseNumberWithWhiteSpaces() {
        BigDecimal value = parserFormat4.parse("123 456 789,012");
        assertThat(value).isEqualTo(BigDecimal.valueOf(123456789.012));
    }

    @Test
    public void testParseEmptyString() {
        BigDecimal value = parserFormat1.parse(null);
        assertThat(value).isNull();
        value = parserFormat1.parse("");
        assertThat(value).isNull();
    }

    @Test
    public void testTrimWhiteSpaces() {
        BigDecimal value = parserFormat1.parse("    123,456,789.012    ");
        assertThat(value).isEqualTo(BigDecimal.valueOf(123456789.012));
    }

    @Test(expected = ValueParserException.class)
    public void testParsedException() {
        parserFormat1.parse("text");
    }
}
