package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex;

import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RegisterFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 31-aug-2011
 * Time: 16:44:24
 */
public class XemexRegisterFactory extends DSMR40RegisterFactory {

    public static final ObisCode ERROR_REGISTER = ObisCode.fromString("0.0.97.97.0.255");
    public static final ObisCode ALARM_REGISTER = ObisCode.fromString("0.0.97.98.0.255");
    public static final ObisCode ALARM_FILTER = ObisCode.fromString("0.0.97.98.10.255");

    public XemexRegisterFactory(final AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    @Override
    protected RegisterValue convertCustomAbstractObjectsToRegisterValues(final Register register, AbstractDataType abstractDataType) throws IOException {
        ObisCode rObisCode = getCorrectedRegisterObisCode(register);
        if (rObisCode.equals(ERROR_REGISTER) || rObisCode.equals(ALARM_REGISTER) || rObisCode.equals(ALARM_FILTER)) {
            long value = abstractDataType.longValue();
            String text = "0x" + ProtocolTools.getHexStringFromInt((int) value, 4, "");
            return new RegisterValue(register, new Quantity(new BigDecimal(value), Unit.getUndefined()), null, null, null, new Date(), 0, text);
        }
        return super.convertCustomAbstractObjectsToRegisterValues(register, abstractDataType);
    }
}