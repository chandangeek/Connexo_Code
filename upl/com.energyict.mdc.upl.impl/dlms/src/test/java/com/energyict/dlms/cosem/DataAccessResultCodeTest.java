package com.energyict.dlms.cosem;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Copyrights EnergyICT
 * Date: 9/01/12
 * Time: 12:47
 */
public class DataAccessResultCodeTest {

    @Test
    public void testByResultCode() throws Exception {
        for (DataAccessResultCode code : DataAccessResultCode.values()) {
            DataAccessResultCode byResultCode = DataAccessResultCode.byResultCode(code.getResultCode());
            assertNotNull(byResultCode);
            assertEquals(byResultCode, code);
            assertEquals(byResultCode.getDescription(), code.getDescription());
            assertEquals(byResultCode.getResultCode(), code.getResultCode());
        }

        assertNull(DataAccessResultCode.byResultCode(-1));

    }

}
