/**
 * ABBA1350ObisMapper.java
 * 
 * Created on 24-nov-2008, 11:46:49 by jme
 * 
 */
package com.energyict.protocolimpl.iec1107.abba1350;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * @author jme
 *
 */
public class ABBA1350ObisCodeMapper {
    private LinkedHashMap obisMap = new LinkedHashMap();
    private ABBA1350 abba1350 = null;
    
    public ABBA1350ObisCodeMapper(ABBA1350 abba1350) {
		this.abba1350 = abba1350;
	}

	void initObis() throws IOException { 
        {
            
            obisMap.put( "1.1.0.1.2.255", "Date and time (0.9.1 0.9.2)" );
            //obisMap.put("", "");
            
            
            
            obisMap.put( "1.1.1.2.0.255", "+P, cumulative maximum, M0 (1.2.0)" );

            String obis = "1.1.1.2.0.VZ";
            String dscr = "+P, cumulative maximum, M0 (1.2.0*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }
            
            obisMap.put( "1.1.1.6.0.255", "+P, maximum, M0 (1.6.0)" );
            
            obis = "1.1.1.6.0.VZ";
            dscr = "+P, maximum, M0 (1.6.0*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }
            
            obisMap.put( "1.1.1.8.1.255", "+A, Time integral 1, T1 (1.8.1)" );
            
            obis = "1.1.1.8.1.VZ";
            dscr = "+A, Time integral 1, T1 (1.8.1*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }
            
            obisMap.put( "1.1.1.8.2.255", "+A, Time integral 1, T2 (1.8.2)" );
            
            obis = "1.1.1.8.2.VZ";
            dscr = "+A, Time integral 1, T1 (1.8.2*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }
            
            obisMap.put( "1.1.2.2.0.255",   "-P, cumulative maximum, M0 (2.2.0)" );
            
            obis = "1.1.2.2.0.VZ";
            dscr = "-P, cumulative maximum, M0 (2.2.0*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }
            
            obisMap.put( "1.1.2.6.0.255", "-P, maximum, M0 (2.6.0)" );
            
            obis = "1.1.2.6.0.VZ";
            dscr = "-P, maximum, M0 (2.6.0*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }
            
            obisMap.put( "1.1.2.8.1.255", "-A, Time integral 1, T1 (2.8.1)" );

            obis = "1.1.2.8.1.VZ";
            dscr = "-A, Time integral 1, T1 (2.8.1*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }
            
            obisMap.put( "1.1.2.8.2.255", "-A, Time integral 1, T2 (2.8.2)" );

            obis = "1.1.2.8.2.VZ";
            dscr = "-A, Time integral 1, T2 (2.8.2*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }
            
            obisMap.put( "1.1.3.8.1.255", "+R, Time integral 1, T1 (3.8.1)" );

            obis = "1.1.3.8.1.VZ";
            dscr = "+R, Time integral 1, T1 (3.8.1*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }
            
            obisMap.put( "1.1.3.8.2.255", "+R, Time integral 1, T2 (3.8.2)" );

            obis = "1.1.3.8.2.VZ";
            dscr = "+R, Time integral 1, T2 (3.8.2*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }
            
            obisMap.put( "1.1.4.8.1.255", "-R, Time integral 1, T1 (4.8.1)" );

            obis = "1.1.4.8.1.VZ";
            dscr = "-R, Time integral 1, T1 (4.8.1*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }

            
            obisMap.put( "1.1.4.8.2.255", "-R, Time integral 1, T2 (4.8.2)" );

            obis = "1.1.4.8.2.VZ";
            dscr = "-R, Time integral 1, T2 (4.8.2*"; 
            
            for( int i = 0; i < abba1350.getBillingCount(); i ++ ) {
                String bpOString = obis;
                if( i > 0 ) bpOString = bpOString + "-" + i;
                String bpDscr = dscr + (abba1350.getBillingCount() - i) + ")";
                obisMap.put(bpOString, bpDscr);
            }

        }
    };

    public LinkedHashMap getObisMap() {
		return obisMap;
	}
    
}
