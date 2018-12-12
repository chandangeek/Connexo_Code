package com.elster.us.protocolimpl.landisgyr.quad4;

import java.io.IOException;
import java.util.Date;

/**
 * Variables from a table can be read in a Direct Access manner.  Without
 * loading the complete table, only the part that is required.
 * 
 * @author fbo
 */

class TableAddress {

    Quad4 quad4;
    int table; 
    int displacement;
    
    TableAddress(Quad4 quad4, int table, int displacement ){
        this.quad4 = quad4;
        this.table = table;
        this.displacement = displacement;
    }
    
    int getDisplacement() {
        return displacement;
    }

    Quad4 getQuad4() {
        return quad4;
    }

    int getTable() {
        return table;
    }

    Object read( ) throws IOException {
        Assembly assembly = new Assembly(quad4, quad4.read( this ) );
        return new Double( assembly.doubleValue() );
    }
    
    Date readDate( ) throws IOException {
        Assembly assembly = new Assembly(quad4, quad4.read( this ) );
        return TypeDateTimeRcd.parse( assembly ).toDate();
    }
    
    String readString( int length ) throws IOException {
        Assembly assembly = new Assembly(quad4, quad4.read( this ) );
        return assembly.stringValue(length);
    }
    
    byte[] readBytes( int length ) throws IOException {
        Assembly assembly = new Assembly(quad4, quad4.read( this ) );
        return assembly.getBytes(length).getBytes();
    }
    
    TableAddress copy( ){
        return copy( 0 );
    }
    
    TableAddress copy( int offset ) {
        TableAddress rslt = new TableAddress(quad4, table, displacement);
        rslt.displacement = rslt.displacement + offset;
        return rslt;
    }
    
    public String toString() {
        return "TableAddress[ tn=" + table + " disp=" + displacement + " ]";
    }
    
}
