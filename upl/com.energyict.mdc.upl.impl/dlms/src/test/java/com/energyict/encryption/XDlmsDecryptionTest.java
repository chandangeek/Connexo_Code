/**
 *
 */
package com.energyict.encryption;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author jme
 */
public class XDlmsDecryptionTest {

    private static final byte CONTROL_BYTE = 0x30;
    private static final byte[] SYSTEMTITLE = new byte[]{0x30, 0x31, 0x35, 0x30, 0x32, 0x33};
    private static final byte[] FRAME_COUNTER = new byte[]{0x00, 0x00, 0x00, 0x38};
    private static final int SECURITY_SUITE = 0;
    private static final byte[] GLOBALKEY = new byte[]{
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
            (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
            (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
            (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F
    };
    private static final byte[] AUTHKEY = new byte[]{
            (byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3,
            (byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7,
            (byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xDB,
            (byte) 0xDC, (byte) 0xDD, (byte) 0xDE, (byte) 0xDF
    };
    private static final byte[] PLAINTEXT = new byte[]{
            (byte) 0x08, (byte) 0x00, (byte) 0x06, (byte) 0x5F,
            (byte) 0x1F, (byte) 0x04, (byte) 0x00, (byte) 0x1C,
            (byte) 0x02, (byte) 0x20, (byte) 0x00, (byte) 0xE1,
            (byte) 0x00, (byte) 0x01
    };
    private static byte[] CIPHERED = new byte[]{
            (byte) 0x34, (byte) 0x86, (byte) 0xE9, (byte) 0x77,
            (byte) 0x9A, (byte) 0x65, (byte) 0x51, (byte) 0x3B,
            (byte) 0x30, (byte) 0x5A, (byte) 0x91, (byte) 0xEE,
            (byte) 0x1F, (byte) 0x86
    };
    private static byte[] CIPHERED_INVALID = new byte[]{
            (byte) 0x00, (byte) 0x86, (byte) 0x00, (byte) 0x77,
            (byte) 0x00, (byte) 0x65, (byte) 0x00, (byte) 0x3B,
            (byte) 0x00, (byte) 0x5A, (byte) 0x00, (byte) 0xEE,
            (byte) 0x00, (byte) 0x86
    };
    private static byte[] TAG = new byte[]{
            (byte) 0xD1, (byte) 0xEE, (byte) 0x28, (byte) 0xA7,
            (byte) 0xD4, (byte) 0xAC, (byte) 0x27, (byte) 0xC1,
            (byte) 0xAD, (byte) 0x56, (byte) 0xB8, (byte) 0x02
    };

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setSystemTitle(byte[])}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetSystemTitleNull() {
        new XDlmsDecryption(SECURITY_SUITE).setSystemTitle(null);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setSystemTitle(byte[])}.
     */
    @Test
    public final void testSetSystemTitle() {
        try {
            for (int i = 0; i < 20; i++) {
                byte[] systemTitle = new byte[i];
                Arrays.fill(systemTitle, (byte) i);
                XDlmsDecryption xdlms = new XDlmsDecryption(SECURITY_SUITE);
                xdlms.setSystemTitle(systemTitle);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setFrameCounter(byte[])}.
     */
    @Test
    public final void testSetFrameCounter() {
        try {
            new XDlmsDecryption(SECURITY_SUITE).setFrameCounter(new byte[4]);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setFrameCounter(byte[])}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetFrameCounterNull() {
        new XDlmsDecryption(SECURITY_SUITE).setFrameCounter(null);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setFrameCounter(byte[])}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetFrameCounterToShort() {
        new XDlmsDecryption(SECURITY_SUITE).setFrameCounter(new byte[3]);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setFrameCounter(byte[])}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetFrameCounterToLong() {
        new XDlmsDecryption(SECURITY_SUITE).setFrameCounter(new byte[5]);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setGlobalKey(byte[])}.
     */
    @Test
    public final void testSetGlobalKey() {
        try {
            new XDlmsDecryption(SECURITY_SUITE).setGlobalKey(new byte[16]);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setGlobalKey(byte[])}.
     */
    @Test(expected = DeviceConfigurationException.class)
    public final void testSetGlobalKeyNull() {
        new XDlmsDecryption(SECURITY_SUITE).setGlobalKey(null);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setGlobalKey(byte[])}.
     */
    @Test(expected = DeviceConfigurationException.class)
    public final void testSetGlobalKeyToLong() {
        new XDlmsDecryption(SECURITY_SUITE).setGlobalKey(new byte[17]);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setGlobalKey(byte[])}.
     */
    @Test(expected = DeviceConfigurationException.class)
    public final void testSetGlobalKeyToShort() {
        new XDlmsDecryption(SECURITY_SUITE).setGlobalKey(new byte[15]);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setAuthenticationKey(byte[])}.
     */
    @Test
    public final void testSetAuthenticationKey() {
        try {
            new XDlmsDecryption(SECURITY_SUITE).setAuthenticationKey(new byte[16]);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setAuthenticationKey(byte[])}.
     */
    @Test(expected = DeviceConfigurationException.class)
    public final void testSetAuthenticationKeyNull() {
        new XDlmsDecryption(SECURITY_SUITE).setAuthenticationKey(null);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setAuthenticationKey(byte[])}.
     */
    @Test(expected = DeviceConfigurationException.class)
    public final void testSetAuthenticationKeyToLong() {
        new XDlmsDecryption(SECURITY_SUITE).setAuthenticationKey(new byte[17]);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setAuthenticationKey(byte[])}.
     */
    @Test(expected = DeviceConfigurationException.class)
    public final void testSetAuthenticationKeyToShort() {
        new XDlmsDecryption(SECURITY_SUITE).setAuthenticationKey(new byte[15]);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setCipheredText(byte[])}.
     */
    @Test
    public final void testSetCipheredText() {
        try {
            for (int i = 0; i < 255; i++) {
                byte[] cipheredText = new byte[i];
                Arrays.fill(cipheredText, (byte) i);
                new XDlmsDecryption(SECURITY_SUITE).setCipheredText(cipheredText);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setCipheredText(byte[])}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetCipheredTextNull() {
        new XDlmsDecryption(SECURITY_SUITE).setCipheredText(null);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setAuthenticationTag(byte[])}.
     */
    @Test
    public final void testSetAuthenticationTag() {
        try {
            new XDlmsDecryption(SECURITY_SUITE).setAuthenticationTag(new byte[12]);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setAuthenticationTag(byte[])}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetAuthenticationTagNull() {
        new XDlmsDecryption(SECURITY_SUITE).setAuthenticationTag(null);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setAuthenticationTag(byte[])}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetAuthenticationTagToLong() {
        new XDlmsDecryption(SECURITY_SUITE).setAuthenticationTag(new byte[13]);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setAuthenticationTag(byte[])}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetAuthenticationTagToShort() {
        new XDlmsDecryption(SECURITY_SUITE).setAuthenticationTag(new byte[11]);
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#setSecurityControlByte(byte)}.
     */
    @Test
    public final void testSetSecurityControlByte() {
        for (int secCtrlByte = 0; secCtrlByte < 255; secCtrlByte++) {
            try {
                new XDlmsDecryption(SECURITY_SUITE).setSecurityControlByte((byte) secCtrlByte);
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#generatePlainText()}.
     */
    @Test
    public final void testGeneratePlainText() {
        try {
            XDlmsDecryption xdlms = new XDlmsDecryption(SECURITY_SUITE);
            xdlms.setCipheredText(CIPHERED);
            xdlms.setSystemTitle(SYSTEMTITLE);
            xdlms.setFrameCounter(FRAME_COUNTER);
            xdlms.setGlobalKey(GLOBALKEY);
            xdlms.setAuthenticationKey(AUTHKEY);
            xdlms.setSecurityControlByte(CONTROL_BYTE);
            xdlms.setAuthenticationTag(TAG);
            assertArrayEquals(PLAINTEXT, xdlms.generatePlainText());
        } catch (ConnectionException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = ConnectionException.class)
    public final void testGeneratePlainTextFromInvalid() throws ConnectionException {
        XDlmsDecryption xdlms = new XDlmsDecryption(SECURITY_SUITE);
        xdlms.setCipheredText(CIPHERED_INVALID);
        xdlms.setSystemTitle(SYSTEMTITLE);
        xdlms.setFrameCounter(FRAME_COUNTER);
        xdlms.setGlobalKey(GLOBALKEY);
        xdlms.setAuthenticationKey(AUTHKEY);
        xdlms.setSecurityControlByte(CONTROL_BYTE);
        xdlms.setAuthenticationTag(TAG);
        assertArrayEquals(PLAINTEXT, xdlms.generatePlainText());
    }

    /**
     * Test method for {@link com.energyict.encryption.XDlmsDecryption#toString()}.
     */
    @Test
    public final void testToString() {
        XDlmsDecryption xdlms = new XDlmsDecryption(SECURITY_SUITE);
        assertNotNull(xdlms.toString());
        xdlms.setCipheredText(CIPHERED);
        assertNotNull(xdlms.toString());
        xdlms.setSystemTitle(SYSTEMTITLE);
        assertNotNull(xdlms.toString());
        xdlms.setFrameCounter(FRAME_COUNTER);
        assertNotNull(xdlms.toString());
        xdlms.setGlobalKey(GLOBALKEY);
        assertNotNull(xdlms.toString());
        xdlms.setAuthenticationKey(AUTHKEY);
        assertNotNull(xdlms.toString());
        xdlms.setSecurityControlByte(CONTROL_BYTE);
        assertNotNull(xdlms.toString());
        xdlms.setAuthenticationTag(TAG);
        xdlms.setCipheredText(CIPHERED_INVALID);
        assertNotNull(xdlms.toString());
    }

}
