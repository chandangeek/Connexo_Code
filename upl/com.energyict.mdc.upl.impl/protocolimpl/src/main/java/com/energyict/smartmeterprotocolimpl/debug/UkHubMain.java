package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.debug.AbstractSmartDebuggingMain;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;

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

        properties.setProperty("Retries", "3");
        properties.setProperty("Timeout", "10000");

        //properties.setProperty("ClientMacAddress", "64");

        properties.setProperty("Connection", "1");
        properties.setProperty("SecurityLevel", "3:0");

        return properties;
    }

    public static void main(String[] args) {
        UkHubMain main = new UkHubMain();
        main.setTimeZone(TimeZone.getTimeZone("GMT"));
        main.setPhoneNumber("10.113.0.18:4059");
        //main.setPhoneNumber("10.0.2.209:4059");
        //main.setPhoneNumber("10.0.2.222:4059");
        main.setShowCommunication(false);
        main.run();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getMeterProtocol().getDlmsSession().getCosemObjectFactory();
    }

    public void doDebug() throws LinkException, IOException {
        createHAN();
    }

    private void createHAN() throws IOException {
        //String content = "<Change_HAN_SAS HAN_SAS_EXTENDED_PAN_ID=\"0102030405060708\" HAN_SAS_PAN_ID=\"1234\" HAN_SAS_PAN_Channel=\"134215680\" HAN_SAS_Insecure_Join=\"1\"/>";
        String content = "<Change_HAN_SAS HAN_SAS_PAN_ID=\"45493\" />";
        String trackingId = "";
        MessageEntry messageEntry = new MessageEntry(content, trackingId);
        // getMeterProtocol().queryMessage(messageEntry);

        messageEntry = new MessageEntry("<Create_Han_Network/>", "");
        getMeterProtocol().queryMessage(messageEntry);
    }

    private void readLogbooks() throws IOException {
        List<MeterEvent> meterEvents = getMeterProtocol().getMeterEvents(new Date(0));
        for (MeterEvent meterEvent : meterEvents) {
            System.out.println(meterEvent.getTime() + "  " +  meterEvent.toString());
        }
    }

}
