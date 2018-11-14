package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.registers.enums;

import com.energyict.dlms.axrdencoding.Structure;

import java.util.Date;

/**
 * Created by iulian on 8/18/2016.
 */

/**
 * cell_info
 Represents the cell information:
 cell_info_type ::= structure
 {
     cell_ID            : long-unsigned,
     location_ID        : long-unsigned,
     signal_quality     : unsigned,
     ber                : unsigned,
     mcc                : long-unsigned,
     mnc                : long-unsigned,
     channel_number     double-long-unsigned
        last reject cause  unsigned
        last rejected mcc  long-unsigned
        last rejected mnc  long-unsigned
         timestamp or last rejection TBD
 }
 Where:
 - cell_ID: Two-byte cell ID in hexadecimal format;
 - location_ID: Two-byte location area code (LAC) or Tracking Area Code (TAC) for LTE network in hexadecimal format (e.g. "00C3" equals 195 in decimal);
 - signal_quality: Represents the signal quality:
 (0) –113 dBm or less,
 (1) –111 dBm,
 (2...30) –109…-53 dBm,
 (31) –51 or greater,
 (99) not known or not detectable;
 - ber: Bit error (BER) measurement in percent:
 (0…7) as RXQUAL_n values specified in Fout! Verwijzingsbron niet gevonden. 8.2.4.
 (99) not known or not detectable.
 - mcc: Mobile Country Code of the serving network, as defined in ITU-T E.212
 - mnc: Mobile Network Code of the serving network, as defined in ITU-T E.212
 - channel_number: Represents the Absolute radio-frequency channel number (ARFCN or eaRFCN for LTE network)
 - reject cause:
 */
@Deprecated
public class LTEDiagnosticCellInfo {
    private static final String SEP = ";\n";
    private long cellId;
    private long locationId;
    private int signalQuality;
    private int ber;
    private long    mcc;
    private long    mnc;
    private long  channelNumber;
    private int lastRejectCause;
    private long lastRejectedMCC;
    private long lastRejectedMNC;
    private Date lastRejection;

    private boolean decoded;
    private String errorMessage;

    public LTEDiagnosticCellInfo(Structure structure) {
        try {
            cellId = structure.getNextDataType().longValue();
            locationId = structure.getNextDataType().longValue();
            signalQuality = structure.getNextDataType().intValue();
            ber = structure.getNextDataType().intValue();
            mcc = structure.getNextDataType().longValue();
            mnc = structure.getNextDataType().longValue();
            channelNumber = structure.getNextDataType().longValue();

            //TODO - implement next steps after Segem finished

            decoded = true;
        } catch(Exception ex){
            decoded = false;
            errorMessage = "Could not decode structure: "+ex.getMessage();
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("cellId=").append(getCellId()).append(SEP);
        sb.append("locationId=").append(getLocationId()).append(SEP);
        sb.append("signalQuality=").append(getSignalQuality()).append(SEP);
        sb.append("ber=").append(getBer()).append(SEP);
        sb.append("mcc=").append(getMcc()).append(SEP);
        sb.append("mnc=").append(getMnc()).append(SEP);
        sb.append("channelNr=").append(getChannelNumber());

        return sb.toString();
    }

    public long getCellId() {
        return cellId;
    }

    public long getLocationId() {
        return locationId;
    }

    /**
     (0) –113 dBm or less,
     (1) –111 dBm,
     (2...30) –109…-53 dBm,
     (31) –51 or greater,
     (99) not known or not detectable;

     * @return
     */
    public int getSignalQuality() {
        return signalQuality;
    }

    public int getBer() {
        return ber;
    }

    public long getMcc() {
        return mcc;
    }

    public long getMnc() {
        return mnc;
    }

    public long getChannelNumber() {
        return channelNumber;
    }

    @Deprecated
    public int getLastRejectCause() {
        return lastRejectCause;
    }

    @Deprecated
    public long getLastRejectedMCC() {
        return lastRejectedMCC;
    }

    @Deprecated
    public long getLastRejectedMNC() {
        return lastRejectedMNC;
    }

    @Deprecated
    public Date getLastRejection() {
        return lastRejection;
    }

    public boolean isDecoded() {
        return decoded;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
