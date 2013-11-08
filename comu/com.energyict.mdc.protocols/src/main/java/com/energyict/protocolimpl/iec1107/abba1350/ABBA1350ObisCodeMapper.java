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
    
	private static final int DEBUG = 0;

	public static final String ID1 = "Device ID1";
	public static final String ID2 = "Device ID2";
	public static final String ID3 = "Device ID3";
	public static final String ID4 = "Device ID4";
	public static final String ID5 = "Device ID5";
	public static final String ID6 = "Device ID6";
	public static final String SERIAL = "Device Serial number";
	public static final String FIRMWARE = "Device  Firmware/Hardware information";
	public static final String FIRMWAREID = "Firmware version ID";
	public static final String DATETIME = "Date and time (0.9.1 0.9.2)";
	public static final String BILLINGCOUNTER = "Billing counter";
	
	public static final String IEC1107_ID = "Device IEC1107_ID";
	public static final String IEC1107_ADDRESS_OP = "Device IEC1107_ADDRESS_OP (optical)";
	public static final String IEC1107_ADDRESS_EL = "Device IEC1107_ADDRESS_EL (electrical)";
	
	
	private LinkedHashMap obisMap = new LinkedHashMap();
    private ABBA1350 abba1350 = null;
    
    public ABBA1350ObisCodeMapper(ABBA1350 abba1350) {
		this.abba1350 = abba1350;
		initObisUnconnected();
	}


    void initObisUnconnected() {

        obisMap.put("1.1.0.1.0.255", BILLINGCOUNTER);
    	obisMap.put( "1.1.0.1.2.255", DATETIME );

        obisMap.put("1.1.0.2.0.255", FIRMWAREID);

        obisMap.put("1.1.0.0.0.255", SERIAL);
        obisMap.put("1.1.0.0.1.255", ID1);
        obisMap.put("1.1.0.0.2.255", ID2);
        obisMap.put("1.1.0.0.3.255", ID3);
        obisMap.put("1.1.0.0.4.255", ID4);
        obisMap.put("1.1.0.0.5.255", ID5);
        obisMap.put("1.1.0.0.6.255", ID6);

        obisMap.put("1.1.0.0.7.255", IEC1107_ID);
        obisMap.put("1.1.0.0.8.255", IEC1107_ADDRESS_OP);
        obisMap.put("1.1.0.0.9.255", IEC1107_ADDRESS_EL);
        
        obisMap.put("1.1.0.0.10.255", FIRMWARE);

    }
    
    void initObis() throws IOException { 
        {
            
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
