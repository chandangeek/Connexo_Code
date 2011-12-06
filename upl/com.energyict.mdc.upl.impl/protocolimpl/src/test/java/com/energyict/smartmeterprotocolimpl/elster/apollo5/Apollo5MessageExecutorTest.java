package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 2/12/11
 * Time: 10:43
 */
public class Apollo5MessageExecutorTest extends TestCase {

    @Test
    public void test() throws IOException {
        String xml = "<SetPublicKeysOfAggregationGroup><KeyPair1>0102030405010203040501020304050102030405010203040501020304050101,0102030405010203040501020304050102030405010203040501020304050101</KeyPair1><KeyPair2>0102030405010203040501020304050102030405010203040501020304050101,0102030405010203040501020304050102030405010203040501020304050101</KeyPair2></SetPublicKeysOfAggregationGroup>";
        Apollo5MessageExecutor executor = new Apollo5MessageExecutor(new Apollo5());
        List<String> keyPairs = executor.parseKeyPairs(new MessageEntry(xml, "0"));

        for (String keyPair : keyPairs) {
            String[] keys = keyPair.split(",");
            assertEquals(ProtocolTools.getBytesFromHexString(keys[0], "").length, 32);
            assertEquals(ProtocolTools.getBytesFromHexString(keys[1], "").length, 32);
        }

    }

}
