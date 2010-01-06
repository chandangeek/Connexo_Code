package com.energyict.protocolimpl.edmi.mk10.debug;

import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.edmi.mk10.MK10;

public class Mk10Main {

	private static final String		OBSERVER_FILENAME	= "c:\\logging\\Mk10Main\\communications.log";
	private static final Logger		LOGGER				= Logger.getLogger(Mk10Main.class.getCanonicalName());
	private static final TimeZone	DEFAULT_TIMEZONE	= TimeZone.getTimeZone("GMT+01");

	private static final String		COMPORT				= "COM4";
	private static final int		BAUDRATE			= 9600;
	private static final int		DATABITS			= SerialCommunicationChannel.DATABITS_8;
	private static final int		PARITY				= SerialCommunicationChannel.PARITY_NONE;
	private static final int		STOPBITS			= SerialCommunicationChannel.STOPBITS_1;

	private static MK10 mk10 = null;
	private static Dialer dialer = null;

	public static MK10 getMk10() {
		if (mk10 == null) {
			mk10 = new MK10();
		}
		return mk10;
	}

	public static Dialer getDialer() {
		if (dialer == null) {
			dialer = DialerFactory.getDirectDialer().newDialer();
			dialer.setStreamObservers(new DebuggingObserver(OBSERVER_FILENAME, false));
		}
		return dialer;
	}

	public static void main(String[] args) throws LinkException, IOException {

		getDialer().init(COMPORT);
		getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
		getDialer().connect();

		try {
			getMk10().setProperties(getProperties());
			getMk10().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, LOGGER);
			getMk10().connect();
			System.out.println(getMk10().readRegister(ObisCode.fromString("1.1.1.9.1.255")));
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
		properties.setProperty("DevideId", "EDMI");
		properties.setProperty("Password", "IMDEIMDE");
		properties.setProperty("SerialNumber", "204600109");

		return properties;
	}

}
