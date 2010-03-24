/**
 *
 */
package com.energyict.protocolimpl.debug;

import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200;
import com.energyict.protocolimpl.utils.ProtocolTools;

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
	private static final int		BAUDRATE				= 9600;
	private static final int		DATABITS				= SerialCommunicationChannel.DATABITS_8;
	private static final int		PARITY					= SerialCommunicationChannel.PARITY_NONE;
	private static final int		STOPBITS				= SerialCommunicationChannel.STOPBITS_1;

	private static UNIFLO1200 uniflo1200 = null;
	private static Dialer dialer = null;
	private static Logger logger = null;

	public static UNIFLO1200 getUniflo1200() {
		if (uniflo1200 == null) {
			uniflo1200 = new UNIFLO1200();
			log("Created new instance of " + uniflo1200.getClass().getCanonicalName() + " [" + uniflo1200.getProtocolVersion() + "]");
		}
		return uniflo1200;
	}

	public static Dialer getDialer() {
		if (dialer == null) {
			dialer = DialerFactory.getStandardModemDialer().newDialer();
			dialer.setStreamObservers(new DebuggingObserver(OBSERVER_FILENAME, false));
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
		properties.setProperty("Retries", "10");
		properties.setProperty("Timeout", "10000");
		properties.setProperty("ProfileInterval", "3600");
		properties.setProperty("Password", "789");
		properties.setProperty("DevideId", "1");
		properties.setProperty("InterframeTimeout", "100");
		properties.setProperty("SecurityLevel", "3");
		properties.setProperty("PhysicalLayer", "1");
		properties.setProperty("LoadProfileNumber", "1");
		return properties;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws LinkException
	 */
	public static void main(String[] args) throws IOException, LinkException {

		getDialer().init(COMPORT, "ATM0");
		getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
		getDialer().connect("00031621813118", 60 * 1000);

		try {
			getUniflo1200().setProperties(getProperties());
			getUniflo1200().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, getLogger());
			getUniflo1200().enableHHUSignOn(getDialer().getSerialCommunicationChannel());
			getUniflo1200().connect();

			System.out.println(getUniflo1200().getTime());
			ProfileData pd = readProfile(true);
			System.out.println(pd);
			ProtocolTools.writeBytesToFile("c:\\profiel_" + System.currentTimeMillis() + ".txt", pd.toString().getBytes(), false);

		} finally {
			ProtocolTools.delay(DELAY_BEFORE_DISCONNECT);
			log("Done. Closing connections. \n");
			getUniflo1200().disconnect();
			getDialer().disConnect();
		}

	}

	private static ProfileData readProfile(boolean incluideEvents) throws IOException {
		Calendar fromTime = Calendar.getInstance();
		fromTime.set(2010, Calendar.JANUARY, 1, 0, 0);

		Calendar toTime = Calendar.getInstance();
		toTime.set(2010, Calendar.FEBRUARY, 1, 0, 0);
		toTime.setTimeInMillis(fromTime.getTimeInMillis());
		toTime.add(Calendar.HOUR_OF_DAY, 1);

		return getUniflo1200().getProfileData(fromTime.getTime(), incluideEvents);
	}

	private static void log(Object message) {
		getLogger().log(Level.INFO, message == null ? "null" : message.toString());
	}

}
