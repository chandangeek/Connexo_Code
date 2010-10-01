package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 1-okt-2010
 * Time: 1:30:46
 */
public class EncryptionStatusTest {

    @Test
    public void testGetDescription() throws Exception {
        assertNotNull(EncryptionStatus.NO_ENCRYPTION.getDescription());
        assertNotNull(EncryptionStatus.KEYC_ENCRYPTION.getDescription());
        assertNotNull(EncryptionStatus.KEYF_ENCRYPTION.getDescription());
        assertNotNull(EncryptionStatus.KEYT_ENCRYPTION.getDescription());
        assertNotNull(EncryptionStatus.UNKNOWN_ENCRYPTION.getDescription());
    }

    @Test
    public void testGetEncryptionStateBits() throws Exception {
        assertEquals(0x00, EncryptionStatus.NO_ENCRYPTION.getEncryptionStateBits());
        assertEquals(0x01, EncryptionStatus.KEYC_ENCRYPTION.getEncryptionStateBits());
        assertEquals(0x02, EncryptionStatus.KEYT_ENCRYPTION.getEncryptionStateBits());
        assertEquals(0x03, EncryptionStatus.KEYF_ENCRYPTION.getEncryptionStateBits());
        assertEquals(-1, EncryptionStatus.UNKNOWN_ENCRYPTION.getEncryptionStateBits());
    }

    @Test
    public void testFromEncryptionBits() throws Exception {
        assertEquals(EncryptionStatus.NO_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0xFC));
        assertEquals(EncryptionStatus.KEYC_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0xFD));
        assertEquals(EncryptionStatus.KEYT_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0xFE));
        assertEquals(EncryptionStatus.KEYF_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0xFF));

        assertEquals(EncryptionStatus.NO_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0x0C));
        assertEquals(EncryptionStatus.KEYC_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0x0D));
        assertEquals(EncryptionStatus.KEYT_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0x0E));
        assertEquals(EncryptionStatus.KEYF_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0x0F));

        assertEquals(EncryptionStatus.NO_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0x00));
        assertEquals(EncryptionStatus.KEYC_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0x01));
        assertEquals(EncryptionStatus.KEYT_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0x02));
        assertEquals(EncryptionStatus.KEYF_ENCRYPTION, EncryptionStatus.fromEncryptionBits(0x03));
    }
}
