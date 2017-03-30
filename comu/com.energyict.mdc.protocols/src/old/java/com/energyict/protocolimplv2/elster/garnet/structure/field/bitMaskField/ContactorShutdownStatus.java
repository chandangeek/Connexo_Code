/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class ContactorShutdownStatus extends ContactorStatus<ContactorShutdownStatus> {

    public static final int LENGTH = 2; // The length expressed in nr of bits

    private BitSet contactorMask;
    private int contactorStatusCode;
    private DisconnectStatus contactorState;

    public ContactorShutdownStatus() {
        this.contactorMask = new BitSet(LENGTH);
        this.contactorState = DisconnectStatus.UNKNOWN;
    }

    public ContactorShutdownStatus(DisconnectStatus contactorState) {
        this.contactorState = contactorState;
    }

    public BitSet getBitMask() {
        return contactorMask;
    }

    @Override
    public ContactorShutdownStatus parse(BitSet bitSet, int posInMask) throws ParsingException {
        int startPos = posInMask * LENGTH;
        contactorMask = bitSet.get(startPos, startPos + LENGTH);
        contactorStatusCode = convertBitSetToInt(contactorMask);
        contactorState = DisconnectStatus.fromContactorCode(contactorStatusCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getContactorStatusCode() {
        return contactorStatusCode;
    }

    public String getContactorStatusInfo() {
        if (!this.contactorState.equals(DisconnectStatus.UNKNOWN)) {
            return contactorState.getContactorInfo();
        } else {
            return (contactorState.getContactorInfo() + " " + contactorStatusCode);
        }
    }
}