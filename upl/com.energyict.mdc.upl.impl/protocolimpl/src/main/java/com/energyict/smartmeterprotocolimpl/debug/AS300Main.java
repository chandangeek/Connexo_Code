package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.TariffCalender;

import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocolimpl.debug.AbstractSmartDebuggingMain;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 19/07/11
 * Time: 13:45
 */
public class AS300Main extends AbstractSmartDebuggingMain<AS300> {

    private static AS300 as300 = null;

    private static final boolean IP = true;
    public static final String COMPORT = "COM6";
    private static final int BAUDRATE = 9600;
    private static final int DATABITS = SerialCommunicationChannel.DATABITS_8;
    private static final int PARITY = SerialCommunicationChannel.PARITY_NONE;
    private static final int STOPBITS = SerialCommunicationChannel.STOPBITS_1;


    public AS300 getMeterProtocol() {
        if (as300 == null) {
            as300 = new AS300(new Dummy());
            log("Created new instance of " + as300.getClass().getCanonicalName() + " [" + as300.getVersion() + "]");
        }
        return as300;
    }

    protected Properties getProperties() {
        if (IP) {
            return getIpProperties();
        } else {
            return getOpticalProperties();
        }
    }

    private Properties getIpProperties() {
        Properties properties = new Properties();

        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MAXTIMEDIFF.getName(), "300");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MINTIMEDIFF.getName(), "1");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName(), "0");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName(), "900");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), "12345678");

        properties.setProperty("Retries", "10");
        properties.setProperty("Timeout", "60000");

        //properties.setProperty("ClientMacAddress", "80");

        properties.setProperty("Connection", "1");
        properties.setProperty("SecurityLevel", "5:3");

        properties.setProperty("DataTransportAuthenticationKey", "0F0E0D0C0B0A09080706050403020100");
        properties.setProperty("DataTransportEncryptionKey", "0F0E0D0C0B0A09080706050403020100");
        properties.setProperty("DataTransportKey", "0F0E0D0C0B0A09080706050403020100");

        return properties;
    }

    private Properties getOpticalProperties() {
        Properties properties = new Properties();

        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MAXTIMEDIFF.getName(), "300");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MINTIMEDIFF.getName(), "1");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName(), "0");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName(), "900");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), "12345678");

        properties.setProperty("Retries", "1");
        properties.setProperty("Timeout", "5000");

        properties.setProperty("ClientMacAddress", "64");
        properties.setProperty("Connection", "0");
        properties.setProperty("SecurityLevel", "1:0");
        properties.setProperty("ServerMacAddress", "1:17");

        return properties;
    }

    public static void main(String[] args) {
        AS300Main main = new AS300Main();
        main.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (IP) {
            setIpSettings(main);
        } else {
            setOpticalSettings(main);
        }
        main.setShowCommunication(false);
        main.run();
    }

    private static void setIpSettings(AS300Main main) {
        main.setPhoneNumber("10.113.0.20:4059");
    }

    private static void setOpticalSettings(AS300Main main) {
        main.setCommPort(COMPORT);
        main.setBaudRate(BAUDRATE);
        main.setDataBits(DATABITS);
        main.setParity(PARITY);
        main.setStopBits(STOPBITS);
    }

    public void doDebug() throws LinkException, IOException {
        System.out.println(getMeterProtocol().getObjectFactory().getActivityCalendar().readCalendarNameActive());
    }

    private static class Dummy implements TariffCalendarFinder {
        @Override
        public Optional<TariffCalender> from(String identifier) {
            return Optional.empty();
        }
    }
}
