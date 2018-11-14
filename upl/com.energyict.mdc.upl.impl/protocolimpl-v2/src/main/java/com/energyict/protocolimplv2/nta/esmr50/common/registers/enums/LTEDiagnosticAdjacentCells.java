package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;

import java.util.Iterator;

/**
 * Created by iulian on 8/18/2016.
 */
public class LTEDiagnosticAdjacentCells {
    private long cellId;
    private long signalQuality;
    private String errorMessage;
    private boolean decoded;

    String description = "";
    public LTEDiagnosticAdjacentCells(Array cells) {
        StringBuilder sb = new StringBuilder();

        try {
            Iterator<AbstractDataType> it = cells.iterator();
            while (it.hasNext()) {
                AbstractDataType item = it.next();
                if (item.isStructure()) {
                    Structure cell = item.getStructure();
                    cellId = cell.getNextDataType().longValue();
                    signalQuality = cell.getNextDataType().longValue();
                    sb.append("{").append(cellId).append(":").append(signalQuality).append("}").append("\n");
                } else {
                    sb.append(item.intValue()).append(";");
                }
            }
            description = sb.toString();
            decoded = true;
        } catch (Exception ex){
            decoded = false;
            errorMessage = ex.getMessage();
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isDecoded() {
        return decoded;
    }

    public String toString() {
        return description;
    }

    public long getSignalQuality() {
        return signalQuality;
    }

    public long getCellId() {
        return cellId;
    }
}
