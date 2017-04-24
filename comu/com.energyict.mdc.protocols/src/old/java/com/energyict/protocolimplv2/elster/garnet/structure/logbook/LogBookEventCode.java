/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class LogBookEventCode extends AbstractField<LogBookEventCode> {

    public static final int LENGTH = 1;

    private EventCode eventCode;

    public LogBookEventCode() {
        this.eventCode = EventCode.UNKNOWN;
    }

    public LogBookEventCode(EventCode eventCode) {
        this.eventCode = eventCode;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(eventCode.getCode(), LENGTH);
    }

    @Override
    public LogBookEventCode parse(byte[] rawData, int offset) throws ParsingException {
        int code = getIntFromBytesLE(rawData, offset, LENGTH);
        eventCode = EventCode.fromCode(code);
        if (eventCode.equals(EventCode.UNKNOWN)) {
            throw new ParsingException("Encountered invalid/unknown Logbook event code " + ProtocolTools.getHexStringFromInt(code, 1, "0x") + ".");
        }
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public EventCode getEventCode() {
        return eventCode;
    }

    public void setEventCode(EventCode eventCode) {
        this.eventCode = eventCode;
    }

    public static enum EventCode {

        SLAVE_REGISTRATION(0x34),
        SCHEDULING_CONFIGURATION(0x36),
        CUSTOMER_CONFIGURATION(0x38),
        DISPLAY_CONFIGURATION(0x3A),
        DISCONNECT_EVENT(0x3C),
        RECONNECT_EVENT(0x3E),
        CONCENTRATOR_CONFIGURATION(0x40),
        CONTACTOR_OPERATION_VIA_SERIAL(0x42),
        SENSOR_IN_ALARM(0x81),
        METER_SETUP_INCORRECT(0x82),
        AUTOMATIC_CONTACOTR_OPERATION(0x83),
        UNKNOWN(0x0);

        private final int code;

        private EventCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static EventCode fromCode(int code) {
            for (EventCode eventCode : EventCode.values()) {
                if (eventCode.getCode() == code) {
                    return eventCode;
                }
            }
            return EventCode.UNKNOWN;
        }
    }
}