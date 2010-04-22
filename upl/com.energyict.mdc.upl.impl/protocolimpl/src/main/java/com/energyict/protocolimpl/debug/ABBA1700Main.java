/**
 *
 */
package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.*;
import com.energyict.dialer.coreimpl.OpticalDialer;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.iec1107.abba1700.ABBA1700;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class can be used to test the ABBA1700 protocol without the need of the
 * CommServerJ, CommServerJOffline, EIServer or ProtocolTester. This class
 * should only be used for debugging purposes.
 *
 * @author jme
 */
public class ABBA1700Main {

    private static final Level LOG_LEVEL = Level.SEVERE;
    private static final String OBSERVER_FILENAME = "c:\\logging\\ABBA1700Main\\communications.log";
    private static final long DELAY_BEFORE_DISCONNECT = 100;

    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT+01");
    private static final String COMPORT = "COM1";
    private static final int BAUDRATE = 9600;
    private static final int DATABITS = SerialCommunicationChannel.DATABITS_8;
    private static final int PARITY = SerialCommunicationChannel.PARITY_NONE;
    private static final int STOPBITS = SerialCommunicationChannel.STOPBITS_1;
    private static final long PROFILE_LENGTH = 1000 * 60 * 60 * 24 * 1;

    private static ABBA1700 abba1700 = null;
    private static Dialer dialer = null;
    private static Logger logger = null;

    public static ABBA1700 getABBA1700() {
        if (abba1700 == null) {
            abba1700 = new ABBA1700();
            log("Created new instance of " + abba1700.getClass().getCanonicalName() + " [" + abba1700.getProtocolVersion() + "]");
        }
        return abba1700;
    }

    public static Dialer getDialer() {
        if (dialer == null) {
            dialer = DialerFactory.getStandardModemDialer().newDialer();
            dialer.setStreamObservers(new DebuggingObserver(OBSERVER_FILENAME, false, false));
        }
        return dialer;
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(ABBA1700Main.class.getCanonicalName());
            logger.setLevel(LOG_LEVEL);
        }
        return logger;
    }

    private static Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.NODEID, "101");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "1800");
        properties.setProperty(MeterProtocol.PASSWORD, "WPDM2010");
        properties.setProperty(MeterProtocol.SERIALNUMBER, "K09D02786");
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

        try {
            getDialer().init(COMPORT, "ATM0");
            getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
            getDialer().connect("000447975171697", 60 * 1000);

            try {

                getABBA1700().setProperties(getProperties());
                getABBA1700().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, getLogger());
                if (getDialer() instanceof OpticalDialer) {
                    getABBA1700().enableHHUSignOn(getDialer().getSerialCommunicationChannel());
                }
                getABBA1700().connect();

                ProfileData pd = getABBA1700().getProfileData(new Date(System.currentTimeMillis() - PROFILE_LENGTH), false);
                System.out.println(pd.toString());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ProtocolTools.delay(DELAY_BEFORE_DISCONNECT);
            }

            getABBA1700().disconnect();

        } catch (Exception e) {
            log("Error: " + e.getMessage() + ". \n");
        } finally {
            log("Closing connections. \n");
            if (getDialer().getStreamConnection().isOpen()) {
                getDialer().disConnect();
            }
        }

        System.out.println("\r\n");

    }

    private static void log(Object message) {
        getLogger().log(Level.INFO, message == null ? "null" : message.toString());
    }

}
