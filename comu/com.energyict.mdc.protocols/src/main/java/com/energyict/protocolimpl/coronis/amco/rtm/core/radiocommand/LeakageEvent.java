/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;

import java.io.IOException;
import java.util.Date;

public class LeakageEvent {

    int status;
    int consumptionRate;
    Date date = null;
    boolean valid = false;

    public static final String LEAKAGETYPE_EXTREME = "extreme";
    public static final String LEAKAGETYPE_RESIDUAL = "residual";
    public static final String END = "End";
    public static final String START = "Start";
    public static final String A = "A";
    public static final String B = "B";
    public static final String C = "C";
    public static final String D = "D";

    public String getDescription() {
        return getStatusDescription() + "of " + getLeakageType() + " leakage on port "  + getCorrespondingInputChannel() + ". Consumption-rate = " + getConsumptionRate();
    }

    final public boolean isValid() {
        return valid;
    }

    public LeakageEvent(int status, int consumptionRate, byte[] timestamp, RTM rtm) throws IOException {
        this.status = status;
        this.consumptionRate = consumptionRate;

        for (byte b : timestamp) {
            if ((b != (byte) 0xff) && (b != (byte) 0x00)) {
                valid = true;
                this.date = TimeDateRTCParser.parse(timestamp, rtm.getTimeZone()).getTime();
                break;
            }
        }
    }

    final public int getStatus() {
        return status;
    }

    final public int getConsumptionRate() {
        return consumptionRate;
    }

    final public Date getDate() {
        return date;
    }

    /**
     * The first bit (LSB) in the status byte indicates [start / stop] of the leakage event.
     * @return start or stop string
     */
    public String getStatusDescription() {
        switch (getStatus() & 0x01) {
            case 0:
                return END;
            case 1:
                return START;
            default:
                return "";
        }
    }

    /**
     * The flags in the status byte indicate what type the leakage event is
     * @return event type (extreme leakage or residual leakage)
     */
    public String getLeakageType() {
        switch ((getStatus() & 0x02) >> 1) {
            case 0:
                return LEAKAGETYPE_EXTREME;
            case 1:
                return LEAKAGETYPE_RESIDUAL;
            default:
                return "";
        }
    }

    /**
     * Bit7 and bit6 represent the corresponding input channel (00 = A, 10 = B, 01 = C, 11 = D)
     * If there's only 2 input channels used, only bit7 is important.
     * @return the name of the corresponding input channel.
     */
    public String getCorrespondingInputChannel() {
        switch ((getStatus() & 0xC0) >> 6)  {
            case 0:
                return A;
            case 2:
                return B;
            case 1:
                return C;
            case 3:
                return D;
            default:
                return "";
        }
    }
}