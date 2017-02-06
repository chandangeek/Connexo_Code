/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Calculate.java
 *
 * Created on 14 januari 2003, 17:01
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import java.math.BigDecimal;

/**
 * @author Koen
 */
public class Calculate {

    final static private int BIG_ENDIAN = 0;
    final static private int LITTLE_ENDIAN = 1;

    /**
     * Creates a new instance of Calculate
     */
    public Calculate() {
    }

    public static long convert(long a, long b) {
        double da = (double) a;
        double db = (double) b;
        double dval = da * Math.pow(10, db);
        return Math.round(dval);
    }

    public static long exp(long b) {
        double db = (double) b;
        double dval = Math.pow(10, db);
        return Math.round(dval);
    }

    public static Number convertIEEE32fp2NumberLE(byte data[], int iOffset) {
        return convertIEEE32fp2Number(data, iOffset, Calculate.LITTLE_ENDIAN);
    }

    public static Number convertIEEE32fp2Number(byte data[], int iOffset) {
        return convertIEEE32fp2Number(data, iOffset, Calculate.BIG_ENDIAN);
    }

    private static Number convertIEEE32fp2Number(byte data[], int iOffset, int order) {
        long lTemp = 0, lE = 0;
        double lF = 0, lS = 0;

        float flVal = 0;

        if (order == Calculate.LITTLE_ENDIAN) {
            lTemp = ((long) data[0 + iOffset] & 0xff) |
                    (((long) data[1 + iOffset] & 0xff) << 8) |
                    (((long) data[2 + iOffset] & 0xff) << 16) |
                    (((long) data[3 + iOffset] & 0xff) << 24);
        } else {
            lTemp = ((long) data[0 + iOffset] & 0xff) << 24 |
                    (((long) data[1 + iOffset] & 0xff) << 16) |
                    (((long) data[2 + iOffset] & 0xff) << 8) |
                    (((long) data[3 + iOffset] & 0xff));
        }

        lF = 0;
        for (int i = 0; i < 23; i++) {
            if ((lTemp & (0x01 << i)) != 0) {
                lF = (float) (lF + 1) / 2;
            } else {
                lF = (float) (lF) / 2;
            }
        }

        if (order == Calculate.LITTLE_ENDIAN) {
            lE = ((long) (data[2 + iOffset] >> 7) & 0x1) | ((long) (data[3 + iOffset] << 1) & 0xFE);
        } else {
            lE = ((long) (data[1 + iOffset] >> 7) & 0x1) | ((long) (data[0 + iOffset] << 1) & 0xFE);
        }

        if ((lTemp & 0x80000000) != 0) {
            lS = -1;
        } else {
            lS = 1;
        }

        if ((lE > 0) && (lE < 255)) {
            flVal = (float) lS * (float) Math.pow(2, lE - 127) * (((float) lF) + 1);
        } else if (lE == 0) {
            flVal = (float) lS * (float) Math.pow(2, lE - 127) * (float) lF;
        }

        // KV 17032006 changed to return float value! only 4 decimals... values are rounded!
        return new BigDecimal("" + flVal);
    }

    public static Number convertNormSignedFP2NumberLE(byte data[], int iOffset) {
        return convertNormSignedFP2Number(data, iOffset, Calculate.LITTLE_ENDIAN);
    }

    public static Number convertNormSignedFP2Number(byte data[], int iOffset) {
        return convertNormSignedFP2Number(data, iOffset, Calculate.BIG_ENDIAN);
    }

    private static Number convertNormSignedFP2Number(byte data[], int iOffset, int order) {
        long lTemp = 0;
        double lF = 1, lS = 0;
        double val = 0;

        if (order == Calculate.LITTLE_ENDIAN) {
            lTemp = ((long) data[0 + iOffset] & 0xff) |
                    (((long) data[1 + iOffset] & 0xff) << 8);
        } else {
            lTemp = (((long) data[0 + iOffset] & 0xff) << 8) |
                    (((long) data[1 + iOffset] & 0xff));
        }

        // sign bit ?
        lS = ((lTemp & 0x8000) != 0) ? -1 : 1;

        // take two's complement
        if (lS == -1) {
            lTemp = (lTemp ^ 0xFFFF) + 1;
        }

        // calc normalized value
        for (int i = 0; i < 15; i++) {
            lF = lF / 2;
            if ((lTemp & (0x4000 >> i)) != 0) {
                val += lF;
            }
        }
        if ((val == 0) && (lS == -1)) {
            val = 1;
        }
        // use sign bit
        return new BigDecimal(lS * val);

    } // private static Number convertNormSignedFP2Number(byte data[], int iOffset,int order)


}
