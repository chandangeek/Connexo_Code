package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.debug.AbstractSmartDebuggingMain;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP;

import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 19/07/11
 * Time: 13:45
 */
public class WebRTUKPMain extends AbstractSmartDebuggingMain<WebRTUKP> {

    private static WebRTUKP webRtuKP = null;
    public static final String MASTER_SERIAL_NUMBER = "1000827";
    public static final String MBUS_SERIAL_NUMBER = "SIM1000827006301";

    public WebRTUKP getMeterProtocol() {
        if (webRtuKP == null) {
            webRtuKP = new WebRTUKP();
            log("Created new instance of " + webRtuKP.getClass().getCanonicalName() + " [" + webRtuKP.getVersion() + "]");
        }
        return webRtuKP;
    }

    protected Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.Property.MAXTIMEDIFF.getName(), "300");
        properties.setProperty(MeterProtocol.Property.MINTIMEDIFF.getName(), "1");
        properties.setProperty(MeterProtocol.Property.CORRECTTIME.getName(), "0");
        properties.setProperty(MeterProtocol.Property.PROFILEINTERVAL.getName(), "900");
        properties.setProperty(MeterProtocol.Property.PASSWORD.getName(), "ntaSim");
        properties.setProperty(MeterProtocol.Property.SERIALNUMBER.getName(), MASTER_SERIAL_NUMBER);
        properties.setProperty("NTASimulationTool", "1");
        properties.setProperty("SecurityLevel", "1:0");
        properties.setProperty("Retries", "3");
        properties.setProperty("Timeout", "10000");

        return properties;
    }

    public static void main(String[] args) {
        WebRTUKPMain main = new WebRTUKPMain();
        main.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        main.setPhoneNumber("jme.eict.local:4059");
        main.setShowCommunication(false);
        main.run();
    }

    public void doDebug() throws LinkException, IOException {

    }

}
