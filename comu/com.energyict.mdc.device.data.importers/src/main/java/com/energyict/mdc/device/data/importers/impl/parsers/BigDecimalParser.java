/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.parsers;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.elster.jupiter.util.Checks.is;

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
        if (is(value).emptyOrOnlyWhiteSpace()) {
            throw new ValueParserException(value, numberFormat.getExample());
        }
        return parseNonEmptyBigDecimalString(value);
    }

    private BigDecimal parseNonEmptyBigDecimalString(String value) throws ValueParserException {
        if (value.split("\\" + numberFormat.getDecimalSeparator().toString()).length > 2
                || !numberFormat.hasGroupSeparator() && Pattern.compile("[^-0-9" + numberFormat.getDecimalSeparator() + " ]").matcher(value).find()) {
            throw new ValueParserException(value, numberFormat.getExample());
        }
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
