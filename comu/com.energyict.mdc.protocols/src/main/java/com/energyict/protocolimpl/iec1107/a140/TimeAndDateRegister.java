package com.energyict.protocolimpl.iec1107.a140;

import java.io.IOException;
import java.util.Date;

public class TimeAndDateRegister extends Register {
    
    Date date = null;
    
    public TimeAndDateRegister(A140 a140, String id, int length, int sets, int options ) {
        super(a140, id, length, sets, options );
    }
    
    public Date getTime() throws IOException {
        read();
        return date;
    }
    
    public void setTime(Date date) throws IOException {
        this.date = date;
    }
    
    public void parse( byte [] data ) throws IOException{
        date = a140.getDataType().dateTime.parse( data );      
    }
    
    public byte[] construct( ){
        return a140.getDataType().dateTime.construct( date );      
    }
    
}
