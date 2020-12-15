package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
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
     * Indicates the current circuit switched status.
     * cs_attachment_enum ::= enum:
     * (0) inactive,
     * (1) incoming call,
     * (2) active,
     * (3) ... (255) reserved
     *
     * @return
     * @throws IOException
     */
    public TypeEnum readCSAttachment() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.CS_ATTACHMENT, TypeEnum.class);
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
     * Represents the cell information:
     * cell_info_type ::= structure
     * {
     * cell_ID: double-long-unsigned,
     * location_ID: long-unsigned,
     * signal_quality: unsigned,
     * ber: unsigned,
     * mcc: long-unsigned,
     * mnc: long-unsigned,
     * channel_number: double-long-unsigned
     * }
     * Where:
     * cell_ID Four-byte cell ID in hexadecimal format;
     * location_ID Two-byte location area code (LAC) in the case of GSM networks or Tracking
     * Area Code (TAC) in the case of UMTS, CDMA or LTE networks in hexadecimal format
     * (e.g. “00C3” equals 195 in decimal)
     * signal_quality Represents the signal quality:
     * (0) -113 dBm or less,
     * (1) -111 dBm,
     * (2...30) -109...-53 dBm,
     * (31) -51 or greater,
     * (99) not known or not detectable
     * For GSM (2G) networks, this value indicates the average received signal level, taken on
     * a channel within the reporting period of length one SACCH multi frame. For UMTS, the
     * value maps the CPICH RSCP.
     * ber Bit Error Rate (BER) measurement in percent:
     * enum:
     * (0...7) as RXQUAL_n values specified in ETSI GSM 05.08, 8.2.4
     * (99) not known or not detectable.
     * mcc Mobile Country Code of the serving network, as defined in ITU-T E.212 (05.2008)
     * mnc Mobile Network Code of the serving network, as defined in ITU-T E.212 (05.2008)
     * channel_number Represents the absolute radio-frequency channel number (ARFCN or eaRFCN
     * for LTE network).
     *
     * @return
     * @throws IOException
     */
    public Structure readCellInfo() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.CELL_INFO, Structure.class);
    }

    /**
     * Contains an array of adjacent_cell_info.
     * adjacent_cell_info ::= structure
     * {
     * cell_ID: double-long-unsigned,
     * signal_quality: unsigned
     * }
     * cell_ID Four-byte cell ID in hexadecimal format
     * signal_quality Represents the signal quality:
     * (0) -113 dBm or less,
     * (1) -111 dBm,
     * (2...30) -109...-53 dBm,
     * (31) -51 or greater,
     * (99) not known or not detectable.
     *
     * @return
     * @throws IOException
     */
    public Array readAdjacentCells() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.ADJACENT_CELLS, Array.class);
    }

    /**
     * Holds the date and time when the data have been last captured.
     *
     * @return
     * @throws IOException
     */
    public AXDRDateTime readCaptureTime() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.CAPTURE_TIME, AXDRDateTime.class);
    }

    /**
     * Holds the wireless modem type present in the device.
     *
     * @return
     * @throws IOException
     */
    public OctetString readModemType() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.MODEM_TYPE, OctetString.class);
    }

    /**
     * Holds the firmware identifier of the wireless modem present in the device.
     *
     * @return
     * @throws IOException
     */
    public OctetString readModemVersion() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.MODEM_VERSION, OctetString.class);
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

    /**
     * Holds the MS-ISDN number associated with the SIM. This number is usually mapped into the
     * telephone numbering plan of the provider.
     *
     * @return
     * @throws IOException
     */
    public OctetString readMSISDNNumber() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.MS_ISDN_NUMBER, OctetString.class);
    }

    public Structure readPP3NetworkStatus() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.PP3_NETWORk_STATUS, Structure.class);
    }

    /**
     * Holds the total amount of data transmitted (bytes) via the modem since the last time the SIM
     * card has been swapped.
     *
     * @return
     * @throws IOException
     */
    public Unsigned64 readTotalTXBytes() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.TOTAL_TX_BYTES, Unsigned64.class);
    }

    /**
     * Holds the total amount of data received (bytes) via the modem since the last time the SIM card
     * has been swapped.
     *
     * @return
     * @throws IOException
     */
    public Unsigned64 readTotalRXBytes() throws IOException {
        return readDataType(GSMDiagnosticsAttributes.TOTAL_RX_BYTES, Unsigned64.class);
    }

}