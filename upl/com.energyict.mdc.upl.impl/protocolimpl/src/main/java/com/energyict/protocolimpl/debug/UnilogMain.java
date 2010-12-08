package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.iec1107.unilog.*;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 1-jun-2010
 * Time: 9:02:21
 */
public class UnilogMain extends AbstractDebuggingMain<Unilog> {

    private static Unilog unilog = null;

    @Override
    Unilog getMeterProtocol() {
        if (unilog == null) {
            unilog = new Unilog();
            log("Created new instance of " + unilog.getClass().getCanonicalName() + " [" + unilog.getProtocolVersion() + "]");
        }
        return unilog;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "3600");
        properties.setProperty(MeterProtocol.PASSWORD, "kamstrup");
        properties.setProperty(MeterProtocol.SERIALNUMBER, "PL00013144");
        properties.setProperty("ChannelMap", "0+5,0+5,0+5,0,0,0,0");
        properties.setProperty("IEC1107Compatible", "1");
        properties.setProperty("SecurityLevel", "2");
        properties.setProperty("Software7E1", "1");
        properties.setProperty("Retries", "5");
        properties.setProperty("Timeout", "10000");
        properties.setProperty(MeterProtocol.NODEID, "UNILOG10");

        return properties;
    }

    public static void main(String[] args) throws IOException, LinkException {
        UnilogMain main = new UnilogMain();
        main.setCommPort("COM1");
        main.setBaudRate(9600);
        main.setStopBits(SerialCommunicationChannel.STOPBITS_1);
        main.setParity(SerialCommunicationChannel.PARITY_NONE);
        main.setDataBits(SerialCommunicationChannel.DATABITS_8);
        main.setPhoneNumber("00031627858525");
        main.setModemInit("ATM0");
        main.setAsciiMode(true);
        main.set7E1Mode(true);
        main.setShowCommunication(true);
        main.run();
    }

    @Override
    void doDebug() throws LinkException, IOException {

        StringBuilder sb = new StringBuilder();
        List<ObisCode> obisList = UnilogRegistry.getSupportedObisCodes();
        for (int i = 0; i < obisList.size(); i++) {
            ObisCode obisCode = obisList.get(i);
            try {
                sb.append(getMeterProtocol().readRegister(obisCode)).append("\n");
            } catch (IOException e) {
                sb.append(obisCode).append(" failed: ").append(e.getMessage()).append("\n");
            }
        }

        System.out.println();
        System.out.println(sb.toString());
        System.out.println();

        sb = new StringBuilder();
        UnilogRegister[] unilogRegisters = UnilogRegistry.getUnilogRegisters();
        for (int i = 0; i < unilogRegisters.length; i++) {
            UnilogRegister unilogRegister = unilogRegisters[i];
            sb.append(unilogRegister.getObis().getA()).append("\t");
            sb.append(unilogRegister.getObis().getB()).append("\t");
            sb.append(unilogRegister.getObis().getC()).append("\t");
            sb.append(unilogRegister.getObis().getD()).append("\t");
            sb.append(unilogRegister.getObis().getE()).append("\t");
            sb.append(unilogRegister.getObis().getF()).append("\t");
            sb.append("[").append(unilogRegister.getRegisterId()).append("] ").append(unilogRegister.getName()).append("\n");
        }

        System.out.println();
        System.out.println(sb.toString());
        System.out.println();

    }

}