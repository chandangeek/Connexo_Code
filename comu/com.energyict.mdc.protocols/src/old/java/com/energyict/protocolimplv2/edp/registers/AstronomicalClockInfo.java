/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp.registers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.dlms.axrdencoding.util.AXDRTime;

public class AstronomicalClockInfo {

    private static final String NEWLINE = "\n\r";

    public static String parse(AbstractDataType valueAttr) {
        if (valueAttr.isArray()) {
            Array array = valueAttr.getArray();
            StringBuilder sb = new StringBuilder();
            for (AbstractDataType abstractDataType : array.getAllDataTypes()) {
                Structure structure = abstractDataType.getStructure();
                if (structure != null) {
                    String date = AXDRDate.toDescription(structure.getDataType(0).getOctetString());
                    String sunriseTime = new AXDRTime(structure.getDataType(1).getBEREncodedByteArray()).getShortTimeDescription();
                    String sunsetTime = new AXDRTime(structure.getDataType(2).getBEREncodedByteArray()).getShortTimeDescription();
                    sb.append(date).append(": ").append(sunriseTime).append(" - ").append(sunsetTime).append(NEWLINE);
                }
            }

            if (sb.toString().length() == 0) {
                sb.append("(empty)");
            }
            return sb.toString();
        }
        return "";
    }
}