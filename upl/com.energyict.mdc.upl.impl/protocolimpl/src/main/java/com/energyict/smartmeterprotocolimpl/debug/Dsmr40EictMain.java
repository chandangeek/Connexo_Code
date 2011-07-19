package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.debug.AbstractSmartDebuggingMain;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eict.Dsmr40Eict;

import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 19/07/11
 * Time: 13:45
 */
public class Dsmr40EictMain extends AbstractSmartDebuggingMain<Dsmr40Eict> {

    private static Dsmr40Eict dsmr40Eict = null;

    public Dsmr40Eict getMeterProtocol() {
        if (dsmr40Eict == null) {
            dsmr40Eict = new Dsmr40Eict();
            log("Created new instance of " + dsmr40Eict.getClass().getCanonicalName() + " [" + dsmr40Eict.getVersion() + "]");
        }
        return dsmr40Eict;
    }

    protected Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "900");
        properties.setProperty(MeterProtocol.PASSWORD, "ntaSim");
        properties.setProperty(MeterProtocol.SERIALNUMBER, "1000827");
        properties.setProperty("NTASimulationTool", "1");
        properties.setProperty("SecurityLevel", "1:0");
        properties.setProperty("Retries", "3");
        properties.setProperty("Timeout", "10000");

        return properties;
    }

    public static void main(String[] args) {
        Dsmr40EictMain main = new Dsmr40EictMain();
        main.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        main.setPhoneNumber("jme.eict.local:4059");
        main.setShowCommunication(false);
        main.run();
    }

    public void doDebug() throws LinkException, IOException {

    }

}
