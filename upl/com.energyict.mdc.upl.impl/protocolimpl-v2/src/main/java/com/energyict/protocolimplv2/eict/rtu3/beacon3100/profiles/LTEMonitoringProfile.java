package com.energyict.protocolimplv2.eict.rtu3.beacon3100.profiles;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.Clock;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LTEMonitoringProfile extends AbstractHardcodedProfileParser {

    public static final ObisCode LTE_MONITORING_QUALITY_OF_SERVICE_RSRQ = ObisCode.fromString("0.0.25.11.3.255");
    public static final ObisCode LTE_MONITORING_QUALITY_OF_SERVICE_RSRP = ObisCode.fromString("0.0.25.11.4.255");

    public LTEMonitoringProfile(byte[] encodedData) throws IOException {
        super(encodedData);
    }


    /**
     * Hardcoded channels for LTE Monitoring
     * clock                                  = {8, 0-0:1.0.0.255, 2, 0 }
     * lte_monitoring_quality_of_service_rsrq = {151, 0-0:25.11.0.255, 2, 3 }
     * lte_monitoring_quality_of_service_rsrp = {151, 0-0:25.11.0.255, 2, 4 }
     *
     * @param lpc
     */
    public static List<ChannelInfo> getHardcodedChannelInfos(CollectedLoadProfileConfiguration lpc) {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();

        int ch = 0;

        channelInfos.add(new ChannelInfo(ch++, LTE_MONITORING_QUALITY_OF_SERVICE_RSRQ.toString(), Unit.get("dB"), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, LTE_MONITORING_QUALITY_OF_SERVICE_RSRP.toString(), Unit.get("dBm"), lpc.getMeterSerialNumber(), false));
        return channelInfos;
    }

    /**
     * Convert the DLMS ranged values to real modem values.
     * <p>
     * See how Beacon prepares those values in
     * com.energyict.concentrator.wwan.common.dlms.LTEMonitoringIC.LTEQualityOfService#toRsrqValue
     * com.energyict.concentrator.wwan.common.dlms.LTEMonitoringIC.LTEQualityOfService#toRsrpValue
     *
     * @param channelId channel ID (0 = date/time)
     * @param rawValue  original value (read from Beacon)
     * @return
     */
    @Override
    protected Number postProcess(int channelId, Number rawValue) {
        long rangedValue = rawValue.longValue();

        switch (channelId) {
            case 1: // RSRQ
                if (rangedValue == 0) {
                    return -19.5;
                }
                if (rangedValue == 1) {
                    return -19;
                }
                if (rangedValue == 3) {
                    return -3;
                }
                return (rangedValue - 40) / 2;

            case 2: // RSRP
                if (rangedValue == 0) {
                    return -140;
                }
                if (rangedValue == 1) {
                    return -139;
                }
                if (rangedValue == 95) {
                    return -44;
                }
                if (rangedValue == 99) {
                    return 0; // not available
                }
                return (rangedValue - 141);
        }

        return super.postProcess(channelId, rawValue);
    }


}