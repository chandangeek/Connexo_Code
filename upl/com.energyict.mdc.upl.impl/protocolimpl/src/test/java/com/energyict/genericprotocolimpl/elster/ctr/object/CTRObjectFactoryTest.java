package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 12:52:55
 */
public class CTRObjectFactoryTest {

    /**
     * Tests parsing of common objects
     * Tests converting the result to byte arrays again
     * @throws Exception
     */
    @Test
    public void testParse() throws Exception {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType();
        type.setHasAccessDescriptor(true);
        type.setHasDefaultValue(true);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        type.setHasIdentifier(true);
        type.setRegisterQuery(true);

        List<byte[]> bytes = new ArrayList<byte[]>();

        bytes.add(ProtocolTools.getBytesFromHexString("$09$02$0F$31$32$33$00$00$0F$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$16$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$00$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$10$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$30$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$01$00$0F$00$00$01$0F$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$01$20$0F$00$00$01$0F$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$04$00$0F$00$00$01$0F$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$07$00$0F$00$00$01$0F$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$0A$00$0F$00$00$01$0F$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$0A$16$0F$00$00$01$0F$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$37$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$38$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$39$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$0C$00$0F$12$34$56$78$90$00$00$0F$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$36$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$3A$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$3B$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$3C$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$10$10$0F$00$03$0F$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$50$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$51$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$52$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$53$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$54$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$55$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$12$00$0F$02$0F$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$12$00$FF"));
        bytes.add(ProtocolTools.getBytesFromHexString("$0D$90$0F$00$01$0F$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$12$10$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$12$20$0F$00$01$0F$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$55$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$0E$C0$0F$01$0F$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$08$12$0F$00$01$0F$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$0F$50$0F$00$00$01$00$00$01$00$00$01$00$00$01$0F$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$0F$51$0F$00$00$01$00$00$01$00$00$01$00$00$01$0F$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$0F$52$0F$00$00$01$00$00$01$00$00$01$00$00$01$0F$00$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$09$03$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$09$04$0F$00$00$00$00$00$01$0F$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$09$07$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$09$05$0F$00$00$01$0F$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$02$31$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$03$11$0F$00$00$00$01$0F$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$11$01$00$01$0A$0A$0A$06$00$00$00$01$01$00$00$00$00$00$00$00$00$00$00$00"));
        bytes.add(ProtocolTools.getBytesFromHexString("$15$01$0F$00$00$00$01$00$00$00$01$00$00$00$01$00$00$00$01$00$00$00$01$00$00$00$01$00$00$00$01$00$00$00$01$0F$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"));

        for (byte[] aByteArray : bytes) {
            assertArrayEquals(aByteArray, factory.parse(aByteArray, 0, type).getBytes());
        }
    }
}