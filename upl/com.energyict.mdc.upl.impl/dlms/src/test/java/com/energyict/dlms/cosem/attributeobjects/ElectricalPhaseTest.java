package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 1/02/12
 * Time: 7:59
 */
public class ElectricalPhaseTest {

    @Test
    public void testToString() throws Exception {
        for (int i = 0; i < 10; i++) {
            TypeEnum typeEnum = new TypeEnum(i);
            byte[] ber = typeEnum.getBEREncodedByteArray();
            byte[] berOffset = ProtocolTools.concatByteArrays(new byte[i], ber);

            assertNotNull(new ElectricalPhase(i).toString());
            assertNotNull(new ElectricalPhase(typeEnum).toString());
            assertNotNull(new ElectricalPhase(ber).toString());
            assertNotNull(new ElectricalPhase(berOffset, i).toString());
        }

    }

    @Test
    public void testGetValue() throws Exception {
        for (int i = 0; i < 10; i++) {
            TypeEnum typeEnum = new TypeEnum(i);
            byte[] ber = typeEnum.getBEREncodedByteArray();
            byte[] berOffset = ProtocolTools.concatByteArrays(new byte[i], ber);

            assertEquals(i, new ElectricalPhase(i).getValue());
            assertEquals(i, new ElectricalPhase(typeEnum).getValue());
            assertEquals(i, new ElectricalPhase(ber).getValue());
            assertEquals(i, new ElectricalPhase(berOffset, i).getValue());
        }
    }

}
