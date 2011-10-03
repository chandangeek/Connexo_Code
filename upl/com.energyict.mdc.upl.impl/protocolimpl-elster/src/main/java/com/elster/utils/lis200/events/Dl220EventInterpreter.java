package com.elster.utils.lis200.events;

import com.energyict.protocol.MeterEvent;

import java.util.Date;

/**
 * This class interprets all DL220 specific lis200 events.
 *
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 09:03:08
 */
public class Dl220EventInterpreter extends EventInterpreter {

    public MeterEvent interpretEvent(Date timeStamp, int event) {
        MeterEvent me = null;

        String msg = "";
        int mec = MeterEvent.OTHER;

        switch (event) {
        case 0x8105:
            msg = "New interval in interval archive 1";
            break;
        case 0x8106:
            msg = "New interval in interval archive 2";
            break;
        case 0x8005:
            msg = "Reset watch in interval archive 1";
            break;
        case 0x8006:
            msg = "Reset watch in interval archive 2";
            break;
        case 0x8202:
            msg = "Relevant value for interval archive 1 changed - post change";
            break;
        case 0x8204:
            msg = "Relevant value for interval archive 2 changed - post change";
            break;
        case 0x8302:
            msg = "Relevant value for interval archive 1 changed - pre change";
            break;
        case 0x8304:
            msg = "Relevant value for interval archive 2 changed - pre change";
            break;
        case 0x8502:
            msg = "Frozen values in interval archive 1";
            break;
        case 0x8504:
            msg = "Frozen values in interval archive 2";
            break;
        case 0x0301:
            msg = "Message 'Error output 1, pulse buffer overflow' gone";
            break;
        case 0x0302:
            msg = "Message 'Error output 2, pulse buffer overflow' gone";
            break;
        case 0x0401:
            msg = "Message 'pulse comparison input 1 deviation' gone";
            break;
        case 0x0402:
            msg = "Message 'pulse comparison input 2 deviation' gone";
            break;
        case 0x0501:
            msg = "Message 'warning limit input 1 overrun' gone";
            break;
        case 0x0502:
            msg = "Message 'warning limit input 2 overrun' gone";
            break;
        case 0x0701:
            msg = "Message 'warning input 1' gone";
            break;
        case 0x0702:
            msg = "Message 'warning input 2' gone";
            break;
        case 0x0B01:
            msg = "Message 'notice limit input 1 overrun' gone";
            break;
        case 0x0B02:
            msg = "Message 'notice limit input 2 overrun' gone";
            break;
        case 0x0C01:
            msg = "Message 'notice signal input 1' gone";
            break;
        case 0x0C02:
            msg = "Message 'notice signal input 2' gone";
            break;
        case 0x0F01:
        case 0x0F02:
            msg = "time slot " + (event & 0xF)
                    + "for incoming calls closed";
            break;
        // messages raised
        case 0x2301:
            msg = "Message 'Error output 1, pulse buffer overflow' raised";
            mec = MeterEvent.METER_ALARM; // 0x0E
            break;
        case 0x2302:
            msg = "Message 'Error output 2, pulse buffer overflow' raised";
            mec = MeterEvent.METER_ALARM; // 0x0E
            break;
        case 0x2401:
            msg = "Message 'pulse comparison input 1 deviation' raised";
            break;
        case 0x2402:
            msg = "Message 'pulse comparison input 2 deviation' raised";
            break;
        case 0x2501:
            msg = "Message 'warning limit input 1 overrun' raised";
            break;
        case 0x2502:
            msg = "Message 'warning limit input 2 overrun' raised";
            break;
        case 0x2701:
            msg = "Message 'warning input 1' raised";
            break;
        case 0x2702:
            msg = "Message 'warning input 2' raised";
            break;
        case 0x2B01:
            msg = "Message 'notice limit input 1 overrun' raised";
            break;
        case 0x2B02:
            msg = "Message 'notice limit input 2 overrun' raised";
            break;
        case 0x2C01:
            msg = "Message 'notice signal input 1' raised";
            break;
        case 0x2C02:
            msg = "Message 'notice signal input 2' raised";
            break;
        case 0x2F01:
        case 0x2F02:
            msg = "time slot " + (event & 0xF)
                    + "for incoming calls opened";
            break;

        default:
            me = super.interpretEvent(timeStamp, event);
        }

        if (me == null) {
            me = new MeterEvent(timeStamp, mec, event, msg);
        }
        return me;
    }
}
