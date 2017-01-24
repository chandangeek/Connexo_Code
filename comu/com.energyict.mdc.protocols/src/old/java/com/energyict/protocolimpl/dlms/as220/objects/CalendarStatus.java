package com.energyict.protocolimpl.dlms.as220.objects;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 14-jan-2011
 * Time: 11:23:55
 */
public class CalendarStatus extends Data {

    public static final int SUCCESS = 0;
    public static final int READ_ERROR = 1;
    public static final int WRITE_ERROR = 2;
    public static final int IN_PROGRESS = 3;

    private int status = -1;

    public CalendarStatus(CosemObjectFactory cof, ObisCode obis) throws IOException {
        super(cof.getProtocolLink(), cof.getObjectReference(obis));
    }

    public String getCalendarStatus() throws IOException {
        int currentStatus = getStatus();
        switch (currentStatus) {
            case SUCCESS:
                return "[0] Success";
            case READ_ERROR:
                return "[1] Read error";
            case WRITE_ERROR:
                return "[2] Write error";
            case IN_PROGRESS:
                return "[3] In progress (R/W)";
            default:
                return "[" + status + "] Invalid";
        }
    }

    private int getStatus() throws IOException {
        if (status == -1) {
            byte[] rawData = getRawValueAttr();
            AbstractDataType dataType = AXDRDecoder.decode(rawData);
            if (dataType != null) {
                this.status = dataType.intValue();
            } else {
                throw new IOException("Unknown status (invalid axdr data type): [" + ProtocolTools.getHexStringFromBytes(rawData) + "]");
            }
        }
        return status;
    }

    public boolean isInProgress() throws IOException {
        return IN_PROGRESS == getStatus();
    }

    public boolean isReadError() throws IOException {
        return READ_ERROR == getStatus();
    }

    public boolean isSuccess() throws IOException {
        return SUCCESS == getStatus();
    }

    public boolean isWriteError() throws IOException {
        return WRITE_ERROR == getStatus();
    }

}
