package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

/** @author  Koen */

public class TariffSources {

    private final static int NUMBER_TARIF_REGISTERS = 8;
    int[] regSource = null;

    /** Creates a new instance of TariffSources */
    public TariffSources(byte[] data) throws ProtocolException {
        regSource = new int[NUMBER_TARIF_REGISTERS];
        parse(data);
    }

    private void parse(byte[] data) throws ProtocolException {
        for (int i=0;i<NUMBER_TARIF_REGISTERS;i++) {
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

    public String toString() {

        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<NUMBER_TARIF_REGISTERS;i++) {
            try {
                strBuff.append("Tariff register "+(i+1)+", "+EnergyTypeCode.getDescriptionfromRegSource(getRegSource()[i],true)+"\n");
            }
            catch(NoSuchRegisterException e) {
                strBuff.append("Tariff register "+(i+1)+", no source for "+getRegSource()[i]+"\n");
            }
        }
        return strBuff.toString();
    }

}
