package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class ContactorMode extends AbstractField<ContactorMode> {

    public static final int LENGTH = 1;

    private int contactorModeCode;
    private Mode mode;

    public ContactorMode() {
        this.mode = Mode.UNKNOWN;
        this.contactorModeCode = 0;
    }

    public ContactorMode(Mode mode) {
        this.mode = mode;
        this.contactorModeCode = mode.getContactorMode();
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(contactorModeCode, LENGTH);
    }

    @Override
    public ContactorMode parse(byte[] rawData, int offset) throws ParsingException {
        contactorModeCode = getIntFromBytesLE(rawData, offset, LENGTH);
        mode = Mode.fromCode(contactorModeCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getContactorModeCode() {
        return contactorModeCode;
    }

    public String getFunctionInfo() {
        if (!this.mode.equals(Mode.UNKNOWN)) {
            return mode.getFunctionDescription();
        } else {
            return (mode.getFunctionDescription() + " " + contactorModeCode);
        }
    }

    public enum Mode {

        DISCONNECT(0x00, "Disconnect"),
        RECONNECT(0x01, "Reconnect"),
        UNKNOWN(0xFF, "Unknown contactor mode");

        private final int contactorMode;

        private final String contactorModeDescription;

        private Mode(int contactorMode, String contactorModeDescription) {
            this.contactorMode = contactorMode;
            this.contactorModeDescription = contactorModeDescription;
        }

        public int getContactorMode() {
            return contactorMode;
        }

        public String getFunctionDescription() {
            return contactorModeDescription;
        }

        public static Mode fromCode(int code) {
            for (Mode mode : Mode.values()) {
                if (mode.getContactorMode() == code) {
                    return mode;
                }
            }
            return Mode.UNKNOWN;
        }
    }
}