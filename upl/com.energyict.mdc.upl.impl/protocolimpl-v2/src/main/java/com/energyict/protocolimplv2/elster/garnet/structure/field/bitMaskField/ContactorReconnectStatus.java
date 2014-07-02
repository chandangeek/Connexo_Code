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
    private State contactorState;

    public ContactorReconnectStatus() {
        this.contactorMask = new BitSet(LENGTH);
        this.contactorState = State.UNKNOWN;
    }

    public ContactorReconnectStatus(State contactorState) {
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
        contactorState = State.fromContactorCode(contactorStatusCode);
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
        if (!this.contactorState.equals(State.UNKNOWN)) {
            return contactorState.getContactorInfo();
        } else {
            return (contactorState.getContactorInfo() + " " + contactorStatusCode);
        }
    }

    private enum State {
        NOT_NEEDED(00, "Did not close contactor, because was already closed"),
        SUCCESSFUL(01, "Contactor closed correctly"),
        STILL_MISSING_VOLTAGE(10, "Contactor closed, but still missing voltage"),
        CONTACTOR_OPENED(11, "Contactor shutdown, but still detecting voltage"),
        UNKNOWN(-1, "Unknown contactor state");


        private final int contactorCode;
        private final String contactorInfo;

        private State(int contactorCode, String contactorInfo) {
            this.contactorCode = contactorCode;
            this.contactorInfo = contactorInfo;
        }

        public String getContactorInfo() {
            return contactorInfo;
        }

        public int getContactorCode() {
            return contactorCode;
        }

        public static State fromContactorCode(int statusCode) {
            for (State state : State.values()) {
                if (state.getContactorCode() == statusCode) {
                    return state;
                }
            }
            return State.UNKNOWN;
        }
    }
}