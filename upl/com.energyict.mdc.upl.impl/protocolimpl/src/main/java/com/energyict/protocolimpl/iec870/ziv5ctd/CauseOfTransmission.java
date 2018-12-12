/*
 * CauseOfTransmission.java
 *
 * Created on 10 January 2006, 15:37
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.TreeMap;

/**
 * IEC870-5-102 causes of transmission
 *
 * @author  fbo
 */
public class CauseOfTransmission implements Marshalable {

    public final static TreeMap idMap = new TreeMap();
    
    static {
        map( 3, "spontaneous" );
        map( 4, "initialized" );
        map( 5, "request or requested" );
        map( 6, "activation" );
        map( 7, "activation confirmation" );
        map( 8, "deactivation" );
        map( 9, "deactivation confirmation" );
        map( 10, "activation termination" );
        map( 11, null ); // not used
        map( 12, null ); // not used
        map( 13, "requested data record not available" );
        map( 14, "requested ASDU not available" );
        map( 15, "record number in the ASDU sent by the controlling station is not known" );
        map( 16, "address specification in the ASDU sent by the controlling station is not known" );
        map( 17, "requested information object not available" );
        map( 18, "requested integration period not available" );
        map( 19, "reserved for further compatible definitions" );
    }

    
    static final CauseOfTransmission REQUESTED = get(5);
    static final CauseOfTransmission ACTIVATION = get(6);
    static final CauseOfTransmission ACTIVATION_CONFIRMATION = get(7);
    static final CauseOfTransmission ACTIVATION_TERMINATION = get(10);
    
    int id = 0;
    String description = null;
    
    /** Creates a new instance of CauseOfTransmission */
    CauseOfTransmission( int id, String description ) {
        this.id = id;
        this.description = description;
    }

    /** shorthand factory method */
    private static void map( int id, String description ){
        idMap.put( new Integer(id), new CauseOfTransmission( id, description ) );
    }
    
    public static CauseOfTransmission get( int id ) {
        return (CauseOfTransmission)idMap.get(new Integer(id));
    }
    
    public Object parse(ByteArray byteArray) {
        int id = (int)byteArray.get( 0 );
        return get( id );
    }

    public ByteArray toByteArray() {
        return new ByteArray().add((byte)id);    
    }
    
    public boolean equals( Object o ){
        if( ! (o instanceof  CauseOfTransmission ) )
            return false;
        CauseOfTransmission other = (CauseOfTransmission)o;
        return id == other.id;
    }

    public String toString(){
        return "CauseOfTransmission [" + id + ", " + description + "]";
    }

}