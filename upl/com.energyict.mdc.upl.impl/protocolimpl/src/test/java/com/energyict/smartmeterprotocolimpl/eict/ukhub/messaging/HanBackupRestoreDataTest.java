package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 17-aug-2011
 * Time: 15:30:33
 */
public class HanBackupRestoreDataTest {

    @Test
    public void testHanBackupRestoreData() throws Exception {

        byte[] berEncodedStructure = new byte[]{1,4,9,8,31,32,33,34,35,36,37,38,9,16,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,9,16,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,9,8,31,32,33,34,35,36,37,38,1,1,2,3,9,1,39,9,20,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,9,8,7,(byte)219,-1,-1,-1,-1,-1,-1};
        byte[] expectedDataStruct = new byte[]{2,2,9,8,31,32,33,34,35,36,37,38,1,1,2,2,9,1,39,9,20,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        HanBackupRestoreData hbrd = new HanBackupRestoreData(berEncodedStructure, 0, 0);
        assertArrayEquals(expectedDataStruct, hbrd.getRestoreData().getBEREncodedByteArray());
    }
}
