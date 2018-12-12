package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class ContactorReconnectStatus extends ContactorStatus<ContactorReconnectStatus> {

    public static final int LENGTH = 2; // The length expressed in nr of bits

    private BitSet contactorMask;
    private int contactorStatusCode;
    private ReconnectStatus contactorState;

    public ContactorReconnectStatus() {
        this.contactorMask = new BitSet(LENGTH);
        this.contactorState = ReconnectStatus.UNKNOWN;
    }

    public ContactorReconnectStatus(ReconnectStatus contactorState) {
        this.contactorState = contactorState;
    }

    public BitSet getBitMask() {
        return contactorMask;
    }

    @Override
    public ContactorReconnectStatus parse(BitSet bitSet, int posInMask) throws ParsingException {
        int startPos = posInMask * LENGTH;
        contactorMask = bitSet.get(startPos, startPos + LENGTH);
        contactorStatusCode = convertBitSetToInt(contactorMask);
        contactorState = ReconnectStatus.fromContactorCode(contactorStatusCode);
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
        if (!this.contactorState.equals(ReconnectStatus.UNKNOWN)) {
            return contactorState.getContactorInfo();
        } else {
            return (contactorState.getContactorInfo() + " " + contactorStatusCode);
        }
    }
}