package com.energyict.protocolimpl.dlms.siemenszmd;

import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 21-jun-2011
 * Time: 15:23:24
 */
public class ZMDSecurityProviderTest {

    @Test
    public void testEncryptByManufacturer() throws Exception {
        Properties props = new Properties();
        props.put("Password", "0000000");

        ZMDSecurityProvider sProvider = new ZMDSecurityProvider(props);
        try {
            sProvider.associationEncryptionByManufacturer(new byte[0]);
            fail("We should have gotten an length-error when we try to encrypt an empty byteArray");
        } catch (Exception e) {
            if (e.getMessage().indexOf("RespondingAuthenticationValue should be 8 bytes instead of 0") < 0) {
                fail("We should have gotten a different error.");
            }
        }

        assertEquals(0, sProvider.associationEncryptionByManufacturer(null).length);
        String aValue1 = "12345670";    // Level0
        String rValue1 = "12345670";
        assertArrayEquals(rValue1.getBytes(), sProvider.associationEncryptionByManufacturer(aValue1.getBytes()));

        props = new Properties();
        props.put("Password", "5102074");
        sProvider = new ZMDSecurityProvider(props);
        String aValue2 = "18811E81";    // Level1    
        String rValue2 = "59831FC1";
        assertArrayEquals(rValue2.getBytes(), sProvider.associationEncryptionByManufacturer(aValue2.getBytes()));

        props = new Properties();
        props.put("Password", "1234567");
        sProvider = new ZMDSecurityProvider(props);
        String aValue3 = "8B729272";    // Level2
        String rValue3 = "9946C402";
        assertArrayEquals(rValue3.getBytes(), sProvider.associationEncryptionByManufacturer(aValue3.getBytes()));

        String aValue4 = "A348AA43";    // Level3
        String rValue4 = "B37CFEB3";
        assertArrayEquals(rValue4.getBytes(), sProvider.associationEncryptionByManufacturer(aValue4.getBytes()));

        String aValue5 = "52F959F4";    // Level4
        String rValue5 = "602DAF64";
        assertArrayEquals(rValue5.getBytes(), sProvider.associationEncryptionByManufacturer(aValue5.getBytes()));

        String aValue6 = "B48FBB85";    // Level5
        String rValue6 = "C6B301F5";
        assertArrayEquals(rValue6.getBytes(), sProvider.associationEncryptionByManufacturer(aValue6.getBytes()));        
    }

    @Test
    public void convertIntegerArrayToASCIIArrayTest() {
        byte[] testByte = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        assertArrayEquals(new byte[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46},
                ZMDSecurityProvider.convertIntegerArrayToASCIIArray(testByte));
    }

    @Test
    public void convertASCIIArrayToIntegerArrayTest() {
        byte[] testByte = new byte[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46};
        assertArrayEquals(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, ZMDSecurityProvider.convertASCIIArrayToIntegerArray(testByte));
    }

    @Test
    public void bitWiseAddTest() throws IOException {
        try {
            ZMDSecurityProvider.bitWiseAdd(null, null);
            fail("We should have gotten a nul-argument error when we try to ADD null arguments");
        } catch (Exception e) {
            if (e.getMessage().indexOf("Bitwise ADD-operation requires two not-null arguments.") < 0) {
                fail("We should have gotten a different error.");
            }
        }
        byte[] arg1 = new byte[]{0x01};
        byte[] arg2 = new byte[]{0x02};
        assertEquals(1, ZMDSecurityProvider.bitWiseAdd(arg1, arg2).length);
        assertEquals(3, ZMDSecurityProvider.bitWiseAdd(arg1, arg2)[0]);

        arg1 = new byte[]{0x01};
        arg2 = new byte[]{0x03};
        assertEquals(1, ZMDSecurityProvider.bitWiseAdd(arg1, arg2).length);
        assertEquals(4, ZMDSecurityProvider.bitWiseAdd(arg1, arg2)[0]);

        arg1 = new byte[]{0x0A};
        arg2 = new byte[]{0x05};
        assertEquals(1, ZMDSecurityProvider.bitWiseAdd(arg1, arg2).length);
        assertEquals(15, ZMDSecurityProvider.bitWiseAdd(arg1, arg2)[0]);

        arg1 = new byte[]{0x08, 0x03};
        arg2 = new byte[]{0x09, 0x02};
        assertArrayEquals(new byte[]{1, 5}, ZMDSecurityProvider.bitWiseAdd(arg1, arg2));
    }

    @Test
    public void bitWiseOrTest() throws IOException {
        try {
            ZMDSecurityProvider.bitWiseOr(null, null);
            fail("We should have gotten a nul-argument error when we try to OR null arguments");
        } catch (Exception e) {
            if (e.getMessage().indexOf("Bitwise OR-operation requires two not-null arguments.") < 0) {
                fail("We should have gotten a different error.");
            }
        }
        byte[] arg1 = new byte[]{0x01};
        byte[] arg2 = new byte[]{0x02};
        assertEquals(1, ZMDSecurityProvider.bitWiseOr(arg1, arg2).length);
        assertEquals(3, ZMDSecurityProvider.bitWiseOr(arg1, arg2)[0]);

        arg1 = new byte[]{0x01};
        arg2 = new byte[]{0x03};
        assertEquals(1, ZMDSecurityProvider.bitWiseOr(arg1, arg2).length);
        assertEquals(3, ZMDSecurityProvider.bitWiseOr(arg1, arg2)[0]);

        arg1 = new byte[]{0x0A};
        arg2 = new byte[]{0x05};
        assertEquals(1, ZMDSecurityProvider.bitWiseOr(arg1, arg2).length);
        assertEquals(15, ZMDSecurityProvider.bitWiseOr(arg1, arg2)[0]);

        arg1 = new byte[]{0x07, 0x03};
        arg2 = new byte[]{0x09, 0x02};
        assertEquals(2, ZMDSecurityProvider.bitWiseOr(arg1, arg2).length);
        assertArrayEquals(new byte[]{15, 3}, ZMDSecurityProvider.bitWiseOr(arg1, arg2));
    }

