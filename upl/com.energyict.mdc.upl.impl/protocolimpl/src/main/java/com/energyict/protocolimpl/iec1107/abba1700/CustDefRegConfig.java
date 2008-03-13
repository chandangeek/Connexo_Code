/*
 * CustDefRegConfig.java
 *
 * Created on 16 juni 2004, 13:33
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import java.io.*;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.cbo.Unit;
/**
 *
 * @author  Koen
 */
public class CustDefRegConfig {
    int[][] custRegSource = new int[3][2];
    
    /** Creates a new instance of CustDefRegConfig */
    public CustDefRegConfig(byte[] data) throws IOException {
        for (int i=0;i<3;i++) {
            custRegSource[i][0] = ProtocolUtils.getIntLE(data,i*2,1);
            custRegSource[i][1] = ProtocolUtils.getIntLE(data,i*2+1,1);
        }
    }
    
    /**
     * Getter for property custRegSource.
     * @return Value of property custRegSource.
     */
    public int[][] getCustRegSource() {
        return this.custRegSource;
    }
    
    public int getRegSource(int custReg) {
        return custRegSource[custReg][0];
    }
}
