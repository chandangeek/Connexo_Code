package com.elster.jupiter.metering.imports.impl.usagepoint.parsers;

import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ValueParserException;
import com.elster.jupiter.metering.imports.impl.usagepoint.properties.SupportedNumberFormat;
import com.elster.jupiter.util.Checks;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class BigDecimalParser implements FieldParser<BigDecimal> {

    private SupportedNumberFormat numberFormat;

    public BigDecimalParser(SupportedNumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public BigDecimal parse(String value) throws ValueParserException {
        if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
            throw new ValueParserException(value, numberFormat.getExample());
        }
        return parseNonEmptyBigDecimalString(value);
    }

    public BigDecimal parseNonEmptyBigDecimalString(String value) throws ValueParserException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setDecimalSeparator(numberFormat.getDecimalSeparator());
        if (numberFormat.getGroupSeparator() != null) {
            symbols.setGroupingSeparator(numberFormat.getGroupSeparator());
        }
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setDecimalFormatSymbols(symbols);
        decimalFormat.setParseBigDecimal(true);
        try {
            return (BigDecimal) decimalFormat.parse(value.replace(" ", ""));
        } catch (ParseException e) {
            throw new ValueParserException(value, numberFormat.getExample());
        }
    }
}
