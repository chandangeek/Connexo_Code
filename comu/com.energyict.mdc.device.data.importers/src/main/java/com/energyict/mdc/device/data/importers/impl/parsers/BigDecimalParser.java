package com.energyict.mdc.device.data.importers.impl.parsers;

import com.energyict.mdc.device.data.importers.impl.exceptions.ParserException;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

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

    public BigDecimal parse(String value) throws ParserException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return parseNonEmptyBigDecimalString(value);
    }

    public BigDecimal parseNonEmptyBigDecimalString(String value) throws ParserException {
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
            throw new ParserException(ParserException.Type.INVALID_DATE_FORMAT, numberFormat.getExample(), value);
        }
    }
}
