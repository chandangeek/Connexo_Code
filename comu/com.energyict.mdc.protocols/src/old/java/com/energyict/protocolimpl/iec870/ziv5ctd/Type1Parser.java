/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Type1Parser.java
 *
 * Created on 14 april 2006, 15:46
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Date;
import java.util.TimeZone;

/** @author fbo */

public class Type1Parser implements TypeParser {
    
    TimeZone timeZone;
    
    /** Creates a new instance of Type1Parser */
    public Type1Parser( TimeZone timeZone ) {
        this.timeZone = timeZone;
    }

    public InformationObject parse(ByteArray byteArray) {
        InformationObject1 io1 = new InformationObject1();
        
        int nrIncidents = byteArray.sub( 1, 1 ).intValue(0);
        
        int offset = 6;
        for( int ei = 0; ei < nrIncidents; ei ++ ) {
            int spa = byteArray.sub( offset, 1 ).intValue(0);   // 6
            offset = offset + 1;
            int spq = byteArray.sub( offset, 1 ).bitValue(1,7); // 7
            int spi = byteArray.sub( offset, 1 ).bitValue(0,0); // 7
            offset = offset + 1;
            Date date = new CP56Time( timeZone, byteArray.sub( offset, 7 ) ).getDate();
            offset = offset + 7;
            io1.add( new InformationObject1Event( spa, spq, spi, date ) );
        }
        return io1;
    }
    
}
