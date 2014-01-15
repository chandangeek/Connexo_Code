package com.energyict.protocolimpl.iec1107.a140;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

public class ConfigureReadRegister extends Register {

    private int value;

    public ConfigureReadRegister(
            A140 a140,
            String id, int length, int sets, int options) {
        super(a140, id, length, sets, options);
    }

    public int getValue( ) throws IOException{
        read();
        return value;
    }

    public void setValue( int v ) {
        value = v;
    }

    public void parse(byte[] ba) throws IOException {
        value = (int) ProtocolUtils.getLongLE( ba, 0, 2 );
    }

    public byte[] construct() {
        long lVal = value;
        byte[] data = new byte[4];
        ProtocolUtils.val2HEXascii((int)lVal&0xFF,data,0);
        ProtocolUtils.val2HEXascii((int)(lVal>>8)&0xFF,data,2);
        return data;
    }

}
