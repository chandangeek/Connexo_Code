/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;
import java.util.Date;

/**
 * Variables from a table can be read in a Direct Access manner.  Without
 * loading the complete table, only the part that is required.
 * 
 * @author fbo
 */

class TableAddress {

    MaxSys maxSys;
    int table; 
    int displacement;
    
    TableAddress( MaxSys maxSys, int table, int displacement ){
        this.maxSys = maxSys;
        this.table = table;
        this.displacement = displacement;
    }
    
    int getDisplacement() {
        return displacement;
    }

    MaxSys getMaxSys() {
        return maxSys;
    }

    int getTable() {
        return table;
    }

    Object read( ) throws IOException {
        Assembly assembly = new Assembly( maxSys, maxSys.read( this ) );
        return new Double( assembly.doubleValue() );
    }
    
    Date readDate( ) throws IOException {
        Assembly assembly = new Assembly( maxSys, maxSys.read( this ) );
        return TypeDateTimeRcd.parse( assembly ).toDate();
    }
    
    String readString( int length ) throws IOException {
        Assembly assembly = new Assembly( maxSys, maxSys.read( this ) );
        return assembly.stringValue(length);
    }
    
    byte[] readBytes( int length ) throws IOException {
        Assembly assembly = new Assembly( maxSys, maxSys.read( this ) );
        return assembly.getBytes(length).getBytes();
    }
    
    TableAddress copy( ){
        return copy( 0 );
    }
    
    TableAddress copy( int offset ) {
        TableAddress rslt = new TableAddress(maxSys, table, displacement);
        rslt.displacement = rslt.displacement + offset;
        return rslt;
    }
    
    public String toString() {
        return "TableAddress[ tn=" + table + " disp=" + displacement + " ]";
    }
    
}
