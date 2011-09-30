package com.elster.utils.lis200.events;

import com.energyict.protocol.MeterEvent;

import java.util.Date;

/**
 * This class interprets all EK260 specific lis200 events.
 *
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 09:05:01
 */
public class Ek260EventInterpreter extends EventInterpreter {

    public MeterEvent interpretEvent(Date timeStamp, int event) {
        MeterEvent me = null;

        int mec = MeterEvent.OTHER;

        String msg = "";

        switch (event) {
        case 0x8004:
            msg = "Change of time (new interval) backwards";
            break;
        case 0x8104:
            msg = "New interval in interval archive";
            break;
        case 0x8203:
            msg = "Relevant value for interval archive changed - post change";
            break;
        case 0x8303:
            msg = "Relevant value for interval archive changed - pre change";
            break;
        case 0x8503:
            msg = "Frozen values in interval archive";
            break;

        case 0x0005:
            msg = "Message 'Can't calculate conversion factor' gone";
            break;
        case 0x0006:
            msg = "Message 'alarm limit temperature' gone";
            break;
        case 0x0007:
            msg = "Message 'alarm limit pressure' gone";
            break;
        case 0x0008:
            msg = "Message 'Can't calculate k factor' gone";
            break;
        case 0x0009:
            msg = "Message 'Can't calculate compressibility' gone";
            break;
        case 0x0101:
            msg = "Message 'no answer from encoder' gone";
            break;
        case 0x0105:
            msg = "Message 'no valid data for temperature' gone";
            break;
        case 0x0106:
            msg = "Message 'no valid data for pressure' gone";
            break;
        case 0x0301:
        case 0x0302:
        case 0x0303:
        case 0x0304:
            msg = "Message 'Error at output " + (event & 0xF) + "' gone";
            break;
        case 0x0402:
            msg = "Message 'Error pulse comparison input 2' gone";
            break;
        case 0x0501:
            msg = "Message 'warning limit power overrun' gone";
            break;
        case 0x0502:
            msg = "Message 'warning limit flow rate (base conditions) overrun' gone";
            break;
        case 0x0504:
            msg = "Message 'warning limit flow rate (measuring conditions) overrun' gone";
            break;
        case 0x0506:
            msg = "Message 'warning limit temperature overrun' gone";
            break;
        case 0x0507:
            msg = "Message 'warning limit pressure overrun' gone";
            break;
        case 0x0702:
            msg = "Message 'warning input 2' gone";
            break;
        case 0x0703:
            msg = "Message 'warning input 3' gone";
            break;
        case 0x0C02:
            msg = "Message 'notice signal input 2' gone";
            break;
        case 0x0C03:
            msg = "Message 'notice signal input 3' gone";
            break;
        case 0x0E01:
            msg = "Message 'extended time slot 1' gone";
            break;
        case 0x0F01:
        case 0x0F02:
            msg = "time slot " + (event & 0xF)
                    + "for incoming calls closed";
            break;
        // messages raised
        case 0x2005:
            msg = "Message 'Can't calculate conversion factor' raised";
            break;
        case 0x2006:
            msg = "Message 'alarm limit temperature' raised";
            break;
        case 0x2007:
            msg = "Message 'alarm limit pressure' raised";
            break;
        case 0x2008:
            msg = "Message 'Can't calculate k factor' raise";
            break;
        case 0x2009:
            msg = "Message 'Can't calculate compressibility' raised";
            break;
        case 0x2101:
            msg = "Message 'no answer from encoder' raised";
            break;
        case 0x2105:
            msg = "Message 'no valid data for temperature' raised";
            break;
        case 0x2106:
            msg = "Message 'no valid data for pressure' raised";
            break;
        case 0x2301:
        case 0x2302:
        case 0x2303:
        case 0x2304:
            msg = "Message 'Error at output " + (event & 0xF) + "' raised";
            break;
        case 0x2402:
            msg = "Message 'Error pulse comparison input 2' raised";
            break;
        case 0x2501:
            msg = "Message 'warning limit power overrun' raised";
            break;
        case 0x2502:
            msg = "Message 'warning limit flow rate (base conditions) overrun' raised";
            break;
        case 0x2504:
            msg = "Message 'warning limit flow rate (measuring conditions) overrun' raised";
            break;
        case 0x2506:
            msg = "Message 'warning limit temperature overrun' raised";
            break;
        case 0x2507:
            msg = "Message 'warning limit pressure overrun' raised";
            break;
        case 0x2702:
            msg = "Message 'warning input 2' raised";
            break;
        case 0x2703:
            msg = "Message 'warning input 3' raised";
            break;
        case 0x2C02:
            msg = "Message 'notice signal input 2' raised";
            break;
        case 0x2C03:
            msg = "Message 'notice signal input 3' raised";
            break;
        case 0x2E01:
            msg = "Message 'extended time slot 1' raised";
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
