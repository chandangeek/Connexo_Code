package com.energyict.protocolimpl.iec1107.ppmi1.register;

import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;

/**
 * can parse a status from a byte, and display itself.
 * 
 * @author fbo
 */

public class LoadProfileStatus {

    /* LoadProfileStatus Type */
    static byte SS_DIAGNOSTIC_FLAG = 0x01;

    static byte SS_WRITE_ACCESS = 0x02;

    static byte SS_PARTIAL_DEMAND = 0x04;

    static byte SS_REVERSE_RUN = 0x08;

    int status;

    public LoadProfileStatus(byte b) {
        status = Integer.parseInt(Byte.toString(b), 16);
    }

    public LoadProfileStatus(int i) {
        status = i;
    }

    public int getEIStatus() {
        int result = IntervalStateBits.OK;
        if (is(SS_DIAGNOSTIC_FLAG))
            result |= IntervalStateBits.OTHER;
        if (is(SS_WRITE_ACCESS))
            result |= IntervalStateBits.CONFIGURATIONCHANGE
                    | IntervalStateBits.SHORTLONG;
        if (is(SS_PARTIAL_DEMAND))
            result |= IntervalStateBits.PHASEFAILURE;
        if (is(SS_REVERSE_RUN))
            result |= IntervalStateBits.REVERSERUN;
        return result;
    }

    public boolean is(byte statusType) {
        return (status & statusType) > 0;
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();

        if (is(SS_DIAGNOSTIC_FLAG))
            sb.append("[Diagnostic flag] ");
        if (is(SS_WRITE_ACCESS))
            sb.append("[Write access] ");
        if (is(SS_PARTIAL_DEMAND))
            sb.append("[Partial Demand] ");
        if (is(SS_REVERSE_RUN))
            sb.append("[Reverse Running] ");

        sb.append(Integer.toBinaryString(status) + " "
                + PPMUtils.toHexaString(status));

        return sb.toString();
    }

}