/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.cewe.prometer;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers 0040 up to 0064 contain the 25 most recent errors.  This class
 * contains all the errorcodes that can occur.
 * <p/>
 * These errors are read out and stored as the meter logbook.
 *
 * @author fbo
 */

class ErrorMessage {

    private final int id;
    private final int meterEvent;
    private final String description;

    private static final Map<Integer, ErrorMessage> MESSAGES = new HashMap<Integer, ErrorMessage>();

    private static void put(int id, int meterEvent, String description) {
        ErrorMessage eMsg = new ErrorMessage(id, meterEvent, description);
        MESSAGES.put(new Integer(id), eMsg);
    }

    static {
        put(1, MeterEvent.OTHER, "Only for internal use");
        put(2, MeterEvent.OTHER, "Only for internal use");
        put(3, MeterEvent.OTHER, "Only for internal use");
        put(4, MeterEvent.OTHER, "Only for internal use");
        put(5, MeterEvent.OTHER, "Only for internal use");
        put(6, MeterEvent.OTHER, "Only for internal use");
        put(7, MeterEvent.OTHER, "Only for internal use");
        put(8, MeterEvent.OTHER, "Only for internal use");
        put(9, MeterEvent.OTHER, "Only for internal use");
        put(10, MeterEvent.VOLTAGE_SAG, "R_Phase has dropped below 80% of nom. voltage.");
        put(11, MeterEvent.VOLTAGE_SAG, "S_Phase has dropped below 80% of nom. voltage.");
        put(12, MeterEvent.VOLTAGE_SAG, "T_Phase has dropped below 80% of nom. voltage.");
        put(13, MeterEvent.POWERDOWN, "Power fail.");
        put(14, MeterEvent.WATCHDOGRESET, "Bus_exception. Reset by watchdog");
        put(15, MeterEvent.OTHER, "Calibration Led rate too high, disabled");
        put(16, MeterEvent.OTHER, "Real_Time_Clock initialised after stop (low battery?)");
        put(17, MeterEvent.OTHER, "Too early Demand_Reset_Pulse through input");
        put(18, MeterEvent.OTHER, "Too early End_Of_Billing_Pulse through input");
        put(19, MeterEvent.OTHER, "Terminal cover has been closed.");
        put(20, MeterEvent.OTHER, "Terminal cover has been opened.");
        put(21, MeterEvent.OTHER, "Terminal cover sensor has been illuminated.");
        put(22, MeterEvent.HARDWARE_ERROR, "Illegal end of log_file.");
        put(23, MeterEvent.OTHER, "Brown_out");
        put(24, MeterEvent.HARDWARE_ERROR, "Checksum error in even EPROM.");
        put(25, MeterEvent.HARDWARE_ERROR, "Checksum error in odd EPROM.");
        put(26, MeterEvent.HARDWARE_ERROR, "AD_converter failed");
        put(27, MeterEvent.HARDWARE_ERROR, "Factory-option RS232 is disabled due to bad UART");
        put(28, MeterEvent.OTHER, "Multiple failed FLAG-passwords, opto_port");
        put(29, MeterEvent.OTHER, "Multiple failed FLAG-passowrds, modem_port");
        put(30, MeterEvent.OTHER, "Input #1, too short pulse.");
        put(31, MeterEvent.OTHER, "Input #1, too long pulse.");
        put(32, MeterEvent.OTHER, "Input #1, too tight between pulses.");
        put(33, MeterEvent.OTHER, "Input #2, too short pulse.");
        put(34, MeterEvent.OTHER, "Input #2, too long pulse.");
        put(35, MeterEvent.OTHER, "Input #2, too tight between pulses.");
        put(36, MeterEvent.OTHER, "Input #3, too short pulse.");
        put(37, MeterEvent.OTHER, "Input #3, too long pulse.");
        put(38, MeterEvent.OTHER, "Input #3, too tight between pulses.");
        put(39, MeterEvent.OTHER, "Only for internal use");
        put(40, MeterEvent.OTHER, "Only for internal use");
        put(41, MeterEvent.OTHER, "Only for internal use");
        put(42, MeterEvent.OTHER, "Only for internal use");
        put(43, MeterEvent.OTHER, "Only for internal use");
        put(44, MeterEvent.OTHER, "Only for internal use");
        put(45, MeterEvent.OTHER, "Only for internal use");
        put(46, MeterEvent.OTHER, "Only for internal use");
        put(47, MeterEvent.OTHER, "Only for internal use");
        put(48, MeterEvent.OTHER, "Only for internal use");
        put(49, MeterEvent.OTHER, "Only for internal use");
        put(50, MeterEvent.CONFIGURATIONCHANGE, "Relay #1 allocated to aSum with SUB-operator");
        put(51, MeterEvent.CONFIGURATIONCHANGE, "Relay #2 allocated to aSum with SUB-operator");
        put(52, MeterEvent.CONFIGURATIONCHANGE, "Relay #3 allocated to aSum with SUB-operator");
        put(53, MeterEvent.CONFIGURATIONCHANGE, "Relay #4 allocated to aSum with SUB-operator");
        put(54, MeterEvent.CONFIGURATIONCHANGE, "Relay #5 allocated to aSum with SUB-operator");
        put(55, MeterEvent.CONFIGURATIONCHANGE, "Relay #6 allocated to aSum with SUB-operator");
        put(56, MeterEvent.OTHER, "Alarm #1, U_Mean < U_Limit");
        put(57, MeterEvent.OTHER, "Alarm #1, U_Assymmetry > Y_Assymmetry_Limit");
        put(58, MeterEvent.OTHER, "Alarm #1, Power_Factor<Power_Factor_Limit");
        put(59, MeterEvent.OTHER, "Alarm #1, P>P_Limit");
        put(60, MeterEvent.OTHER, "Alarm #2, U_Mean < U_Limit");
        put(61, MeterEvent.OTHER, "Alarm #2, U_Assymmetry > Y_Assymmetry_Limit");
        put(62, MeterEvent.OTHER, "Alarm #2, Power_Factor<Power_Factor_Limit");
        put(63, MeterEvent.OTHER, "Alarm #2, P>P_Limit");
        put(64, MeterEvent.OTHER, "P_Power is invalid number, set to 0,");
        put(65, MeterEvent.OTHER, "Q_Power is invalid number, set to 0,");
        put(66, MeterEvent.OTHER, "Data of logg channel #1, out of range");
        put(67, MeterEvent.OTHER, "Data of logg channel #2, out of range");
        put(68, MeterEvent.OTHER, "Data of logg channel #3, out of range");
        put(69, MeterEvent.OTHER, "Data of logg channel #4, out of range");
        put(70, MeterEvent.OTHER, "Data of logg channel #5, out of range");
        put(71, MeterEvent.OTHER, "Data of logg channel #6, out of range");
        put(72, MeterEvent.OTHER, "Illegal Date_time in logg_file.Detected in Partition_Logging_Memory().");
        put(73, MeterEvent.HARDWARE_ERROR, "Modem could not be initialised.");
        put(74, MeterEvent.HARDWARE_ERROR, "Corrupted date-time from RTC.");
        put(75, MeterEvent.OTHER, "Probably wrong meter time.");
        put(76, MeterEvent.OTHER, "Real-time clock has been restarted to preset time.");
        put(77, MeterEvent.WATCHDOGRESET, "Watchdog timeout restart");
    }

    public static ErrorMessage get(int id) {
        return MESSAGES.get(new Integer(id));
    }

    private ErrorMessage(int id, int meterEvent, String description) {
        this.id = id;
        this.meterEvent = meterEvent;
        this.description = description;
    }

    public int getProtocolCode() {
        return id;
    }

    public int getEiCode() {
        return meterEvent;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return "ErrorMessage[" + id + ", " + description + "]";
    }

}
