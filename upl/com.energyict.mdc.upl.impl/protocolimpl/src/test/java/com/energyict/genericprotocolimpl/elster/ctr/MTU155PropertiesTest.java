package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 12-okt-2010
 * Time: 8:43:40
 */
public class MTU155PropertiesTest {

    private static final int TEST_ROUNDS = 128;

    @Test
    public void testGetOptionalKeys() throws Exception {
        assertNotNull(new MTU155Properties().getOptionalKeys());
    }

    @Test
    public void testGetRequiredKeys() throws Exception {
        assertNotNull(new MTU155Properties().getRequiredKeys());
    }

    @Test
    public void testGetRetries() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        for (int i = 0; i < TEST_ROUNDS; i++) {
            properties.addProperty(MTU155Properties.RETRIES, String.valueOf(i));
            assertEquals(i, properties.getRetries());
        }
    }

    @Test
    public void testGetTimeout() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        for (int i = 0; i < TEST_ROUNDS; i++) {
            properties.addProperty(MTU155Properties.TIMEOUT, String.valueOf(i));
            assertEquals(i, properties.getTimeout());
        }
    }

    @Test
    public void testGetExtractInstallationDate() {
        MTU155Properties properties = new MTU155Properties();
        for (int i = 0; i < TEST_ROUNDS; i++) {
            properties.addProperty(MTU155Properties.EXTRACTINSTALLATIONDATE, String.valueOf(i));
            assertEquals(i, properties.getExtractInstallationDate());
        }
    }

    @Test
    public void testGetDelayAfterError() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        for (int i = 0; i < TEST_ROUNDS; i++) {
            properties.addProperty(MTU155Properties.DELAY_AFTER_ERROR, String.valueOf(i));
            assertEquals(i, properties.getDelayAfterError());
        }
    }

    @Test
    public void testGetForcedDelay() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        for (int i = 0; i < TEST_ROUNDS; i++) {
            properties.addProperty(MTU155Properties.FORCED_DELAY, String.valueOf(i));
            assertEquals(i, properties.getForcedDelay());
        }
    }

    @Test
    public void testGetKeyC() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        for (int i = 0; i < TEST_ROUNDS; i++) {
            byte[] randomValue = new byte[16];
            new Random().nextBytes(randomValue);
            String randomKey = ProtocolTools.getHexStringFromBytes(randomValue, "");
            properties.addProperty(MTU155Properties.KEYC, randomKey);
            assertEquals(randomKey, properties.getKeyC());
            assertArrayEquals(randomValue, properties.getKeyCBytes());
        }
    }

    @Test
    public void testGetKeyT() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        for (int i = 0; i < TEST_ROUNDS; i++) {
            byte[] randomValue = new byte[16];
            new Random().nextBytes(randomValue);
            String randomKey = ProtocolTools.getHexStringFromBytes(randomValue, "");
            properties.addProperty(MTU155Properties.KEYT, randomKey);
            assertEquals(randomKey, properties.getKeyT());
            assertArrayEquals(randomValue, properties.getKeyTBytes());
        }
    }

    @Test
    public void testGetKeyF() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        for (int i = 0; i < TEST_ROUNDS; i++) {
            byte[] randomValue = new byte[16];
            new Random().nextBytes(randomValue);
            String randomKey = ProtocolTools.getHexStringFromBytes(randomValue, "");
            properties.addProperty(MTU155Properties.KEYF, randomKey);
            assertEquals(randomKey, properties.getKeyF());
            assertArrayEquals(randomValue, properties.getKeyFBytes());
        }
    }

    @Test
    public void testGetPassword() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        for (int i = 0; i < TEST_ROUNDS; i++) {
            byte[] randomValue = new byte[8];
            new Random().nextBytes(randomValue);
            String randomPassword = ProtocolTools.getHexStringFromBytes(randomValue, "");
            properties.addProperty(MTU155Properties.PASSWORD, randomPassword);
            assertEquals(randomPassword, properties.getPassword());
        }
    }

    @Test
    public void testGetAddress() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        for (int i = 0; i < TEST_ROUNDS; i++) {
            properties.addProperty(MTU155Properties.FORCED_DELAY, String.valueOf(i));
            assertEquals(i, properties.getForcedDelay());
        }
    }

    @Test
    public void testToString() throws Exception {
        assertNotNull(new MTU155Properties().toString());
        assertTrue(new MTU155Properties().toString().length() > 0);
    }

}
