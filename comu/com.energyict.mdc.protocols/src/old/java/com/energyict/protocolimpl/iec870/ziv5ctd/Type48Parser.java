/*
 * Type48Parser.java
 *
 * Created on 5 april 2006, 17:33
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.TimeZone;

/** @author fbo */

public class Type48Parser implements TypeParser {
    
    TimeZone timeZone;
    
    /** Creates a new instance of Type48Parser */
    public Type48Parser(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public InformationObject parse(ByteArray byteArray) {
       return new InformationObject48( byteArray, timeZone );
    }
    
}
