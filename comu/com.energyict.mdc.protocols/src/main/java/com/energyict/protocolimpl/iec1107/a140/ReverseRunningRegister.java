package com.energyict.protocolimpl.iec1107.a140;

import com.energyict.mdc.protocol.device.events.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ReverseRunningRegister extends Register {

    ArrayList events = new ArrayList();

    public ReverseRunningRegister(A140 a140, String id, int length, int sets, int options) {
        super(a140, id, length, sets, options);
    }

    public ArrayList getEvents( ) throws IOException{
        read( );
        return events;
    }

    public void parse(byte[] ba) throws IOException {
        DataType.UTC utc = a140.getDataType().utc;

        add( utc.parse( ba, 2 ), MeterEvent.OTHER, "Reverse Running" );
        add( utc.parse( ba, 6 ), MeterEvent.OTHER, "Reverse Running" );
        add( utc.parse( ba, 10 ), MeterEvent.OTHER, "Reverse Running" );

    }

    void add( Calendar c, int eiCode, String txt ) {
        if( c != null )
            events.add( new MeterEvent( c.getTime(), eiCode,txt ) );
    }

    /** create an for now and other event code */
    void add( String txt ){
        events.add( new MeterEvent( new Date(), MeterEvent.OTHER, txt ) );
    }

}
