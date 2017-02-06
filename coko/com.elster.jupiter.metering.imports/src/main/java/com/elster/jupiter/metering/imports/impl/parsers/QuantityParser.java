/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.parsers;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;

import static com.elster.jupiter.properties.QuantityValueFactory.VALUE_UNIT_SEPARATOR;


public class QuantityParser implements FieldParser<Quantity> {

    BigDecimalParser bigDecimalParser;
    NumberParser numberParser;
    LiteralStringParser literalStringParser;

    public QuantityParser(BigDecimalParser bigDecimalParser, NumberParser numberParser, LiteralStringParser literalStringParser) {
        this.bigDecimalParser = bigDecimalParser;
        this.numberParser = numberParser;
        this.literalStringParser = literalStringParser;
    }

    @Override
    public Class<Quantity> getValueType() {
        return Quantity.class;
    }

    @Override
    public Quantity parse(String value) throws ValueParserException {
        String[] quantityParameters = value.split(VALUE_UNIT_SEPARATOR);
        if (quantityParameters.length == 3) {
            try {
                BigDecimal bigDecimalValue = bigDecimalParser.parse(quantityParameters[0]);
                int multiplier = numberParser.parse(quantityParameters[1]).intValue();
                String unitString = quantityParameters[2];
                try {
                    Unit.get(unitString);
                } catch (IllegalArgumentException e) {
                    throw new ValueParserException(unitString, "A, V, Wh, kg");
                }
                return Quantity.create(bigDecimalValue, multiplier, unitString);
            } catch (IllegalArgumentException e) {
                throw new ValueParserException(value, bigDecimalParser.getNumberFormat().getExample() + ":3:A");
            }
        } else {
            throw new ValueParserException(value, bigDecimalParser.getNumberFormat().getExample() + ":3:A");
        }
    }
}
