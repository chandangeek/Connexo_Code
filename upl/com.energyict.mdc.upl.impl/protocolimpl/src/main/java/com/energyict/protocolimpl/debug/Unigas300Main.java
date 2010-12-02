package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.kamstrup.unigas300.*;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 1-jun-2010
 * Time: 9:02:21
 */
public class Unigas300Main extends AbstractDebuggingMain<Unigas300> {

    private static Unigas300 unigas300 = null;

    @Override
    Unigas300 getMeterProtocol() {
        if (unigas300 == null) {
            unigas300 = new Unigas300();
            log("Created new instance of " + unigas300.getClass().getCanonicalName() + " [" + unigas300.getProtocolVersion() + "]");
        }
        return unigas300;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "3600");
        properties.setProperty(MeterProtocol.PASSWORD, "00000000");
        properties.setProperty(MeterProtocol.SERIALNUMBER, "");
        properties.setProperty("IEC1107Compatible", "1");
        properties.setProperty("SecurityLevel", "1");
        properties.setProperty("Retries", "5");
        properties.setProperty("Timeout", "5000");
        properties.setProperty(MeterProtocol.NODEID, "");

        return properties;
    }

    public static void main(String[] args) throws IOException, LinkException {
        Unigas300Main main = new Unigas300Main();
        main.setCommPort("COM1");
        main.setBaudRate(9600);
        main.setStopBits(SerialCommunicationChannel.STOPBITS_1);
        main.setParity(SerialCommunicationChannel.PARITY_EVEN);
        main.setDataBits(SerialCommunicationChannel.DATABITS_7);
        main.setAsciiMode(true);
        main.setShowCommunication(true);
        main.run();
    }

    @Override
    void doDebug() throws LinkException, IOException {

        List<RegisterMapping> registerMappings = new RegisterMappingFactory().getRegisterMappings();
        for (RegisterMapping registerMapping : registerMappings) {
            try {
                ObisCode obisCode = registerMapping.getObisCode();
                RegisterValue value = getMeterProtocol().readRegister(obisCode);
                if (value != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(obisCode).append(" = ");
                    if (value.getQuantity() != null) {
                        sb.append(value.getQuantity().getAmount()).append(" ");
                        sb.append(value.getQuantity().getUnit()).append(" ");
                    }
                    if (value.getText() != null) {
                        sb.append(value.getText()).append(" ");
                    }
                    System.out.println(sb.toString());
                } else {
                    System.out.println("Register value was null for obis: " + obisCode.toString());
                }
            } catch (IOException e) {
                System.out.println("Failed: " + e.getMessage());
            }
        }
    }

}