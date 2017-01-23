package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.registers;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
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

    public static final Map<ObisCode, DLMSAttribute> ABSTRACT_REGISTER = new HashMap<ObisCode, DLMSAttribute>();

    static {

        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_CLOCK_TIME_SHIFT_EVENT_LIMIT, new DLMSAttribute(ObisCodeProvider.REG_CLOCK_TIME_SHIFT_EVENT_LIMIT, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_CLOCK_SYNC_WINDOW, new DLMSAttribute(ObisCodeProvider.REG_CLOCK_SYNC_WINDOW, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_ERROR, new DLMSAttribute(ObisCodeProvider.REG_ERROR, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_ALARM, new DLMSAttribute(ObisCodeProvider.REG_ALARM, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_TIME_SINCE_LAST_EOB_1, new DLMSAttribute(ObisCodeProvider.REG_TIME_SINCE_LAST_EOB_1, RegisterAttributes.VALUE));

        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOTAL, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOTAL, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_1, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_1, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_2, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_2, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_3, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_3, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_4, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_4, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_5, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_5, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_6, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_6, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_7, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_7, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_GAS_VOLUME_TOU_8, new DLMSAttribute(ObisCodeProvider.REG_GAS_VOLUME_TOU_8, RegisterAttributes.VALUE));

        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_PROFILE_DEMAND, new DLMSAttribute(ObisCodeProvider.REG_PROFILE_DEMAND, RegisterAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_DEMAND_TOTAL, new DLMSAttribute(ObisCodeProvider.REG_DEMAND_TOTAL, DemandRegisterAttributes.CURRENT_AVG_VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.REG_MAXIMUM_DEMAND_ENERGY_IMPORT, new DLMSAttribute(ObisCodeProvider.REG_MAXIMUM_DEMAND_ENERGY_IMPORT, ExtendedRegisterAttributes.VALUE));

        ABSTRACT_REGISTER.put(ObisCodeProvider.DeviceId9, new DLMSAttribute(ObisCodeProvider.DeviceId9, DataAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.DeviceId50, new DLMSAttribute(ObisCodeProvider.DeviceId50, DataAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.DeviceId51, new DLMSAttribute(ObisCodeProvider.DeviceId51, DataAttributes.VALUE));
        ABSTRACT_REGISTER.put(ObisCodeProvider.DeviceId52, new DLMSAttribute(ObisCodeProvider.DeviceId52, DataAttributes.VALUE));
    }

    public static final Map<ObisCode, DLMSAttribute> ABSTRACT_VALUE = new HashMap<ObisCode, DLMSAttribute>();

    static {

    }

    public boolean isAbstractRegister(ObisCode obisCode) {
        return getAbstractRegisterDLMSAttribute(obisCode) != null;
    }


    public DLMSAttribute getAbstractRegisterDLMSAttribute(ObisCode obisCode) {
        return ABSTRACT_REGISTER.get(obisCode);
    }

    public static final Map<ObisCode, DLMSAttribute> ABSTRACT_TEXT = new HashMap<ObisCode, DLMSAttribute>();

    static {
        //Device ID's
        ABSTRACT_TEXT.put(ObisCodeProvider.DeviceId1, new DLMSAttribute(ObisCodeProvider.DeviceId1, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.DeviceId2, new DLMSAttribute(ObisCodeProvider.DeviceId2, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.DeviceId3, new DLMSAttribute(ObisCodeProvider.DeviceId3, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.DeviceId4, new DLMSAttribute(ObisCodeProvider.DeviceId4, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.DeviceId5, new DLMSAttribute(ObisCodeProvider.DeviceId5, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.DeviceId6, new DLMSAttribute(ObisCodeProvider.DeviceId6, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.DeviceId7, new DLMSAttribute(ObisCodeProvider.DeviceId7, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.DeviceId8, new DLMSAttribute(ObisCodeProvider.DeviceId8, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.DeviceId10, new DLMSAttribute(ObisCodeProvider.DeviceId10, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.MeteringPointId, new DLMSAttribute(ObisCodeProvider.MeteringPointId, DataAttributes.VALUE));


        //FirmwareVersions
        ABSTRACT_TEXT.put(ObisCodeProvider.OperationalFirmwareMonlicitic, new DLMSAttribute(ObisCodeProvider.OperationalFirmwareMonlicitic, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.OperationalFirmwareBootloader, new DLMSAttribute(ObisCodeProvider.OperationalFirmwareBootloader, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.OperationalFirmwareHAN, new DLMSAttribute(ObisCodeProvider.OperationalFirmwareHAN, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.OperationalFirmwareMID, new DLMSAttribute(ObisCodeProvider.OperationalFirmwareMID, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.OperationalFirmwareNonMIDApp, new DLMSAttribute(ObisCodeProvider.OperationalFirmwareNonMIDApp, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.OperationalFirmwareStackVersion, new DLMSAttribute(ObisCodeProvider.OperationalFirmwareStackVersion, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.OperationalFirmwareWAN, new DLMSAttribute(ObisCodeProvider.OperationalFirmwareWAN, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.OperationalFirmwareZCLVersion, new DLMSAttribute(ObisCodeProvider.OperationalFirmwareZCLVersion, DataAttributes.VALUE));
        ABSTRACT_TEXT.put(ObisCodeProvider.OperationalFirmwareZigbeeChip, new DLMSAttribute(ObisCodeProvider.OperationalFirmwareZigbeeChip, DataAttributes.VALUE));
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

}
