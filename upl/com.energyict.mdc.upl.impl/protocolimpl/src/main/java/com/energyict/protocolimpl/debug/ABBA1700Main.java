/**
 *
 */
package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.iec1107.abba1700.ABBA1700;

import java.io.IOException;
import java.util.Properties;

/**
 * This class can be used to test the ABBA1700 protocol without the need of the
 * CommServerJ, CommServerJOffline, EIServer or ProtocolTester. This class
 * should only be used for debugging purposes.
 *
 * @author jme
 */
public class ABBA1700Main extends AbstractDebuggingMain<ABBA1700> {

    private ABBA1700 abba1700 = null;

    @Override
    public ABBA1700 getMeterProtocol() {
        if (abba1700 == null) {
            abba1700 = new ABBA1700();
            log("Created new instance of " + abba1700.getClass().getCanonicalName() + " [" + abba1700.getProtocolVersion() + "]");
        }
        return abba1700;
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.NODEID, "001");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "1800");
        properties.setProperty(MeterProtocol.PASSWORD, "ABCD0002");
        properties.setProperty(MeterProtocol.SERIALNUMBER, "K10FC00404");
        properties.setProperty("Software7E1", "1");
        properties.setProperty("IEC1107Compatible", "1");
        properties.setProperty("SecurityLevel", "2");
        properties.setProperty("Retries", "2");
        properties.setProperty("Timeout", "35000");

        return properties;
    }

    /**
     * @param args
     * @throws IOException
     * @throws LinkException
     */
    public static void main(String[] args) throws IOException, LinkException {
        ABBA1700Main main = new ABBA1700Main();

        main.setBaudRate(9600);
        main.setDataBits(SerialCommunicationChannel.DATABITS_8);
        main.setParity(SerialCommunicationChannel.PARITY_NONE);
        main.setStopBits(SerialCommunicationChannel.STOPBITS_1);

        main.setPhoneNumber("00447738901891");
        main.setCommPort("COM1");
        main.setAsciiMode(true);
        main.setShowCommunication(true);
        main.set7E1Mode(true);
        main.run();
    }

    @Override
    void doDebug() throws LinkException, IOException {
        readRegister("1.1.0.1.2.VZ");
    }

}
