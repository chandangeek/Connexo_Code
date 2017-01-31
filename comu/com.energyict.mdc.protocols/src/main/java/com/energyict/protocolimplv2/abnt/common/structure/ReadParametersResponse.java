/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ReadParametersResponse extends Data<ReadParametersResponse> {

    public static final Integer[] NULL_DATA_LENGTHS = new Integer[]{20, 3, 1, 32, 5, 5, 25, 1, 5, 5, 1};
    public static final int DATE_TIME_LENGTH = 6;
    public static final int NUMBER_OF_LOAD_PROFILE_WORDS_LENGTH = 3;
    public static final int NUMERATOR_LENGTH = 3;
    public static final int METER_MODEL_LENGTH = 2;
    public static SimpleDateFormat dateFormatter = new SimpleDateFormat("HHmmssddMMyy");

    private Map<ReadParameterFields, AbstractField> valueMap;

    public ReadParametersResponse(TimeZone timeZone) {
        super(ResponseFrame.RESPONSE_DATA_LENGTH, timeZone);
        this.valueMap = new HashMap<>();
    }

    @Override
    public ReadParametersResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;
        super.parse(rawData, ptr);
        for (ReadParameterFields parameterField : ReadParameterFields.values()) {
            AbstractField field = parameterField.getFieldParser().parse(rawData, ptr);
            valueMap.put(parameterField, field);
            ptr += field.getLength();
        }
        return this;
    }

    private Map<ReadParameterFields, AbstractField> getValueMap() {
        return valueMap;
    }

    /**
     * Getter for the {@link AbstractField} corresponding to the given {@link ReadParameterFields} key.
     *
     * @param field the ReadParameterFields key
     * @return the AbstractField, or null if the field could not be found
     */

    public AbstractField getField(ReadParameterFields field) {
        return getValueMap().get(field);
    }
}