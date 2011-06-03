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
            "1.1.0.8.0.255"
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
