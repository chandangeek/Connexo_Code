/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;

class TypeEventTimeRcd {
    
    TypeShortTimeRcd time;
    int swSumCntrl;
    int swRateCntrl;
    int swThreshd;
    
    String _swSumCntrl;
    String _swRateCntrl;
    String _swThreshd;
    
    ByteArray ba;
    
    static TypeEventTimeRcd parse( Assembly assembly ) throws IOException {
        TypeEventTimeRcd rcd = new TypeEventTimeRcd();
        int maxDataBlks = assembly.getMaxSys().getTable0().getTypeMaximumValues().getMaxDataBlks();
        
        rcd.time = TypeShortTimeRcd.parse( assembly );
        
        int nrBytes = ( ( maxDataBlks * 3 ) / 8 ) + ( ( ( maxDataBlks * 3 ) % 8 ) > 0 ? 1 : 0 );
        rcd.ba = assembly.getBytes(nrBytes);
        
        String sw = rcd.ba.toBinaryString();
        rcd._swSumCntrl = sw.substring( 0, maxDataBlks-1 );
        rcd._swRateCntrl = sw.substring( maxDataBlks, (maxDataBlks*2)-1 );
        rcd._swThreshd = sw.substring( (maxDataBlks*2), (maxDataBlks*3)-1 );
        
//        for( int i =0 ; i < 2; i ++  ) {
//            
//        rcd.swSumCntrl = assembly.intValue();
//        rcd.swRateCntrl = assembly.intValue();
//        rcd.swThreshd = assembly.intValue();
//        }
        return rcd;
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "TypeEventTimeRcd [" );
        rslt.append( time.toString() );
        //rslt.append( " " + ba.toBinaryString() );
        rslt.append( " \t" + _swSumCntrl + " " + _swRateCntrl + " " + _swThreshd );
        rslt.append( " ]" );
        return rslt.toString();
    }
    
}