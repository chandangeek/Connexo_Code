package com.energyict.protocolimpl.iec870.ziv5ctd;

/** */

import java.io.IOException;

public class FixedFrame extends Frame {

    /** Constructor for data to be transmitted */
    public FixedFrame(Address address, ControlField controlField) {
        super(address, controlField);
    }
    
    /** Constructor for received data */
    public FixedFrame(Address address, ControlField controlField, ByteArray rawdata){
        super(address, controlField, rawdata);
    }
    
    Frame requestRespond(LinkLayer linkLayer) throws IOException, ParseException {
        return linkLayer.requestRespond(this);
    }

    public ByteArray toByteArray( ) {
        ByteArray byteArray = new ByteArray( )
            .add( (byte) 0x10 )
            .add( controlField.toByteArray() )
            .add( address.toByteArray() );
        byte [] data = byteArray.toByteArray();
        byte checksum = 0;
        checksum += (int)data[1]&0xFF;
        checksum += (int)data[2]&0xFF;
        checksum += (int)data[3]&0xFF;
        byteArray
            .add(checksum)
            .add( (byte)0x16 );
        return byteArray;
    }

    public String toString(){
        return
            new StringBuffer()
                .append(" FixedFrame [")
                .append(address.toString()).append(", ")
                .append(controlField)
                .append(" ]").toString();
    }



}
