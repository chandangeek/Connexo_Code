package com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.Clock;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.ESMR50RegisterFactory;

import java.util.ArrayList;
import java.util.List;

public class LTEMonitoringProfileVersion1 {

    /** Hardcoded profiles for LTE Monitoring

     Clock 8, 0.0.1.0.0.255, 2, 0 (like all loadprofiles)
     GSM diagnostic, operator 151, 0.1.25.11.0.255, 2, 0. This attribute consists itself of a structure of five (so this will mean 5 channels in EIServer):
     T3402: long-unsigned (timer in seconds, used on PLMN selection procedure and sent by the network to the modem)
     T3412: long-unsigned (timer in seconds used to manage the periodic tracking area updating procedure and sent by the network to the modem)
     T3412ext2: double-long-unsigned (timer in seconds (extended periodic tracking area update timer). Refer to 3GPP TS 24.301 v13.11.0 and 3GPP TS 24.008 v13.7.0 for details)
     T3324: long-unsigned (timer in seconds (Power saving mode active timer) Refer to 3GPP TS 24.301 v13.11.0 and 3GPP TS 24.008 v13.7.0 for details.)
     TeDRX: double-long unsigned (timer in seconds (Extended Idle mode DRX cycle timer)  Refer to 3GPP TS 24.301 v13.11.0 and 3GPP TS 24.008 v13.7.0 for details;)
            Remark: the double-long unsigned value should be multiplied by 0,01 to get the real value in seconds. E.g. 512 represents 5,12 seconds.
     TPTW: long-unsigned (timer in seconds (Extended Idle mode DRX paging time window) Refer to 3GPP TS 24.301 v13.11.0 and 3GPP TS 24.008 v13.7.0 for details;)
            Remark: the long-unsigned value should be multiplied by 0,01 to get the real value in seconds. For example: 896 means 8,96 seconds.
     RSRQ: integer (represents the signal quality)
     RSRP: integer (represents the signal level )
     qRxlevMin: integer (specifies the minimum required Rx level in the cell in dBm)
     qRxlevMinCE-r13: integer (specifies the minimum required Rx level in enhanced coverage CE Mode A, Cat M1. For CE Mode A Cat M1 we need a value from -70…-22 in steps of 1.)
            Remark: This field is not used in case of Cat NB-IoT.
     qRxlevMinCE1-r13: integer : (specifies the minimum required Rx level in enhanced coverage CE Mode B, Cat M1. For CE Mode B Cat M1 we need a value from -78…-22 in steps of 1.)
            Remark: This field is not used in case of Cat NB-IoT.
     GSM diagnostic, cell_info 47, 0.1.25.6.0.255, 6, 1. This attribute consists itself of a structure of seven (so this will mean 7 channels in EIServer):
     cell_ID: long-unsigned (Four-byte cell ID in hexadecimal format)
     location_ID: long-unsigned (Two-byte location area code (LAC) in the case of GSM networks or Tracking Area Code (TAC) in the case of UMTS, CDMA or LTE networks in hexadecimal format )
     signal_quality: unsigned (Represents the signal quality)
     ber: unsigned (Bit Error Rate (BER) measurement in percent)
     mcc: long-unsigned (Mobile Country Code of the serving network)
     mnc: long-unsigned (Mobile Network Code of the serving network)
     channel_number double-long-unsigned (Represents the absolute radio-frequency channel number (ARFCN or eaRFCN for LTE network))
     GSM diagnostic, adjacent_cells 47, 0.1.25.6.0.255, 7, 1. This attribute is an array and within each cell of the array is a structure of two. Because the number of entries in the array will never be more than 3, this will be 6 channels in EIServer). The structure of two:
     cell_ID: double-long-unsigned (Four-byte cell ID in hexadecimal format)
     signal_quality: unsigned (Represents the signal quality)
     LTE connection rejection 1, 0.1.94.31.7.255, 2, 0. This attribute consists itself of a structure of four (so this will mean 4 channels in EIServer):
     last_reject_cause unsigned (provides the last rejected cause on network)
     last_rejected_mcc long-unsigned (Mobile Country Code of the last rejected network)
     last_rejected_mnc long-unsigned (Mobile Network Code of the last rejected network)
     timestamp_last_rejection date_time (specifies the date and time of the rejection). This date_time can be stored in EIServer in epoch format.
     *
     * @param lpc
     */
    public static List<ChannelInfo> getLTEMonitoringChannelInfos(CollectedLoadProfileConfiguration lpc) {
        List<ChannelInfo> channelInfos = new ArrayList<>();

        int ch = 0;
        channelInfos.add(new ChannelInfo(ch++, Clock.getDefaultObisCode().toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), true));

        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3402.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3412.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3412ext2.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3324.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_TeDRX.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_TPTW.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRQ.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRP.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN_CE_r13.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN_CE1_r13.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));

        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_CELL_ID.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_LOCATION_ID.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_SIGNAL_QUALITY.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_BER.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_MCC.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_MNC.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_CHANNEL_NUMBER.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));

        //1
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        //2
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID_2.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY_2.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        //3
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID_3.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY_3.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));

        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_CONNECTION_REJECTION_LAST_REJECTED_CAUSE.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_CONNECTION_REJECTION_LAST_REJECTED_MCC.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_CONNECTION_REJECTION_LAST_REJECTED_MNC.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_CONNECTION_REJECTION_TIMESTAMP_LAST_REJECTION.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));

        return channelInfos;
    }
}
