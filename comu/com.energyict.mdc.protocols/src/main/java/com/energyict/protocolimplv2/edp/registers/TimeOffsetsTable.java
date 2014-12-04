package com.energyict.protocolimplv2.edp.registers;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDate;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 9:19
 * Author: khe
 */
public class TimeOffsetsTable {

    private static final String SEPARATOR = ",";
    private static final String NEWLINE = "\n\r";

    public static String parse(AbstractDataType valueAttr) throws IOException {
        if (valueAttr.isArray()) {
            Array array = valueAttr.getArray();
            StringBuilder sb = new StringBuilder();
            for (AbstractDataType abstractDataType : array.getAllDataTypes()) {
                Structure structure = abstractDataType.getStructure();
                if (structure != null) {
                    if (structure.nrOfDataTypes() == 4) {
                        String beginDate;
                        String endDate;
                        int offsetOff;
                        int offsetOn;
                        if (structure.getDataType(0).isOctetString()) {
                            beginDate = AXDRDate.toDescription(structure.getDataType(0).getOctetString());
                            endDate = AXDRDate.toDescription(structure.getDataType(1).getOctetString());
                            offsetOff = structure.getDataType(2).intValue();
                            offsetOn = structure.getDataType(3).intValue();
                        } else {
                            offsetOff = structure.getDataType(0).intValue();
                            offsetOn = structure.getDataType(1).intValue();
                            beginDate = AXDRDate.toDescription(structure.getDataType(2).getOctetString());
                            endDate = AXDRDate.toDescription(structure.getDataType(3).getOctetString());
                        }

                        sb.append(beginDate).append(SEPARATOR).append(endDate).append(SEPARATOR).append(offsetOff).append(SEPARATOR).append(offsetOn).append(NEWLINE);
                    } else {
                        throw new IOException("Unexpected structure in the time offsets table: should contain 4 elements (begin date, end date, off offset and on offset, but contained only " + structure.nrOfDataTypes() + " elements");
                    }
                } else {
                    throw new IOException("Unexpected data type in the time offsets table: expected array of structures but received array of " + abstractDataType.getClass().getSimpleName());
                }
            }

            if (sb.toString().length() == 0) {
                sb.append("(empty)");
            }
            return sb.toString();
        }
        throw new IOException("Unexpected data type in the time offsets table: expected array of structures but received " + valueAttr.getClass().getSimpleName());
    }
}