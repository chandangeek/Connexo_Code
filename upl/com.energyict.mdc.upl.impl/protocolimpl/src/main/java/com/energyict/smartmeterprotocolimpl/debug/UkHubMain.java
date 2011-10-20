package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.debug.AbstractSmartDebuggingMain;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 19/07/11
 * Time: 13:45
 */
public class UkHubMain extends AbstractSmartDebuggingMain<UkHub> {

    private static UkHub ukHub = null;

    public UkHub getMeterProtocol() {
        if (ukHub == null) {
            ukHub = new UkHub();
            log("Created new instance of " + ukHub.getClass().getCanonicalName() + " [" + ukHub.getVersion() + "]");
        }
        return ukHub;
    }

    protected Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "900");
        properties.setProperty(MeterProtocol.PASSWORD, "E5i9c3t20");

        properties.setProperty("Retries", "10");
        properties.setProperty("Timeout", "60000");

        //properties.setProperty("ClientMacAddress", "80");

        properties.setProperty("Connection", "1");
        properties.setProperty("SecurityLevel", "3:0");

        return properties;
    }

    public static void main(String[] args) {
        UkHubMain main = new UkHubMain();
        main.setTimeZone(TimeZone.getTimeZone("GMT"));
        main.setPhoneNumber("10.113.0.18:4059");
        main.setShowCommunication(false);
        main.run();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getMeterProtocol().getDlmsSession().getCosemObjectFactory();
    }

    public void doDebug() throws LinkException, IOException {
        String[] obisCodesAsString = new String[]{
                "0-0:96.12.5.255"
        };
        for (String obisCodeAsString : obisCodesAsString) {
            String correctObisString = obisCodeAsString.replace("-", ".").replace(":", ".");
            testRegisterObisCode(ObisCode.fromString(correctObisString));
        }
    }

    private void testProfileObisCode(ObisCode obisCode) {
        System.out.println("Reading profile generic object with obisCode [" + obisCode + "]");
        try {
            ProfileGeneric profileGeneric = getCosemObjectFactory().getProfileGeneric(obisCode);
            List<CapturedObject> captureObjects = profileGeneric.getCaptureObjects();
            int capturePeriod = profileGeneric.getCapturePeriod();
            //int sortMethod = profileGeneric.getSortMethod();

            System.out.println("Captured objects = ");
            for (CapturedObject captureObject : captureObjects) {
                System.out.println("  > " + captureObject);
            }
            System.out.println("capturePeriod = " + capturePeriod);
            //System.out.println("sortMethod = " + sortMethod);

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println();
    }

    private void testRegisterObisCode(ObisCode obisCode) {
        System.out.println("Reading register object with obisCode [" + obisCode + "]");
        try {
            Register data = getCosemObjectFactory().getRegister(obisCode);
            AbstractDataType valueAttr = data.getValueAttr();
            ScalerUnit scalerUnit = data.getScalerUnit();
            System.out.println("valueAttrib = " + valueAttr.toString().replace("\n", " "));
            System.out.println("scalerUnit = " + scalerUnit);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println();
    }

    private void testDataObisCode(ObisCode obisCode) {
        System.out.println("Reading data object with obisCode [" + obisCode + "]");
        try {
            Data data = getCosemObjectFactory().getData(obisCode);
            AbstractDataType valueAttr = data.getValueAttr();
            if (valueAttr.isOctetString()) {
                System.out.println("value = " + valueAttr.getOctetString().stringValue());
            }
            System.out.println(valueAttr);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println();
    }

}
