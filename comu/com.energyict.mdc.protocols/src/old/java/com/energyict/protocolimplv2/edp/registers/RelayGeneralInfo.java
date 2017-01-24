package com.energyict.protocolimplv2.edp.registers;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDate;

import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 9:59
 * Author: khe
 */
public class RelayGeneralInfo {

    private static final String NEWLINE = "\n\r";

    public static String parse(AbstractDataType abstractDataType, TimeZone deviceTimeZone) {
        StringBuilder sb = new StringBuilder();

        Structure structure = abstractDataType.getStructure();
        sb.append("Current IP output status: ").append(structure.getDataType(0).getBooleanObject().getState() ? "IP Connected (output relay closed)" : "IP disconnected (output relay opened)").append(NEWLINE);
        sb.append("Current operating mode: ").append(RelayOperatingMode.fromValue(structure.getDataType(1).getTypeEnum().getValue()).getDescription()).append(NEWLINE);
        String switchOffDate = getOctetDateDescription(deviceTimeZone, structure.getDataType(2).getOctetString());
        String switchOnDate = getOctetDateDescription(deviceTimeZone, structure.getDataType(3).getOctetString());
        sb.append("Next switch_off transition: ").append(switchOffDate).append(NEWLINE);
        sb.append("Next switch_on transition: ").append(switchOnDate);

        return sb.toString();
    }

    private static String getOctetDateDescription(TimeZone deviceTimeZone, OctetString switchOffDate) {
        if (switchOffDate.getOctetStr().length == 12) {   //Date & time
            return switchOffDate.getDateTime(deviceTimeZone).getValue().getTime().toString();
        } else {                                          //Date only
            return AXDRDate.toDescription(switchOffDate);
        }
    }
}