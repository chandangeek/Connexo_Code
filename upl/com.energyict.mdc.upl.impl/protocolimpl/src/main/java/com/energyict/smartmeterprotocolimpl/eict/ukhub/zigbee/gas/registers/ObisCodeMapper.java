package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.registers;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ObisCodeProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 25/07/11
 * Time: 9:50
 */
public class ObisCodeMapper {

    public static final Map<ObisCode, DLMSAttribute> ABSTRACT_VALUE = new HashMap<ObisCode, DLMSAttribute>();

    static {

    }

    public static final Map<ObisCode, DLMSAttribute> ABSTRACT_TEXT = new HashMap<ObisCode, DLMSAttribute>();

    static {
        ABSTRACT_TEXT.put(ObisCodeProvider.SERIAL_NUMBER, new DLMSAttribute(ObisCodeProvider.SERIAL_NUMBER, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.FIRMWARE_VERSION_MID, new DLMSAttribute(ObisCodeProvider.FIRMWARE_VERSION_MID, DataAttributes.VALUE));
    }

    public boolean isAbstractTextRegister(ObisCode obisCode) {
        return getAbstractTextDLMSAttribute(obisCode) != null;
    }

    public DLMSAttribute getAbstractTextDLMSAttribute(ObisCode obisCode) {
        return ABSTRACT_TEXT.get(obisCode);
    }

    public boolean isAbstractValueRegister(ObisCode obisCode) {
        return getAbstractValueDLMSAttribute(obisCode) != null;
    }

    public DLMSAttribute getAbstractValueDLMSAttribute(ObisCode obisCode) {
        return ABSTRACT_VALUE.get(obisCode);
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        Map<ObisCode, DLMSAttribute> allMappings = new HashMap<ObisCode, DLMSAttribute>();

        return allMappings.get(obisCode);
    }

}
