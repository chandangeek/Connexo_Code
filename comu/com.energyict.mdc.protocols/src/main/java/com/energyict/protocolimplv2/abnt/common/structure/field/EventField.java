package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 2/09/2014 - 13:20
 */
public class EventField extends AbstractField<EventField> {

    public static final int LENGTH = 1;

    private int eventCode;
    private Event event;

    public EventField() {
        this.event = Event.UNKNOWN;
    }

    public EventField(Event event) {
        this.event = event;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(eventCode, LENGTH);
    }

    @Override
    public EventField parse(byte[] rawData, int offset) throws ParsingException {
        eventCode = Integer.parseInt(getHexStringFromBCD(rawData, offset, LENGTH));
        event = Event.fromEventCode(eventCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getEventCode() {
        return eventCode;
    }

    public String getEventMessage() {
        if (!this.event.equals(Event.UNKNOWN)) {
            return event.getEventMessage();
        } else {
            return (event.getEventMessage() + " " + eventCode);
        }
    }

    public Event getEvent() {
        return event;
    }

    public enum Event {
        COMMAND_11(11, "Request for an opening communication session with password"),
        COMMAND_12(12, "Password programming"),
        COMMAND_13(13, "String request for password calculation"),
        COMMAND_14(14, "Instantaneous quantities reading"),
        COMMAND_20(20, "Parameters Reading with demand response"),
        COMMAND_21(21, "Parameters Reading without current demand response"),
        COMMAND_22(22, "Parameters Reading without previous demand response"),
        COMMAND_23(23, "Registers Reading of visible channels after last demand response"),
        COMMAND_24(24, "Registers Reading of visible channels relatives to the last demand response"),
        COMMAND_25(25, "Reading of outages periods"),
        COMMAND_26(26, "Reading of load profile counters since the last demand response"),
        COMMAND_27(27, "Reading of the load profile counters before the last demand response"),
        COMMAND_28(28, "Reading of the modification registers"),
        COMMAND_29(29, "Date modification"),
        COMMAND_30(30, "Hour modification"),
        COMMAND_31(31, "Demand interval modification"),
        COMMAND_32(32, "National holidays modification"),
        COMMAND_33(33, "Multiplication constants modification"),
        COMMAND_34(34, "Seasonal periods modification"),
        COMMAND_35(35, "Time segments modification"),
        COMMAND_36(36, "Reserved time modification"),
        COMMAND_37(37, "Occurrence condition modification on the meter"),
        COMMAND_38(38, "Meter Initialization"),
        COMMAND_39(39, "Command information not implemented"),
        COMMAND_40(40, "Occurrence information in the meter"),
        COMMAND_41(41, "Previous partials registers reading of 1º visible channel"),
        COMMAND_42(42, "Previous partials registers reading of 2º visible channel"),
        COMMAND_43(43, "Previous partials registers Reading of 3º visible channel"),
        COMMAND_44(44, "Current partials registers Reading of 1º visible channel"),
        COMMAND_45(45, "Current partials registers Reading of 2º visible channel"),
        COMMAND_46(46, "Current partials registers Reading of 3º visible channel"),
        COMMAND_47(47, "Modification of the form of maximum demand calculation"),
        COMMAND_51(51, "Parameter Reading without demand response, with load profile reading"),
        COMMAND_52(52, "Load profile counters reading"),
        COMMAND_53(53, "Program load initialization"),
        COMMAND_54(54, "Program transfer"),
        COMMAND_55(55, "Program load completion"),
        COMMAND_56(56, "Modification of the condition for the visualization of the demands on peak-time (obsolete)"),
        COMMAND_59(59, "Modification of the condition for the visualization of the additional codes of 2º visible channel"),
        COMMAND_63(63, "Modification of the condition of automatic demand response"),
        COMMAND_64(64, "Modification of DST"),
        COMMAND_65(65, "Modification of the set of 2 time segments"),
        COMMAND_66(66, "Modification of the channel quantities"),
        COMMAND_67(67, "Modification of the reactive tariffs"),
        COMMAND_68(68, "Modification of the clock base time"),
        COMMAND_70(70, "Reading of the available commands table on Meter/Meter (Obsolete)"),
        COMMAND_73(73, "Modification on load profile interval"),
        COMMAND_74(74, "Modification on the type of reversion of the pulses"),
        COMMAND_75(75, "Modification on the presentation time of the quantities in the display"),
        COMMAND_76(76, "Modification on the condition of division by 100 of the quantities in the display"),
        COMMAND_77(77, "Modification on the time segments for Saturdays, Sundays and holidays"),
        COMMAND_78(78, "Modification on composed time"),
        COMMAND_79(79, "Modification on the visualization condition of the codes in the display"),
        COMMAND_80(80, "Metering parameters reading"),
        COMMAND_81(81, "Modification on the condition of the extended customer serial"),
        COMMAND_84(84, "Modification on the QTD and DTD parameters"),
        COMMAND_87(87, "Modification or Reading of installation code"),
        COMMAND_88(88, "Modification or Reading of losses compensation parameters"),
        COMMAND_90(90, "Modification on the presentation mode of the quantities in the display"),
        COMMAND_92(92, "Modification on the universal time post"),
        COMMAND_93(93, "Modification on the constants ke, kh, tp and tc and of the register mode (Obsolete)"),
        COMMAND_95(95, "Generic Modification and Reading of parameters"),
        COMMAND_98(98, "Extended command"),
        COMMAND_99(99, "Command reserved for fast load of operational program"),
        UNKNOWN(-1, "Unknown event code");

        private final int eventCode;
        private final String eventMessage;

        private Event(int eventCode, String eventMessage) {
            this.eventCode = eventCode;
            this.eventMessage = eventMessage;
        }

        public int getEventCode() {
            return eventCode;
        }

        public String getEventMessage() {
            return eventMessage;
        }

        public static Event fromEventCode(int eventCode) {
            for (Event event : Event.values()) {
                if (event.getEventCode() == eventCode) {
                    return event;
                }
            }
            return Event.UNKNOWN;
        }
    }
}