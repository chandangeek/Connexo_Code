package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.Date;

/**
 * Created by cisac on 12/12/2016.
 */
public class MultiAPNConfigMapping extends G3Mapping{

    public MultiAPNConfigMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        final Data data = cosemObjectFactory.getData(getObisCode());
        return parse(data.getValueAttr());
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        Structure structure = abstractDataType.getStructure();
        long activeAPN = structure.getDataType(0).longValue();
        Array apnConfigs = structure.getDataType(1).getArray();
        StringBuilder multiApnSetup = new StringBuilder();
        multiApnSetup.append("Active APN: " + activeAPN + ".");
        multiApnSetup.append(" Available APN configurations:");
        int index = 1;
        for(AbstractDataType config: apnConfigs){
            multiApnSetup.append(" APN config " + index + ":");
            multiApnSetup.append(" APN name: " + getStringValueFromStructureEntry(config, 0));
            multiApnSetup.append(" User name: " + getStringValueFromStructureEntry(config, 1));
            multiApnSetup.append(" Passwrod: " + getStringValueFromStructureEntry(config, 2));
        }
        return new RegisterValue(getObisCode(), multiApnSetup.toString());
    }

    private String getStringValueFromStructureEntry(AbstractDataType config, int entryIndex) {
        return config.getStructure().getDataType(entryIndex).getOctetString().stringValue();
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.DATA.getClassId();
    }
}
