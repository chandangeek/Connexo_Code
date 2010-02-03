package com.energyict.protocolimpl.edmi.mk6.debug;

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
import com.energyict.dialer.coreimpl.IPDialer;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.edmi.mk6.MK6;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.VirtualDevice;

public class Mk6Main {

	private static final String		OBSERVER_FILENAME	= "c:\\logging\\Mk6Main\\communications.log";
	private static final Logger		LOGGER				= Logger.getLogger(Mk6Main.class.getCanonicalName());
	private static final TimeZone	DEFAULT_TIMEZONE	= TimeZone.getTimeZone("GMT");

	private static final String		COMPORT				= "COM1";
	private static final int		BAUDRATE			= 9600;
	private static final int		DATABITS			= SerialCommunicationChannel.DATABITS_8;
	private static final int		PARITY				= SerialCommunicationChannel.PARITY_NONE;
	private static final int		STOPBITS			= SerialCommunicationChannel.STOPBITS_1;

	private static final String		MODEM_INIT			= "ATM0";
	//private static final String		PHONE_NUMBER		= "000447590872514";
	private static final String		PHONE_NUMBER		= null;
	private static final String		IP_ADDRESS			= "127.0.0.1:12345";

	private static final int		CONNECT_TIMEOUT		= 60 * 1000;


	private static MK6 mk6 = null;
	private static Dialer dialer = null;

	public static MK6 getMk6() {
		if (mk6 == null) {
			mk6 = new MK6();
		}
		return mk6;
	}

	public static Dialer getDialer() throws LinkException {
		if (dialer == null) {
			if (PHONE_NUMBER != null) {
			dialer = DialerFactory.getStandardModemDialer().newDialer();
			} else if (IP_ADDRESS != null) {
				dialer = DialerFactory.get("IPDIALER").newDialer();
			} else {
				dialer = DialerFactory.getDirectDialer().newDialer();
			}
			dialer.setStreamObservers(new DebuggingObserver(OBSERVER_FILENAME, false));
			getDialer().init(COMPORT, MODEM_INIT);
		}
		return dialer;
	}

	public static void main(String[] args) throws LinkException, IOException {

		VirtualDevice vd = new VirtualDevice();
		vd.startDevice();

		getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
		if (getDialer() instanceof ATDialer) {
			getDialer().connect(PHONE_NUMBER, CONNECT_TIMEOUT);
		} else if (getDialer() instanceof IPDialer){
			getDialer().connect(IP_ADDRESS, CONNECT_TIMEOUT);
		} else {
			getDialer().connect();
		}

		try {
			getMk6().setProperties(getProperties());
			getMk6().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, LOGGER);
			getMk6().connect();

			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();

			from.set(2001, Calendar.AUGUST, 1, 0, 0, 0);
			to.set(2009, Calendar.OCTOBER, 10, 13, 11, 9);

			System.out.println(ProtocolTools.getProfileInfo(getMk6().getProfileData(from.getTime(), to.getTime(), false)));

			System.out.println();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			getMk6().disconnect();
			System.out.println("\n");
			getDialer().disConnect();
			vd.stopDevice();
		}

	}

	private static Properties getProperties() {
		Properties properties = new Properties();

		properties.setProperty("MaximumTimeDiff", "300");
		properties.setProperty("MinimumTimeDiff", "1");
		properties.setProperty("CorrectTime", "0");
		properties.setProperty("ExtendedLogging", "0");

		properties.setProperty("LoadSurveyName", "A0DD157E_94A7_4C26_8D11_C2FBE7DFCFA1");
		properties.setProperty("ForcedDelay", "100");

		properties.setProperty("ProfileInterval", "1800");

		properties.setProperty("DevideId", "READER");
		properties.setProperty("Password", "READER");
		properties.setProperty("SerialNumber", "209152266");

		return properties;
	}

}
