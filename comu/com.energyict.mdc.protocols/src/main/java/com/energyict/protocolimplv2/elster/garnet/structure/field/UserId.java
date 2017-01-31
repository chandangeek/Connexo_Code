/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class UserId extends AbstractField<UserId> {

    public static final int LENGTH = 4;

    private int userId;

    public UserId() {
        this.userId = 0;
    }

    public UserId(int userId) {
        this.userId = userId;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(userId, LENGTH);
    }

    @Override
    public UserId parse(byte[] rawData, int offset) throws ParsingException {
        userId = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getUserId() {
        return userId;
    }
}