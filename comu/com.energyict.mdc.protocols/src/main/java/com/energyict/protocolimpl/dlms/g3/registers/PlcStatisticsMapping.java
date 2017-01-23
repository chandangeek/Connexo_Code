package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.PLCOFDMType2MACSetup;
import com.energyict.dlms.cosem.PLCOFDMType2PHYAndMACCounters;
import com.energyict.dlms.cosem.SixLowPanAdaptationLayerSetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PLCOFDMType2MACSetupMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PLCOFDMType2PHYAndMACCountersMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.SixLowPanAdaptationLayerSetupMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

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
    public ObisCode getBaseObisCode() {                 //Set the B-field to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 1, (byte) 0);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (plcofdmType2MACSetupMapping == null) {
            this.plcofdmType2MACSetupMapping = new PLCOFDMType2MACSetupMapping(cosemObjectFactory);
        }
        if (plcofdmType2PHYAndMACCountersMapping == null) {
            this.plcofdmType2PHYAndMACCountersMapping = new PLCOFDMType2PHYAndMACCountersMapping(cosemObjectFactory);
        }
        if (sixLowPanAdaptationLayerSetupMapping == null) {
            this.sixLowPanAdaptationLayerSetupMapping = new SixLowPanAdaptationLayerSetupMapping(cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        return getObisCode().getB();        //The B-field of the obiscode indicates which attribute is being read
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);  //Not used here

        if (plcofdmType2MACSetupMapping.canRead(getObisCode())) {
            return plcofdmType2MACSetupMapping.parse(getObisCode(), abstractDataType);
        }
        if (plcofdmType2PHYAndMACCountersMapping.canRead(getObisCode())) {
            return plcofdmType2PHYAndMACCountersMapping.parse(getObisCode(), abstractDataType);
        }
        if (sixLowPanAdaptationLayerSetupMapping.canRead(getObisCode())) {
            return sixLowPanAdaptationLayerSetupMapping.parse(getObisCode(), abstractDataType);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
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

    @Override
    public int getDLMSClassId() {
        if (getObisCode().equalsIgnoreBChannel(PLCOFDMType2MACSetup.getDefaultObisCode())) {
            return DLMSClassId.PLC_OFDM_TYPE2_MAC_SETUP.getClassId();
        } else if (getObisCode().equalsIgnoreBChannel(PLCOFDMType2PHYAndMACCounters.getDefaultObisCode())) {
            return DLMSClassId.PLC_OFDM_TYPE2_PHY_AND_MAC_COUNTERS.getClassId();
        } else if (getObisCode().equalsIgnoreBChannel(SixLowPanAdaptationLayerSetup.getDefaultObisCode())) {
            return DLMSClassId.SIX_LOW_PAN_ADAPTATION_LAYER_SETUP.getClassId();
        } else {
            return -1;
        }
    }
}
