package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;

/**
 * Copyrights EnergyICT
 * Date: 11-okt-2010
 * Time: 13:55:24
 */
public class StartDateTest extends TestCase {

    public void test() {
        byte[] rawData = new byte[]{1,0x02,0x1F,4};
        byte[] b = ProtocolTools.getSubArray(rawData, 0, 4);

        String sDate = "";
        String prefix = "";
        for (byte byte1 : b) {
            sDate += prefix + Integer.toString(byte1 & 0xFF);
            prefix = ",";
        }
    }

}
