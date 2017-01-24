/*
 * EnergyTypeList.java
 *
 * Created on 22 maart 2004, 17:26
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.pact.core.common.EnergyTypeCode;
/**
 * @author  Koen
 */
public class EnergyTypeList extends MeterReadingsBlockImpl {

	private int[] eType;

    /** Creates a new instance of EnergyTypeList */
    public EnergyTypeList(byte[] data) {
        super(data);
    }


    protected void parse() throws java.io.IOException {
        eType = new int[7];
        for (int i=0;i<7;i++) {
            setEType(i, ProtocolUtils.byte2int(getData()[1+i]));
        }
    }

    protected String print() {
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<7;i++) {
            strBuff.append("0x"+Integer.toHexString(getEType(i))+"("+EnergyTypeCode.getUnit(getEType(i),true)+"), ");
        }
        return strBuff.toString();
    }

    public int getEType(int index) {
        return this.eType[index];
    }

    public void setEType(int index, int eType) {
        this.eType[index] = eType;
    }

}
