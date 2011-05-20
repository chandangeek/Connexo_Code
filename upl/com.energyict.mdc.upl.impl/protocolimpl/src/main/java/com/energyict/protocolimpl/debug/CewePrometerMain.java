package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 10/05/11
 * Time: 15:47
 */
public class CewePrometerMain extends AbstractDebuggingMain<CewePrometer> {

    private static CewePrometer cewePrometer = null;
    public static final String SERIAL_NEW_FW = "1610901";
    public static final String SERIAL_OLD_FW = "1483801";
    private static final String SERIAL = SERIAL_NEW_FW;

    @Override
    CewePrometer getMeterProtocol() {
        if (cewePrometer == null) {
            cewePrometer = new CewePrometer();
            log("Created new instance of " + cewePrometer.getClass().getCanonicalName() + " [" + cewePrometer.getProtocolVersion() + "]");
        }
        return cewePrometer;
    }

    @Override
    Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "1800");
        properties.setProperty(MeterProtocol.PASSWORD, "222222");
        properties.setProperty(MeterProtocol.SERIALNUMBER, SERIAL);
        properties.setProperty("SecurityLevel", "2");
        properties.setProperty("Software7E1", "1");
        properties.setProperty("Retries", "3");
        properties.setProperty("Timeout", "10000");
        properties.setProperty(MeterProtocol.NODEID, SERIAL);

        properties.setProperty("Logger", "1");

        return properties;
    }

    public static void main(String[] args) {
        CewePrometerMain main = new CewePrometerMain();
        main.setCommPort("COM20");
        main.setBaudRate(9600);
        main.setStopBits(SerialCommunicationChannel.STOPBITS_1);
        main.setParity(SerialCommunicationChannel.PARITY_NONE);
        main.setDataBits(SerialCommunicationChannel.DATABITS_8);
        main.setPhoneNumber("004615577556");
        main.setModemInit("ATM0");
        main.setAsciiMode(true);
        main.set7E1Mode(true);
        main.setShowCommunication(true);
        main.run();
    }

    @Override
    void doDebug() throws LinkException, IOException {
        requestProfileData();
    }

    private void requestProfileData() throws IOException {
        Date from = ProtocolTools.createCalendar(2000, 5, 15, 0, 0, 0, 0).getTime();
        ProfileData profileData = getMeterProtocol().getProfileData(new Date(), false);
        System.out.println("\n");
        for (ChannelInfo channelInfo : profileData.getChannelInfos()) {
            System.out.println(channelInfo);
        }
    }

}
