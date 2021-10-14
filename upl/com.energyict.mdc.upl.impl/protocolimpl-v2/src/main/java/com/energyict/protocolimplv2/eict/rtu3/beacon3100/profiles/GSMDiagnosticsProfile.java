package com.energyict.protocolimplv2.eict.rtu3.beacon3100.profiles;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.Clock;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GSMDiagnosticsProfile extends AbstractHardcodedProfileParser {
    /*
    Indicates the registration status of the mode
            (0) not registered,
            (1) registered, home network,
            (2) not registered, but MT is currently searching a new operator to register to,
            (3) registration denied,
            (4) unknown,
            (5) registered, roaming,
            (6) ... (255) reserved
                 */
    public static final ObisCode GSM_MONITORING_PROFILE_1_STATUS = ObisCode.fromString("0.0.25.6.3.255");

    /**
     * The ps_status value field indicates the packet switched status of the modem.
             (0) inactive,
             (1) GPRS,
             (2) EDGE,
             (3) UMTS,
             (4) HSDPA,
             (5) LTE,
             (6) CDMA,
             (7) ... (255) reserved
     */
    public static final ObisCode GSM_MONITORING_PROFILE_2_PS_STATUS = ObisCode.fromString("0.0.25.6.5.255");

    /**
     * cell_ID Four-byte cell ID in hexadecimal format;
     */
    public static final ObisCode GSM_MONITORING_PROFILE_3_CELL_ID = ObisCode.fromString("0.0.25.6.61.255");

    /*
     location_ID Two-byte location area code (LAC) in the case of GSM networks or Tracking
     Area Code (TAC) in the case of UMTS, CDMA or LTE networks in hexadecimal format
     (e.g. “00C3” equals 195 in decimal)
     */
    public static final ObisCode GSM_MONITORING_PROFILE_4_LOCATION_ID = ObisCode.fromString("0.0.25.6.62.255");

    /*
    signal_quality Represents the signal quality:
     (0) -113 dBm or less,
     (1) -111 dBm,
     (2...30) -109...-53 dBm,
     (31) -51 or greater,
     (99) not known or not detectable
     For GSM (2G) networks, this value indicates the average received signal level, taken on
     a channel within the reporting period of length one SACCH multi frame. For UMTS, the
     value maps the CPICH RSCP.
    */
    public static final ObisCode GSM_MONITORING_PROFILE_5_SIGNAL_QUALITY = ObisCode.fromString("0.0.25.6.63.255");

    /*
    ber Bit Error Rate (BER) measurement in percent:
     enum:
     (0...7) as RXQUAL_n values specified in ETSI GSM 05.08, 8.2.4
     (99) not known or not detectable.
     */
    public static final ObisCode GSM_MONITORING_PROFILE_6_BER = ObisCode.fromString("0.0.25.6.64.255");

    /*
     mcc Mobile Country Code of the serving network, as defined in ITU-T E.212 (05.2008)
     */
    public static final ObisCode GSM_MONITORING_PROFILE_7_MCC = ObisCode.fromString("0.0.25.6.65.255");

    /*
    mnc Mobile Network Code of the serving network, as defined in ITU-T E.212 (05.2008)
    */
    public static final ObisCode GSM_MONITORING_PROFILE_8_MNC = ObisCode.fromString("0.0.25.6.66.255");

    /*
    channel_number Represents the absolute radio-frequency channel number (ARFCN or eaRFCN for LTE network).
    */
    public static final ObisCode GSM_MONITORING_PROFILE_9_CHANNEL_NUMBER = ObisCode.fromString("0.0.25.6.67.255");

    public GSMDiagnosticsProfile(byte[] encodedData) throws IOException {
        super(encodedData);
    }


    /*
         capture_time: date-time,
         status: enum,
         ps_status: enum,
         cell_id: double-long-unsigned,
         location_id: long-unsigned,
         signal_quality: unsigned,
         ber: unsigned,
         mcc: long-unsigned,
         mnc: long-unsigned,
         channel_number: double-long-unsigned

     */

    public static List<ChannelInfo> getHardcodedChannelInfos(CollectedLoadProfileConfiguration lpc) {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();

        int ch = 0;
        //channelInfos.add(new ChannelInfo(ch++, Clock.getDefaultObisCode().toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), true));

        channelInfos.add(new ChannelInfo(ch++, GSM_MONITORING_PROFILE_1_STATUS.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, GSM_MONITORING_PROFILE_2_PS_STATUS.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, GSM_MONITORING_PROFILE_3_CELL_ID.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, GSM_MONITORING_PROFILE_4_LOCATION_ID.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, GSM_MONITORING_PROFILE_5_SIGNAL_QUALITY.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, GSM_MONITORING_PROFILE_6_BER.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, GSM_MONITORING_PROFILE_7_MCC.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, GSM_MONITORING_PROFILE_8_MNC.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, GSM_MONITORING_PROFILE_9_CHANNEL_NUMBER.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));

        return channelInfos;
    }




}