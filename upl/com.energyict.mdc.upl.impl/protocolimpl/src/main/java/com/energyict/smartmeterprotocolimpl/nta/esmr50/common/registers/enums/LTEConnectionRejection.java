package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.registers.enums;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.DateTime;

import java.util.TimeZone;

/**
 * Created by avrancea on 10/5/2016.
 */
@Deprecated
public class LTEConnectionRejection {
    private static final String SEP = ";\n";
    private int last_reject_cause;
    private long last_rejected_mcc;
    private long last_rejected_mnc;
    private DateTime timestamp_last_rejection;

    private boolean decoded;

    private String errorMessage;

    public LTEConnectionRejection(AbstractDataType abstractDataType, TimeZone timeZone) {
        decoded = false;
        if (abstractDataType.isStructure()){
            try {
                Structure structure = abstractDataType.getStructure();
                last_reject_cause = structure.getNextDataType().intValue();
                last_rejected_mcc = structure.getNextDataType().longValue();
                last_rejected_mnc = structure.getNextDataType().longValue();
                timestamp_last_rejection = structure.getNextDataType().getOctetString().getDateTime(timeZone);
                decoded = true;
            } catch (Exception ex){
                errorMessage = ex.getMessage();
            }
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getLast_reject_cause() {
        return last_reject_cause;
    }

    public long getLast_rejected_mcc() {
        return last_rejected_mcc;
    }

    public long getLast_rejected_mnc() {
        return last_rejected_mnc;
    }

    public DateTime getTimestamp_last_rejection() {
        return timestamp_last_rejection;
    }

    public boolean isDecoded() {
        return decoded;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("LAST_REJECT_CAUSE:").append(last_reject_cause).append(SEP);
        sb.append("LAST_REJECTED_MCC:").append(last_rejected_mcc).append(SEP);
        sb.append("LAST_REJECTED_MNC:").append(last_rejected_mnc).append(SEP);
        sb.append("TIMESTAMP_LAST_REJECTION:").append(timestamp_last_rejection).append(SEP);
        return sb.toString();
    }
}
