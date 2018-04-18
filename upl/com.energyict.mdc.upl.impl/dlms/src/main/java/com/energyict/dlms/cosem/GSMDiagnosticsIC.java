package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.cosem.attributes.GSMDiagnosticsAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * class id = 47, version = 1, logical name = 0-0:25.6.0.255 (0000190600FF)
 * The cellular network is undergoing constant changes in terms of registration status, signal quality etc. It is necessary to monitor and log the relevant parameters in order to obtain
 * diagnostic information that allows identifying communication problems in the network.
 * An instance of the “GSM diagnostic” class stores parameters of the GSM/GPRS, UMTS, CDMA or LTE network necessary for analysing the operation of the network.
 * A GSM diagnostic “Profile generic” object is also available to capture the attributes of the GSM diagnostic object.
 * Created by H165680 on 17/04/2017.
 */
public class GSMDiagnosticsIC extends AbstractCosemObject {


    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.25.6.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public GSMDiagnosticsIC(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.GSM_DIAGNOSTICS.getClassId();
    }

    /**
     * Holds the name of the network operator e.g. “Proximus”.
     *
     * @return
     * @throws IOException
     */
    public VisibleString readOperator() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.OPERATOR, VisibleString.class);
    }

    /**
     * Indicates the registration status of the modem.
     * status_enum ::= enum:
     * (0) not registered,
     * (1) registered, home network,
     * (2) not registered, but MT is currently searching a new operator to register to,
     * (3) registration denied,
     * (4) unknown,
     * (5) registered, roaming,
     * (6) ... (255) reserved
     *
     * @return
     * @throws IOException
     */
    public TypeEnum readStatus() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.STATUS, TypeEnum.class);
    }

    /**
     * The ps_status value field indicates the packet switched status of the modem.
     * ps_status_enum ::= enum:
     * (0) inactive,
     * (1) GPRS,
     * (2) EDGE,
     * (3) UMTS,
     * (4) HSDPA,
     * (5) LTE,
     * (6) CDMA,
     * (7) ... (255) reserved
     *
     * @return
     * @throws IOException
     */
    public TypeEnum readPSStatus() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.PS_STATUS, TypeEnum.class);
    }

    /**
     * Holds the International Mobile Equipment Identifier for the wireless modem present in the
     * device. Empty string in case no modem is present
     *
     * @return
     * @throws IOException
     */
    public OctetString readIMEI() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.IMEI, OctetString.class);
    }

    /**
     * Holds the International Mobile Subscriber Identifier for the SIM card present in the device.
     * Empty string in case no SIM is present.
     *
     * @return
     * @throws IOException
     */
    public OctetString readIMSI() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.IMSI, OctetString.class);
    }

    /**
     * Holds the SIM card identifier for the SIM present in the device.
     * Empty in case no SIM is present.
     *
     * @return
     * @throws IOException
     */
    public OctetString readSimCardId() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.SIM_CARD_ID, OctetString.class);
    }

}