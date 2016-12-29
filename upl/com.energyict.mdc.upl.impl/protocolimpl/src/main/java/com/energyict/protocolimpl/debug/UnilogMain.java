package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.Services;

import com.energyict.dialer.core.LinkException;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.unilog.Unilog;
import com.energyict.protocolimpl.iec1107.unilog.UnilogRegister;
import com.energyict.protocolimpl.iec1107.unilog.UnilogRegistry;

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
            unilog = new Unilog(Services.propertySpecService());
            log("Created new instance of " + unilog.getClass().getCanonicalName() + " [" + unilog.getProtocolVersion() + "]");
        }
        return unilog;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MAXTIMEDIFF.getName(), "300");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MINTIMEDIFF.getName(), "1");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName(), "0");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName(), "3600");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), "kamstrup");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), "PL00013144");
        properties.setProperty("ChannelMap", "0+5,0+5,0+5,0,0,0,0");
        properties.setProperty("IEC1107Compatible", "1");
        properties.setProperty("SecurityLevel", "2");
        properties.setProperty("Software7E1", "1");
        properties.setProperty("Retries", "5");
        properties.setProperty("Timeout", "10000");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "UNILOG10");

        return properties;
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