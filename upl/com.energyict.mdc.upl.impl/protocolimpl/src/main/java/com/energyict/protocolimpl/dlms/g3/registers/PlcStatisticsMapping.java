package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.dlms.DlmsSession;
import com.energyict.genericprotocolimpl.rtuplusserver.g3.register.mapping.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 8:31
 */
public class PlcStatisticsMapping extends G3Mapping {

    private PLCOFDMType2MACSetupMapping plcofdmType2MACSetupMapping;
    private PLCOFDMType2PHYAndMACCountersMapping plcofdmType2PHYAndMACCountersMapping;
    private SixLowPanAdaptationLayerSetupMapping sixLowPanAdaptationLayerSetupMapping;

    public PlcStatisticsMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        instantiateMappers(as330D.getSession());
        return readRegister(getObisCode());
    }

    private void instantiateMappers(DlmsSession session) {
        if (plcofdmType2MACSetupMapping == null) {
            this.plcofdmType2MACSetupMapping = new PLCOFDMType2MACSetupMapping(session);
        }
        if (plcofdmType2PHYAndMACCountersMapping == null) {
            this.plcofdmType2PHYAndMACCountersMapping = new PLCOFDMType2PHYAndMACCountersMapping(session);
        }
        if (sixLowPanAdaptationLayerSetupMapping == null) {
            this.sixLowPanAdaptationLayerSetupMapping = new SixLowPanAdaptationLayerSetupMapping(session);
        }
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (plcofdmType2MACSetupMapping.canRead(obisCode)) {
            return plcofdmType2MACSetupMapping.readRegister(obisCode);
        }
        if (plcofdmType2PHYAndMACCountersMapping.canRead(obisCode)) {
            return plcofdmType2PHYAndMACCountersMapping.readRegister(obisCode);
        }
        if (sixLowPanAdaptationLayerSetupMapping.canRead(obisCode)) {
            return sixLowPanAdaptationLayerSetupMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }
}
