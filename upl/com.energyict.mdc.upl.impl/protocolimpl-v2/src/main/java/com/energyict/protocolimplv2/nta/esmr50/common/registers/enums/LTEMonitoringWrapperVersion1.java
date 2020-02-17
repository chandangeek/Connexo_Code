package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;

/**
 * Created by Paul van Minderhout on 11/07/2019.

 LTE_quality_of_service

 Represent the quality of service for the LTE network
 LTE_qos_type ::= structure
 {
 T3402:			    long-unsigned,
 T3412:			    long-unsigned,
 T3412ext2:			double-long-unsigned,
 T3324			    long-unsigned,
 TeDRX			    double-long unsigned,
 TPTW			    long-unsigned,
 (N)RSRQ:			integer,
 (N)RSRP:			integer,
 qRxlevMin:			integer,
 qRxlevMinCE-r13:	integer,
 qRxlevMinCE1-r13	integer,

 }
 Where:
 -	T3402: timer in seconds, used on PLMN selection procedure and sent by the network to the modem. Refer to 3GPP TS 24.301 V13.4.0 (2016-01) for details;
 -	T3412: timer in seconds used to manage the periodic tracking area updating procedure and sent by the network to the modem. Refer to 3GPP TS 24.301 V13.4.0 (2016-01) for details;
 -	T3412ext2: timer in seconds (extended periodic tracking area update timer). Refer to 3GPP TS 24.301 v13.11.0 and 3GPP TS 24.008 v13.7.0 for details;
 -	T3324	timer in seconds (Power saving mode active timer).
 Refer to 3GPP TS 24.301 v13.11.0 and 3GPP TS 24.008 v13.7.0 for details.
 -	TeDRX timer in seconds (Extended Idle mode DRX cycle timer)
 Refer to 3GPP TS 24.301 v13.11.0 and 3GPP TS 24.008 v13.7.0 for details;
 Remark: the double-long unsigned value should be multiplied by 0,01 to get the real value in seconds. E.g. 512 represents 5,12 seconds.
 -	TPTW timer in seconds (Extended Idle mode DRX paging time window)
 Refer to 3GPP TS 24.301 v13.11.0 and 3GPP TS 24.008 v13.7.0 for details;
 Remark: the long-unsigned value should be multiplied by 0,01 to get the real value in seconds. For example: 896 means 8,96 seconds.
 -	(N)RSRQ: represents the signal quality as defined in 3GPP TS 36.133:
 For Cat M1 a value range from -30 up to 46 is necessary to represent RSRQ. Refer to Release 13 version of 3GPP TS 36.133 v13.11.0 (2018-04) for details.
 For Cat NB-IoT a value range from -30 up to 46 is necessary to represent NRSRQ. Refer to Release 14 version of 3GPP TS 36.133 v14.4.0 (2017-08) for details.

 By means of the ps_status attribute (attribute 5 of the GSM Status object), the link can be made whether the RSRQ or NRSRQ level is represented.

 (-30)          -34 dB
 (-29)          -33,5 dB
 (-28..-1)     -33…-19,5 dB
 (0)             -19,5 dB or less
 (1)             -19 dB
 (2..31)       -18,5…-4 dB
 (32)           -3,5 dB
 (33)           -3 dB
 (34)           -3 dB or less
 (35)           -2,5 dB
 (36)           -2 dB
 (37..45)     -1,5…2,5 dB
 (46)            2,5 dB or better
 (99)            Not known or not detectable
 Remark: Reported values of (-30..-1) and (34..46) apply for UE that support extended RSRQ range (Coverage Enhancement Levels)
 -	(N)RSRP: represents the signal level as defined in 3GPP TS 36.133:

 For Cat M1 a value range from -17 up to 97 is necessary to represent RSRP. Refer to Release 13 version of 3GPP TS 36.133 v13.11.0 (2018-04) for details

 (-17)          -156 dBm
 (-16)          -155 dBm
 (-15..-1)     -154…-140 dBm
 (0)             -140 dBm or less.
 (1)             -139 dBm
 (2)             -138 dBm
 (3..96)       -137…-44 dBm
 (97)           -44 or better
 (127)         Not known or not detectable
 Remark: depending on the use of Coverage Enhancement modes, a reported value of (0) will be used or not (for devices that do not use CE modes, values (-17..-1) will not be used. A Reported value of (0) means in that case a RSRP < -140 dBm).

 For NB-IoT a value range from 0 to 113 is necessary to represent NRSRP value. Refer to Release 14 version of 3GPP TS 36.133 v14.4.0 (2017-08) for details.

 (0)           -156 dBm
 (1)           -155 dBm
 (2)           -154 dBm
 (3..112)   -153… --44  dBm
 (113)       -44 dBm or better
 (127)       Not known or not detectable

 By means of the ps_status attribute (attribute 5 of the GSM Status object), the link can be made whether the RSRP or NRSRP level is represented.
 Remark: for the NRSRP value (NB-IoT) the mapping is different from the mapping for the RSRP value (Cat M1) !
 -	qRxlevMin: specifies the minimum required Rx level in the cell in dBm as defined in 3GPP TS 36.304 V13.8.0 (2018-01).
 -	qRxlevMinCE-r13: specifies the minimum required Rx level in enhanced coverage CE Mode A, Cat M1. For CE Mode A Cat M1 we need a value from -70…-22 in steps of 1.
 Remark: This field is not used in case of Cat NB-IoT.
 -	qRxlevMinCE1-r13: specifies the minimum required Rx level in enhanced coverage CE Mode B, Cat M1. For CE Mode B Cat M1 we need a value from -78…-22 in steps of 1.
 Remark: This field is not used in case of Cat NB-IoT.


 */
