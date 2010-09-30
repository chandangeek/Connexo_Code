package com.energyict.genericprotocolimpl.elster.ctr.object;

import org.junit.Test;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 12:52:55
 */
public class CTRObjectFactoryTest {

    @Test
    public void testParse() throws Exception {
        byte[] bytes = new byte[1024];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0x05;
        }

        bytes[0] = 0x04;
        bytes[1] = 0x60;

        CTRObjectFactory factory = new CTRObjectFactory();
        AbstractCTRObject ctrObject = factory.parse(bytes, 0);

        byte[] bytes2 = ctrObject.getBytes();
        //System.out.println(ctrObject.getClass().getName());


    }
}
