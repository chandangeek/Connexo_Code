package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 16:36:31
 */
public class EncryptionTest {

    private static final String KEYC = "c34c052cc0da8d73451afe5f03be297f";
    private static final String KEYT = "c34c052cc0da8d73451afe5f03be297f";
    private static final String KEYF = "c34c052cc0da8d73451afe5f03be297f";

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

}
