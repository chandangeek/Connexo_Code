package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.helper;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;

public class ActualAttributeReader {

    public AbstractDataType read(AbstractDlmsProtocol dlmsProtocol, ObisCode obisCode, int attributeNo) throws IOException {
        ComposedRegister composedRegister = new ComposedRegister();
        UniversalObject universalObject = dlmsProtocol.getDlmsSession().getMeterConfig().findObject(obisCode);
        DLMSAttribute valueAttribute = new DLMSAttribute(obisCode, attributeNo, universalObject.getClassID());
        composedRegister.setRegisterValue(valueAttribute);

        return new ComposedCosemObject(dlmsProtocol.getDlmsSession(),
                dlmsProtocol.getDlmsSession().getProperties().isBulkRequest(),
                composedRegister.getAllAttributes()).getAttribute(composedRegister.getRegisterValueAttribute());
    }

}
