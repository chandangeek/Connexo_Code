package com.energyict.genericprotocolimpl.common.obiscodemappers;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.nta.eict.WebRTUKP;
import com.energyict.genericprotocolimpl.webrtu.common.obiscodemappers.MbusObisCodeMapper;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Logger;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MbusObisCodeMapperTest {

    private static DummyDLMSConnection connection;
    private static CosemObjectFactory cof;
    private static WebRTUKP webRtuKp;
    private static ObisCode mbusEncryptionObisCode = ObisCode.fromString("0.1.24.50.0.255");
    private static ObisCode mbusConnectState = ObisCode.fromString("0.0.24.4.129.255");
    private static String expectedResponse = "10000AC401C10001010f00";
    private static String expectedResponse2 = "10000AC401C10001010f04";
    private static Log logger = LogFactory.getLog(MbusObisCodeMapperTest.class);

    @BeforeClass
    public static void setUpOnce() throws BusinessException, SQLException {
        webRtuKp = new WebRTUKP();
        connection = new DummyDLMSConnection();
        webRtuKp.setDLMSConnection(connection);
        cof = new CosemObjectFactory(webRtuKp);
    }

    @Test
    public void getEncryptionStatus() {
        MbusObisCodeMapper mocm = new MbusObisCodeMapper(cof);
        try {
            connection.setResponseByte(DLMSUtils.hexStringToByteArray(expectedResponse));
            RegisterValue rv = mocm.getRegisterValue(mbusEncryptionObisCode);

            assertEquals("No encryption is applied", rv.getText());
            assertEquals(BigDecimal.valueOf(0), rv.getQuantity().getAmount());

            connection.setResponseByte(DLMSUtils.hexStringToByteArray(expectedResponse2));
            rv = mocm.getRegisterValue(mbusEncryptionObisCode);

            assertEquals("AES encryption is applied on P2", rv.getText());
            assertEquals(BigDecimal.valueOf(4), rv.getQuantity().getAmount());
        } catch (IOException e) {
            logger.error(e.getMessage());
            fail();
        }
    }


    @Test
    public void getMbusControlStateTest() {
        MbusObisCodeMapper mocm = new MbusObisCodeMapper(cof);
        String expResponse = "010005c401c10102";
        try {
            connection.setResponseByte(DLMSUtils.hexStringToByteArray(expResponse));
            RegisterValue rv = mocm.getRegisterValue(mbusConnectState);
            assertEquals("ConnectControl state has an UNKNOWN state", rv.getText());
        } catch (NullPointerException e) {
            logger.error(e.getMessage());
            fail();
        } catch (IOException e) {
            logger.error(e.getMessage());
            fail();
        }
    }
}
