package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ModemWatchdogConfiguration;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import org.json.JSONException;
import org.json.JSONObject;

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
        return (ModemWatchdogConfiguration.getDefaultObisCode().equalsIgnoreBillingField(obisCode) ||
                ModemWatchdogConfiguration.getLegacyObisCode().equalsIgnoreBillingField(obisCode)) &&
                (obisCode.getF() >= MIN_ATTR) &&
                (obisCode.getF() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final ModemWatchdogConfiguration modemWatchdogConfiguration = getCosemObjectFactory().getModemWatchdogConfiguration();
        return parse(obisCode, readAttribute(obisCode, modemWatchdogConfiguration));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, ModemWatchdogConfiguration modemWatchdogConfiguration) throws IOException {
        switch (obisCode.getF()) {
            // Logical name
            case 1:
                return OctetString.fromObisCode(ModemWatchdogConfiguration.getDefaultObisCode());
            case 2:
                return modemWatchdogConfiguration.readWDConfigAttribute();
            case 3:
                return modemWatchdogConfiguration.readModemWatchdogEnabledAttribute();
            default:
                throw new NoSuchRegisterException("Modem Watchdog Configuration attribute [" + obisCode.getF() + "] not supported!");
        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getF()) {
            // Logical name
            case 1:
                return new RegisterValue(obisCode, ModemWatchdogConfiguration.getDefaultObisCode().toString());
            case 2:
                try {
                    return new RegisterValue(obisCode, getWDConfigString(abstractDataType));
                } catch (JSONException e) {
                    return new RegisterValue(obisCode, abstractDataType.toString());
                }
            case 3:
                boolean state =  abstractDataType.getBooleanObject().getState();
                return new RegisterValue(obisCode,  new Quantity(state?1:0, Unit.getUndefined()));
            default:
                throw new NoSuchRegisterException("Modem Watchdog Configuration attribute [" + obisCode.getE() + "] not supported!");
        }
    }

    public String getWDConfigString(AbstractDataType wdConfigAttribute) throws IOException, JSONException {
        JSONObject json = new JSONObject();

        if (wdConfigAttribute.isStructure()) {
            Structure wdConfig = wdConfigAttribute.getStructure();

            json.put("interval", wdConfig.getDataType(0).getUnsigned16().getValue());
            json.put("initialDelay: ", wdConfig.getDataType(1).getUnsigned16().getValue());
            json.put("pppResetThreshold: ", wdConfig.getDataType(2).getUnsigned16().getValue());
            json.put("modemResetThreshold: ", wdConfig.getDataType(3).getUnsigned16().getValue());
            json.put("deviceResetThreshold: ", wdConfig.getDataType(4).getUnsigned16().getValue());

            return json.toString();
        } else {
            throw new ProtocolException("Could not get correct WDConfig attribute format.");
        }
    }

}



