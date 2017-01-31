/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.a140;

import java.io.IOException;

public class SerialNumberRegister extends Register {

    String serialNumber;
    
    public SerialNumberRegister(A140 a140, String id, int length, int sets, int options) {
        super(a140, id, length, sets, options);
    }
    
    public String getSerialNumber() throws IOException{
        read();
        return serialNumber;
    }
    
    public void parse(byte[] ba) throws IOException {
        serialNumber = (String)a140.getDataType().string.parse( ba, 0, ba.length );
    }

}
