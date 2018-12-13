package com.energyict.protocolimpl.iec1107.a140;

import java.io.IOException;

public class TariffSourcesRegister extends Register {
    
    public static final int IMPORT = 1;
    public static final int EXPORT = 2;
    
    int [] source = new int[4];
    
    public TariffSourcesRegister(A140 a140, String id, int length, int sets, int options) {
        super(a140, id, length, sets, options);
    }
    
    public void parse(byte[] ba) throws IOException {
        
        source[0] = ba[0]&0x03;
        source[1] = ba[0]&0x0c;
        source[2] = ba[1]&0x03;
        source[3] = ba[1]&0xc0;

    }
    
    public int[] getSource( ){
        return source;
    }
    
    public String toString( ){
        StringBuffer result = new StringBuffer();
        
        result.append( source[0] + "-" );
        result.append( source[1] + "-" );
        result.append( source[2] + "-" );
        result.append( source[4] + "-" );
        
        return result.toString();
    }


}
