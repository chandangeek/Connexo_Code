package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 16:36:31
 */
public class EncryptionTest {

    private static final String KEYC = "c34c052cc0da8d73451afe5f03be297f";
    private static final String KEYT = "c34c052cc0da8d73451afe5f03be297f";
    private static final String KEYF = "c34c052cc0da8d73451afe5f03be297f";

    private byte[] unencryptedTestFrame = ProtocolTools.getBytesFromHexString("0A0000003F53013030303030310702020A0A0B0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FB31CFA800000D", "");
    private byte[] encryptedTestFrame = ProtocolTools.getBytesFromHexString("0A0000007FC6E17593FF989FCFDAA64C1C2E6773D13BF18C2479F1F577A012087E54E56A8DEF9B849EABBCF1661259CC6953881DE649FCB03FB4F2CE6CAD5E20B104C0F82AFBC76696AC6DE041B53A19F1BEBC0E009A0E3253B83ACCBC245536CA5C220728D0C246062BDDB5279C13988CAAF8E48C474847C0B973D59668E46AA5B90E91E78F08FFD31E33722A0D", "");

    @Test
    public void testEncryption() {
        CTREncryption ctrEncryption = new CTREncryption(KEYC, KEYT, KEYF);
        Frame request = new GPRSFrame();
        Frame unencryptedFrame = new GPRSFrame();
        try {
            Frame encrypted = ctrEncryption.encryptFrame(request);
            unencryptedFrame = (GPRSFrame) ctrEncryption.decryptFrame(encrypted);
        } catch (CtrCipheringException e) {
            fail(e.getMessage());
        }
        assertArrayEquals(request.getBytes(), unencryptedFrame.getBytes());
    }

    @Test
    public void testDecryptCPA() throws Exception {
        String keyC = "32323232323232323232323232323232";
        byte[] keyCBytes = ProtocolTools.getBytesFromHexString(keyC, "");

        GPRSFrame unencryptedFrame = new GPRSFrame().parse(unencryptedTestFrame, 0);
        unencryptedFrame.generateAndSetCpa(keyCBytes);

        GPRSFrame encryptedFrame = new GPRSFrame().parse(encryptedTestFrame, 0);
        CTREncryption ctrEncryption = new CTREncryption(keyC, keyC, keyC);
        GPRSFrame decrypted = (GPRSFrame) ctrEncryption.decryptFrame(encryptedFrame);
        assertEquals(unencryptedFrame.getCpa(), encryptedFrame.getCpa());
        assertEquals(unencryptedFrame.getCpa(), decrypted.getCpa());

    }


}
