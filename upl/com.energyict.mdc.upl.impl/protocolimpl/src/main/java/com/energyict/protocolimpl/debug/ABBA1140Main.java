package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.iec1107.abba1140.ABBA1140;

import java.io.IOException;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 1-jun-2010
 * Time: 9:02:21
 */
public class ABBA1140Main extends AbstractDebuggingMain<ABBA1140> {

    private static ABBA1140 abba1140 = null;

    @Override
    ABBA1140 getMeterProtocol() {
        if (abba1140 == null) {
            abba1140 = new ABBA1140();
            log("Created new instance of " + abba1140.getClass().getCanonicalName() + " [" + abba1140.getProtocolVersion() + "]");
        }
        return abba1140;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.NODEID, "101");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "1800");
        properties.setProperty(MeterProtocol.PASSWORD, "ABCD0002");
        properties.setProperty(MeterProtocol.SERIALNUMBER, "07045683");
        properties.setProperty("IEC1107Compatible", "1");
        properties.setProperty("SecurityLevel", "2");
        properties.setProperty("Retries", "2");
        properties.setProperty("Timeout", "35000");
        properties.setProperty(MeterProtocol.NODEID, "001");

        return properties;
    }

    public static void main(String[] args) throws IOException, LinkException {
        ABBA1140Main main = new ABBA1140Main();
        main.setPhoneNumber("000447918125447");
        main.setCommPort("COM1");
        main.setObserverFilename("c:\\logging\\ABBA1140\\communications.log");
        main.run();
    }

    @Override
    void doDebug() throws LinkException, IOException {
        readRegister("1.1.0.1.2.0");
    }

}