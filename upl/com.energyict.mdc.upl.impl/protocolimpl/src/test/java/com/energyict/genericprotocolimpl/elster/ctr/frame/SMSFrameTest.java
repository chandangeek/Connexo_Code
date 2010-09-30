package com.energyict.genericprotocolimpl.elster.ctr.frame;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 12:07:10
 */
public class SMSFrameTest {

    byte[] correctFrame = ProtocolTools.getBytesFromHexString(
            "$00$00$00$00$4A$32$30$00$B3$00$12$34$56$78$90$00" +
                    "$00$00$00$09$00$44$26$44$20$45$6C$65$74$74$72$6F" +
                    "$20$49$4D$50$4C$52$49$4D$50$4C$52$30$2E$30$32$34" +
                    "$41$41$41$52$31$30$01$00$03$E8$55$E0$74$9C$41$78" +
                    "$78$78$20$20$20$20$20$20$20$20$20$20$20$20$20$20" +
                    "$20$20$04$3A$00$00$00$00$00$00$00$00$00$00$00$00" +
                    "$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00" +
                    "$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00" +
                    "$00$00$00$00$00$00$00$00$00$01$45$1C"
    );

    @Test
    public void testParse() throws Exception {
        assertArrayEquals(correctFrame, new SMSFrame().parse(correctFrame, 0).getBytes());
    }

    @Test
    public void testGetBytes() throws Exception {
        assertArrayEquals(correctFrame, new SMSFrame().parse(correctFrame, 0).getBytes());
    }

}
