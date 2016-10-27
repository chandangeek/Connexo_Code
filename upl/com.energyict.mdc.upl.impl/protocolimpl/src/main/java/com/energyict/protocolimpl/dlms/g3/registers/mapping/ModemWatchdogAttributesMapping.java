package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ModemWatchdogConfiguration;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
/**
 * Copyrights EnergyICT
 */

public class ModemWatchdogAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 3;

    public ModemWatchdogAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return PPPSetup.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final ModemWatchdogConfiguration modemWatchdogConfiguration = getCosemObjectFactory().getModemWatchdogConfiguration();
        return parse(obisCode, readAttribute(obisCode, modemWatchdogConfiguration));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, ModemWatchdogConfiguration modemWatchdogConfiguration) throws IOException {
        switch (obisCode.getB()) {
            // Logical name
            case 1:
                return OctetString.fromObisCode(ModemWatchdogConfiguration.getDefaultObisCode());
            case 2:
                return modemWatchdogConfiguration.readWDConfigAttribute();
            case 3:
                return modemWatchdogConfiguration.readModemWatchdogEnabledAttribute();
            default:
                throw new NoSuchRegisterException("Modem Watchdog Configuration attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {
            // Logical name
            case 1:
                return new RegisterValue(obisCode, ModemWatchdogConfiguration.getDefaultObisCode().toString());
            case 2:
                return new RegisterValue(obisCode, "WD Config: " + getWDConfigString(abstractDataType));
            case 3:
                return new RegisterValue(obisCode, "Is modem watchdog enabled: " + abstractDataType.getBooleanObject().getState());
            default:
                throw new NoSuchRegisterException("Modem Watchdog Configuration attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    public String getWDConfigString(AbstractDataType wdConfigAttribute) throws IOException {
        StringBuffer builder = new StringBuffer();

        if (wdConfigAttribute.isStructure()) {
            Structure wdConfig = wdConfigAttribute.getStructure();
            builder.append(" Interval: " + wdConfig.getDataType(0).getUnsigned16().getValue());
            builder.append(" Initial delay: " + wdConfig.getDataType(1).getUnsigned16().getValue());
            builder.append(" Ppp reset threshold: " + wdConfig.getDataType(2).getUnsigned16().getValue());
            builder.append(" Modem reset threshold: " + wdConfig.getDataType(3).getUnsigned16().getValue());
            builder.append(" Device reset threshold: " + wdConfig.getDataType(4).getUnsigned16().getValue());

            return builder.toString();
        } else {
            throw new ProtocolException("Could not get correct WDConfig attribute format.");
        }
    }

}



