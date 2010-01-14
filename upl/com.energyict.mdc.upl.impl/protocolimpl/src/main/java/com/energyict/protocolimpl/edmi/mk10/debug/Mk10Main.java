package com.energyict.protocolimpl.edmi.mk10.debug;

import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dialer.coreimpl.ATDialer;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.edmi.mk10.MK10;
import com.energyict.protocolimpl.utils.ProtocolTools;

public class Mk10Main {

	private static final String		OBSERVER_FILENAME	= "c:\\logging\\Mk10Main\\communications.log";
	private static final Logger		LOGGER				= Logger.getLogger(Mk10Main.class.getCanonicalName());
	private static final TimeZone	DEFAULT_TIMEZONE	= TimeZone.getTimeZone("GMT+01");

	private static final String		COMPORT				= "COM1";
	private static final int		BAUDRATE			= 9600;
	private static final int		DATABITS			= SerialCommunicationChannel.DATABITS_8;
	private static final int		PARITY				= SerialCommunicationChannel.PARITY_NONE;
	private static final int		STOPBITS			= SerialCommunicationChannel.STOPBITS_1;

	private static final String		MODEM_INIT			= "ATM0";
	private static final String		PHONE_NUMBER		= "000447703556023";
	private static final int		CONNECT_TIMEOUT		= 60 * 1000;

	private static MK10 mk10 = null;
	private static Dialer dialer = null;

	public static MK10 getMk10() {
		if (mk10 == null) {
			mk10 = new MK10();
		}
		return mk10;
	}

	public static Dialer getDialer() throws LinkException {
		if (dialer == null) {
			if (PHONE_NUMBER != null) {
			dialer = DialerFactory.getStandardModemDialer().newDialer();
			} else {
				dialer = DialerFactory.getDirectDialer().newDialer();
			}
			dialer.setStreamObservers(new DebuggingObserver(OBSERVER_FILENAME, false));
			getDialer().init(COMPORT, MODEM_INIT);
		}
		return dialer;
	}

	public static void main(String[] args) throws LinkException, IOException {

		getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
		if (getDialer() instanceof ATDialer) {
			getDialer().connect(PHONE_NUMBER, CONNECT_TIMEOUT);
		} else {
			getDialer().connect();
		}

		try {
			getMk10().setProperties(getProperties());
			getMk10().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, LOGGER);
			getMk10().connect();
//			System.out.println(getMk10().readRegister(ObisCode.fromString("1.1.1.9.1.255")));

			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();

			from.set(2009, Calendar.JANUARY, 1, 0, 0, 0);
			to.set(2010, Calendar.JANUARY, 15, 0, 0, 0);
			System.out.println(ProtocolTools.getProfileInfo(getMk10().getProfileData(from.getTime(), to.getTime(), false)));

			System.out.println();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			getMk10().disconnect();
			System.out.println("\n");
			getDialer().disConnect();
		}

	}

	private static Properties getProperties() {
		Properties properties = new Properties();

		properties.setProperty("MaximumTimeDiff", "300");
		properties.setProperty("MinimumTimeDiff", "1");
		properties.setProperty("CorrectTime", "0");
		properties.setProperty("ExtendedLogging", "0");

		properties.setProperty("LoadSurveyNumber", "1");

		properties.setProperty("ProfileInterval", "1800");

		/* My own device
		properties.setProperty("DevideId", "EDMI");
		properties.setProperty("Password", "IMDEIMDE");
		properties.setProperty("SerialNumber", "206332371");
		 */

		/* Imserv settings for device on phone: 000447872368862
		properties.setProperty("DevideId", "READER");
		properties.setProperty("Password", "READER");
		properties.setProperty("SerialNumber", "204618802");
		 */

		/* Imserv settings for device on phone: 000447703556023
		properties.setProperty("DevideId", "READER");
		properties.setProperty("Password", "READER");
		properties.setProperty("SerialNumber", "206902570");
		 */

		properties.setProperty("DevideId", "READER");
		properties.setProperty("Password", "READER");
		properties.setProperty("SerialNumber", "206902570");

		return properties;
	}

}
