package com.elster.protocolimpl.dlms.profile;

import com.energyict.protocol.MeterEvent;

/**
 * User: heuckeg
 * Date: 19.07.11
 * Time: 12:01
 *
 * enum of dlms events
 */
@SuppressWarnings({"unused"})
public enum DlmsEvent {

    Event1(1, 0x9001, MeterEvent.WATCHDOGRESET, "Device reset"),
    Event2(2, 0x9002, MeterEvent.EVENT_LOG_CLEARED, "metrological Event Log reset"),
    Event3(3, 0x9003, MeterEvent.EVENT_LOG_CLEARED, "Std Event Log reset"),
    Event4(4, 0x9004, MeterEvent.OTHER, "Auxiliary generic input #N status transition off-->on"),
    Event5(5, 0x9005, MeterEvent.OTHER, "Auxiliary generic input #N status transition on--> off"),
    Event8(8, 0x9008, MeterEvent.BILLING_ACTION, "Calendar new activation (starting now)"),
    Event9(9, 0x9009, MeterEvent.BILLING_ACTION, "Calendar new configuration (i.e.: download of a new calendar)"),
    Event10(10, 0x900A, MeterEvent.CLOCK_INVALID, "Clock synchronization failed"),
    Event11(11, 0x900B, MeterEvent.OTHER, "Clock synchronization non-regulatory-relevant"),
    Event12(12, 0x900C, MeterEvent.OTHER, "Clock synchronization regulatory-relevant"),
    Event13(13, 0x900D, MeterEvent.REMOTE_CONNECTION, "Communication & interaction - Local console interaction session started"),
    Event14(14, 0x900E, MeterEvent.REMOTE_DISCONNECTION, "Communication & interaction - Remote communication session started"),
    Event15(15, 0x900F, MeterEvent.CONFIGURATIONCHANGE, "Configuration of 1 single metrological parameter"),
    Event16(16, 0x9010, MeterEvent.CONFIGURATIONCHANGE, "Configuration of 1 single NON metrological parameter"),
    Event17(17, 0x9011, MeterEvent.REMOTE_CONNECTION, "Configuration settings - parameters programming session started"),
    Event18(18, 0x9012, MeterEvent.REMOTE_DISCONNECTION, "Configuration settings - successful parameters programming session closure"),
    Event19(19, 0x9013, MeterEvent.FATAL_ERROR, "Configuration settings - unsuccessful parameters programming session closure"),
    Event22(22, 0x9016, MeterEvent.HARDWARE_ERROR, "Device general failure: start"),
    Event23(23, 0x9017, MeterEvent.OTHER, "Device general failure: gone"),
    Event26(26, 0x901A, MeterEvent.OTHER, "Metrological event register full"),
    Event27(27, 0x901B, MeterEvent.OTHER, "Metrological event register >= 90% full"),
    Event30(30, 0x901E, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, "Firmware - Firmware downloaded and ready for upgrade"),
    Event31(31, 0x901F, MeterEvent.FIRMWARE_ACTIVATED, "Firmware  upgrade executed"),
    Event32(32, 0x9020, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas conversion factor above maximum programmed threshold value: start"),
    Event33(33, 0x9021, MeterEvent.LIMITER_THRESHOLD_OK, "Gas conversion factor above maximum programmed threshold value: gone"),
    Event34(34, 0x9022, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas conversion factor below minimum programmed threshold value: start"),
    Event35(35, 0x9023, MeterEvent.LIMITER_THRESHOLD_OK, "Gas conversion factor below minimum programmed threshold value: end"),
    Event36(36, 0x9024, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas flow-rate below minimum programmed threshold value (alias cut-off): start"),
    Event37(37, 0x9025, MeterEvent.LIMITER_THRESHOLD_OK, "Gas flow-rate below minimum programmed threshold value (alias cut-off): gone"),
    Event38(38, 0x9026, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas flow-rate above maximum programmed threshold value (alias Qmax): start"),
    Event39(39, 0x9027, MeterEvent.LIMITER_THRESHOLD_OK, "Gas flow-rate above maximum programmed threshold value (alias Qmax): end"),
    Event44(44, 0x902C, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas pressure application limit above maximum programmed threshold value: start"),
    Event45(45, 0x902D, MeterEvent.LIMITER_THRESHOLD_OK, "Gas pressure application limit above maximum programmed threshold value: end"),
    Event46(46, 0x902E, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas pressure application limit below minimum programmed threshold value: start"),
    Event47(47, 0x902F, MeterEvent.LIMITER_THRESHOLD_OK, "Gas pressure application limit below minimum programmed threshold value: end"),
    Event48(48, 0x9030, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas pressure physical above maximum accepted value (out of physical working range): start"),
    Event49(49, 0x9031, MeterEvent.LIMITER_THRESHOLD_OK, "Gas pressure physical above maximum accepted value (out of physical working range): end"),
    Event50(50, 0x9032, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas pressure physical  below minimum accepted value (out of physical working range): start"),
    Event51(51, 0x9033, MeterEvent.LIMITER_THRESHOLD_OK, "Gas pressure physical  below minimum accepted value (out of physical working range): end"),
    Event54(54, 0x9036, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas temperature application limit above maximum programmed threshold value: start"),
    Event55(55, 0x9037, MeterEvent.LIMITER_THRESHOLD_OK, "Gas temperature application limit above maximum programmed threshold value: end"),
    Event56(56, 0x9038, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas temperature application limit below minimum programmed threshold value: start"),
    Event57(57, 0x9039, MeterEvent.LIMITER_THRESHOLD_OK, "Gas temperature application limit below minimum programmed threshold value: end"),
    Event58(58, 0x903A, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas temperature physical above maximum accepted value (out of physical working range): start"),
    Event59(59, 0x903B, MeterEvent.LIMITER_THRESHOLD_OK, "Gas temperature physical above maximum accepted value (out of physical working range): end"),
    Event60(60, 0x903C, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Gas temperature physical below minimum accepted value (out of physical working range): start"),
    Event61(61, 0x903D, MeterEvent.LIMITER_THRESHOLD_OK, "Gas temperature physical below minimum accepted value (out of physical working range): end"),
    Event67(67, 0x9043, MeterEvent.OTHER, "Mode - Device mode (normal, not_configured, programming, maintenance) changed "),
    Event68(68, 0x9044, MeterEvent.OTHER, "Password changed"),
    Event69(69, 0x9045, MeterEvent.OTHER, "Password default restored"),
    Event72(72, 0x9048, MeterEvent.OTHER, "Power - Main power outage: start"),
    Event73(73, 0x9049, MeterEvent.OTHER, "Power - Main power outage: end"),
    Event74(74, 0x904A, MeterEvent.BATTERY_VOLTAGE_LOW, "Power - Battery level below 10%: start"),
    Event75(75, 0x904B, MeterEvent.OTHER, "Power - Battery level below 10%: end"),
    Event76(76, 0x904C, MeterEvent.VOLTAGE_SAG, "Power - AC Mains power failure (above limit) for more than 30 minutes: start"),
    Event77(77, 0x904D, MeterEvent.OTHER, "Power - AC Mains power failure (above limit) for more than 30 minutes: end"),
    Event84(84, 0x9054, MeterEvent.HARDWARE_ERROR, "Remote communication module failure "),
    Event86(86, 0x9056, MeterEvent.OTHER, "DST (daylight saving time): start"),
    Event87(87, 0x9057, MeterEvent.OTHER, "DST (daylight saving time): end"),
    Event88(88, 0x9058, MeterEvent.APPLICATION_ALERT_START, "Remote Communication - General error (push operation): start"),
    Event89(89, 0x9059, MeterEvent.APPLICATION_ALERT_STOP, "Remote Communication - General error (push operation): end"),
    Event90(90, 0x905A, MeterEvent.BILLING_ACTION, "Billing period finished - regular"),
    Event91(91, 0x905B, MeterEvent.BILLING_ACTION, "Billing period finished - change of tariff calendar"),
    Event92(92, 0x905C, MeterEvent.BILLING_ACTION, "Billing period finished - async local request"),
    Event93(93, 0x905D, MeterEvent.BILLING_ACTION, "Billing period finished - async remote request");

    private final int lisEventCode;
    private final int dlsmEventCode;
    private final int eisEventCode;
    private final String msg;

    private DlmsEvent(int dlmsEventCode, int lisEventCode, int eisEventCode, String msg) {
        this.lisEventCode = lisEventCode;
        this.dlsmEventCode = dlmsEventCode;
        this.eisEventCode = eisEventCode;
        this.msg = msg;
    }

    public int getDlmsEventCode() {
        return dlsmEventCode;
    }

    public int getLisEventCode() {
        return lisEventCode;
    }

    public int getEisEventCode() {
        return eisEventCode;
    }

    public String getMsg() {
        return msg;
    }

    public static DlmsEvent findEvent(int lisEventCode) {
        for (DlmsEvent e : DlmsEvent.values()) {
            if (e.getLisEventCode() == lisEventCode) {
                return e;
            }
        }
        return null;
    }
}
