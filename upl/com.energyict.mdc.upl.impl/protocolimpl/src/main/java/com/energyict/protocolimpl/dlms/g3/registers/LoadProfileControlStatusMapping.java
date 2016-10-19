package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 13/10/12
 * Time: 10:01
 */
public class LoadProfileControlStatusMapping extends G3Mapping {

        public LoadProfileControlStatusMapping(ObisCode obisCode) {
            super(obisCode);
        }

        @Override
        public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
            final Data data = cosemObjectFactory.getData(getObisCode());
            return parse(data.getValueAttr());
        }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        return null;
    }

    public RegisterValue parse(AbstractDataType abstractDataType) throws IOException {
            return new RegisterValue(getObisCode(), getStatusString(abstractDataType));
        }

    public String getStatusString(AbstractDataType InOutStatusAttribute) throws IOException {
        StringBuffer builder = new StringBuffer();

        if (InOutStatusAttribute.isUnsigned8()) {
            int status = InOutStatusAttribute.getUnsigned8().getValue();

            switch (status) {
                case 0:
                    builder.append("Activated capturing in load profiles 1 and 2");
                    break;
                case 1:
                    builder.append("Activated capturing in load profile 2. Deactivated capturing in load profile 1.");
                    break;
                case 2:
                    builder.append("Activated capturing in load profile 1. Deactivated capturing in load profile 2.");
                    break;
                case 3:
                    builder.append("Deactivated capturing in load profiles 1 and 2.");
                    break;
                default:
                    builder.append("Not supported value for LoadProfile In/Out Status: "+ status);
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get correct LoadProfile In/Out Status attribute format.");
        }
    }

        @Override
        public int getDLMSClassId() {
            return DLMSClassId.DATA.getClassId();
        }
    }

