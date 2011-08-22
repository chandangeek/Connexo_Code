package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
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
        main.setShowCommunication(false);
        main.run();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getMeterProtocol().getDlmsSession().getCosemObjectFactory();
    }

    public void doDebug() throws LinkException, IOException {
        String b64 = new BASE64Encoder().encode(ProtocolTools.readBytesFromFile("C:\\Users\\jme\\Desktop\\am110r-22-08-2011_1400.bin"));
        ProtocolTools.writeStringToFile("C:\\Users\\jme\\Desktop\\am110r-22-08-2011_1400_B64.bin", b64, false);
    }

    private void writePPPSettings() {
        try {
            Data data = getCosemObjectFactory().getData(ObisCode.fromString("0.129.0.0.0.255"));
            //data.setValueAttr(OctetString.fromString("<Configuration><Config><PPP><UserName>EICTSMQ007</UserName><Password>1893903</Password></PPP></Config></Configuration>"));
            data.setValueAttr(OctetString.fromString("<Configuration><Config><PPP><UserName>EICTSMQ006</UserName><Password>4091459</Password></PPP></Config></Configuration>"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printObjectList() throws IOException {
        UniversalObject[] buffer = getCosemObjectFactory().getAssociationLN().getBuffer();
        StringBuilder sb = new StringBuilder();
        for (UniversalObject universalObject : buffer) {
            sb.append(universalObject.getDescription()).append('\n');
        }
        String text = sb.toString();
        System.out.println(text);
        ProtocolTools.writeStringToFile("c:\\ukhub_objectlist.txt", text, false);
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