    @Test
    public void bitWiseXorTest() throws IOException {
        try {
            ZMDSecurityProvider.bitWiseXor(null, null);
            fail("We should have gotten a nul-argument error when we try to XOR null arguments");
        } catch (Exception e) {
            if (e.getMessage().indexOf("Bitwise XOR-operation requires two not-null arguments.") < 0) {
                fail("We should have gotten a different error.");
            }
        }
        byte[] arg1 = new byte[]{0x01};
        byte[] arg2 = new byte[]{0x02};
        assertEquals(1, ZMDSecurityProvider.bitWiseXor(arg1, arg2).length);
        assertEquals(3, ZMDSecurityProvider.bitWiseXor(arg1, arg2)[0]);

        arg1 = new byte[]{0x01};
        arg2 = new byte[]{0x03};
        assertEquals(1, ZMDSecurityProvider.bitWiseXor(arg1, arg2).length);
        assertEquals(2, ZMDSecurityProvider.bitWiseXor(arg1, arg2)[0]);

        arg1 = new byte[]{0x0A};
        arg2 = new byte[]{0x05};
        assertEquals(1, ZMDSecurityProvider.bitWiseXor(arg1, arg2).length);
        assertEquals(15, ZMDSecurityProvider.bitWiseXor(arg1, arg2)[0]);

        arg1 = new byte[]{0x07, 0x03};
        arg2 = new byte[]{0x09, 0x02};
        assertEquals(2, ZMDSecurityProvider.bitWiseXor(arg1, arg2).length);
        assertArrayEquals(new byte[]{14, 1}, ZMDSecurityProvider.bitWiseXor(arg1, arg2));
    }

    @Test
    public void bitWiseAddOrTest() throws IOException {
        try {
            ZMDSecurityProvider.bitWiseAddOr(null, null);
            fail("We should have gotten a nul-argument error when we try to Add/Or null arguments");
        } catch (Exception e) {
            if (e.getMessage().indexOf("Bitwise Add/Or-operation requires two not-null arguments.") < 0) {
                fail("We should have gotten a different error.");
            }
        }
        byte[] arg1 = new byte[]{0x01};
        byte[] arg2 = new byte[]{0x02};
        assertEquals(1, ZMDSecurityProvider.bitWiseAddOr(arg1, arg2).length);
        assertEquals(3, ZMDSecurityProvider.bitWiseAddOr(arg1, arg2)[0]);

        arg1 = new byte[]{0x08, 0x03};
        arg2 = new byte[]{0x09, 0x02};
        assertArrayEquals(new byte[]{1, 3}, ZMDSecurityProvider.bitWiseAddOr(arg1, arg2));

        arg1 = new byte[]{0x06, 0x05};
        arg2 = new byte[]{0x0C, 0x09};
        assertArrayEquals(new byte[]{2, 13}, ZMDSecurityProvider.bitWiseAddOr(arg1, arg2));

        arg1 = new byte[]{0x02, 0x05, 0x0C, 0x09};
        arg2 = new byte[]{0x0C, 0x03, 0x0A, 0x07};
        assertArrayEquals(new byte[]{14, 7, 6, 15}, ZMDSecurityProvider.bitWiseAddOr(arg1, arg2));

    }

    @Test
    public void bitWiseAddXorTest() throws IOException {
        try {
            ZMDSecurityProvider.bitWiseAddXor(null, null);
            fail("We should have gotten a nul-argument error when we try to Add/Xor null arguments");
        } catch (Exception e) {
            if (e.getMessage().indexOf("Bitwise Add/Xor-operation requires two not-null arguments.") < 0) {
                fail("We should have gotten a different error.");
            }
        }
        byte[] arg1 = new byte[]{0x01};
        byte[] arg2 = new byte[]{0x02};
        assertEquals(1, ZMDSecurityProvider.bitWiseAddXor(arg1, arg2).length);
        assertEquals(3, ZMDSecurityProvider.bitWiseAddXor(arg1, arg2)[0]);

        arg1 = new byte[]{0x07, 0x03};
        arg2 = new byte[]{0x09, 0x02};
        assertEquals(2, ZMDSecurityProvider.bitWiseAddXor(arg1, arg2).length);
        assertArrayEquals(new byte[]{0, 1}, ZMDSecurityProvider.bitWiseAddXor(arg1, arg2));

        arg1 = new byte[]{0x06, 0x05};
        arg2 = new byte[]{0x0C, 0x09};
        assertEquals(2, ZMDSecurityProvider.bitWiseAddXor(arg1, arg2).length);
        assertArrayEquals(new byte[]{2, 12}, ZMDSecurityProvider.bitWiseAddXor(arg1, arg2));

        arg1 = new byte[]{0x02, 0x05, 0x0C, 0x09};
        arg2 = new byte[]{0x0C, 0x03, 0x0A, 0x07};
        assertEquals(4, ZMDSecurityProvider.bitWiseAddXor(arg1, arg2).length);
        assertArrayEquals(new byte[]{14, 6, 6, 14}, ZMDSecurityProvider.bitWiseAddXor(arg1, arg2));
    }
}
