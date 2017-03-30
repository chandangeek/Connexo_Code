/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InformationObject88BillingPeriod.java
 *
 * Created on 13 april 2006, 15:12
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/** @author fbo */

public class InformationObject88BillingPeriod extends InformationObject {

    Map objectIdMap = new TreeMap();
    Map eFieldMap = new TreeMap();
    
    private Date start;
    private Date end;
    
    /** Creates a new instance of InformationObject88BillingPeriod */
    public InformationObject88BillingPeriod() { }
    
    InformationObject88Period getPeriod( int id ) {
        return (InformationObject88Period)eFieldMap.get( new Integer(id) );
    }
    
    InformationObject88BillingPeriod add( InformationObject88Period period ){
        int objectId = period.getInfoAddress();
        
        if( objectId < 20 || objectId > 29 )
            throw new ParseException( "Object id not supported: " + objectId );
        
        objectIdMap.put( new Integer( period.getInfoAddress()), period);
        eFieldMap.put( new Integer( period.getInfoAddress()-20), period);
        return this;
    }
    
    Date getStart() {
        return start;
    }

    void setStart(Date start) {
        this.start = start;
    }

    Date getEnd() {
        return end;
    }

    void setEnd(Date end) {
        this.end = end;
    }
       
    public String toString( ){
        return "InformationObject88BillingPeriod [ " +
                start + ", " +
                end + " " +
                "]" ;
    }
    
}
