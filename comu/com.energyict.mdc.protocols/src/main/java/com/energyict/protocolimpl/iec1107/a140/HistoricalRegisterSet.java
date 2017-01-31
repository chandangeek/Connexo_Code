/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.a140;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;

import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 * 1 byte       billing triggers
 * 2 bytes      billing date
 * 4 bytes      UTC
 * 5 bytes      KWh Cumulative Import Register
 * 5 bytes      KWh Cumulative Export Register
 * 5 bytes      KWh TOU Rate 1 Register
 * 5 bytes      KWh TOU Rate 2 Register
 * 5 bytes      KWh TOU Rate 3 Register
 * 5 bytes      KWh TOU Rate 4 Register
 * 1 bytes      TOU Sources
 */

public class HistoricalRegisterSet extends Register {

    private int dbg = 0;

    public static final String[] triggerSrc = {
        "Timed Billing Event",
        "Season Start Billing Event",
        "Tarif Changeover Billing Event",
        "Remote Port Commanded Billing",
        "FLAG Port Commanded Billing",
        "Battery Fail Billing"
    };

    public static final int [] triggerSrcCode =  {
        0x01,
        0x02,
        0x04,
        0x08,
        0x10,
        0x20,
        0x40,
        0x80
    };

    HistoricalRegister hr [] = new HistoricalRegister[5];

    public HistoricalRegisterSet(A140 a140, String id, int length, int sets,
            int options) {
        super(a140, id, length, sets, options);
    }

    public HistoricalRegister get(int index) throws IOException {
        read();
        return (HistoricalRegister) hr[index];
    }

    public void parse(byte[] ba) throws IOException {
        // Sorted set with reversed order
        SortedSet hrSet = new TreeSet( new Comparator(){
            public int compare(Object o1, Object o2) {
                HistoricalRegister hr1 = (HistoricalRegister)o1;
                HistoricalRegister hr2 = (HistoricalRegister)o2;
                if( hr1.getTime() == null ) return 1;
                if( hr2.getTime() == null ) return -1;
                return hr2.getTime().compareTo(hr1.getTime());
            }
        });

        int setSize = length / sets;
        Unit uWh = Unit.get("mWh");
        DataType dataType = a140.getDataType();

        for (int si = 0; si < sets; si++) {

            int sOff = si * setSize; // start position of this set

            int t = (int)(ba[sOff + 0]&0xFF);
            int trSrc = 0;
            for( int i = 0; i < triggerSrc.length; i ++ ){
                if( triggerSrcCode[i] == t ) {
                    trSrc = i;
                    break;
                }
            }

            Calendar c = a140.getDataType().utc.parse(ba, sOff + 3);
            Date d = (c != null) ? c.getTime() : null;

            Quantity ci = dataType.bcd.toQuantity(ba, sOff+7, 5, uWh);
            Quantity ce = dataType.bcd.toQuantity(ba, sOff+12, 5, uWh);

            Quantity r1 = dataType.bcd.toQuantity(ba, sOff+17, 5, uWh);
            Quantity r2 = dataType.bcd.toQuantity(ba, sOff+22, 5, uWh);
            Quantity r3 = dataType.bcd.toQuantity(ba, sOff+27, 5, uWh);
            Quantity r4 = dataType.bcd.toQuantity(ba, sOff+32, 5, uWh);

            byte src[] = new byte[1];
            System.arraycopy( ba, sOff+37, src, 0, 1 );
            TouSourceRegister tsr = new TouSourceRegister( a140 );
            tsr.read( src );

            hrSet.add(new HistoricalRegister( trSrc, d, ci, ce, r1, r2, r3, r4, tsr));

        }

        hr = (HistoricalRegister[])hrSet.toArray( new HistoricalRegister[0] );

        if(dbg > 0) a140.getLogger().log(Level.FINEST, this.toString());

    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        for( int i = 0; i < hr.length; i ++ ) {
            result.append( hr[i] + "\n");
        }
        return result.toString();
    }

}
