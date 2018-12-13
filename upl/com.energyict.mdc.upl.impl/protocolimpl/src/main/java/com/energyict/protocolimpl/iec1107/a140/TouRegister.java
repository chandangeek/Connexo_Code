package com.energyict.protocolimpl.iec1107.a140;

import java.io.IOException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

/** @author fbo */

public class TouRegister extends Register {

    private Quantity[] rate;
    
    public TouRegister(A140 a140, String id, int length, int sets, int options ) {
        super(a140, id, length, sets, options );
    }
    
    public Quantity get( int rateIndex ) throws IOException {
        read();
        return rate[rateIndex];
    }

    public void parse(byte[] ba) throws IOException {
        
        Unit mWh = Unit.get( "mWh" );
        rate = new Quantity[4];
        rate[0] = a140.getDataType().bcd.toQuantity( ba, 0, 5, mWh);
        rate[1] = a140.getDataType().bcd.toQuantity( ba, 5, 5, mWh);
        rate[2] = a140.getDataType().bcd.toQuantity( ba, 10, 5, mWh);
        rate[3] = a140.getDataType().bcd.toQuantity( ba, 15, 5, mWh);
        
    }
    
}
