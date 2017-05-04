/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class RegisterReadResponse extends Data<RegisterReadResponse> {

    public static final int ENERGY_LENGTH = 5;
    public static final int DEMAND_LENGTH = 3;

    private Map<RegisterReadFields, AbstractField> valueMap;

    public RegisterReadResponse(TimeZone timeZone) {
        super(ResponseFrame.RESPONSE_DATA_LENGTH, timeZone);
        this.valueMap = new HashMap<>();
    }

    @Override
    public RegisterReadResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;
        super.parse(rawData, ptr);
        for (RegisterReadFields registerReadField : RegisterReadFields.values()) {
            AbstractField field = registerReadField.getFieldParser().parse(rawData, ptr);
            valueMap.put(registerReadField, field);
            ptr += field.getLength();
        }
        return this;
    }

    protected Map<RegisterReadFields, AbstractField> getValueMap() {
        return valueMap;
    }

    /**
     * Getter for the {@link AbstractField} corresponding to the given {@link RegisterReadFields} key.
     *
     * @param field the RegisterReadFields key
     * @return the AbstractField, or null if the field could not be found
     */

    public AbstractField getField(RegisterReadFields field) {
        return getValueMap().get(field);
    }
}