/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DataBlkCntrl {
    
    final static int TABLE = 15;
    
    final static int DATE_SIZE = 6;
    final static int DOUBLE_SIZE = 8;
    final static int WORD_SIZE = 2;
    
    private TypeChannelSelectRcd summationSelect [];
    private TypeChannelSelectRcd rateSelect[];
    private double rateThreshold;
    
    private int maxSummations;
    private int maxConcValues;
    private int maxRatePeaks;
    
    private int dataBlkRcdByteSize;
    
    Firmware firmware;
    
    static DataBlkCntrl parse(Assembly assembly, int byteOffset, int datablockIndex ) 
        throws IOException{
        
        DataBlkCntrl dbc = new DataBlkCntrl();
        MaxSys ms = assembly.getMaxSys();
        
        TypeMaximumValues tmv = ms.getTable0().getTypeMaximumValues(); 
        dbc.maxSummations = tmv.getMaxSummations();
        dbc.maxConcValues = tmv.getMaxConcValues();
        dbc.maxRatePeaks = tmv.getMaxRatePeaks();
        
        dbc.summationSelect = new TypeChannelSelectRcd[dbc.maxSummations];
        for (int i = 0; i < dbc.summationSelect.length; i++) {
            dbc.summationSelect[i] = TypeChannelSelectRcd.parse(assembly, datablockIndex, i+1);
            TableAddress ta = new TableAddress(ms, TABLE, byteOffset );
            dbc.summationSelect[i].setTableAddress(ta);
            byteOffset = byteOffset + dbc.getSummationRcdSize();
        }
        
        dbc.rateSelect = new TypeChannelSelectRcd[dbc.maxConcValues];
        for (int i = 0; i < dbc.rateSelect.length; i++) {
            dbc.rateSelect[i] = TypeChannelSelectRcd.parse(assembly, datablockIndex, i+5);
            TableAddress ta = new TableAddress(ms, TABLE, byteOffset+DATE_SIZE );
            dbc.rateSelect[i].setTableAddress(ta);
            byteOffset = byteOffset + dbc.getRateRcdSize();
            if( (i>0) && ( (i%dbc.maxRatePeaks) == 0 ) )
                byteOffset = byteOffset + ( dbc.maxConcValues * 2 );
        }        
        
        dbc.rateThreshold = assembly.doubleValue();
        byteOffset = byteOffset + 8;
        
        dbc.dataBlkRcdByteSize = byteOffset;
        dbc.firmware = ms.getFirmware();
        
        return dbc;
    }
    
    int getSummationRcdSize( ) { 
        return DOUBLE_SIZE + WORD_SIZE;
    }
    
    int getRateRcdSize( ){
        return DATE_SIZE + ( maxConcValues * DOUBLE_SIZE );
    }
    
    int getDataBlkRcdByteSize() {
        return dataBlkRcdByteSize;
    }
    
    List getSummations( ) {
        ArrayList rslt = new ArrayList( );
        for (int i = 0; i < summationSelect.length; i++) {
            rslt.add( summationSelect[i] );
        }
        return rslt;
    }
    
    List getRates( ){
        ArrayList rslt = new ArrayList( );
        for (int i = 0; i < rateSelect.length; i++) {
            rslt.add( rateSelect[i] );
        }
        return rslt;
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        
        rslt.append( "DataBlkCntrl [\n" );
        
        rslt.append( "summations:\n" );
        for (int j = 0; j < summationSelect.length; j++) {
            TypeChannelSelectRcd rcd = summationSelect[j]; 
            Object value = null;
            try {
                value = rcd.getTableAddress().read();
            } catch( IOException ioe ) {
                value = ioe;
            }
            rslt.append( rcd.toString() + " = " + value + "\n" );
        }
        
        rslt.append( "rates: \n" );
        for (int i = 0; i < rateSelect.length; i++) {
            TypeChannelSelectRcd rcd = rateSelect[i];
            Object value = null;
            try {
                value = rcd.getTableAddress().read();
            } catch( IOException ioe ){
                value = ioe;
            }
            rslt.append( rateSelect[i].toString() + " = " + value + "\n" );
        }
        
        rslt.append( "rateThreshold " + rateThreshold + "]\n" );
        return rslt.toString();
    }
    
}
