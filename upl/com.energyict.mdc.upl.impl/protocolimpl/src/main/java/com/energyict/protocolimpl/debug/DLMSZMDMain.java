package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.Services;

import com.energyict.dialer.core.LinkException;
import com.energyict.protocolimpl.dlms.DLMSZMD;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.VirtualDeviceDialer;

import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 26-apr-2010
 * Time: 9:10:57
 */
public class DLMSZMDMain {

	private static final Level LOG_LEVEL				= Level.SEVERE;
	private static final long	DELAY_BEFORE_DISCONNECT	= 100;

	private static DLMSZMD dlmsZmd = null;

	private static Logger logger = null;

    public static DLMSZMD getZmd() {
		if (dlmsZmd == null) {
			dlmsZmd = new DLMSZMD(Services.propertySpecService(), new NoTariffCalendars(), new NoDeviceMessageFiles(), new DefaultDateFormatter(), new DummyNumberLookupExtractor());
			log("Created new instance of " + dlmsZmd.getClass().getCanonicalName() + " [" + dlmsZmd.getProtocolVersion() + "]");
		}
		return dlmsZmd;
	}

	public static Logger getLogger() {
		 if (logger == null) {
			 logger = Logger.getLogger(DLMSZMDMain.class.getCanonicalName());
			 logger.setLevel(LOG_LEVEL);
		 }
		 return logger;
	}

	private static Properties getProperties() {
		Properties properties = new Properties();

		properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName(), "900");
		properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), "00000000");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "3");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName(), "LGZ80131045");

		return properties;
	}

	public static void main(String[] args) throws IOException, LinkException {

        String debugFile = DLMSZMDMain.class.getResource("ZMD_New.log").getFile();

        VirtualDeviceDialer virtualDeviceDialer = new VirtualDeviceDialer(debugFile);
        virtualDeviceDialer.setShowCommunication(true);

		try {
			getZmd().setProperties(TypedProperties.copyOf(getProperties()));
			getZmd().init(virtualDeviceDialer.getInputStream(), virtualDeviceDialer.getOutputStream(), TimeZone.getTimeZone("GMT+01"), getLogger());
//			getZmd().enableHHUSignOn(virtualDeviceDialer.getSerialCommunicationChannel(), false);
			getZmd().connect();

			Calendar time = ProtocolTools.createCalendar(2010, 4, 20, 9, 10, 19, 0);

//			System.out.println(ProtocolTools.getProfileInfo(getZmd().getProfileData(from.getTime(), false)));

		} finally {
			ProtocolTools.delay(DELAY_BEFORE_DISCONNECT);
			log("Done. Closing connections. \n");
			getZmd().disconnect();
			virtualDeviceDialer.disConnect();
		}

	}

	private static void log(Object message) {
		getLogger().log(Level.INFO, message == null ? "null" : message.toString());
	}

}