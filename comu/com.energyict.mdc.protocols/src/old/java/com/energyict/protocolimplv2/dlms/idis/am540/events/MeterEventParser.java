/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am540.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cisac on 6/2/2016.
 */
public class MeterEventParser {

    public static List<MeterEvent> parseEventCode(Date date, long meterEventCode, int alarmRegister) {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        //first byte
        if (ProtocolTools.isBitSet(meterEventCode, 0)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.CLOCK_INVALID, 6, "Clock invalid"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.POWERDOWN, 1, "Total Power Failure"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 1)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.REPLACE_BATTERY, 7, "Battery replace"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.POWERUP, 2, "Power Resume"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 2)) {
            if (alarmRegister == 1){
                //do nothing
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 82, "Voltage Missing Phase L1"));
                //TODO use proper event code
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 3)) {
            if (alarmRegister == 1){
                //do nothing
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 83, "Voltage Missing Phase L2"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 4)) {
            if (alarmRegister == 1){
                //do nothing
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 84, "Voltage Missing Phase L3"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 5)) {
            if (alarmRegister == 1){
                //do nothing
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 85, "Voltage Normal Phase L1"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 6)) {
            if (alarmRegister == 1){
                //do nothing
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 86, "Voltage Normal Phase L2"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 7)) {
            if (alarmRegister == 1){
                //do nothing
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 87, "Voltage Normal Phase L3"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 8)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.PROGRAM_MEMORY_ERROR, 12, "Program memory error"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 89, "Missing Neutral"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 9)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.RAM_MEMORY_ERROR, 13, "RAM error"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 90, "Phase Asymmetry"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 10)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.NV_MEMORY_ERROR, 14, "NV memory error"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.REVERSE_RUN, 91, "Current Reversal"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 11)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.MEASUREMENT_SYSTEM_ERROR, 16, "Measurement system error"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.REVERSE_RUN, 88, "Wrong Phase Sequence"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 12)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.WATCHDOG_ERROR, 15, "Watchdog error"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 52, "Unexpected Consumption"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 13)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, 40, "Fraud attempt"));
                //TODO check if this is correct
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 48, "Key Exchanged"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 14)) {
            if (alarmRegister == 1){
                //do nothing
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 92, "Bad Voltage Quality L1"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 15)) {
            if (alarmRegister == 1){
                //do nothing
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 93, "Bad Voltage Quality L2"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 16)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.COMMUNICATION_ERROR_MBUS, 100, "M-Bus communication error ch1"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 94, "Bad Voltage Quality L3"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 17)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.COMMUNICATION_ERROR_MBUS, 110, "M-Bus communication error ch2"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 20, "External Alert"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 18)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.COMMUNICATION_ERROR_MBUS, 120, "M-Bus communication error ch3"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 53, "Local communication attempt"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 19)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.COMMUNICATION_ERROR_MBUS, 130, "M-Bus communication error ch4"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 105, "New M-Bus Device Installed Ch1"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 20)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, 103, "M-Bus fraud attempt ch1"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 115, "New M-Bus Device Installed Ch2"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 21)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, 113, "M-Bus fraud attempt ch2"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 125, "New M-Bus Device Installed Ch3"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 22)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, 123, "M-Bus fraud attempt ch3"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 135, "New M-Bus Device Installed Ch4"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 23)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, 133, "M-Bus fraud attempt ch4"));
            } else if (alarmRegister == 2) {
                //do nothing
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 24)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 100, "Permanent error M-bus ch1"));
                //TODO see if PeermanentError event should be added
            } else if (alarmRegister == 2) {
                //do nothing
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 25)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 110, "Permanent error M-bus ch2"));
            } else if (alarmRegister == 2) {
                //do nothing
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 26)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 120, "Permanent error M-bus ch3"));
            } else if (alarmRegister == 2) {
                //do nothing
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 27)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 130, "Permanent error M-bus ch4"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.VALVE_ALARM_MBUS, 164, "M-Bus valve alarm Ch1"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 28)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, 103, "Battery low on M-bus ch1"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.VALVE_ALARM_MBUS, 174, "M-Bus valve alarm Ch2"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 29)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, 113, "Battery low on M-bus ch2"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.VALVE_ALARM_MBUS, 184, "M-Bus valve alarm Ch3"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 30)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, 123, "Battery low on M-bus ch3"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.VALVE_ALARM_MBUS, 194, "M-Bus valve alarm Ch4"));
            }
        }

        if (ProtocolTools.isBitSet(meterEventCode, 31)) {
            if (alarmRegister == 1){
                meterEvents.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, 133, "Battery low on M-bus ch4"));
            } else if (alarmRegister == 2) {
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 68, "Disconnect/Reconnect Failure"));
            }
        }

        return meterEvents;
    }

}
