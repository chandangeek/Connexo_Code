package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 14/10/12
 * Time: 16:01
 */
public class LoadProfileDisplayControlStatusMapping extends G3Mapping {

    public LoadProfileDisplayControlStatusMapping(ObisCode obisCode) {
        super(obisCode);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        final Data data = cosemObjectFactory.getData(getObisCode());
        return parse(data.getValueAttr());
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        if (abstractDataType.isUnsigned8()) {
            int status = abstractDataType.getUnsigned8().getValue();
            return new RegisterValue(getObisCode(),
                    new Quantity(BigDecimal.valueOf(status), Unit.get("")),
                    null, null, null, new Date(), 0,
                    getStatusString(status));
        } else {
            return new RegisterValue(getObisCode(), abstractDataType.toString());
        }
    }

    public String getStatusString(int status) throws IOException {
        StringBuffer builder = new StringBuffer();
        switch (status) {
            case 0:
                builder.append("LoadProfile1 readout via display enabled");
                break;
            case 1:
                builder.append("LoadProfile1 readout via display disabled");
                break;
            default:
                builder.append("Not supported value for LoadProfile Display Control Status: " + status);
        }
        return builder.toString();
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.DATA.getClassId();
    }
}


