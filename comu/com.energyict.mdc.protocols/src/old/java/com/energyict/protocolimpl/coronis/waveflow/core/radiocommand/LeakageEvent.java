/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;
import java.util.Date;

public class LeakageEvent {


    /**
     * indicates the event type (occurrence or disappearance) and the corresponding Port.
     * Bit 7 Bit 6 Bit 5 Bit 4 Bit 3 Bit 2 Bit 1 Bit 0
     * Corresponding Port
     * bit7..6: 00 : Port A 01 : Port B
     * bit5..2: reserved
     * bit1: Leak type 0 : Extreme leak	1 : Residual leak
     * bit0: Event Type	0 : disappearance 1 : occurrence
     */
    int status;

    /**
     *
     */
    int consumptionRate;

    /**
     * leakage event timestamp
     */
    Date date = null;

    /**
     * valid or not
     */
    boolean valid = false;
    public static final String LEAKAGETYPE_EXTREME = "extreme";
    public static final String LEAKAGETYPE_RESIDUAL = "residual";
    public static final String END = "End";
    public static final String START = "Start";
    public static final String A = "A";
    public static final String B = "B";
    public static final String C = "C";
    public static final String D = "D";


    final public boolean isValid() {
        return valid;
    }

    public LeakageEvent(int status, int consumptionRate, byte[] timestamp, WaveFlow waveFlow) throws IOException {
        this.status = status;
        this.consumptionRate = consumptionRate;

        for (byte b : timestamp) {
            if (b != (byte) 0xff) {
                valid = true;
                this.date = TimeDateRTCParser.parse(timestamp, waveFlow.getTimeZone()).getTime();
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

    public String getEventDescription() {
        if (getLeakageType().equals(LEAKAGETYPE_EXTREME)) {
            return "burst";
        }
        return "leak";
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