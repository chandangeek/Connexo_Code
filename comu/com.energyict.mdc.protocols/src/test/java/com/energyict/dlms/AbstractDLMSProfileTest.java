package com.energyict.dlms;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Copyrights
 * Date: 3/06/11
 * Time: 10:58
 */
public class AbstractDLMSProfileTest {

    private static final String[] VALID_OBISCODES = new String[]{
            "1.1.0.8.0.255",
            "4.0.0.5.10.255",
            "5.0.0.5.1.255",
            "6.0.0.5.1.255",
            "7.0.3.0.0.255",
            "7.0.3.1.0.255",
            "7.0.13.0.0.255",
            "7.0.13.1.0.255",
            "7.0.13.2.0.255",
            "7.0.33.2.0.255",
            "7.0.51.0.0.255",
            "7.0.52.0.0.255",
            "7.0.53.0.0.255",
            "7.0.53.2.0.255",
            "7.0.53.11.0.255",
            "7.0.53.12.0.255",
            "7.0.54.0.0.255",
            "7.0.41.0.0.255",
            "7.0.41.2.0.255",
            "7.0.41.3.0.255",
            "7.0.42.0.0.255",
            "7.0.42.2.0.255",
            "7.0.42.3.0.255",
            "8.0.0.7.1.255",
            "9.0.0.7.1.255"
    };

    private static final String[] INVALID_OBISCODES = new String[]{
            "0.0.96.10.1.255"
    };

    private class DLMSProfileToTest extends AbstractDLMSProfile {

        protected CosemObjectFactory getCosemObjectFactory() {
            return null;
        }

        protected ObisCode getCorrectedObisCode(ObisCode baseObisCode) {
            return baseObisCode;
        }
    }

    @Test
    public void testGetCorrectedObisCode() throws Exception {
        DLMSProfileToTest profile = new DLMSProfileToTest();
        for (String validObisCode : VALID_OBISCODES) {
            ObisCode obisCode = ObisCode.fromString(validObisCode);
            assertTrue("ObisCode [" + obisCode + "] should be valid, but was not!", profile.isValidChannelObisCode(obisCode));
        }

        for (String validObisCode : INVALID_OBISCODES) {
            ObisCode obisCode = ObisCode.fromString(validObisCode);
            assertFalse("ObisCode [" + obisCode + "] should be invalid, but was not!", profile.isValidChannelObisCode(obisCode));
        }
    }

}
