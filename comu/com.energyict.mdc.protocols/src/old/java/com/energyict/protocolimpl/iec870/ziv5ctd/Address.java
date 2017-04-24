/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

/** */

public class Address implements Marshalable {

    static final Address DEFAULT = new Address( 1 );

    int address;

    public Address( int address ) {
        this.address = address;
    }

    public static Address parse( byte [] bai ) throws ParseException  {
        if( bai.length != 2 )
            throw new ParseException( "Address.parse() length must be 2" );
        return new Address( bai[0] );
    }
    
    public static Address parse( ByteArray ba ) throws ParseException {
        return new Address( ba.intValue(0) );
    }

    public ByteArray toByteArray( ){
        return new ByteArray( new byte[]{(byte)address, 0x00} );
    }

    public String toString(){
        return toByteArray().toHexaString();
    }

}
