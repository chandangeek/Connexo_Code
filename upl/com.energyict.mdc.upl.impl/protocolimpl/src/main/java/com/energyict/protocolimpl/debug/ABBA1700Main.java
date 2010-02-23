/**
 *
 */
package com.energyict.protocolimpl.debug;

import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.iec1107.abba1700.ABBA1700;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * This class can be used to test the ABBA1700 protocol without the need of the
 * CommServerJ, CommServerJOffline, EIServer or ProtocolTester. This class
 * should only be used for debugging purposes.
 *
 * @author jme
 */
public class ABBA1700Main {

	private static final Level	LOG_LEVEL				= Level.SEVERE;
	private static final String	OBSERVER_FILENAME		= "c:\\logging\\ABBA1700Main\\communications.log";
	private static final long	DELAY_BEFORE_DISCONNECT	= 100;

	private static final TimeZone	DEFAULT_TIMEZONE		= TimeZone.getTimeZone("GMT+01");
	private static final String		COMPORT					= "COM4";
	private static final int		BAUDRATE				= 9600;
	private static final int		DATABITS				= SerialCommunicationChannel.DATABITS_7;
	private static final int		PARITY					= SerialCommunicationChannel.PARITY_EVEN;
	private static final int		STOPBITS				= SerialCommunicationChannel.STOPBITS_1;

	private static ABBA1700 ppm = null;
	private static Dialer dialer = null;
	private static Logger logger = null;

	public static ABBA1700 getABBA1700() {
		if (ppm == null) {
			ppm = new ABBA1700();
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

		properties.setProperty("Retries", "3");
		properties.setProperty("Timeout", "3000");

		properties.setProperty("BreakBeforeConnect", "1");

		properties.setProperty(MeterProtocol.PROFILEINTERVAL, "1800");
		properties.setProperty(MeterProtocol.PASSWORD, "ABCD0002");
		//properties.setProperty(MeterProtocol.SERIALNUMBER, "02052224");

		return properties;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws LinkException
	 */
	public static void main(String[] args) throws IOException, LinkException {

		int ec = 0;
		for (int i = 0; i < 50; i++) {
			getDialer().init(COMPORT);
			getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
			getDialer().connect();

			try {
				getABBA1700().setProperties(getProperties());
				getABBA1700().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, getLogger());
				getABBA1700().enableHHUSignOn(getDialer().getSerialCommunicationChannel());
				getABBA1700().connect();
			} catch (Exception e) {
				ec++;
				e.printStackTrace();
			} finally {
				ProtocolTools.delay(DELAY_BEFORE_DISCONNECT);
				log("Done. Closing connections. \n");
				getABBA1700().disconnect();
				getDialer().disConnect();
			}
			System.out.println("\r\n");
		}

		System.out.println("ErrorCount = " + ec);

	}

	private static void log(Object message) {
		getLogger().log(Level.INFO, message == null ? "null" : message.toString());
	}

}
