package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.cbo.Unit;
import com.energyict.dialer.core.LinkException;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.coreimpl.RtuMessageImpl;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.debug.AbstractSmartDebuggingMain;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP;

import java.io.IOException;
import java.util.*;

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

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "900");
        properties.setProperty(MeterProtocol.PASSWORD, "ntaSim");
        properties.setProperty(MeterProtocol.SERIALNUMBER, MASTER_SERIAL_NUMBER);
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
