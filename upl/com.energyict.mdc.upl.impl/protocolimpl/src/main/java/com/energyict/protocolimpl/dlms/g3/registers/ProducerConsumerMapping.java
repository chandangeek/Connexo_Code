package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;

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
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        final CosemObjectFactory cof = as330D.getSession().getCosemObjectFactory();
        final Data data = cof.getData(getObisCode());
        final TypeEnum valueAttr = data.getValueAttr(TypeEnum.class);
        final String textValue = valueAttr.getValue() == 0 ? "CONSUMER_MODE" : "PRODUCER_MODE";
        return new RegisterValue(getObisCode(), textValue);
    }
}
