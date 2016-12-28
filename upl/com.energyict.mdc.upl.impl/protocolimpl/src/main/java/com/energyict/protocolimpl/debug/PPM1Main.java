/**
 *
 */
package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.iec1107.ppmi1.PPM;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class can be used to test the PPM1 protocol without the need of the
 * CommServerJ, CommServerJOffline, EIServer or ProtocolTester. This class
 * should only be used for debugging purposes.
 *
 * @author jme
 */
public class PPM1Main {

	private static final Level	LOG_LEVEL				= Level.SEVERE;
	private static final String	OBSERVER_FILENAME		= "c:\\logging\\PPM1Main\\communications.log";
	private static final long	DELAY_BEFORE_DISCONNECT	= 100;

	private static final TimeZone	DEFAULT_TIMEZONE		= TimeZone.getTimeZone("GMT+01");
	private static final String		COMPORT					= "COM4";
	private static final int		BAUDRATE				= 300;
	private static final int		DATABITS				= SerialCommunicationChannel.DATABITS_7;
	private static final int		PARITY					= SerialCommunicationChannel.PARITY_EVEN;
	private static final int		STOPBITS				= SerialCommunicationChannel.STOPBITS_1;

	private static PPM ppm = null;
	private static Dialer dialer = null;
	private static Logger logger = null;

	public static PPM getPPM() {
		if (ppm == null) {
			ppm = new PPM();
			log("Created new instance of " + ppm.getClass().getCanonicalName() + " [" + ppm.getProtocolVersion() + "]");
		}
		return ppm;
	}

	public static Dialer getDialer() {
		if (dialer == null) {
			dialer = DialerFactory.getOpticalDialer().newDialer();
			dialer.setStreamObservers(new DebuggingObserver(OBSERVER_FILENAME, true, true));
		}
		return dialer;
	}

	public static Logger getLogger() {
		 if (logger == null) {
			 logger = Logger.getLogger(PPM1Main.class.getCanonicalName());
			 logger.setLevel(LOG_LEVEL);
		 }
		 return logger;
	}

	private static Properties getProperties() {
		Properties properties = new Properties();

		properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MAXTIMEDIFF.getName(), "300");
		properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MINTIMEDIFF.getName(), "1");
		properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName(), "0");

		properties.setProperty("Retries", "3");
		properties.setProperty("Timeout", "5000");

		properties.setProperty("OPUS", "0");

		properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName(), "1800");
		properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), "--------");
		properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), "--------K9302433");

		return properties;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws LinkException
	 */
	public static void main(String[] args) throws IOException, LinkException {

		getDialer().init(COMPORT);
		getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
		getDialer().connect();

		try {
			getPPM().setProperties(TypedProperties.copyOf(getProperties()));
			getPPM().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, getLogger());
			getPPM().enableHHUSignOn(getDialer().getSerialCommunicationChannel());
			getPPM().connect();

			Calendar from = ProtocolTools.createCalendar(2010, 4, 18, 0, 0, 0, 0);

			System.out.println(ProtocolTools.getProfileInfo(getPPM().getProfileData(from.getTime(), false)));

			System.out.println();


		} finally {
			ProtocolTools.delay(DELAY_BEFORE_DISCONNECT);
			log("Done. Closing connections. \n");
			getPPM().disconnect();
			getDialer().disConnect();
		}

	}

	private static void log(Object message) {
		getLogger().log(Level.INFO, message == null ? "null" : message.toString());
	}

}