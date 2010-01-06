package com.energyict.protocolimpl.dlms.as220.debug;

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
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.Register;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.dlms.as220.AS220;

public class AS220Main {

	private static final String		OBSERVER_FILENAME	= "c:\\logging\\AS220Main\\communications.log";
	private static final Level		LOG_LEVEL			= Level.ALL;
	private static final TimeZone	DEFAULT_TIMEZONE	= TimeZone.getTimeZone("GMT+01");

	private static final String		COMPORT				= "COM5";
	private static final int		BAUDRATE			= 115200;
	private static final int		DATABITS			= SerialCommunicationChannel.DATABITS_8;
	private static final int		PARITY				= SerialCommunicationChannel.PARITY_NONE;
	private static final int		STOPBITS			= SerialCommunicationChannel.STOPBITS_1;

	private static AS220 as220 = null;
	private static Dialer dialer = null;
	private static Logger logger = null;

	public static AS220 getAs220() {
		if (as220 == null) {
			as220 = new AS220();
		}
		return as220;
	}

	public static Dialer getDialer() {
		if (dialer == null) {
			dialer = DialerFactory.getDirectDialer().newDialer();
			dialer.setStreamObservers(new DebuggingObserver(OBSERVER_FILENAME, false));
		}
		return dialer;
	}

	public static Logger getLogger() {
		 if (logger == null) {
			 logger = Logger.getLogger(AS220Main.class.getCanonicalName());
			 logger.setLevel(LOG_LEVEL);
		 }
		 return logger;
	}

	private static Properties getProperties() {
		Properties properties = new Properties();

		properties.setProperty("MaximumTimeDiff", "300");
		properties.setProperty("MinimumTimeDiff", "1");
		properties.setProperty("CorrectTime", "0");

		properties.setProperty("Retries", "0");
		properties.setProperty("Timeout", "10000");

		properties.setProperty("SecurityLevel", "2");
		properties.setProperty("ProfileInterval", "900");
		properties.setProperty("Password", "12345678");
		properties.setProperty("SerialNumber", "303135303233");

		properties.setProperty("AddressingMode", "-1");
		properties.setProperty("Connection", "3");
		properties.setProperty("ClientMacAddress", "2");
		properties.setProperty("ServerLowerMacAddress", "1");
		properties.setProperty("ServerUpperMacAddress", "1");

		return properties;
	}

	public static void readProfile(boolean incluideEvents) throws IOException {
		Calendar from = Calendar.getInstance(DEFAULT_TIMEZONE);
		from.add(Calendar.HOUR_OF_DAY, -12);
		System.out.println(getAs220().getProfileData(from.getTime(), incluideEvents));
	}

	public static void readRegisters() {
		UniversalObject[] universalObjects = getAs220().getMeterConfig().getInstantiatedObjectList();
		for (UniversalObject uo : universalObjects) {
			if (uo.getClassID() == Register.CLASSID) {
				try {
					System.out.println(getAs220().readRegister(uo.getObisCode()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws LinkException, IOException {

		getDialer().init(COMPORT);
		getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
		getDialer().connect();

		try {
			getAs220().setProperties(getProperties());
			getAs220().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, getLogger());
			getAs220().connect();

			//readRegisters();
			readProfile(true);

		} finally {
			System.out.println("\nDone. Closing connections. \n");
			getAs220().disconnect();
			System.out.println("\n");
			getDialer().disConnect();
		}

	}

}
