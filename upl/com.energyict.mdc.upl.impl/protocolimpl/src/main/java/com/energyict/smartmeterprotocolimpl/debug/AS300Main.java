package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.debug.AbstractSmartDebuggingMain;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300;

import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 19/07/11
 * Time: 13:45
 */
public class AS300Main extends AbstractSmartDebuggingMain<AS300> {

    private static AS300 as300 = null;

    private static final boolean IP = true;
    public static final String COMPORT = "COM6";
    private static final int BAUDRATE = 9600;
    private static final int DATABITS = SerialCommunicationChannel.DATABITS_8;
    private static final int PARITY = SerialCommunicationChannel.PARITY_NONE;
    private static final int STOPBITS = SerialCommunicationChannel.STOPBITS_1;


    public AS300 getMeterProtocol() {
        if (as300 == null) {
            as300 = new AS300();
            log("Created new instance of " + as300.getClass().getCanonicalName() + " [" + as300.getVersion() + "]");
        }
        return as300;
    }

    protected Properties getProperties() {
        if (IP) {
            return getIpProperties();
        } else {
            return getOpticalProperties();
        }
    }

    private Properties getIpProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "900");
        properties.setProperty(MeterProtocol.PASSWORD, "12345678");

        properties.setProperty("Retries", "3");
        properties.setProperty("Timeout", "10000");

        properties.setProperty("ClientMacAddress", "64");
        properties.setProperty("Connection", "1");
        properties.setProperty("SecurityLevel", "1:0");

        return properties;
    }

    private Properties getOpticalProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "900");
        properties.setProperty(MeterProtocol.PASSWORD, "12345678");

        properties.setProperty("Retries", "1");
        properties.setProperty("Timeout", "5000");

        properties.setProperty("ClientMacAddress", "64");
        properties.setProperty("Connection", "0");
        properties.setProperty("SecurityLevel", "1:0");
        properties.setProperty("ServerMacAddress", "1:17");

        return properties;
    }

    public static void main(String[] args) {
        AS300Main main = new AS300Main();
        main.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (IP) {
            setIpSettings(main);
        } else {
            setOpticalSettings(main);
        }
        main.setShowCommunication(true);
        main.run();
    }

    private static void setIpSettings(AS300Main main) {
        main.setPhoneNumber("10.113.0.20:4059");
    }

    private static void setOpticalSettings(AS300Main main) {
        main.setCommPort(COMPORT);
        main.setBaudRate(BAUDRATE);
        main.setDataBits(DATABITS);
        main.setParity(PARITY);
        main.setStopBits(STOPBITS);
    }

    public void doDebug() throws LinkException, IOException {
        CosemObjectFactory cof = getMeterProtocol().getDlmsSession().getCosemObjectFactory();
        ActivePassive activePassive = cof.getActivePassive(ObisCode.fromString("1.0.35.3.8.255"));

        String message = "Goeiemorgen";

        Structure value = new Structure();
        value.addDataType(new Unsigned32(1));                       // messageId
        value.addDataType(OctetString.fromString(message));         // Message
        value.addDataType(new Unsigned16(10));                      // duration
        value.addDataType(new BitString(0x0008, 8));                // messageControl
        value.addDataType(new Unsigned16(0));                       // macAddress

        System.out.println("\n\n" + value + "\n\n");
        System.out.println("\n\n" + ProtocolTools.getHexStringFromBytes(value.getBEREncodedByteArray()) + "\n\n");

        activePassive.writePassiveValue(value);

        System.out.println("\n\nActivate\n\n");

        activePassive.activate();

    }

}
