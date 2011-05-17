package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.iec1107.cewe.prometer.Prometer;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 10/05/11
 * Time: 15:47
 */
public class PrometerMain extends AbstractDebuggingMain<Prometer> {

    private static Prometer prometer = null;
    private static final String SERIAL = "1095704";

    @Override
    Prometer getMeterProtocol() {
        if (prometer == null) {
            prometer = new Prometer();
            log("Created new instance of " + prometer.getClass().getCanonicalName() + " [" + prometer.getProtocolVersion() + "]");
        }
        return prometer;
    }

    @Override
    Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "1800");
        properties.setProperty(MeterProtocol.PASSWORD, "0000");
        properties.setProperty(MeterProtocol.SERIALNUMBER, SERIAL);
        properties.setProperty("SecurityLevel", "1");
        properties.setProperty("Software7E1", "1");
        properties.setProperty("Retries", "3");
        properties.setProperty("Timeout", "10000");
        properties.setProperty(MeterProtocol.NODEID, SERIAL);

        return properties;
    }

    public static void main(String[] args) {
        PrometerMain main = new PrometerMain();
        main.setCommPort("COM20");
        main.setBaudRate(9600);
        main.setStopBits(SerialCommunicationChannel.STOPBITS_1);
        main.setParity(SerialCommunicationChannel.PARITY_NONE);
        main.setDataBits(SerialCommunicationChannel.DATABITS_8);
        main.setTimeZone(TimeZone.getTimeZone("GMT"));
        main.setPhoneNumber("00441383611487");
        main.setModemInit("ATM0");
        main.setAsciiMode(true);
        main.set7E1Mode(true);
        main.setShowCommunication(true);
        main.setObserverFilename("c:\\log\\prometer\\EVENT_BUG_" + System.currentTimeMillis() + ".txt");
        main.run();
    }

    @Override
    void doDebug() throws LinkException, IOException {
        ProfileData profileData = getMeterProtocol().getProfileData(new Date(), true);
        List<IntervalData> intervalDatas = profileData.getIntervalDatas();
        List<MeterEvent> meterEvents = profileData.getMeterEvents();
        System.out.println("\n");
        for (IntervalData intervalData : intervalDatas) {
            System.out.println(intervalData);
        }
        for (MeterEvent event : meterEvents) {
            System.out.println(event.getTime() + " - " + event + " [" + event.getProtocolCode() + "|" + event.getEiCode() + "]");
        }
    }

    @RunOnDevice(protocolClass = Prometer.class)
    public void debugMethod() {

    }

}
