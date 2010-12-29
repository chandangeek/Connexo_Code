package com.energyict.protocolimpl.dlms.elgama;

import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;

/**
 * Copyrights EnergyICT
 * Date: 29-dec-2010
 * Time: 14:40:13
 */
public class G3BTest extends TestCase {

    public void test() {
        byte[] bytes = ProtocolTools.getBytesFromHexString("$80$FF$FF");
        ProtocolTools.getUnsignedIntFromBytes(bytes);
    }
}
