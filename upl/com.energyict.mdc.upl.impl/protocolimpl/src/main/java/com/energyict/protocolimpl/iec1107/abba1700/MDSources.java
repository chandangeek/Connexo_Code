/*
 * MDSources.java
 *
 * Created on 16 juni 2004, 11:12
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class MDSources {
    private static final int MD_REGISTERS = 8; // in fact 8 x 3 for maximum demand but 8 for the CMD
    int[] regSource = new int[MD_REGISTERS];
    /** Creates a new instance of MDSources */
    public MDSources(byte[] data) throws ProtocolException {
        parse(data);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<MD_REGISTERS;i++) {
            try {
               strBuff.append("(C)MD register "+(i+1)+", "+EnergyTypeCode.getDescriptionfromRegSource(getRegSource()[i],false)+"\n");
            }
            catch(NoSuchRegisterException e) {
               strBuff.append("(C)MD register "+(i+1)+", no source for "+getRegSource()[i]+"\n");
            }
        }
        return strBuff.toString();
    }

    private void parse(byte[] data) throws ProtocolException {
        for (int i=0;i<MD_REGISTERS;i++) {
            regSource[i] = ProtocolUtils.getIntLE(data,i,1);
        }
    }

    /**
     * Getter for property regSource.
     * @return Value of property regSource.
     */
    public int[] getRegSource() {
        return this.regSource;
    }


}
