package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.registers;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.attributes.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ObisCodeProvider;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 25/07/11
 * Time: 9:50
 */
public class ObisCodeMapper {

    /**




     ObisCode REG_PROFILE_DEMAND                 = ObisCode.fromString("3       7.0.1.25.0.255
     ObisCode REG_DEMAND_TOTAL		             = ObisCode.fromString("5	0	7.0.1.4.0.255
     ObisCode REG_MAXIMUM_DEMAND_ENERGY_IMPORT	 = ObisCode.fromString("4	0	7.0.1.6.1.255

     */

    public static final Map<ObisCode, DLMSAttribute> ABSTRACT_REGISTER = new HashMap<ObisCode, DLMSAttribute>();

    static {

        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_CLOCK_TIME_SHIFT_EVENT_LIMIT, new DLMSAttribute(ObisCodeProvider.REG_CLOCK_TIME_SHIFT_EVENT_LIMIT, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_CLOCK_SYNC_WINDOW, new DLMSAttribute(ObisCodeProvider.REG_CLOCK_SYNC_WINDOW, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_ERROR, new DLMSAttribute(ObisCodeProvider.REG_ERROR, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_ALARM, new DLMSAttribute(ObisCodeProvider.REG_ALARM, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_TIME_SINCE_LAST_EOB_1, new DLMSAttribute(ObisCodeProvider.REG_TIME_SINCE_LAST_EOB_1, RegisterAttributes.Register_Value));

        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOTAL, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOTAL, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_1, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_1, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_2, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_2, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_3, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_3, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_4, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_4, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_5, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_5, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_6, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_6, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_7, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_7, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_8, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_8, RegisterAttributes.Register_Value));

        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_PROFILE_DEMAND, new DLMSAttribute(ObisCodeProvider.REG_PROFILE_DEMAND, RegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_DEMAND_TOTAL, new DLMSAttribute(ObisCodeProvider.REG_DEMAND_TOTAL, DemandRegisterAttributes.Register_Value));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_MAXIMUM_DEMAND_ENERGY_IMPORT, new DLMSAttribute(ObisCodeProvider.REG_MAXIMUM_DEMAND_ENERGY_IMPORT, ExtendedRegisterAttributes.Register_Value));

    }

    public static final Map<ObisCode, DLMSAttribute> ABSTRACT_VALUE = new HashMap<ObisCode, DLMSAttribute>();

    static {

    }

    public boolean isAbstractRegister(ObisCode obisCode) {
        return getAbstractRegisterDLMSAttribute(obisCode) != null;
    }


    public DLMSAttribute getAbstractRegisterDLMSAttribute(ObisCode obisCode) {
        return ABSTRACT_TEXT.get(obisCode);
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
        allMappings.putAll(ABSTRACT_TEXT);
        allMappings.putAll(ABSTRACT_VALUE);
        return allMappings.get(obisCode);
    }

    public boolean isSupported(ObisCode obisCode) {
        return getDLMSAttribute(obisCode) != null;
    }

    public List<DLMSAttribute> getDLMSAttributes(List<Register> registers) {
        List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
        for (Register register : registers) {
            ObisCode obisCode = register.getObisCode();
            if (isSupported(obisCode)) {
                dlmsAttributes.add(getDLMSAttribute(obisCode));
            }
        }
        return dlmsAttributes;
    }

    public static void main(String[] args) throws IOException {
        byte[] bytesFromHexString = ProtocolTools.getBytesFromHexString("01020203090c07db0717060e12000000000012000006000000000203090c07db0717060e1100000000001200140600000000", "");
        AbstractDataType decode = AXDRDecoder.decode(bytesFromHexString);
        System.out.println(decode);
    }

}
