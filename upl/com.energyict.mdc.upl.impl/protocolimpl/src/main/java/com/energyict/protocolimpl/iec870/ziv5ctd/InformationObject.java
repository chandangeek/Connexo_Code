package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** new ByteArray(
    new byte[] {
    0x0b, 0x01, 0x08, 0x01, 0x00, 0x09, 0x03,
    0x06, 0x00, 0x00, 0x0a, 0x03, 0x06 } ); */

class InformationObject implements Marshalable { 

    Date date;
    TreeMap map = new TreeMap();
    
    InformationObject addRegister( Register register ){
        map.put( register.address, register );
        return this;
    }
    
    void setDate( Date date ){
        this.date = date;
    }
    
    Date getDate( ) {
        return date;
    }
    
    Register getRegister( int id ) {
        return (Register)map.get( new Integer(id) );
    }
    
    boolean containsAddress( int id ){
        return map.containsKey(new Integer(id));
    }
    
    Set addressSet( ){
        return map.keySet();
    }

    public ByteArray toByteArray() {
        return new ByteArray();
    }
    
    public String toString(){
        StringBuffer result = new StringBuffer();
        result.append( "InformationObject [");
        Iterator i = map.entrySet().iterator();
        while( i.hasNext() ){
            Map.Entry entry = (Map.Entry)i.next();
            result.append( "" + entry.getValue() + ", " );
        }
        result.append( "]" );
        return result.toString();
    }
 
    
}
