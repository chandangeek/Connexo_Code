package com.energyict.protocolimpl.iec1107.abba230;

import java.io.IOException;

/** @author  fbo */

public class CustDefRegConfig {
    
    int[][] custRegSource = new int[2][2];
    
    /** Creates a new instance of CustDefRegConfig */
    public CustDefRegConfig(byte[] data) throws IOException {
        for (int i=0;i<custRegSource.length;i++) {
            int b = data[i*2] | data[i*2+1];
            int reg = 0;
            if( (b & 0x0001) > 0 ) { custRegSource[i][reg] = 0; reg = reg + 1; }
            if( (b & 0x0002) > 0 ) { custRegSource[i][reg] = 1; reg = reg + 1; }
            if( (b & 0x0004) > 0 ) { custRegSource[i][reg] = 2; reg = reg + 1; }
            if( (b & 0x0008) > 0 ) { custRegSource[i][reg] = 3; reg = reg + 1; }
            if( (b & 0x0010) > 0 ) { custRegSource[i][reg] = 4; reg = reg + 1; }
            if( (b & 0x0020) > 0 ) { custRegSource[i][reg] = 5; reg = reg + 1; }
            if( (b & 0x0040) > 0 ) { custRegSource[i][reg] = 6; reg = reg + 1; }
            if( (b & 0x0080) > 0 ) { custRegSource[i][reg] = 7; reg = reg + 1; }
        }
    }
    
    public int[][] getCustRegSource() {
        return this.custRegSource;
    }
    
    public int getRegSource(int custReg) {
        return custRegSource[custReg][0];
    }
    
}
