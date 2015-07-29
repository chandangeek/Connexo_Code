package com.energyict.mdc.device.data.importers.impl.parsers;

import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThatThrownBy(() -> parserFormat1.parse(null)).isInstanceOf(ValueParserException.class);
        assertThatThrownBy(() -> parserFormat1.parse("")).isInstanceOf(ValueParserException.class);
    }

    @Test
    public void testTrimWhiteSpaces() {
        BigDecimal value = parserFormat1.parse("    123,456,789.012    ");
        assertThat(value).isEqualTo(BigDecimal.valueOf(123456789.012));
    }

    @Test
    public void testParsedException() {
        assertThatThrownBy(() -> parserFormat1.parse("text")).isInstanceOf(ValueParserException.class);
    }
}
