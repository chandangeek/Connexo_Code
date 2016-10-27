package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.iec1107.abba230.ABBA230;

import java.io.IOException;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 1-jun-2010
 * Time: 9:02:21
 */
public class ABBA230Main extends AbstractDebuggingMain<ABBA230> {

    private static ABBA230 abba230 = null;

    @Override
    ABBA230 getMeterProtocol() {
        if (abba230 == null) {
            abba230 = new ABBA230();
            log("Created new instance of " + abba230.getClass().getCanonicalName() + " [" + abba230.getProtocolVersion() + "]");
        }
        return abba230;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.Property.MAXTIMEDIFF.getName(), "300");
        properties.setProperty(MeterProtocol.Property.MINTIMEDIFF.getName(), "1");
        properties.setProperty(MeterProtocol.Property.CORRECTTIME.getName(), "0");
        properties.setProperty(MeterProtocol.Property.PROFILEINTERVAL.getName(), "1800");
        properties.setProperty(MeterProtocol.Property.PASSWORD.getName(), "ABCD0002");
        properties.setProperty(MeterProtocol.Property.SERIALNUMBER.getName(), "09045210");
        properties.setProperty("IEC1107Compatible", "1");
        properties.setProperty("SecurityLevel", "2");
        properties.setProperty("Retries", "2");
        properties.setProperty("Timeout", "35000");
        properties.setProperty(MeterProtocol.Property.NODEID.getName(), "001");

        return properties;
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
