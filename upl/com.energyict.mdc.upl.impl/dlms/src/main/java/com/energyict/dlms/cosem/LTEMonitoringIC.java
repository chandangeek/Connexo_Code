package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.attributes.LTEMonitoringAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * class id = 151, version = 0, logical name = 0-0:25.11.0.255 (0000190B00FF)
 * Instances of the “LTE monitoring” IC allow monitoring LTE modems by handling all data necessary data for this purpose.
 * Created by H165680 on 17/04/2017.
 */
public class LTEMonitoringIC extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.25.11.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public LTEMonitoringIC(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.LTE_MONITORING.getClassId();
    }

    /**
     Represents the quality of service for the LTE network
     LTE_qos_type ::= structure
     {
     T3402: long-unsigned,
     T3412: long-unsigned,
     RSRQ: unsigned,
     RSRP: unsigned,
     qRxlevMin: integer
     }
     Where:
     • T3402: timer in seconds, used on PLMN selection procedure and sent by the network
     to the modem. Refer to 3GPP TS 24.301 V13.4.0 (2016-01) for details
     • T3412: timer in seconds used to manage the periodic tracking area updating procedure
     and sent by the network to the modem. Refer to 3GPP TS 24.301 V13.4.0 (2016-01)
     for details
     • RSRQ: represents the signal quality as defined in 3GPP TS 24.301 V13.4.0 (2016-01):
         (0) -19,5dB,
         (1) -19 dB,
         (2...31) -18,5...-3,5 dB,
         (32) -3dB,
         (99) Not known or not detectable
     • RSRP: represents the signal level as defined in 3GPP TS 24.301 V13.4.0 (2016-01):
         (0) -140dBm,
         (1) -139 dBm,
         (2...94) -138...-45 dBm,
         (95) -44dBm,
         (99) Not known or not detectable
     • qRxlevMin: specifies the minimum required Rx level in the cell in dBm as defined in
     3GPP TS 24.301 V13.4.0 (2016-01).
     *
     * @return
     * @throws IOException
     */
    public Structure readLTEQoS() throws IOException {
        return readDataType(LTEMonitoringAttributes.LTE_QOS, Structure.class);
    }

}
