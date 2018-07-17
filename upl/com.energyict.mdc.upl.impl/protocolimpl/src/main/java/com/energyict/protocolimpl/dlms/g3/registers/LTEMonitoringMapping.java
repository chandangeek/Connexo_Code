package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.LTEMonitoringIC;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.LTEMonitoringAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Created by H165680 on 17/04/2017.
 */
public class LTEMonitoringMapping extends G3Mapping {

    private LTEMonitoringAttributesMapping lteMonitoringAttributesMapping;

    protected LTEMonitoringMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public ObisCode getBaseObisCode() {                 //Set the E-Filed to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 4, (byte) 0);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (lteMonitoringAttributesMapping == null) {
            lteMonitoringAttributesMapping = new LTEMonitoringAttributesMapping(cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        return getObisCode().getE();    // The E-field of the obiscode indicates which attribute is being read
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);  //Not used here

        if (lteMonitoringAttributesMapping.canRead(getObisCode())) {
            return lteMonitoringAttributesMapping.parse(getObisCode(), abstractDataType);
        }

        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (lteMonitoringAttributesMapping.canRead(obisCode)) {
            return lteMonitoringAttributesMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        if (getObisCode().equalsIgnoreBAndEChannel(LTEMonitoringIC.getDefaultObisCode())) {
            return DLMSClassId.LTE_MONITORING.getClassId();
        } else {
            return -1;
        }
    }
}
