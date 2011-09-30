package com.elster.utils.lis200.events;

import com.energyict.protocol.MeterEvent;

import java.util.Date;

/**
 * This class interprets all DL240 specific lis200 events.
 *
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 09:04:23
 */
public class Dl240EventInterpreter extends EventInterpreter {

    public MeterEvent interpretEvent(Date timeStamp, int event) {
        MeterEvent me = null;

        String msg = "";
        int mec = MeterEvent.OTHER;

        switch (event) {
        case 0x8105:
        case 0x8106:
        case 0x8107:
        case 0x8108:
            msg = "New interval in interval archive "
                    + arcFromEvent(event, 4, 0);
            break;
        case 0x8005:
        case 0x8006:
        case 0x8007:
        case 0x8008:
            msg = "Reset watch in interval archive "
                    + arcFromEvent(event, 4, 0);
            break;
        case 0x8202:
        case 0x8204:
        case 0x8206:
        case 0x8208:
            msg = "Relevant value for interval archive "
                    + arcFromEvent(event, 0, 1) + " changed - post change";
            break;
        case 0x8302:
        case 0x8304:
        case 0x8306:
        case 0x8308:
            msg = "Relevant value for interval archive "
                    + arcFromEvent(event, 0, 1) + " changed - pre change";
            break;
        case 0x8502:
        case 0x8504:
        case 0x8506:
        case 0x8508:
            msg = "Frozen values in interval archive "
                    + arcFromEvent(event, 0, 1);
            break;
        case 0x0301:
        case 0x0302:
        case 0x0303:
        case 0x0304:
            msg = "Message 'Error output " + arcFromEvent(event, 0, 0)
                    + ", pulse buffer overflow' gone";
            break;
        case 0x0401:
        case 0x0402:
        case 0x0403:
        case 0x0404:
            msg = "Message 'deviation during pulse comparison input "
                    + arcFromEvent(event, 0, 0) + "' gone";
            break;
        case 0x0501:
        case 0x0502:
        case 0x0503:
        case 0x0504:
            msg = "Message 'warning limit input "
                    + arcFromEvent(event, 0, 0) + " overrun' gone";
            break;
        case 0x0601:
            msg = "Message 'warning limit calculated channel "
                    + arcFromEvent(event, 0, 0) + " violated' gone";
            break;
        case 0x0701:
        case 0x0702:
        case 0x0703:
        case 0x0704:
            msg = "Message 'warning input " + arcFromEvent(event, 0, 0)
                    + "' gone";
            break;
        case 0x0B01:
        case 0x0B02:
        case 0x0B03:
        case 0x0B04:
            msg = "Message 'notice limit input "
                    + arcFromEvent(event, 0, 0) + " overrun' gone";
            break;
        case 0x0C01:
        case 0x0C02:
        case 0x0C03:
        case 0x0C04:
            msg = "Message 'notice signal input "
                    + arcFromEvent(event, 0, 0) + "' gone";
            break;
        case 0x0F01:
            msg = "Message 'NT active, counting in NT counter' gone";
            break;
        case 0x0F02:
        case 0x0F03:
            msg = "time slot " + arcFromEvent(event, 1, 0)
                    + " for incoming calls closed";
            break;
        // messages raised
        case 0x2301:
        case 0x2302:
        case 0x2303:
        case 0x2304:
            msg = "Message 'Error at output " + arcFromEvent(event, 0, 0)
                    + ", pulse buffer overflow' raised";
            mec = MeterEvent.METER_ALARM;
            break;
        case 0x2401:
        case 0x2402:
        case 0x2403:
        case 0x2404:
            msg = "Message 'deviation during pulse comparison input "
                    + arcFromEvent(event, 0, 0) + "' raised";
            break;
        case 0x2501:
        case 0x2502:
        case 0x2503:
        case 0x2504:
            msg = "Message 'warning limit input "
                    + arcFromEvent(event, 0, 0) + " overrun' raised";
            break;
        case 0x2601:
            msg = "Message 'warning limits calculated channel "
                    + arcFromEvent(event, 0, 0) + " violated' raised";
            break;
        case 0x2701:
        case 0x2702:
        case 0x2703:
        case 0x2704:
            msg = "Message 'warning input " + arcFromEvent(event, 0, 0)
                    + "' raised";
            break;
        case 0x2B01:
        case 0x2B02:
        case 0x2B03:
        case 0x2B04:
            msg = "Message 'notice limit input "
                    + arcFromEvent(event, 0, 0) + " overrun' raised";
            break;
        case 0x2C01:
        case 0x2C02:
        case 0x2C03:
        case 0x2C04:
            msg = "Message 'notice signal input "
                    + arcFromEvent(event, 0, 0) + "' raised";
            break;
        case 0x2F01:
            msg = "Message 'NT active, counting in NT counter' raised";
            break;
        case 0x2F02:
        case 0x2F03:
            msg = "time slot " + arcFromEvent(event, 1, 0)
                    + " for incoming calls opened";
            break;

        default:
            me = super.interpretEvent(timeStamp, event);
        }

        if (me == null) {
            me = new MeterEvent(timeStamp, mec, event, msg);
        }
        return me;
    }

    private int arcFromEvent(int event, int offset, int shift) {
        int arc = (event & 0xF) - offset;
        if (shift > 0) {
            arc = arc >> shift;
        }
        return arc;
    }
}
