package com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRTime;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GatewaySetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author sva
 * @since 15/10/2014 - 13:42
 */
public class GatewaySetupMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 6;

    public GatewaySetupMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        final ObisCode defaultObisCode = GatewaySetup.getDefaultObisCode();
        return ProtocolTools.equalsIgnoreBField(defaultObisCode, obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        AbstractDataType abstractDataType = readAttribute(obisCode, getCosemObjectFactory().getGatewaySetup());
        return parse(obisCode, abstractDataType);
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, GatewaySetup.getDefaultObisCode().toString());

            // Meter whitelist
            case 2:
                final Array meterWhitelist = (Array) abstractDataType;
                String meters = "";
                final Iterator<AbstractDataType> iterator = meterWhitelist.getAllDataTypes().iterator();
                while (iterator.hasNext()) {
                    final OctetString next = (OctetString) iterator.next();
                    meters += next.stringValue();
                    if (iterator.hasNext()) {
                        meters += ", ";
                    }
                }
                return new RegisterValue(obisCode, meters);

            // Operating window start time
            case 3:
                final String windowStartTime = new AXDRTime((OctetString) abstractDataType).getTime();
                return new RegisterValue(obisCode, windowStartTime);

            // Operating window end time
            case 4:
                final String windowEndTime = new AXDRTime((OctetString) abstractDataType).getTime();
                return new RegisterValue(obisCode, windowEndTime);

            // Whitelist is active
            case 5:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            // Operating window is active
            case 6:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            default:
                throw new NoSuchRegisterException("GatewaySetupMapping attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    public AbstractDataType readAttribute(ObisCode obisCode, AbstractCosemObject abstractCosemObject) throws IOException {
        GatewaySetup gatewaySetup = (GatewaySetup) abstractCosemObject;

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return OctetString.fromObisCode(GatewaySetup.getDefaultObisCode());

            // Meter whitelist
            case 2:
                return gatewaySetup.getMeterWhitelist();

            // Operating window start time
            case 3:
                return gatewaySetup.getOperatingWindowStartTime();

            // Operating window end time
            case 4:
                return new AXDRTime(gatewaySetup.getOperatingWindowEndTime());

            // Whitelist is active
            case 5:
                return gatewaySetup.getWhitelistEnabled();

            // Operating window is active
            case 6:
                return gatewaySetup.getOperatingWindowEnabled();

            default:
                throw new NoSuchRegisterException("GatewaySetupMapping attribute [" + obisCode.getB() + "] not supported!");
        }
    }
}