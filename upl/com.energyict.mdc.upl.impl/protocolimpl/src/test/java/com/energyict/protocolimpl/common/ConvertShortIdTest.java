package com.energyict.genericprotocolimpl.webrtu.common;


import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ConvertShortIdTest {
    private final static String EXPECTED_SHORT_ID_LANDIS_GYR = "FML1000013500003";
    private final static String INVALID_EXPECTED_SHORT_ID_LANDIS_GYR = "FL1000013500003";
    private final static String EXPECTED_SHORT_ID_FLONIDAN = "FLO1234567806303";
    private final static String ANOTHER_EXPECTED_SHORT_ID = "FLO1001650100603";
    private final static String INVALID_ANOTHER_EXPECTED_SHORT_ID = "FLO100165010";


    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    /**
     * Test the construction of the ShortID for MBus detection
     */
    @Test
    public void constructShortIdTest(){

        MbusProvider mp = new MbusProvider(null, false);

        try {
            Unsigned8 version = new Unsigned8(DLMSUtils.hexStringToByteArray("1100"),0);
            Unsigned8 deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
            Unsigned32 identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("0610000135"),0);
            Unsigned16 manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("1219ac"),0);
            String value = hexToASCII("1219ac");
            assertEquals(EXPECTED_SHORT_ID_LANDIS_GYR, mp.constructShortId(manufacturerId, identificationNumber, version, deviceType));

            version = new Unsigned8(DLMSUtils.hexStringToByteArray("113F"),0);
            deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
            identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("0612345678"),0);
            manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("12198F"),0);

            assertEquals(EXPECTED_SHORT_ID_FLONIDAN, mp.constructShortId(manufacturerId, identificationNumber, version, deviceType));

            mp = new MbusProvider(null, true);
            version = new Unsigned8(DLMSUtils.hexStringToByteArray("1106"),0);
            deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
            identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("060098d6f5"),0);
            manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("12198F"),0);

            assertEquals(ANOTHER_EXPECTED_SHORT_ID, mp.constructShortId(manufacturerId, identificationNumber, version, deviceType));

        } catch (IOException e) {
            // should not come here, the given data is in the correct format
            fail();
        }
    }

    private static String hexToASCII(String hexValue)
    {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2)
        {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }
    @Test
    public void retrieveMBusProprietiesFromShortId(){
        try {
            MbusProvider mp = new MbusProvider(null, false);
            Unsigned8 version = new Unsigned8(DLMSUtils.hexStringToByteArray("1100"),0);
            Unsigned8 deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
            Unsigned32 identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("0610000135"),0);
            Unsigned16 manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("1219ac"),0);


            assertEquals(mp.getVersion(EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), version.getValue());
            assertEquals(mp.getDeviceType(EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), deviceType.getValue());
            assertEquals(mp.getIdentificationNumber(EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), identificationNumber.getValue());
            assertEquals(mp.getManufacturerID(EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), manufacturerId.getValue());

            version = new Unsigned8(DLMSUtils.hexStringToByteArray("113F"),0);
            deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
            identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("0612345678"),0);
            manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("12198F"),0);

            assertEquals(mp.getVersion(EXPECTED_SHORT_ID_FLONIDAN).getValue(), version.getValue());
            assertEquals(mp.getDeviceType(EXPECTED_SHORT_ID_FLONIDAN).getValue(), deviceType.getValue());
            assertEquals(mp.getIdentificationNumber(EXPECTED_SHORT_ID_FLONIDAN).getValue(), identificationNumber.getValue());
            assertEquals(mp.getManufacturerID(EXPECTED_SHORT_ID_FLONIDAN).getValue(), manufacturerId.getValue());

            mp = new MbusProvider(null, true);
            version = new Unsigned8(DLMSUtils.hexStringToByteArray("1106"),0);
            deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
            identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("060098d6f5"),0);
            manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("12198F"),0);

            assertEquals(mp.getVersion(ANOTHER_EXPECTED_SHORT_ID).getValue(), version.getValue());
            assertEquals(mp.getDeviceType(ANOTHER_EXPECTED_SHORT_ID).getValue(), deviceType.getValue());
            assertEquals(mp.getIdentificationNumber(ANOTHER_EXPECTED_SHORT_ID).getValue(), identificationNumber.getValue());
            assertEquals(mp.getManufacturerID(ANOTHER_EXPECTED_SHORT_ID).getValue(), manufacturerId.getValue());


        } catch (IOException e) {
            // should not come here, the given data is in the correct format
            fail();
        }
    }

    @Test
    public void retrieveMBusProprietiesFromInvalidShortId(){
        boolean thrown = false;
        try {
            MbusProvider mp = new MbusProvider(null, false);
            Unsigned8 version = new Unsigned8(DLMSUtils.hexStringToByteArray("1100"),0);
            Unsigned8 deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
            Unsigned32 identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("0610000135"),0);
            Unsigned16 manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("1219ac"),0);


            assertEquals(mp.getVersion(INVALID_EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), version.getValue());
            assertEquals(mp.getDeviceType(INVALID_EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), deviceType.getValue());
            assertEquals(mp.getIdentificationNumber(INVALID_EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), identificationNumber.getValue());
            assertEquals(mp.getManufacturerID(INVALID_EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), manufacturerId.getValue());

            version = new Unsigned8(DLMSUtils.hexStringToByteArray("113F"),0);
            deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
            identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("0612345678"),0);
            manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("12198F"),0);

            assertEquals(mp.getVersion(INVALID_EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), version.getValue());
            assertEquals(mp.getDeviceType(INVALID_EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), deviceType.getValue());
            assertEquals(mp.getIdentificationNumber(INVALID_EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), identificationNumber.getValue());
            assertEquals(mp.getManufacturerID(INVALID_EXPECTED_SHORT_ID_LANDIS_GYR).getValue(), manufacturerId.getValue());

            mp = new MbusProvider(null, true);
            version = new Unsigned8(DLMSUtils.hexStringToByteArray("1106"),0);
            deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
            identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("060098d6f5"),0);
            manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("12198F"),0);

            assertEquals(mp.getVersion(INVALID_ANOTHER_EXPECTED_SHORT_ID).getValue(), version.getValue());
            assertEquals(mp.getDeviceType(INVALID_ANOTHER_EXPECTED_SHORT_ID).getValue(), deviceType.getValue());
            assertEquals(mp.getIdentificationNumber(INVALID_ANOTHER_EXPECTED_SHORT_ID).getValue(), identificationNumber.getValue());
            assertEquals(mp.getManufacturerID(INVALID_ANOTHER_EXPECTED_SHORT_ID).getValue(), manufacturerId.getValue());


        } catch (IOException e) {
            thrown = true;
        }

        assertTrue(thrown);
    }

    /**
     * Test the construction of the ShortID for MBus detection
     */
    @Test
    public void constructShortIdWithEmptyValuesTest(){
        MbusProvider mp = new MbusProvider(null, false);

        Unsigned8 version = new Unsigned8(0);
        Unsigned8 deviceType = new Unsigned8(0);
        Unsigned32 identificationNumber = new Unsigned32(0);
        Unsigned16 manufacturerId = new Unsigned16(0);
        assertEquals("0000000000000", mp.constructShortId(manufacturerId, identificationNumber, version, deviceType));
    }
}
