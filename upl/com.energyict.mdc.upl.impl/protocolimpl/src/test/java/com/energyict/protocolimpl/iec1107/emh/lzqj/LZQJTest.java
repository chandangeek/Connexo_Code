package com.energyict.protocolimpl.iec1107.emh.lzqj;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Properties;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 11-mrt-2011
 * Time: 14:27:19
 */
public class LZQJTest {

    @Test
    public void readRegisterTest() {
        byte[] responseByte = new byte[]{48, 46, 49, 46, 48, 40, 48, 49, 41, 13, 10};
        try {
            LZQJ protocol = new LZQJ();
            Properties props = new Properties();
            props.put("DataReadout", "1");
            protocol.setUPLProperties(props);
            DummyFlagConnection dConnection = new DummyFlagConnection(null, null, 1, 1, 1, 1, 1, true);
            protocol.setConnection(dConnection);
            protocol.setDataReadout(dataDump1.getBytes());

            dConnection.setResponseByte(responseByte);
            RegisterValue rv = protocol.readRegister(ObisCode.fromString("1.1.1.8.1.VZ"));
            System.out.println(rv);
            assertEquals(new BigDecimal(new BigInteger("27462"), 3), rv.getQuantity().getAmount());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private String dataDump1 = "F.F(00000000)\r\n" +
            "0.0.0(02718483)\r\n" +
            "0.0.1(02718483)\r\n" +
            "0.0.2(00000000)\r\n" +
            "0.9.1(114726)\r\n" +
            "0.9.2(110223)\r\n" +
            "1.8.1(00052.197*kWh)\r\n" +
            "1.8.1*01(00027.462*kWh)\r\n" +
            "1.8.2(00357.646*kWh)\r\n" +
            "1.8.2*01(00180.980*kWh)\r\n" +
            "2.8.1(00000.000*kWh)\r\n" +
            "2.8.1*01(00000.000*kWh)\r\n" +
            "2.8.2(00000.000*kWh)\r\n" +
            "2.8.2*01(00000.000*kWh)\r\n" +
            "3.8.1(00031.571*kvarh)\r\n" +
            "3.8.1*01(00016.783*kvarh)\r\n" +
            "3.8.2(00193.037*kvarh)\r\n" +
            "3.8.2*01(00097.707*kvarh)\r\n" +
            "4.8.1(00000.000*kvarh)\r\n" +
            "4.8.1*01(00000.000*kvarh)\r\n" +
            "4.8.2(00000.000*kvarh)\r\n" +
            "4.8.2*01(00000.000*kvarh)\r\n" +
            "31.25(1.9347*A)\r\n" +
            "51.25(1.8022*A)\r\n" +
            "71.25(1.7293*A)\r\n" +
            "32.25(226.83*V)\r\n" +
            "52.25(227.03*V)\r\n" +
            "72.25(227.75*V)\r\n" +
            "33.25(0.87*P/S)\r\n" +
            "53.25(0.87*P/S)\r\n" +
            "73.25(0.88*P/S)\r\n" +
            "!\r\n";

    @Test
    public void getClearedBillingRegisterTest() {
        byte[] responseByte = new byte[]{48, 46, 49, 46, 48, 40, 48, 48, 41, 13, 10};
        try {
            LZQJ protocol = new LZQJ();
            Properties props = new Properties();
            props.put("DataReadout", "1");
            protocol.setUPLProperties(props);
            DummyFlagConnection dConnection = new DummyFlagConnection(null, null, 1, 1, 1, 1, 1, true);
            protocol.setConnection(dConnection);
            dConnection.setDataReadOut(dataDump2.getBytes());
            protocol.connect();
            dConnection.setResponseByte(responseByte);
            RegisterValue rv = protocol.readRegister(ObisCode.fromString("1.1.1.8.1.VZ"));
            System.out.println(rv);
            assertEquals(new BigDecimal(new BigInteger("20123"), 3), rv.getQuantity().getAmount());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private String dataDump2 = "F.F(00000000)\r\n" +
            "0.0.0(02773731)\r\n" +
            "0.0.1(02773731)\r\n" +
            "0.0.2()\r\n" +
            "0.9.1(115723)\r\n" +
            "0.9.2(110223)\r\n" +
            "1-1:1.8.1(00000.001*kWh)\r\n" +
            "1-1:1.8.1*00(00020.123*kWh)\r\n" +
            "1-1:1.8.2(00000.000*kWh)\r\n" +
            "1-1:1.8.2*00(00000.000*kWh)\r\n" +
            "1-1:2.8.1(00403.807*kWh)\r\n" +
            "1-1:2.8.1*00(00000.000*kWh)\r\n" +
            "1-1:2.8.2(00510.261*kWh)\r\n" +
            "1-1:2.8.2*00(00000.000*kWh)\r\n" +
            "1-1:3.8.1(00000.045*kvarh)\r\n" +
            "1-1:3.8.1*00(00000.000*kvarh)\r\n" +
            "1-1:3.8.2(00000.000*kvarh)\r\n" +
            "1-1:3.8.2*00(00000.000*kvarh)\r\n" +
            "1-1:4.8.1(00121.532*kvarh)\r\n" +
            "1-1:4.8.1*00(00000.000*kvarh)\r\n" +
            "1-1:4.8.2(00152.703*kvarh)\r\n" +
            "1-1:4.8.2*00(00000.000*kvarh)\r\n" +
            "1-2:1.8.0(00000.000*kWh)\r\n" +
            "1-2:1.8.0*00(00000.000*kWh)\r\n" +
            "31.25(0.0013*A)\r\n" +
            "51.25(0.0005*A)\r\n" +
            "71.25(0.0009*A)\r\n" +
            "32.25(234.58*V)\r\n" +
            "52.25(235.33*V)\r\n" +
            "72.25(234.68*V)\r\n" +
            "33.25(0.00*P/S)\r\n" +
            "53.25(0.00*P/S)\r\n" +
            "73.25(0.00*P/S)\r\n" +
            "!\r\n";

}
