/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.a140;

import java.io.IOException;

public class TouSourceRegister extends Register {
    
    public static final int IMPORT = 1;
    public static final int EXPORT = 2;
    
    int [] source = new int[4];
    
    public TouSourceRegister( A140 a140 ){ 
        super( a140 );
    }
    
    public TouSourceRegister(A140 a140, String id, int length, int sets, int options) {
        super(a140, id, length, sets, options);
    }
    
    public void parse(byte[] ba) throws IOException {
        
        source[0] = ba[0]&0x03;
        source[1] = (ba[0]&0x0c) >> 2;
        source[2] = (ba[0]&0x30) >> 4;
        source[3] = (ba[0]&0xc0) >> 6;

    }
    
    public int getSource( int index ) throws IOException{
        read();
        return source[index];
    }
    
    public String toString( ){
        StringBuffer result = new StringBuffer();

        result.append( source[0] == IMPORT ? "import " : "" );
        result.append( source[0] == EXPORT ? "export " : "" );
        result.append( source[1] == IMPORT ? "import " : "" );
        result.append( source[1] == EXPORT ? "export " : "" );
        result.append( source[2] == IMPORT ? "import " : "" );
        result.append( source[2] == EXPORT ? "export " : "" );
        result.append( source[3] == IMPORT ? "import " : "" );
        result.append( source[3] == EXPORT ? "export " : "" );
        
        return result.toString();
    }


}
