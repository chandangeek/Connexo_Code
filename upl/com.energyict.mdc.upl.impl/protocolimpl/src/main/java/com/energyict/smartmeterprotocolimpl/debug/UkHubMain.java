package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.debug.AbstractSmartDebuggingMain;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 19/07/11
 * Time: 13:45
 */
public class UkHubMain extends AbstractSmartDebuggingMain<UkHub> {

    private static UkHub ukHub = null;

    public UkHub getMeterProtocol() {
        if (ukHub == null) {
            ukHub = new UkHub();
            log("Created new instance of " + ukHub.getClass().getCanonicalName() + " [" + ukHub.getVersion() + "]");
        }
        return ukHub;
    }

    protected Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "900");
        properties.setProperty(MeterProtocol.PASSWORD, "E5i9c3t20");

        properties.setProperty("Retries", "10");
        properties.setProperty("Timeout", "60000");

        //properties.setProperty("ClientMacAddress", "80");

        properties.setProperty("Connection", "1");
        properties.setProperty("SecurityLevel", "3:0");

        return properties;
    }

    public static void main(String[] args) {
        UkHubMain main = new UkHubMain();
        main.setTimeZone(TimeZone.getTimeZone("GMT"));
        //main.setPhoneNumber("10.0.2.215:4059");
        main.setPhoneNumber("10.113.0.19:4059");
        main.setShowCommunication(true);
        main.run();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getMeterProtocol().getDlmsSession().getCosemObjectFactory();
    }

    public void doDebug() throws LinkException, IOException {
        String firmwareVersion = getMeterProtocol().getFirmwareVersion();
        System.out.println("\n\n" + firmwareVersion + "\n\n");

    }

}
