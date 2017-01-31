/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.parsers;

import com.elster.jupiter.metering.imports.impl.FieldParser;
import com.elster.jupiter.metering.imports.impl.exceptions.ValueParserException;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.YesNoAnswer;

import java.util.Arrays;
import java.util.stream.Collectors;

public class YesNoAnswerParser implements FieldParser<YesNoAnswer> {

    public YesNoAnswerParser() {
    }

    @Override
    public Class<YesNoAnswer> getValueType() {
        return YesNoAnswer.class;
    }

    public YesNoAnswer parse(String value) {
        return Checks.is(value).emptyOrOnlyWhiteSpace() ? null : Arrays.stream(YesNoAnswer.values())
                .filter(e -> e.toString().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new ValueParserException(value,
                        Arrays.stream(YesNoAnswer.values()).map(YesNoAnswer::toString).collect(Collectors.joining(", "))));
    }
}