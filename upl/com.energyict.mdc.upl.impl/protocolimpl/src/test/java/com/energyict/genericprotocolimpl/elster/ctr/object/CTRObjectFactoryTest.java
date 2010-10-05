package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;


import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 12:52:55
 */
public class CTRObjectFactoryTest {

    @Test
    public void testParse() throws Exception {

        byte[] bytes = ProtocolTools.getBytesFromHexString("$0C$20$FF$55$55$55$55$F2$00$00$55$55$55$55$55$55$55$55$55$55$55$55$56$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00");
        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType();
        type.setHasAccessDescriptor(true);
        type.setHasDefaultValue(true);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        
        AbstractCTRObject ctrObject = factory.parse(bytes, 0, type);

        //assertArrayEquals(bytes, ctrObject.getBytes(type));
    }
}