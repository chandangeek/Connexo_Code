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
        //byte[] bytes = ProtocolTools.getBytesFromHexString("$07$31$00$05$A2$E3$03$21$0F$00$00$00$00$00");
        //byte[] bytes = ProtocolTools.getBytesFromHexString("$07$94$EF$E3$E1$E0$0F$00$00$00");
        //byte[] bytes3 = ProtocolTools.getBytesFromHexString("$08$01$FF$0A$0A$01$C0$05$0F$05$01$01$00$00");

        byte[] b = "ABCDE12345".getBytes(); 

        byte[] bytes = ProtocolTools.getBytesFromHexString(
                "$0C$20$FF$65$66$67$68$00$00$01$52$53$54$55$56$65$66$67$68$69$55$56$65$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"
        );

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType();
        type.setHasAccessDescriptor(true);
        type.setHasDefaultValue(true);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        
        AbstractCTRObject ctrObject = factory.parse(bytes, 0, type);


        //TODO: further testing

        assertArrayEquals(bytes, ctrObject.getBytes(type));
    }
}