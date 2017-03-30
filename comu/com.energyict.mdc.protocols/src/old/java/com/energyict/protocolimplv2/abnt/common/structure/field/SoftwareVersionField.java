/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class SoftwareVersionField extends BcdEncodedField {

    private static final int LENGTH = 2;

    public SoftwareVersionField() {
        super(LENGTH);
    }

    public SoftwareVersionField(int length, String bcdEncodedString) {
        super(length, bcdEncodedString);
    }

    @Override
    public SoftwareVersionField parse(byte[] rawData, int offset) throws ParsingException {
        super.parse(rawData, offset);
        return this;
    }

    public String getSoftwareVersion() {
        return getText().substring(0, 2)
                .concat(".")
                .concat(getText().substring(2, 4));
    }
}