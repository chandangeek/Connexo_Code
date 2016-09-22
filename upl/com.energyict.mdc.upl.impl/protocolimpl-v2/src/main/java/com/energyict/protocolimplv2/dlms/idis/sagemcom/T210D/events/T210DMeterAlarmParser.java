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
        int alarmGeneratedMeterEvent = MeterEvent.OTHER;
        String alarmGeneratedEventDescription = "Alarm generated event: ";

        if (ProtocolTools.isBitSet(meterAlarmCode, 0)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 6, alarmGeneratedEventDescription+"Clock invalid"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 1, alarmGeneratedEventDescription+"Total Power Failure"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 1)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 7, alarmGeneratedEventDescription+"Battery replace"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 2, alarmGeneratedEventDescription+"Power Resume"));
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
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 82, alarmGeneratedEventDescription+"Voltage Phase Failure L1"));
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
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 83, alarmGeneratedEventDescription+"Voltage Phase Failure L2"));
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
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 84, alarmGeneratedEventDescription+"Voltage Phase Failure L3"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 208, alarmGeneratedEventDescription+"M-Bus device uninstalled ch1"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 5)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 85, alarmGeneratedEventDescription+"Voltage Phase Resume L1"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 209, alarmGeneratedEventDescription+"M-Bus device uninstalled ch2"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 6)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 86, alarmGeneratedEventDescription+"Voltage Phase Resume L2"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 210, alarmGeneratedEventDescription+"M-Bus device uninstalled ch3"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 7)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 87, alarmGeneratedEventDescription+"Voltage Phase Resume L3"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 211, alarmGeneratedEventDescription+"M-Bus device uninstalled ch4"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 8)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 12, alarmGeneratedEventDescription+"Program memory error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 89, alarmGeneratedEventDescription+"Missing Neutral"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 9)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 13, alarmGeneratedEventDescription+"RAM error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 90, alarmGeneratedEventDescription+"Phase Asymmetry"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 10)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 14, alarmGeneratedEventDescription+"NV memory error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 91, alarmGeneratedEventDescription+"Current Reversal"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 220, alarmGeneratedEventDescription+"Temporary error M-Bus ch1"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 11)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 16, alarmGeneratedEventDescription+"Measurement system error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 88, alarmGeneratedEventDescription+"Wrong Phase Sequence"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 221, alarmGeneratedEventDescription+"Temporary error M-Bus ch2"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 12)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 15, alarmGeneratedEventDescription+"Watchdog error"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 52, alarmGeneratedEventDescription+"Unexpected Consumption"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 222, alarmGeneratedEventDescription+"Temporary error M-Bus ch3"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 13)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 40, alarmGeneratedEventDescription+"Fraud attempt"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 48, alarmGeneratedEventDescription+"Key Exchanged"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 223, alarmGeneratedEventDescription+"Temporary error M-Bus ch4"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 14)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 92, alarmGeneratedEventDescription+"Bad Voltage Quality L1"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 41, alarmGeneratedEventDescription+"End of fraud attempt"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 15)) {
            switch (alarmRegister){
                case 1:
                    break;//Reserved for future use
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 93, alarmGeneratedEventDescription+"Bad Voltage Quality L2"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 236, alarmGeneratedEventDescription+"Certificate almost expired"));
                    break;
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 16)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 100, alarmGeneratedEventDescription+"M-Bus communication error ch1"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 94, alarmGeneratedEventDescription+"Bad Voltage Quality L3"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 17)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 110, alarmGeneratedEventDescription+"M-Bus communication error ch2"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 20, alarmGeneratedEventDescription+"External Alert"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 18)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 120, alarmGeneratedEventDescription+"M-Bus communication error ch3"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 53, alarmGeneratedEventDescription+"Local communication attempt"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 19)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 130, alarmGeneratedEventDescription+"M-Bus communication error ch4"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 105, alarmGeneratedEventDescription+"New M-Bus Device Installed Ch1"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 20)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 103, alarmGeneratedEventDescription+"M-Bus fraud attempt ch1"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 115, alarmGeneratedEventDescription+"New M-Bus Device Installed Ch2"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 21)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 113, alarmGeneratedEventDescription+"M-Bus fraud attempt ch2"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 125, alarmGeneratedEventDescription+"New M-Bus Device Installed Ch3"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 22)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 123, alarmGeneratedEventDescription+"M-Bus fraud attempt ch3"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 135, alarmGeneratedEventDescription+"New M-Bus Device Installed Ch4"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 23)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 133, alarmGeneratedEventDescription+"M-Bus fraud attempt ch4"));
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
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 106, alarmGeneratedEventDescription+"Permanent error M-bus ch1"));
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
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 116, alarmGeneratedEventDescription+"Permanent error M-bus ch2"));
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
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 126, alarmGeneratedEventDescription+"Permanent error M-bus ch3"));
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
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 136, alarmGeneratedEventDescription+"Permanent error M-bus ch4"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 164, alarmGeneratedEventDescription+"M-Bus valve alarm Ch1"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 28)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 102, alarmGeneratedEventDescription+"Battery low on M-bus ch1"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 174, alarmGeneratedEventDescription+"M-Bus valve alarm Ch2"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 29)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 112, alarmGeneratedEventDescription+"Battery low on M-bus ch2"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 184, alarmGeneratedEventDescription+"M-Bus valve alarm Ch3"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 30)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 122, alarmGeneratedEventDescription+"Battery low on M-bus ch3"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 194, alarmGeneratedEventDescription+"M-Bus valve alarm Ch4"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        if (ProtocolTools.isBitSet(meterAlarmCode, 31)) {
            switch (alarmRegister){
                case 1:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 132, alarmGeneratedEventDescription+"Battery low on M-bus ch4"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent(date, alarmGeneratedMeterEvent, 68, alarmGeneratedEventDescription+"Disconnect/Reconnect Failure"));
                    break;
                case 3:
                    break;//Reserved for future use
            }
        }

        return meterEvents;
    }
}
