/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.Random;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class SessionKeyPart extends AbstractField<SessionKeyPart> {

    public static final int LENGTH = 8;
    private static final Random RANDOM = new Random();

    private byte[] partOfSessionKey;

    public SessionKeyPart() {
        this.partOfSessionKey = new byte[LENGTH];
    }

    public SessionKeyPart(byte[] partOfSessionKey) {
        this.partOfSessionKey = partOfSessionKey;
    }

    @Override
    public byte[] getBytes() {
        return partOfSessionKey;
    }

    @Override
    public SessionKeyPart parse(byte[] rawData, int offset) throws ParsingException {
        partOfSessionKey = ProtocolTools.getSubArray(rawData, offset, offset + LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public void generateRandomHalfOfSessionKey() {
        RANDOM.nextBytes(partOfSessionKey);
    }

    public void setPartOfSessionKey(byte[] partOfSessionKey) {
        this.partOfSessionKey = partOfSessionKey;
    }
}