/*
 * InformationObject88.java
 *
 * Created on 13 april 2006, 15:04
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;

/** Has a collection of BillingPoints 
 * 
 * @author fbo */

public class InformationObject88  extends InformationObject {
    
    // map per enddate
    TreeMap billingPoints = new TreeMap(Collections.reverseOrder());
    
    /** Creates a new instance of InformationObject88 */
    public InformationObject88() { }
    
    void add( InformationObject88Period period ) {
        Date endOfInterval = period.getEndPeriod();
        
        InformationObject88BillingPeriod bp = 
            (InformationObject88BillingPeriod)billingPoints.get(endOfInterval);
        
        if( bp == null ) {
            bp = new InformationObject88BillingPeriod( );
            bp.setStart( period.getStartPeriod() );
            bp.setEnd( endOfInterval );
            billingPoints.put( endOfInterval, bp );
        }
        
        bp.add( period );
        
    }

    InformationObject88BillingPeriod get( int billingPoint ) {
        if( billingPoint >= billingPoints.size() ) {
            return null;
        } else {
            Object key = billingPoints.keySet().toArray(new Object[0])[billingPoint];
            return (InformationObject88BillingPeriod) billingPoints.get(key);
        }
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer( );
        sb.append( "InformationObject88 [ \n" );
        
        for( int i = 0; i < billingPoints.size(); i ++ ) 
            sb.append( " " + i + " " + get(i).toString() + "\n" );
        
        sb.append( "]" );
        return sb.toString();
    }
    
}
