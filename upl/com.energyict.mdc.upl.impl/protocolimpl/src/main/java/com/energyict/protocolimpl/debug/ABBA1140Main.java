package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.Services;

import com.energyict.dialer.core.LinkException;
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
            abba1140 = new ABBA1140(Services.propertySpecService());
            log("Created new instance of " + abba1140.getClass().getCanonicalName() + " [" + abba1140.getProtocolVersion() + "]");
        }
        return abba1140;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MAXTIMEDIFF.getName(), "300");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MINTIMEDIFF.getName(), "1");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName(), "0");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName(), "1800");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), "ABCD0002");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), "07045683");
        properties.setProperty("IEC1107Compatible", "1");
        properties.setProperty("SecurityLevel", "2");
        properties.setProperty("Retries", "2");
        properties.setProperty("Timeout", "35000");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "001");

        return properties;
    }

    public static void main(String[] args) throws IOException, LinkException {
        ABBA1140Main main = new ABBA1140Main();
        main.setPhoneNumber("00447918125447");
        main.setCommPort("COM1");
        main.setObserverFilename("c:\\logging\\ABBA1140\\communications.log");
        main.setAsciiMode(false);
        main.setShowCommunication(false);
        main.run();
    }

    @Override
    void doDebug() throws LinkException, IOException {
        readRegister("1.1.0.1.0.255");
        readRegister("1.1.0.1.2.0");
        readRegister("1.1.0.1.2.1");
        readRegister("1.1.0.1.2.2");
        readRegister("1.1.0.1.2.3");
        readRegister("1.1.0.1.2.4");
        readRegister("1.1.0.1.2.5");
        readRegister("1.1.0.1.2.6");
        readRegister("1.1.0.1.2.7");
        readRegister("1.1.0.1.2.8");
        readRegister("1.1.0.1.2.9");
    }

}