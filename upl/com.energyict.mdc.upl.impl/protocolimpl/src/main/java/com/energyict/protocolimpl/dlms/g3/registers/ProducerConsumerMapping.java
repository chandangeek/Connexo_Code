package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 9:21
 */
class ProducerConsumerMapping extends G3Mapping {

    public ProducerConsumerMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public RegisterValue readRegister(DlmsSession dlmsSession) throws IOException {
        final CosemObjectFactory cof = dlmsSession.getCosemObjectFactory();
        final Data data = cof.getData(getObisCode());
        return parse(data.getValueAttr(TypeEnum.class));
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        final TypeEnum valueAttr = (TypeEnum) abstractDataType;
        final String textValue = valueAttr.getValue() == 0 ? "CONSUMER_MODE" : "PRODUCER_MODE";
        return new RegisterValue(getObisCode(), textValue);
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.DATA.getClassId();
    }
}
