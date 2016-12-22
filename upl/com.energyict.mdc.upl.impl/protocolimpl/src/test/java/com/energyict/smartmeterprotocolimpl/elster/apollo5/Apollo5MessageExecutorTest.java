package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 2/12/11
 * Time: 10:43
 */
public class Apollo5MessageExecutorTest {

    @Test
    public void test() throws IOException {
        String xml = "<SetPublicKeysOfAggregationGroup><KeyPair1>0102030405010203040501020304050102030405010203040501020304050101,0102030405010203040501020304050102030405010203040501020304050101</KeyPair1><KeyPair2>0102030405010203040501020304050102030405010203040501020304050101,0102030405010203040501020304050102030405010203040501020304050101</KeyPair2></SetPublicKeysOfAggregationGroup>";
        AS300DPETMessageExecutor executor = new AS300DPETMessageExecutor(new AS300DPET());
        List<String> keyPairs = executor.parseKeyPairs(MessageEntry.fromContent(xml).trackingId("0").finish());

        for (String keyPair : keyPairs) {
            String[] keys = keyPair.split(",");
            assertEquals(ProtocolTools.getBytesFromHexString(keys[0], "").length, 32);
            assertEquals(ProtocolTools.getBytesFromHexString(keys[1], "").length, 32);
        }

    }

}