public class LTEMonitoringWrapperVersion1 extends LTEMonitoringWrapper{

    private long t3412ext2;
    private long t3324;
    private long teDrx;
    private long tptw;
    private int qRxlevMinCEr13;
    private int qRxlevMinCE1r13;

    public LTEMonitoringWrapperVersion1(AbstractDataType abstractDataType) {
        decoded = false;
        if (abstractDataType.isStructure()){
            try {
                Structure structure = abstractDataType.getStructure();
                t3402 = structure.getNextDataType().longValue();
                t3412 = structure.getNextDataType().longValue();
                t3412ext2 = structure.getNextDataType().longValue();
                t3324 = structure.getNextDataType().longValue();
                teDrx = structure.getNextDataType().longValue();
                tptw = structure.getNextDataType().longValue();
                rsrq = structure.getNextDataType().intValue();
                rsrp = structure.getNextDataType().intValue();
                qRxlevMin = structure.getNextDataType().intValue();
                qRxlevMinCEr13 = structure.getNextDataType().intValue();
                qRxlevMinCE1r13 = structure.getNextDataType().intValue();
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
        return "T3402:" + t3402 + SEP +
                "T3412:" + t3412 + SEP +
                "T3412ext2:" + t3412ext2 + SEP +
                "T3324:" + t3324 + SEP +
                "TeDRX:" + teDrx + SEP +
                "TPTW:" + tptw + SEP +
                "(N)RSRQ:" + rsrq + SEP +
                "(N)RSRP:" + rsrp + SEP +
                "qRxlevMin:" + qRxlevMin + SEP +
                "qRxlevMinCE-r13:" + qRxlevMinCEr13 + SEP +
                "qRxlevMinCE1-r13:" + qRxlevMinCE1r13 + SEP;
    }

    public long getT3412ext2() {
        return t3412ext2;
    }

    public long getT3324() {
        return t3324;
    }

    public long getTeDrx() { return teDrx; }

    public long getTptw() { return tptw; }

    public int getqRxlevMinCEr13() {
        return qRxlevMinCEr13;
    }

    public int getqRxlevMinCE1r13() {
        return qRxlevMinCE1r13;
    }
}
