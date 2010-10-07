package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 16:36:31
 */
public class EncryptionTest extends TestCase {

    @Test
    public void testEncryption() {
        GPRSFrame frame = new GPRSFrame();
        GPRSFrame framex = new GPRSFrame();

        GPRSFrame frame2 = new GPRSFrame();
        GPRSFrame frame3 = new GPRSFrame();

        CTREncryption ctrEncryption = new CTREncryption("c34c052cc0da8d73451afe5f03be297f", "c34c052cc0da8d73451afe5f03be297f", "c34c052cc0da8d73451afe5f03be297f");

        frame = (GPRSFrame) ctrEncryption.setCpa((Frame) frame);
        frame.setCrc();

        framex = (GPRSFrame) ctrEncryption.setCpa((Frame) framex);
        framex.setCrc();


        try {
            frame2 = (GPRSFrame) ctrEncryption.encryptFrame((Frame) frame);
        } catch (CtrCipheringException e) {
            e.printStackTrace();
        }

        try {
            frame3 = (GPRSFrame) ctrEncryption.decryptFrame((Frame) frame2);
        } catch (CtrCipheringException e) {
            e.printStackTrace();
        }

        assertArrayEquals(framex.getBytes(), frame3.getBytes());

    }
}
