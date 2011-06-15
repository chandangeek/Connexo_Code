package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.profile;

import com.energyict.obis.ObisCode;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 15-jun-2011
 * Time: 11:25:53
 */
public class ProfileConfigurationTest {

    @Test
    public void parseTest() throws IOException {
        ProfileConfiguration pc = new ProfileConfiguration("1.0.99.2.0.255", ":1", 900);
        assertEquals(ObisCode.fromString("1.0.99.2.0.255"), pc.getProfileObisCode());
        assertEquals(900, pc.getProfileInterval());
        assertEquals(1, pc.getNrOfChannels());
        assertEquals(0, pc.getCapturedObjectIndexForChannel(0));

        pc = new ProfileConfiguration("1.0.99.2.0.255", "1.0.99.1.0.255:1-2-4-5", 86400);
        assertEquals(86400, pc.getProfileInterval());
        assertEquals(4, pc.getNrOfChannels());
        assertEquals(ObisCode.fromString("1.0.99.1.0.255"), pc.getProfileObisCode());
        assertEquals(0, pc.getCapturedObjectIndexForChannel(0));
        assertEquals(1, pc.getCapturedObjectIndexForChannel(1));
        assertEquals(3, pc.getCapturedObjectIndexForChannel(2));
        assertEquals(4, pc.getCapturedObjectIndexForChannel(3));
        try {
            assertEquals(4, pc.getCapturedObjectIndexForChannel(4));
        } catch (IOException e) {
            assertEquals("Invalid channelIndex  (4), config has only 4 channels.", e.getMessage());
        }

        pc = new ProfileConfiguration("1.0.99.2.0.255", "1.0.99.1.0.255", 86400);
        assertEquals(ObisCode.fromString("1.0.99.1.0.255"), pc.getProfileObisCode());
        assertEquals(0, pc.getCapturedObjectIndexForChannel(0));

        pc = new ProfileConfiguration("1.0.99.2.0.255", "", 86400);
        assertEquals(ObisCode.fromString("1.0.99.2.0.255"), pc.getProfileObisCode());
        assertEquals(0, pc.getNrOfChannels());
    }

}
