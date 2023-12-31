package com.elster.us.protocolimpl.landisgyr.quad4;

import java.io.IOException;
import java.util.Date;

class RateBlkRcd {

    Date eventTime;
    double concValues [];
    
    static RateBlkRcd parse( Assembly assembly ) throws IOException{
        TypeMaximumValues tmv = assembly.getQuad4().getTable0().getTypeMaximumValues();
        
        RateBlkRcd rbr = new RateBlkRcd();
        rbr.eventTime = TypeDateTimeRcd.parse(assembly).toDate();
        
        rbr.concValues = new double[tmv.getMaxConcValues()];
        for (int i = 0; i < rbr.concValues.length; i++) {
            rbr.concValues[i] = assembly.doubleValue();
        }
        
        return rbr;
    }
    
    double[] getConcValues() {
        return concValues;
    }


    Date getEventTime() {
        return eventTime;
    }
    
    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "RateBlkRcd [ " ).append( " eventTime " + eventTime );
        for (int i = 0; i < concValues.length; i++) {
            rslt.append( " " + concValues[i] + " " );
        };
        
        rslt.append( " ]" ) 
            .toString();
        return rslt.toString();
    }
}
