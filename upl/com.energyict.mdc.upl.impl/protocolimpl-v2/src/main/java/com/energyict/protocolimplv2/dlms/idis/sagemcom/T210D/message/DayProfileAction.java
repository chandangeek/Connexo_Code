package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Created by cisac on 19/01/2017.
 */
public class DayProfileAction extends Structure{

    public DayProfileAction(String dayProfileActionDefinition){
        parseDayProfileAction(dayProfileActionDefinition);
    }

    private void parseDayProfileAction(String dayProfileActionDefinition) {
        String[] elements = dayProfileActionDefinition.replace(String.valueOf(">"), "").replace(String.valueOf("<"), "").split(",");
        OctetString startTime = OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(elements[0].trim(), ""));
        OctetString script_logical_name = OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(elements[1].trim(), ""));
        Unsigned16 threshold = new Unsigned16(Integer.parseInt(elements[2].trim()));
        addDataType(startTime);
        addDataType(script_logical_name);
        addDataType(threshold);
    }

}
