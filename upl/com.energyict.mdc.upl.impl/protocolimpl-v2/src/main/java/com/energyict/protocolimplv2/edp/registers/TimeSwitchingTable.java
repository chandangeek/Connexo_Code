package com.energyict.protocolimplv2.edp.registers;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.dlms.axrdencoding.util.AXDRTime;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 9:19
 * Author: khe
 */
public class TimeSwitchingTable {

    private static final String NEWLINE = "\n\r";

    public static String parse(AbstractDataType valueAttr, ObisCode obisCode, AbstractDlmsProtocol protocol) throws IOException {
        if (valueAttr.isArray()) {
            Array array = valueAttr.getArray();
            StringBuilder sb = new StringBuilder();
            for (AbstractDataType abstractDataType : array.getAllDataTypes()) {
                Structure structure = abstractDataType.getStructure();
                if (structure != null) {
                    if (structure.nrOfDataTypes() == 4) {
                        AbstractDataType beginDateDataType = structure.getDataType(0);
                        AbstractDataType endDateDataType = structure.getDataType(1);
                        // Only show the entries for the month (1 based) indicated in the B-field of the obis code
                        if (isForTheConfiguredMonth(beginDateDataType, endDateDataType, obisCode.getB())) {
                            String date1 = AXDRDate.toDescription(beginDateDataType.getOctetString());
                            String date2 = AXDRDate.toDescription(endDateDataType.getOctetString());
                            String sunriseTime = new AXDRTime(structure.getDataType(2).getBEREncodedByteArray()).getShortTimeDescription();
                            String sunsetTime = new AXDRTime(structure.getDataType(3).getBEREncodedByteArray()).getShortTimeDescription();
                            sb.append(date1).append(" - ").append(date2).append(": ").append(sunriseTime).append(" - ").append(sunsetTime).append(NEWLINE);
                        }
                    } else {
                        protocol.journal("Unexpected structure in the time switching table: should contain 4 elements (begin date, end date, switch off time and switch on time, but contained only " +
                                structure.nrOfDataTypes() + " elements: ");
                        for (int i = 0; i < structure.nrOfDataTypes(); i++) {
                            protocol.journal("TimeSwitchingTableStructure[" + i + "] = " + ProtocolTools.getHexStringFromBytes(structure.getDataType(i).getBEREncodedByteArray()));
                        }
                        throw new IOException("Unexpected structure in the time switching table: should contain 4 elements (begin date, end date, switch off time and switch on time, but contained only " +
                                structure.nrOfDataTypes() + " elements.");
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

    private static boolean isForTheConfiguredMonth(AbstractDataType beginDateType,  AbstractDataType endDateType, int month) {
        return beginDateType.getOctetString().toByteArray()[2] == month || endDateType.getOctetString().toByteArray()[2] == month ||
                beginDateType.getOctetString().toByteArray()[2] < month && endDateType.getOctetString().toByteArray()[2] > month;
    }
}