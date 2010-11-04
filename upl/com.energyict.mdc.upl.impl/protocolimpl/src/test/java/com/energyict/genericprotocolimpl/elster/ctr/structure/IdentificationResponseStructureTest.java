package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 13:38:37
 */
public class IdentificationResponseStructureTest {

    private static final byte[] identificationResponse;
    private static final String PDR = "12345678900000";

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("0A000000293000123456789000007819090044264420456C657474726F20494D");
        sb.append("504C52494D504C52302E303530424242523130010003E82D2D2D2D2D2D2D2D2D");
        sb.append("2D2D2D2D2D2D2D2D2D2D2D001500000A013E963DB7026DF84800000000000000");
        sb.append("0000009F7F000000000000000000000000000000000000000000000000000000");
        sb.append("000000000000006A71026F8E450D");
        identificationResponse = ProtocolTools.getBytesFromHexString(sb.toString(), "");
    }

    @Test
    public void testIdentificationResponse() throws Exception {
        GPRSFrame response = new GPRSFrame().parse(identificationResponse, 0);
        assertTrue("Data field should be 'IdentificationResponseStructure'", response.getData() instanceof IdentificationResponseStructure);
        assertNotNull(((IdentificationResponseStructure) response.getData()).getPdr());
        assertEquals(PDR, ((IdentificationResponseStructure) response.getData()).getPdr().getValue());
    }
}
