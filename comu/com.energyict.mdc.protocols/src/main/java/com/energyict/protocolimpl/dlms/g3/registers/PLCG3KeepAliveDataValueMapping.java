package com.energyict.protocolimpl.dlms.g3.registers;


import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 10:01
 */
public class PLCG3KeepAliveDataValueMapping extends G3Mapping {

    public PLCG3KeepAliveDataValueMapping(ObisCode obisCode) {
        super(obisCode);
    }


    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        final Data data = cosemObjectFactory.getData(getObisCode());
        return parse(data.getValueAttr());
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        Structure structure = abstractDataType.getStructure();
        if (structure != null) {
            boolean status = structure.getNextDataType().getBooleanObject().getState();
            long keepAliveTime = structure.getNextDataType().longValue();
            long bucketSize = structure.getNextDataType().longValue();
            StringBuilder builder = new StringBuilder();
            builder.append("Status: ");
            builder.append(status ? "Enabled" : "Disabled");
            builder.append(" - ");
            builder.append("Start time: ");
            builder.append(keepAliveTime);
            builder.append(" - ");
            builder.append("Send period: ");
            builder.append(bucketSize);
            return new RegisterValue(getObisCode(), builder.toString());
        } else {
            throw new ProtocolException("Failed to parse the keep alive data, encountered an invalid dataType: Expected [" + Structure.class.getSimpleName() + "] but received [" + abstractDataType.getClass().getSimpleName() + "]");
        }
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.DATA.getClassId();
    }
}
