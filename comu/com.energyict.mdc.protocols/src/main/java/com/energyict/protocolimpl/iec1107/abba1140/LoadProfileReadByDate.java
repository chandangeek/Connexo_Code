/*
 * LoadProfileReadByDate.java
 *
 * Created on 21 February 2006, 10:00
 *
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import java.util.Date;

/**@author fbo */

public class LoadProfileReadByDate {
    
    Date from;
    Date to;
    
    /** Creates new LoadProfileReadByDate */
    public LoadProfileReadByDate(Date from, Date to) {
        this.from = from;
        this.to = to;
    }
    
    /** @return start of period */
    public Date getFrom( ){
       return from;
    }
    
    /** @return end of period */
    public Date getTo( ){
        return to;
    }
    
    public String toString(){
        return "LoadProfileReadByDate [from=" + from + ", to=" + to + "]";
    }
    
}
