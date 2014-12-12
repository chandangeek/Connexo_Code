package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

/**
 * @author sva
 * @since 12/06/2014 - 14:59
 */

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractBitMaskField;

public abstract class ContactorStatus<T extends AbstractBitMaskField> extends AbstractBitMaskField<T> {

    abstract public int getContactorStatusCode();

    abstract public String getContactorStatusInfo();

    public interface ContactorState {

        public abstract String getContactorInfo();

        public abstract int getContactorCode();

    }

    public enum ReconnectStatus implements ContactorState {
        NOT_NEEDED(0, "Did not close contactor, because was already with voltage"),
        SUCCESSFUL(1, "Contactor closed correctly"),
        STILL_MISSING_VOLTAGE(2, "Contactor closed, but still missing voltage"),
        CONTACTOR_OPENED(3, "Contactor shutdown, but still detecting voltage"),
        UNKNOWN(-1, "Unknown contactor state");


        private final int contactorCode;
        private final String contactorInfo;

        private ReconnectStatus(int contactorCode, String contactorInfo) {
            this.contactorCode = contactorCode;
            this.contactorInfo = contactorInfo;
        }

        public String getContactorInfo() {
            return contactorInfo;
        }

        public int getContactorCode() {
            return contactorCode;
        }

        public static ReconnectStatus fromContactorCode(int statusCode) {
            for (ReconnectStatus reconnectStatus : ReconnectStatus.values()) {
                if (reconnectStatus.getContactorCode() == statusCode) {
                    return reconnectStatus;
                }
            }
            return ReconnectStatus.UNKNOWN;
        }
    }

    public enum DisconnectStatus implements ContactorState {
        NOT_NEEDED(0, "Contactor opened, but was already without voltage"),
        SUCCESSFUL(1, "Contactor opened correctly"),
        STILL_DETECTING_VOLTAGE(2, "Contactor opened, but still detecting voltage"),
        UNKNOWN(-1, "Unknown contactor state");

        private final int contactorCode;
        private final String contactorInfo;

        private DisconnectStatus(int contactorCode, String contactorInfo) {
            this.contactorCode = contactorCode;
            this.contactorInfo = contactorInfo;
        }

        public String getContactorInfo() {
            return contactorInfo;
        }

        public int getContactorCode() {
            return contactorCode;
        }

        public static DisconnectStatus fromContactorCode(int statusCode) {
            for (DisconnectStatus disconnectStatus : DisconnectStatus.values()) {
                if (disconnectStatus.getContactorCode() == statusCode) {
                    return disconnectStatus;
                }
            }
            return DisconnectStatus.UNKNOWN;
        }
    }
}