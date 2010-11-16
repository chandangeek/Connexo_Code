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

    @Test               //Tests parsing of common objects
    public void testParse() throws Exception {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType();
        type.setHasAccessDescriptor(true);
        type.setHasDefaultValue(true);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        type.setHasIdentifier(true);
        type.setRegisterQuery(true);

        byte[] bytes0 = ProtocolTools.getBytesFromHexString("$09$02$0F$31$32$33$00$00$0F$00$00$00$00$00");
        byte[] bytes1 = ProtocolTools.getBytesFromHexString("$02$16$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes2 = ProtocolTools.getBytesFromHexString("$02$00$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes3 = ProtocolTools.getBytesFromHexString("$02$10$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes4 = ProtocolTools.getBytesFromHexString("$02$30$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes5 = ProtocolTools.getBytesFromHexString("$01$00$0F$00$00$01$0F$00$00$00");
        byte[] bytes6 = ProtocolTools.getBytesFromHexString("$01$20$0F$00$00$01$0F$00$00$00");
        byte[] bytes7 = ProtocolTools.getBytesFromHexString("$04$00$0F$00$00$01$0F$00$00$00");
        byte[] bytes8 = ProtocolTools.getBytesFromHexString("$07$00$0F$00$00$01$0F$00$00$00");
        byte[] bytes9 = ProtocolTools.getBytesFromHexString("$0A$00$0F$00$00$01$0F$00$00$00");
        byte[] bytes10 = ProtocolTools.getBytesFromHexString("$0A$16$0F$00$00$01$0F$00$00$00");
        byte[] bytes11 = ProtocolTools.getBytesFromHexString("$02$37$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes12 = ProtocolTools.getBytesFromHexString("$02$38$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes13 = ProtocolTools.getBytesFromHexString("$02$39$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes14 = ProtocolTools.getBytesFromHexString("$0C$00$0F$12$34$56$78$90$00$00$0F$00$00$00$00$00$00$00");
        byte[] bytes15 = ProtocolTools.getBytesFromHexString("$02$36$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes16 = ProtocolTools.getBytesFromHexString("$02$3A$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes17 = ProtocolTools.getBytesFromHexString("$02$3B$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes18 = ProtocolTools.getBytesFromHexString("$02$3C$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes19 = ProtocolTools.getBytesFromHexString("$10$10$0F$00$03$0F$00$00");
        byte[] bytes20 = ProtocolTools.getBytesFromHexString("$02$50$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes21 = ProtocolTools.getBytesFromHexString("$02$51$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes22 = ProtocolTools.getBytesFromHexString("$02$52$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes23 = ProtocolTools.getBytesFromHexString("$02$53$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes24 = ProtocolTools.getBytesFromHexString("$02$54$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes25 = ProtocolTools.getBytesFromHexString("$02$55$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes26 = ProtocolTools.getBytesFromHexString("$12$00$0F$02$0F$00");
        byte[] bytes26b = ProtocolTools.getBytesFromHexString("$12$00$FF");
        byte[] bytes26c = ProtocolTools.getBytesFromHexString("$0D$90$0F$00$01$0F$00$00");
        byte[] bytes27 = ProtocolTools.getBytesFromHexString("$12$10$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes28 = ProtocolTools.getBytesFromHexString("$12$20$0F$00$01$0F$00$00");
        byte[] bytes29 = ProtocolTools.getBytesFromHexString("$02$55$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes30 = ProtocolTools.getBytesFromHexString("$0E$C0$0F$01$0F$00");
        byte[] bytes31 = ProtocolTools.getBytesFromHexString("$08$12$0F$00$01$0F$00$00");
        byte[] bytes32 = ProtocolTools.getBytesFromHexString("$0F$50$0F$00$00$01$00$00$01$00$00$01$00$00$01$0F$00$00$00$00$00$00$00$00$00$00$00$00");
        byte[] bytes33 = ProtocolTools.getBytesFromHexString("$0F$51$0F$00$00$01$00$00$01$00$00$01$00$00$01$0F$00$00$00$00$00$00$00$00$00$00$00$00");
        byte[] bytes34 = ProtocolTools.getBytesFromHexString("$0F$52$0F$00$00$01$00$00$01$00$00$01$00$00$01$0F$00$00$00$00$00$00$00$00$00$00$00$00");

        byte[] bytes35 = ProtocolTools.getBytesFromHexString("$09$03$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes36 = ProtocolTools.getBytesFromHexString("$09$04$0F$00$00$00$00$00$01$0F$00$00$00$00$00$00");
        byte[] bytes37 = ProtocolTools.getBytesFromHexString("$09$07$0F$00$00$00$01$0F$00$00$00$00");
        byte[] bytes38 = ProtocolTools.getBytesFromHexString("$09$05$0F$00$00$01$0F$00$00$00");

        assertArrayEquals(bytes0, factory.parse(bytes0, 0, type).getBytes());
        assertArrayEquals(bytes1, factory.parse(bytes1, 0, type).getBytes());
        assertArrayEquals(bytes2, factory.parse(bytes2, 0, type).getBytes());
        assertArrayEquals(bytes3, factory.parse(bytes3, 0, type).getBytes());
        assertArrayEquals(bytes4, factory.parse(bytes4, 0, type).getBytes());
        assertArrayEquals(bytes5, factory.parse(bytes5, 0, type).getBytes());
        assertArrayEquals(bytes6, factory.parse(bytes6, 0, type).getBytes());
        assertArrayEquals(bytes7, factory.parse(bytes7, 0, type).getBytes());
        assertArrayEquals(bytes8, factory.parse(bytes8, 0, type).getBytes());
        assertArrayEquals(bytes9, factory.parse(bytes9, 0, type).getBytes());
        assertArrayEquals(bytes10, factory.parse(bytes10, 0, type).getBytes());
        assertArrayEquals(bytes11, factory.parse(bytes11, 0, type).getBytes());
        assertArrayEquals(bytes12, factory.parse(bytes12, 0, type).getBytes());
        assertArrayEquals(bytes13, factory.parse(bytes13, 0, type).getBytes());
        assertArrayEquals(bytes14, factory.parse(bytes14, 0, type).getBytes());
        assertArrayEquals(bytes15, factory.parse(bytes15, 0, type).getBytes());
        assertArrayEquals(bytes16, factory.parse(bytes16, 0, type).getBytes());
        assertArrayEquals(bytes17, factory.parse(bytes17, 0, type).getBytes());
        assertArrayEquals(bytes18, factory.parse(bytes18, 0, type).getBytes());
        assertArrayEquals(bytes19, factory.parse(bytes19, 0, type).getBytes());
        assertArrayEquals(bytes20, factory.parse(bytes20, 0, type).getBytes());
        assertArrayEquals(bytes21, factory.parse(bytes21, 0, type).getBytes());
        assertArrayEquals(bytes22, factory.parse(bytes22, 0, type).getBytes());
        assertArrayEquals(bytes23, factory.parse(bytes23, 0, type).getBytes());
        assertArrayEquals(bytes24, factory.parse(bytes24, 0, type).getBytes());
        assertArrayEquals(bytes25, factory.parse(bytes25, 0, type).getBytes());
        assertArrayEquals(bytes26, factory.parse(bytes26, 0, type).getBytes());
        assertArrayEquals(bytes26b, factory.parse(bytes26b, 0, type).getBytes());
        assertArrayEquals(bytes26c, factory.parse(bytes26c, 0, type).getBytes());
        assertArrayEquals(bytes27, factory.parse(bytes27, 0, type).getBytes());
        assertArrayEquals(bytes28, factory.parse(bytes28, 0, type).getBytes());
        assertArrayEquals(bytes29, factory.parse(bytes29, 0, type).getBytes());
        assertArrayEquals(bytes30, factory.parse(bytes30, 0, type).getBytes());
        assertArrayEquals(bytes31, factory.parse(bytes31, 0, type).getBytes());
        assertArrayEquals(bytes32, factory.parse(bytes32, 0, type).getBytes());
        assertArrayEquals(bytes33, factory.parse(bytes33, 0, type).getBytes());
        assertArrayEquals(bytes34, factory.parse(bytes34, 0, type).getBytes());
        assertArrayEquals(bytes35, factory.parse(bytes35, 0, type).getBytes());
        assertArrayEquals(bytes36, factory.parse(bytes36, 0, type).getBytes());
        assertArrayEquals(bytes37, factory.parse(bytes37, 0, type).getBytes());
        assertArrayEquals(bytes38, factory.parse(bytes38, 0, type).getBytes());

    }
}