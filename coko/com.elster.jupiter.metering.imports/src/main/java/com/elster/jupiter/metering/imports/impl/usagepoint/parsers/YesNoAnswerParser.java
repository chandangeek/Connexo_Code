package com.elster.jupiter.metering.imports.impl.usagepoint.parsers;

import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ValueParserException;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.YesNoAnswer;

import org.omg.CORBA.UNKNOWN;

import java.util.Arrays;

public class YesNoAnswerParser implements FieldParser<YesNoAnswer> {

    public YesNoAnswerParser() {
    }

    public YesNoAnswer parse(String value) {
        if (!Checks.is(value).emptyOrOnlyWhiteSpace()){
            return Arrays.stream(YesNoAnswer.values()).filter(e -> e.name().equals(value.trim())).findFirst()
                    .orElseThrow(() -> new ValueParserException(value, String.join(", ", YesNoAnswer.YES.name(), YesNoAnswer.NO.name(), YesNoAnswer.UNKNOWN.name())));
        } else {
            return null;
        }
    }
}