package com.energyict.genericprotocolimpl.elster.ctr.object;

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
        /*byte[] bytes = ProtocolTools.getBytesFromHexString(
                "$09$00$EF" +
                "$28$A4$E3$23$93$28$F1$01$09$2B$9C$22" +
                "$01$09$2B$9C$22" +
                "$01$09$2B$9C" +
                "$01$09$2B$9C$22$29" +
                "$01$09$2B" +
                "$01$09$2B$9C" +
                "$01" +
                "$0E" +
                "$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"
        );*/
        byte[] bytes = ProtocolTools.getBytesFromHexString("$0C$00$FF$12$34$56$78$90$12$34$0F$00$00$00$00$00$00$00");

        CTRObjectFactory factory = new CTRObjectFactory();
        AbstractCTRObject ctrObject = factory.parse(bytes, 0);

        assertArrayEquals(bytes, ctrObject.getBytes());
    }
}