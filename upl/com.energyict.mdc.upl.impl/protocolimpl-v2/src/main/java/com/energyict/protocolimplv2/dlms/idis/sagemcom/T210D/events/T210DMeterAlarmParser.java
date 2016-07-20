package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cisac on 6/29/2016.
 */
public class T210DMeterAlarmParser {

    public static List<MeterEvent> parseAlarmCode(Date date, long meterAlarmCode, int alarmRegister) {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        if (ProtocolTools.isBitSet(meterAlarmCode, 0)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.CLOCK_INVALID, 6, "Clock invalid"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.POWERDOWN, 1, "Total Power Failure"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 1)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.REPLACE_BATTERY, 7, "Battery replace"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.POWERUP, 2, "Power Resume"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 2)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 82, "Voltage Phase Failure L1"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 3)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 83, "Voltage Phase Failure L2"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 4)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 84, "Voltage Phase Failure L3"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 208, "M-Bus device uninstalled ch1"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 5)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 85, "Voltage Phase Resume L1"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 209, "M-Bus device uninstalled ch2"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 6)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 86, "Voltage Phase Resume L2"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 210, "M-Bus device uninstalled ch3"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 7)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 87, "Voltage Phase Resume L3"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 211, "M-Bus device uninstalled ch4"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 8)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.PROGRAM_MEMORY_ERROR, 12, "Program memory error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 89, "Missing Neutral"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 9)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.RAM_MEMORY_ERROR, 13, "RAM error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 90, "Phase Asymmetry"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 10)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.NV_MEMORY_ERROR, 14, "NV memory error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.REVERSE_RUN, 91, "Current Reversal"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 220, "Temporary error M-Bus ch1"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 11)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.MEASUREMENT_SYSTEM_ERROR, 16, "Measurement system error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 88, "Wrong Phase Sequence"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 221, "Temporary error M-Bus ch2"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 12)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.WATCHDOG_ERROR, 15, "Watchdog error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 52, "Unexpected Consumption"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 222, "Temporary error M-Bus ch3"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 12)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.WATCHDOG_ERROR, 15, "Watchdog error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 52, "Unexpected Consumption"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 222, "Temporary error M-Bus ch3"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 13)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, 40, "Fraud attempt"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 48, "Key Exchanged"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 223, "Temporary error M-Bus ch4"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 14)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 92, "Bad Voltage Quality L1"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 41, "End of fraud attempt"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 15)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 93, "Bad Voltage Quality L2"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 236, "Certificate almost expired"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 16)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.COMMUNICATION_ERROR_MBUS, 100, "M-Bus communication error ch1"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 94, "Bad Voltage Quality L3"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 17)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.COMMUNICATION_ERROR_MBUS, 110, "M-Bus communication error ch2"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 20, "External Alert"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 18)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.COMMUNICATION_ERROR_MBUS, 120, "M-Bus communication error ch3"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 53, "Local communication attempt"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 19)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.COMMUNICATION_ERROR_MBUS, 130, "M-Bus communication error ch4"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 105, "New M-Bus Device Installed Ch1"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 20)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, 103, "M-Bus fraud attempt ch1"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 115, "New M-Bus Device Installed Ch2"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 21)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, 113, "M-Bus fraud attempt ch2"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 125, "New M-Bus Device Installed Ch3"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 22)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, 123, "M-Bus fraud attempt ch3"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 135, "New M-Bus Device Installed Ch4"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 23)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, 133, "M-Bus fraud attempt ch4"));
                    break;
                case 2:
                    break;//Reserved for future use
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 24)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 106, "Permanent error M-bus ch1"));
                    break;
                case 2:
                    break;//Reserved for future use
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 25)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 116, "Permanent error M-bus ch2"));
                    break;
                case 2:
                    break;//Reserved for future use
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 26)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 126, "Permanent error M-bus ch3"));
                    break;
                case 2:
                    break;//Reserved for future use
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 27)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 136, "Permanent error M-bus ch4"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.VALVE_ALARM_MBUS, 164, "M-Bus valve alarm Ch1"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 28)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, 102, "Battery low on M-bus ch1"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.VALVE_ALARM_MBUS, 174, "M-Bus valve alarm Ch2"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 29)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, 112, "Battery low on M-bus ch2"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.VALVE_ALARM_MBUS, 184, "M-Bus valve alarm Ch3"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 30)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, 122, "Battery low on M-bus ch3"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.VALVE_ALARM_MBUS, 194, "M-Bus valve alarm Ch4"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 30)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, 132, "Battery low on M-bus ch4"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 68, "Disconnect/Reconnect Failure"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        return meterEvents;
    }
}
