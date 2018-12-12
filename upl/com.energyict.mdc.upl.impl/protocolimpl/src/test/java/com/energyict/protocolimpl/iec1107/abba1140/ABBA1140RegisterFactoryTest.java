package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author: sva
 * @since: 26/10/12 (9:57)
 */
public class ABBA1140RegisterFactoryTest {

    @Test
    public void TestParsingOfCTPrimary() throws IOException {
        ABBA1140RegisterFactory registerFactory = new ABBA1140RegisterFactory();
        ABBA1140Register ctPrimary = registerFactory.getCTPrimary();
        ABBA1140Register ctSecundary = registerFactory.getCTSecundary();
        byte[] data = ProtocolTools.getBytesFromHexString("0000012CABCDEF00", "");

        // Business methods
        Long ctPrimaryValue = (Long) ctPrimary.parse(data);
        Long ctSecundaryValue = (Long) ctSecundary.parse(data);

        // Asserts
        assertEquals((Long) 300L, ctPrimaryValue);
        assertEquals((Long) 2882400000L, ctSecundaryValue);
    }
}