/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp.registers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;

import java.util.Calendar;
import java.util.TimeZone;

public class RelayLastTransitions {

    private static final String NEWLINE = "\n\r";
    private static final String SEPARATOR = ",";

    public static String parse(AbstractDataType array, TimeZone deviceTimeZone) {
        StringBuilder sb = new StringBuilder();
        if (array.isArray()) {
            for (AbstractDataType abstractDataType : array.getArray().getAllDataTypes()) {
                Structure structure = abstractDataType.getStructure();
                for (int index = 0; index < structure.nrOfDataTypes(); index++) {
                    AbstractDataType dataType = structure.getDataType(index);

                    if (dataType.isOctetString()) {
                        Calendar cal = dataType.getOctetString().getDateTime(deviceTimeZone).getValue();
                        sb.append(cal.getTime().toString()).append(SEPARATOR);
                    } else if (dataType.isBooleanObject()) {
                        sb.append(dataType.getBooleanObject().getState() ? "Connected" : "Disconnected").append(SEPARATOR);
                    } else if (dataType.isTypeEnum()) {
                        sb.append(dataType.getTypeEnum().getValue()).append(SEPARATOR);
                    }
                }
                sb.append(NEWLINE);
            }
        }
        return sb.toString();
    }
}