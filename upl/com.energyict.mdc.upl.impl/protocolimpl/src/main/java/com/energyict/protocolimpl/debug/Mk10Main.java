package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.edmi.mk10.MK10;
import com.energyict.protocolimpl.properties.TypedProperties;

import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

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
	private static final String		PHONE_NUMBER		= "000447872368707";

	private static MK10 mk10 = null;
	private static Dialer dialer = null;

    public static MK10 getMk10() {
		if (mk10 == null) {
			mk10 = new MK10();
		}
		return mk10;
	}

	public static Dialer getDialer() throws LinkException, IOException {
		if (dialer == null) {
            DebuggingObserver debuggingObserver = new DebuggingObserver(OBSERVER_FILENAME, false);
			if (PHONE_NUMBER != null) {
                dialer = DebugUtils.getConnectedModemDialer(PHONE_NUMBER, COMPORT, MODEM_INIT, debuggingObserver);
			} else {
				dialer = DebugUtils.getConnectedDirectDialer(COMPORT, BAUDRATE, DATABITS, PARITY, STOPBITS, debuggingObserver);
			}
		}
		return dialer;
	}

    private static Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty("MaximumTimeDiff", "300");
        properties.setProperty("MinimumTimeDiff", "1");
        properties.setProperty("CorrectTime", "0");
        properties.setProperty("ExtendedLogging", "0");

        properties.setProperty("LoadSurveyNumber", "1");

        properties.setProperty("ProfileInterval", "1800");

        /* My own device (206332371)
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

        /* Imserv settings for device on phone: 000447872368707 */
        properties.setProperty("DevideId", "READER");
        properties.setProperty("Password", "READER");
        properties.setProperty("SerialNumber", "208365843");

        /* My own device (204600109)
        properties.setProperty("DevideId", "READER");
        properties.setProperty("Password", "READER");
        properties.setProperty("SerialNumber", "204600109");
        */

        return properties;
    }

    private static void readRegister(String obisCodeAsString) {
        try {
            System.out.println(getMk10().readRegister(ObisCode.fromString(obisCodeAsString)));
        } catch (IOException e) {
            System.out.println(obisCodeAsString + " = " + e.getMessage());
        }
    }

    private static void readBillingRegisters() {
        readRegister("1.1.1.8.0.255");
        readRegister("1.1.1.8.1.255");
        readRegister("1.1.1.8.2.255");
        readRegister("1.1.1.8.3.255");
        readRegister("1.1.1.8.4.255");
        readRegister("1.1.1.8.5.255");
        readRegister("1.1.1.8.6.255");
        readRegister("1.1.1.8.7.255");
        readRegister("1.1.1.8.8.255");
        readRegister("1.1.3.8.0.255");
        readRegister("1.1.0.1.0.255");
        readRegister("1.1.1.16.0.0");
        readRegister("1.1.0.1.2.0");
    }


    private static void readBillingDateRegisters() {
        readRegister("1.1.0.1.2.255");
        readRegister("1.1.0.1.2.0");
        readRegister("1.1.0.1.2.1");
        readRegister("1.1.0.1.2.2");
        readRegister("1.1.0.1.2.3");
        readRegister("1.1.0.1.2.4");
        readRegister("1.1.0.1.2.5");
        readRegister("1.1.0.1.2.6");
        readRegister("1.1.0.1.2.7");
        readRegister("1.1.0.1.2.8");
        readRegister("1.1.0.1.2.9");
    }

    private static void readEnergyRegisters() {
        readRegister("1.1.1.8.0.255");
        readRegister("1.1.1.8.0.0");
        readRegister("1.1.1.8.0.1");
        readRegister("1.1.1.8.0.2");
        readRegister("1.1.1.8.0.3");
        readRegister("1.1.1.8.0.4");
        readRegister("1.1.1.8.0.5");
        readRegister("1.1.1.8.0.6");
        readRegister("1.1.1.8.0.7");
        readRegister("1.1.1.8.0.8");
        readRegister("1.1.1.8.0.9");
    }

	public static void main(String[] args) throws LinkException, IOException {

		try {
			getMk10().setProperties(TypedProperties.copyOf(getProperties()));
			getMk10().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, LOGGER);
			getMk10().connect();

            readBillingDateRegisters();
            System.out.println();
            readEnergyRegisters();
            System.out.println();
            readBillingRegisters();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			getMk10().disconnect();
			System.out.println("\n");
			getDialer().disConnect();
		}

	}

}