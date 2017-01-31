/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.io.IOException;

/** */

public class VariableFrame extends Frame {

    private Asdu asdu;

    /** Constructor for frame to be transmitted */
    public VariableFrame(Address address, ControlField controlField, Asdu asdu) {
        super(address, controlField);
        this.asdu = asdu;
    }

    /** Constructor for frame to be transmitted */
    public VariableFrame(Address address, ControlField controlField, Asdu asdu, ByteArray rawdata ) {
        super(address, controlField, rawdata);
        this.asdu = asdu;
    }

    public Asdu getAsdu(){
        return asdu;
    }

    public CauseOfTransmission getCauseOfTransmission( ){
        return asdu.getTransmissionCause();
    }

    Frame requestRespond(LinkLayer linkLayer) throws IOException, ParseException {
        return linkLayer.requestRespond(this);
    }

    public ByteArray toByteArray( ) {

        ByteArray body = new ByteArray( )
            .add( controlField )
            .add( address )
            .add( asdu );
        byte bLength = (byte)body.length();

        ByteArray byteArray = new ByteArray( )
            .add( Frame.START_VARIABLE )
            .add( bLength )
            .add( bLength )
            .add( Frame.START_VARIABLE )
            .add( body );

        byteArray
            .add(byteArray.sub( 4 ).checksum())
            .add( Frame.END );

        return byteArray;

    }

    public String toString(){
        return "VariableFrame [ " + getAsdu();
    }

}
