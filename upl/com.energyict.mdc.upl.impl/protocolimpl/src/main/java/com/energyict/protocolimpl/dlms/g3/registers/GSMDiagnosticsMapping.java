package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.GSMDiagnosticsIC;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.GSMDiagnosticsAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Created by H165680 on 17/04/2017.
 */
public class GSMDiagnosticsMapping extends G3Mapping {

    private GSMDiagnosticsAttributesMapping gsmDiagnosticsAttributesMapping;

    protected GSMDiagnosticsMapping(ObisCode obis) {
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
        if (gsmDiagnosticsAttributesMapping == null) {
            gsmDiagnosticsAttributesMapping = new GSMDiagnosticsAttributesMapping(cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        return getObisCode().getE();    // The E-field of the obiscode indicates which attribute is being read
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);  //Not used here

        if (gsmDiagnosticsAttributesMapping.canRead(getObisCode())) {
            return gsmDiagnosticsAttributesMapping.parse(getObisCode(), abstractDataType);
        }

        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (gsmDiagnosticsAttributesMapping.canRead(obisCode)) {
            return gsmDiagnosticsAttributesMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        if (getObisCode().equalsIgnoreBAndEChannel(GSMDiagnosticsIC.getDefaultObisCode())) {
            return DLMSClassId.GSM_DIAGNOSTICS.getClassId();
        } else {
            return -1;
        }
    }
}
