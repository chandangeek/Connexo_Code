/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.parsers;

import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import static com.elster.jupiter.util.Checks.is;

public class BigDecimalParser implements FieldParser<BigDecimal> {

    private SupportedNumberFormat numberFormat;

    public BigDecimalParser(SupportedNumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public BigDecimal parse(String value) throws ValueParserException {
        if (is(value).emptyOrOnlyWhiteSpace()) {
            throw new ValueParserException(value, numberFormat.getExample());
        }
        return parseNonEmptyBigDecimalString(value);
    }

    private BigDecimal parseNonEmptyBigDecimalString(String value) throws ValueParserException {
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