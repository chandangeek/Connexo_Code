package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.g3.SerialNumber;

import java.io.IOException;
import java.util.Date;

/**
* Copyrights EnergyICT
* Date: 22/03/12
* Time: 9:23
*/
public class LogicalDeviceNameMapping extends G3Mapping {

    public LogicalDeviceNameMapping(ObisCode obisCode) {
        super(obisCode);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        Data data = cosemObjectFactory.getData(getObisCode());
        return parse(data.getValueAttr(OctetString.class));
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        SerialNumber serialNumber = SerialNumber.fromBytes(((OctetString) abstractDataType).getOctetStr());
        return new RegisterValue(getObisCode(), serialNumber.getEuridisADS());
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.DATA.getClassId();
    }

}
