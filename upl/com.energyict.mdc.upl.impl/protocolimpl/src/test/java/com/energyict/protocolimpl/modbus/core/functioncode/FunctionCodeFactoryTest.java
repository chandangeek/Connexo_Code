package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.modbus.core.connection.ModbusTestConnection;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.PM5561;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Logger;



public class FunctionCodeFactoryTest extends TestCase {
    private PM5561 pm5561 = new PM5561();
    private FunctionCodeFactory functionCodeFactory;
    private static Logger logger;
    private static ModbusTestConnection modbusConnection;


    @Before
    public void setUp() throws Exception {
        logger = Logger.getLogger("global");
        pm5561 = new PM5561();
        this.functionCodeFactory = new FunctionCodeFactory(pm5561);
        modbusConnection = new ModbusTestConnection();
        pm5561.setModbusConnection(modbusConnection);
        pm5561.setLogger(logger);
    }

    @Test
    public void testReadGeneralReference() throws Exception {
        try {
            modbusConnection.setResponseData(buildResponseData("0000000000100607171400000000000000f2444d00000000007c573c3f72acbcffc00000ffc000003f72acbc0000000000fa05f400000000007c5a9200000000000001dc00000000000cd148", FunctionCode.READ_GENERAL_REFERENCE.getFunctionCode()));
            ReadGeneralReferenceRequest request = functionCodeFactory.readGeneralReferenceRequest(0);
            assertNotNull(request);
            assertNotNull(request.getValues());
            assertEquals(request.getValues().get(0).toString(), "4510227149094912");
            assertEquals(request.getValues().get(1).toString(), "15877197");
            assertEquals(request.getValues().get(2).toString(), "8148796");
            assertEquals(request.getValues().get(3).toString(), "1064479932");
            assertEquals(request.getValues().get(4).toString(), "4290772992");
            assertEquals(request.getValues().get(5).toString(), "4290772992");
            assertEquals(request.getValues().get(6).toString(), "1064479932");
            assertEquals(request.getValues().get(7).toString(), "16385524");
            assertEquals(request.getValues().get(8).toString(), "8149650");
            assertEquals(request.getValues().get(9).toString(), "476");
            assertEquals(request.getValues().get(10).toString(), "840008");
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Create a ResponseData object by using the given parameters
     * @param data the data to set
     * @param functionCode the function code to set
     * @return the constructed ResponseData
     */
    private ResponseData buildResponseData(String data, int functionCode){
        return buildResponseData(ParseUtils.hexStringToByteArray(data), functionCode);
    }

    /**
     * Create a ResponseData object by using the given parameters
     * @param data the data to set
     * @param functionCode the function code to set
     * @return the constructed ResponseData
     */
    private ResponseData buildResponseData(byte[] data, int functionCode){
        ResponseData rd = new ResponseData();
        rd.setData(data);
        rd.setFunctionCode(functionCode);
        rd.setAddress(258);
        return rd;
    }
}