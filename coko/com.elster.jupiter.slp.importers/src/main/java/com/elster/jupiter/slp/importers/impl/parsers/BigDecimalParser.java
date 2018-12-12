/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.parsers;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;
import com.elster.jupiter.slp.importers.impl.properties.SupportedNumberFormat;
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

    @Override
    public Class<BigDecimal> getValueType() {
        return BigDecimal.class;
    }

    public BigDecimal parse(String value) throws ValueParserException {
        if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return parseNonEmptyBigDecimalString(value);
    }

    public SupportedNumberFormat getNumberFormat() {
        return numberFormat;
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
            throw new ValueParserException(value, getNumberFormat().getExample());
        }
    }
}
