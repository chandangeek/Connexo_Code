package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockProtocolLink;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 3-jan-2011
 * Time: 13:51:27
 */
public class ComposedCosemObjectTest {

    private static final DLMSAttribute DATA_VALUE_1 = DLMSAttribute.fromString("1:1.1.1.8.0.255:2");
    private static final DLMSAttribute REG_VALUE_2 = DLMSAttribute.fromString("3:1.1.2.8.0.255:2");
    private static final DLMSAttribute EXTENDED_REG_SCALER_3 = DLMSAttribute.fromString("4:1.1.3.8.0.255:3");
    private static final DLMSAttribute REG_SCALER_UNIT_1 = DLMSAttribute.fromString("3:1.1.1.8.0.255:3");
    private static final DLMSAttribute REG_SCALER_UNIT_2 = DLMSAttribute.fromString("3:1.1.2.8.0.255:3");
    private static final DLMSAttribute REG_SCALER_UNIT_3 = DLMSAttribute.fromString("3:1.1.3.8.0.255:3");

    @Test
    public void testContains() throws Exception {
        ComposedCosemObject cco = new ComposedCosemObject(new MockProtocolLink(new MockDLMSConnection()), true, DATA_VALUE_1, REG_VALUE_2, EXTENDED_REG_SCALER_3);
        assertFalse(cco.contains(REG_SCALER_UNIT_1));
        assertFalse(cco.contains(REG_SCALER_UNIT_2));
        assertFalse(cco.contains(REG_SCALER_UNIT_3));
        assertTrue(cco.contains(DATA_VALUE_1));
        assertTrue(cco.contains(REG_VALUE_2));
        assertTrue(cco.contains(EXTENDED_REG_SCALER_3));
    }

    @Test
    public void testToString() throws Exception {
        ComposedCosemObject cco = new ComposedCosemObject(new MockProtocolLink(new MockDLMSConnection()), true, DATA_VALUE_1, REG_VALUE_2, EXTENDED_REG_SCALER_3, REG_SCALER_UNIT_1, REG_SCALER_UNIT_2, REG_SCALER_UNIT_3);
        assertNotNull(cco.toString());
    }
    
    @Test
    public void testShortNaming() throws Exception {
        MockProtocolLink pLink = new MockProtocolLink(new MockDLMSConnection());
        pLink.setReference(ProtocolLink.SN_REFERENCE);
        ComposedCosemObject cco = new ComposedCosemObject(pLink, true, new ArrayList<DLMSAttribute>());
        assertTrue(cco.getObjectReference().isSNReference());
        
        ComposedCosemObject cco2 = new ComposedCosemObject(new MockProtocolLink(new MockDLMSConnection()), true, new ArrayList<DLMSAttribute>());
        assertTrue(cco2.getObjectReference().isLNReference());
    }
}
