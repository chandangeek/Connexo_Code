package com.elster.utils.lis200.events;

import com.energyict.protocol.MeterEvent;

import java.util.Date;

/**
 * This class interprets all DL210 specific lis200 events.
 *
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 09:01:19
 */
public class Dl210EventInterpreter extends EventInterpreter {

    public MeterEvent interpretEvent(Date timeStamp, int event) {
        MeterEvent me = null;

        String msg = "";
        int mec = MeterEvent.OTHER;

        switch (event) {
        case 0x8105:
            msg = "New interval in interval archive";
            break;
        case 0x8115:
            msg = "New day in daily archive";
            break;
        case 0x8005:
            msg = "Reset watch in interval archive";
            break;
        case 0x8015:
            msg = "Reset watch in daily archive";
            break;
        case 0x8202:
            msg = "Relevant value for interval archive changed - post change";
            break;
        case 0x820D:
            msg = "Relevant value for daily archive changed - post change";
            break;
        case 0x8302:
            msg = "Relevant value for interval archive changed - pre change";
            break;
        case 0x830D:
            msg = "Relevant value for daily archive changed - pre change";
            break;
        case 0x8502:
            msg = "Frozen values in interval archive";
            break;
        case 0x850D:
            msg = "Frozen values in daily archive";
            break;
        case 0x0001:
            msg = "Message 'Encoder plausibility error' gone";
            break;
        case 0x0101:
            msg = "Message 'no answer from encoder' gone";
            break;
    	case 0x0401:
      		msg = "Message 'Encoder error at measuring period' gone";
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
        case 0x0804:
            msg = "Message 'voltage of modem battery to low' gone";
            break;
    	case 0x0A01:
      		msg = "Message' Encoder telegram error' gone";
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
        case 0x0F03:
        case 0x0F04:
            msg = "time slot " + (event & 0xF)
                    + "for incoming calls closed";
            break;
        // messages raised
        case 0x2001:
            msg = "Message 'Encoder plausibility error' raised";
            mec = MeterEvent.METER_ALARM;
            break;
        case 0x2101:
            msg = "Message 'no answer from encoder' raised";
            mec = MeterEvent.HARDWARE_ERROR;
		    break;
    	case 0x2401:
      		msg = "Message 'Encoder error at measuring period' raised";
			mec = MeterEvent.HARDWARE_ERROR;
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
        case 0x2804:
            msg = "Message 'voltage of modem battery to low' raised";
            mec = 0x20;
            break;
        case 0x2A01:
            msg = "Message ' Encoder telegram error' raised";
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
        case 0x2F03:
        case 0x2F04:
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
