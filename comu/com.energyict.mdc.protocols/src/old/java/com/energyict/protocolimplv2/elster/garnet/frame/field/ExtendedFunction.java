/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.frame.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class ExtendedFunction extends AbstractField<ExtendedFunction> {

    public static int EXTENDED_COMMAND_CODE = 0x10;
    public static final int LENGTH = 1;


    public ExtendedFunction() {
    }

    @Override
    public byte[] getBytes() {
        return new byte[]{(byte) EXTENDED_COMMAND_CODE};
    }

    @Override
    public ExtendedFunction parse(byte[] rawData, int offset) throws ParsingException {
        int code = getIntFromBytesLE(rawData, offset, LENGTH);

        if (code != EXTENDED_COMMAND_CODE) {
            throw new ParsingException("Invalid Extended command code " + code + ", while " + EXTENDED_COMMAND_CODE + " was expected.");
        }
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }
}