package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.energyict.dialer.core.LinkException;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.debug.AbstractSmartDebuggingMain;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 19/07/11
 * Time: 13:45
 */
public class UkHubTestTool extends AbstractSmartDebuggingMain<UkHub> {

    private static UkHub ukHub = null;
    public static final String XML_TO_SEND = "XmlToSend";
    public static final String TIME_ZONE = "TimeZone";
    public static final String SHOW_COMMUNICATION = "ShowCommunication";
    public static final String DLMS_PORT = "DlmsPort";
    public static final String IP_ADDRESS = "IpAddress";

    public UkHub getMeterProtocol() {
        if (ukHub == null) {
            ukHub = new UkHub();
            log("Created new instance of " + ukHub.getClass().getCanonicalName() + " [" + ukHub.getVersion() + "]");
        }
        return ukHub;
    }

    protected Properties getProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("ukhubtest.properties"));
        } catch (IOException e) {
            getLogger().severe("Unable to load properties from [\"ukhubtest.properties\"]! " + e.getMessage());
        }
        return properties;
    }

    public static void main(String[] args) {
        UkHubTestTool main = new UkHubTestTool();
        main.setTimeZone(TimeZone.getTimeZone(main.getProperties().getProperty(TIME_ZONE)));
        main.setPhoneNumber(main.getProperties().getProperty(IP_ADDRESS) +  ":" + main.getProperties().getProperty(DLMS_PORT));
        main.setShowCommunication(ProtocolTools.getBooleanFromString(main.getProperties().getProperty(SHOW_COMMUNICATION)));
        main.run();
    }

    public void doDebug() throws LinkException, IOException {
        //String content = "<Change_HAN_SAS HAN_SAS_EXTENDED_PAN_ID=\"0102030405060708\" HAN_SAS_PAN_ID=\"1234\" HAN_SAS_PAN_Channel_Mask=\"134215680\" HAN_SAS_Insecure_Join=\"1\"/>";
        String content = getProperties().getProperty(XML_TO_SEND);
        MessageEntry messageEntry = MessageEntry.fromContent(content).trackingId("").finish();
        getLogger().severe("Sending XML message: " + content);
        MessageResult messageResult = getMeterProtocol().queryMessage(messageEntry);
        getLogger().info(messageResult.toString());
    }

}
