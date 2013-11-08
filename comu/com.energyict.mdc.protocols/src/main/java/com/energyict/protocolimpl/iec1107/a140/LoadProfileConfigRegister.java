package com.energyict.protocolimpl.iec1107.a140;

import java.io.IOException;
import java.util.logging.Level;

/**
 * 777 Load Profile Configuration
 * 
 * @author fbo
 */

public class LoadProfileConfigRegister extends Register {
    
    /* The demand periods are defined as follows
     * 000 => Invalid Period Setting
     * 001 => 10 minutes
     * 010 => 15 minutes
     * 011 => 20 minutes
     * 100 => 30 minutes
     * 101 => 60 minutes
     * 110 => Invalid Period Setting
     * 111 => Invalid Period Setting
     */
    final static  int [] DEMAND_PERIOD = { 0, 10, 15, 20, 30, 60, 0, 0 };
    
    boolean read = false;
    
    private boolean dlsEnabled;
    private boolean today;
    private int dlsAdjust;
    private boolean advance;
    private int demandPeriod;
    
    public LoadProfileConfigRegister( A140 a140, String id, int length, int sets, int options ) {
        super(a140, id, length, sets, options );
    }
    
    /** @return true if adjustement is advance, false if retard */
    public boolean isAdvance() throws IOException {
        if( !read ) read();
        return advance;
    }
    /** @return demand period in minutes ( 10, 15, 20, 30, 60 ) */
    public int getDemandPeriod() throws IOException {
        if( !read ) read();
        return demandPeriod;
    }
    /** @return dls adjustement in hours ( 1, 2, 3 ) */
    public int getDlsAdjust() throws IOException {
        if( !read ) read();
        return dlsAdjust;
    }
    /** @return true if enabled, false if not */
    public boolean isDlsEnabled() throws IOException {
        if( !read ) read();
        return dlsEnabled;
    }
    /** @return false if NO adjustement today, true if adjustement today */
    public boolean isToday() throws IOException {
        if( !read ) read();
        return today;
    }
    
    public String toString( ) {
        String rslt = "LoadProfileConfig isAdvance=" + advance
            + ", dlsAdjust=" + dlsAdjust 
            + ", today=" + today
            + ", dlsEnabled=" + dlsEnabled;
        return rslt; 
    }

    public void parse(byte[] ba) {
   
        dlsEnabled = (ba[0] & 0x80) > 1;
        today = (ba[0] & 0x40 ) > 1;
        dlsAdjust = (ba[0] & 0x30);
        advance = (ba[0] & 0x08) > 1;
        
        int dp = ba[0] & 0x07;
        if( dp > 0 && dp < 6 )
            demandPeriod = DEMAND_PERIOD[dp] * 60;
        
        if( a140.getLogger().isLoggable( Level.INFO ))
            a140.getLogger().log( Level.INFO, toString() );
        
    }
    
}
