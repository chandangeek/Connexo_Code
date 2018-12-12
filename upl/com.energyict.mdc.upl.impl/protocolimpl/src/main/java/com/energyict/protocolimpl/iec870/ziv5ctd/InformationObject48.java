/*
 * InformationObject48.java
 *
 * Created on 5 april 2006, 16:49
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Date;
import java.util.TimeZone;

/** @author fbo */

public class InformationObject48 extends InformationObject {
    
    Date date;
    TimeZone timeZone;
    
    /** Creates a new instance of InformationObject48 */
    public InformationObject48(ByteArray byteArray, TimeZone timeZone) {
        this.timeZone = timeZone;
        this.date = new CP56Time( timeZone, byteArray.sub(6,7) ).getDate();
    }
    
    Date getDate( ){
        return date;
    }
}
