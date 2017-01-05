/**
 *
 */
package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.Services;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class can be used to test the UNIFLO12001 protocol without the need of the
 * CommServerJ, CommServerJOffline, EIServer or ProtocolTester. This class
 * should only be used for debugging purposes.
 *
 * @author jme
 */
public class UNIFLO1200Main {

	private static final Level	LOG_LEVEL				= Level.SEVERE;
	private static final String	OBSERVER_FILENAME		= "c:\\logging\\UNIFLO1200Main\\communications.log";
	private static final long	DELAY_BEFORE_DISCONNECT	= 100;

	private static final TimeZone	DEFAULT_TIMEZONE		= TimeZone.getTimeZone("GMT+01");
	private static final String		COMPORT					= "COM1";
	private static final int		BAUDRATE				= 2400;
	private static final int		DATABITS				= SerialCommunicationChannel.DATABITS_8;
	private static final int		PARITY					= SerialCommunicationChannel.PARITY_NONE;
	private static final int		STOPBITS				= SerialCommunicationChannel.STOPBITS_1;

	private static UNIFLO1200 uniflo1200 = null;
	private static Dialer dialer = null;
	private static Logger logger = null;

	public static UNIFLO1200 getUniflo1200() {
		if (uniflo1200 == null) {
			uniflo1200 = new UNIFLO1200(Services.propertySpecService());
			log("Created new instance of " + uniflo1200.getClass().getCanonicalName() + " [" + uniflo1200.getProtocolVersion() + "]");
		}
		return uniflo1200;
	}

    public static Dialer getDirectDialer() {
        if (dialer == null) {
            dialer = DialerFactory.getDirectDialer().newDialer();
            dialer.setStreamObservers(new DebuggingObserver(OBSERVER_FILENAME, true));
        }
        return dialer;
    }

    public static Dialer getDialer() {
        return getDirectDialer();
    }

    public static Dialer getATDialer() {
		if (dialer == null) {
			dialer = DialerFactory.getStandardModemDialer().newDialer();
			dialer.setStreamObservers(new DebuggingObserver(OBSERVER_FILENAME, true));
		}
		return dialer;
	}

	public static Logger getLogger() {
		 if (logger == null) {
			 logger = Logger.getLogger(UNIFLO1200Main.class.getCanonicalName());
			 logger.setLevel(LOG_LEVEL);
		 }
		 return logger;
	}

	private static Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty("Retries", "5");
		properties.setProperty("Timeout", "4000");
        properties.setProperty("InterframeTimeout", "100");
        properties.setProperty("ForcedDelay", "0");
		properties.setProperty("ProfileInterval", "3600");
		properties.setProperty("Password", "789");
		properties.setProperty("DevideId", "1");
		properties.setProperty("SecurityLevel", "3");
		properties.setProperty("LoadProfileNumber", "1");
		return properties;
	}

	public static void main(String[] args) throws IOException, LinkException {

		getATDialer().init(COMPORT, "ATM0");
		getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
		getDialer().connect("00031621813118", 60 * 1000);

/*
        getDirectDialer().init(COMPORT);
        getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
        getDialer().connect();
*/


		try {
			getUniflo1200().setUPLProperties(TypedProperties.copyOf(getProperties()));
			getUniflo1200().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, getLogger());
			getUniflo1200().enableHHUSignOn(getDialer().getSerialCommunicationChannel());
			getUniflo1200().connect();

            Date timeDate = getUniflo1200().getTime();
            String fw = getUniflo1200().getFirmwareVersion();
            ProfileData pd = readProfile(true);

            System.out.println("\r\n");
            System.out.println(pd);
            System.out.println("\r\n");
            System.out.println(timeDate);
            System.out.println(fw);
            System.out.println("\r\n");

		} finally {
			ProtocolTools.delay(DELAY_BEFORE_DISCONNECT);
			log("Done. Closing connections. \n");
			getUniflo1200().disconnect();
			getDialer().disConnect();
		}

	}

	private static ProfileData readProfile(boolean incluideEvents) throws IOException {
		Calendar fromTime = Calendar.getInstance();
		fromTime.set(2010, Calendar.MAY, 28, 0, 0);
		return getUniflo1200().getProfileData(fromTime.getTime(), incluideEvents);
	}

	private static void log(Object message) {
		getLogger().log(Level.INFO, message == null ? "null" : message.toString());
	}

}
