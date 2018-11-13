package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.registers.enums;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;

/**
 * Created by iulian on 8/18/2016.
 */

/**
 * LTE_quality_of_service
 Represent the quality of service for the LTE network
 LTE_qos_type ::= structure
 {
 T3402:			long-unsigned,
 T3412:			long-unsigned,
 RSRQ:			unsigned,
 RSRP:			unsigned,
 qRxlevMin:		integer
 }

 Where:
 -	T3402: timer in seconds, used on PLMN selection procedure and is sent by the network to the modem. Refer to 3GPP TS 24.301 for more details.
 -	T3412: timer in seconds used to manage the periodic tracking area updating procedure and is sent by the network to the modem. Refer to 3GPP TS 24.301 for more details.
 -	RSRQ: represents the signal quality as defined in 3GPP TS 36.214 :
 (0)		–19,5dB,
 (1)		–19 dB,
 (2...31)	 	–18,5…-3,5 dB,
 (32)		–3dB,
 (99)		Not known or not detectable.
 -	RSRP: represents the signal level as defined in 3GPP TS 36.214 :
 (0)		–140dBm,
 (1)		–139 dBm,
 (2... 94)	–138…-45 dBm,
 (95)		–44dBm,
 (99)		Not known or not detectable.
 -	qRxlevMin: specifies the minimum required Rx level in the cell in dBm as defined in 3GPP TS 36.304 .

 */
@Deprecated
public class LTEMonitoringWrapper {

    private static final String SEP = ";\n";
    private boolean decoded = false;
    private String errorMessage;
    private long t3402;
    private long t3412;
    private int rsrq;
    private int rsrp;
    private int qRxlevMin;

    public LTEMonitoringWrapper(AbstractDataType abstractDataType) {
        decoded = false;
        if (abstractDataType.isStructure()){
            try {
                Structure structure = abstractDataType.getStructure();
                t3402 = structure.getNextDataType().longValue();
                t3412 = structure.getNextDataType().longValue();
                rsrq = structure.getNextDataType().intValue();
                rsrp = structure.getNextDataType().intValue();
                qRxlevMin = structure.getNextDataType().intValue();
                decoded = true;
            } catch (Exception ex){
                errorMessage = ex.getMessage();
            }
        }
    }

    public boolean isDecoded() {
        return decoded;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("T3402:").append(t3402).append(SEP);
        sb.append("T3412:").append(t3412).append(SEP);
        sb.append("RSRQ:").append(rsrq).append(SEP);
        sb.append("RSRP:").append(rsrp).append(SEP);
        sb.append("qRxlevMin:").append(qRxlevMin).append(SEP);
        return sb.toString();
    }

    public long getT3402() {
        return t3402;
    }

    public long getT3412() {
        return t3412;
    }

    public int getRsrq() {
        return rsrq;
    }

    public int getRsrp() {
        return rsrp;
    }

    public int getqRxlevMin() {
        return qRxlevMin;
    }
}
