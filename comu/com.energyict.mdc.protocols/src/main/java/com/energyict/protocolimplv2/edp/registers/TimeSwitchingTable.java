package com.energyict.protocolimplv2.edp.registers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.dlms.axrdencoding.util.AXDRTime;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 9:19
 * Author: khe
 */
public class TimeSwitchingTable {

    private static final String NEWLINE = "\n\r";

    public static String parse(AbstractDataType valueAttr, ObisCode obisCode) throws IOException {
        if (valueAttr.isArray()) {
            Array array = valueAttr.getArray();
            StringBuilder sb = new StringBuilder();
            for (AbstractDataType abstractDataType : array.getAllDataTypes()) {
                Structure structure = abstractDataType.getStructure();
                if (structure != null) {
                    if (structure.nrOfDataTypes() == 3) {
                        //Only show the entries for the month (1 based) indicated in the B-field of the obiscode
                        if (structure.getDataType(0).getOctetString().toByteArray()[2] == obisCode.getB()) {
                            String date = AXDRDate.toDescription(structure.getDataType(0).getOctetString());
                            String sunriseTime = new AXDRTime(structure.getDataType(1).getBEREncodedByteArray()).getShortTimeDescription();
                            String sunsetTime = new AXDRTime(structure.getDataType(2).getBEREncodedByteArray()).getShortTimeDescription();
                            sb.append(date).append(": ").append(sunriseTime).append(" - ").append(sunsetTime).append(NEWLINE);
                        }
                    } else {
                        throw new IOException("Unexpected structure in the time switching table: should contain 4 elements (begin date, end date, switch off time and switch on time, but contained only " + structure.nrOfDataTypes() + " elements");
                    }
                } else {
                    throw new IOException("Unexpected data type in the time switching table: expected array of structures but received array of " + abstractDataType.getClass().getSimpleName());
                }
            }
            if (sb.toString().length() == 0) {
                sb.append("(empty)");
            }
            return sb.toString();
        }
        throw new IOException("Unexpected data type in the time switching table: expected array of structures but received " + valueAttr.getClass().getSimpleName());
    }
}