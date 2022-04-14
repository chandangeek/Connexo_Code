package com.energyict.protocolimplv2.dlms.ei6v2021.profiles;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.offline.OfflineDevice;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.dlms.a2.profile.A2ProfileDataReader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EI6v2021LoadProfileDataReader extends A2ProfileDataReader {

    private static final ObisCode HOURLY_LOAD_PROFILE_OBISCODE     = ObisCode.fromString("7.0.99.99.2.255");
    private static final ObisCode DAILY_LOAD_PROFILE_OBISCODE      = ObisCode.fromString("7.0.99.99.3.255");
    private static final ObisCode MONTHLY_LOAD_PROFILE_OBISCODE    = ObisCode.fromString("7.0.99.99.4.255");

    private static final ObisCode EI6_LOAD_PROFILE_STATUS = ObisCode.fromString("7.0.96.5.1.255");

    private static List<ObisCode> supportedLoadProfiles = new ArrayList<>();
    private final CollectedDataFactory collectedDataFactory;
    private final OfflineDevice offlineDevice;

    private Map<ObisCode, List<ChannelInfo>> configurationMap = new HashMap<>();
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;
    private Map<ObisCode, Integer> intervalMap = new HashMap<>();

    static {
        supportedLoadProfiles.add(HOURLY_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(DAILY_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(MONTHLY_LOAD_PROFILE_OBISCODE);
    }

    public EI6v2021LoadProfileDataReader(A2 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                                         OfflineDevice offlineDevice, long limitMaxNrOfDays, List<ObisCode> supportedLoadProfiles) {
        super(protocol, collectedDataFactory, issueFactory, offlineDevice, limitMaxNrOfDays, supportedLoadProfiles);
        this.collectedDataFactory = collectedDataFactory;
        this.offlineDevice = offlineDevice;

        // COMMUNICATION-3953 Hardcode Load Profile channels in EI6 2021 and EI7 protocols to gain connection time
        intervalMap.put(DAILY_LOAD_PROFILE_OBISCODE, 86400);
        intervalMap.put(HOURLY_LOAD_PROFILE_OBISCODE, 3600);
        intervalMap.put(MONTHLY_LOAD_PROFILE_OBISCODE, 2678400);

        configurationMap.put(DAILY_LOAD_PROFILE_OBISCODE, createChannelInfo());
        configurationMap.put(HOURLY_LOAD_PROFILE_OBISCODE, createChannelInfo());
        configurationMap.put(MONTHLY_LOAD_PROFILE_OBISCODE, createMonthlyChannelInfo());
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();
        protocol.journal("Using cached load profile configuration");
        for (LoadProfileReader lpr : loadProfileReaders) {
            CollectedLoadProfileConfiguration lpc = collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), offlineDevice.getDeviceIdentifier(), lpr.getMeterSerialNumber());
            if (isSupported(lpr)) {
                hasStatus.put(getCorrectedLoadProfileObisCode(lpr), true);
                List<ChannelInfo> channelInfos = getChannelInfo(lpr.getProfileObisCode());
                getChannelInfosMap().put(lpr, channelInfos);    //Remember these, they are re-used in method #getLoadProfileData();
                lpc.setChannelInfos(channelInfos);
                lpc.setSupportedByMeter(true);
                lpc.setProfileInterval(intervalMap.get(lpr.getProfileObisCode()));
            } else {
                lpc.setSupportedByMeter(false);
            }
            result.add(lpc);
        }
        return result;
    }

    public static List<ObisCode> getSupportedLoadProfiles() {
        return supportedLoadProfiles;
    }

    @Override
    protected boolean isProfileStatus(ObisCode obisCode) {
        return EI6_LOAD_PROFILE_STATUS.equalsIgnoreBChannel(obisCode);
    }

    @Override
    protected Map<LoadProfileReader, List<ChannelInfo>> getChannelInfosMap() {
        if (channelInfosMap == null) {
            channelInfosMap = new HashMap<>();
        }
        return channelInfosMap;
    }

    private List<ChannelInfo> getChannelInfo(ObisCode obisCode) {
        return configurationMap.get(obisCode);
    }

    private List<ChannelInfo> createChannelInfo() {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        ChannelInfo channelInfo0 = new ChannelInfo(0, "7.0.13.2.0.255", Unit.get(BaseUnit.NORMALCUBICMETER, -3), offlineDevice.getSerialNumber(), false);
        channelInfo0.setMultiplier(new BigDecimal(1));
        channelInfos.add(channelInfo0);
        ChannelInfo channelInfo1 = new ChannelInfo(1, "7.0.12.2.0.255", Unit.get(BaseUnit.NORMALCUBICMETER, -3), offlineDevice.getSerialNumber(), false);
        channelInfo1.setMultiplier(new BigDecimal(1));
        channelInfos.add(channelInfo1);
        return channelInfos;
    }


    private List<ChannelInfo> createMonthlyChannelInfo() {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        ChannelInfo channelInfo0 = new ChannelInfo(0, "7.0.13.2.0.255", Unit.get(BaseUnit.NORMALCUBICMETER, -3), offlineDevice.getSerialNumber(), false);
        channelInfo0.setMultiplier(new BigDecimal(1));
        channelInfos.add(channelInfo0);

        ChannelInfo channelInfo1 = new ChannelInfo(1, "7.0.13.2.1.255", Unit.get(BaseUnit.NORMALCUBICMETER, -3), offlineDevice.getSerialNumber(), false);
        channelInfo1.setMultiplier(new BigDecimal(1));
        channelInfos.add(channelInfo1);

        ChannelInfo channelInfo2 = new ChannelInfo(2, "7.0.13.2.2.255", Unit.get(BaseUnit.NORMALCUBICMETER, -3), offlineDevice.getSerialNumber(), false);
        channelInfo2.setMultiplier(new BigDecimal(1));
        channelInfos.add(channelInfo2);

        ChannelInfo channelInfo3 = new ChannelInfo(3, "7.0.13.2.3.255", Unit.get(BaseUnit.NORMALCUBICMETER, -3), offlineDevice.getSerialNumber(), false);
        channelInfo3.setMultiplier(new BigDecimal(1));
        channelInfos.add(channelInfo3);

        ChannelInfo channelInfo4 = new ChannelInfo(4, "7.0.12.2.0.255", Unit.get(BaseUnit.NORMALCUBICMETER, -3), offlineDevice.getSerialNumber(), false);
        channelInfo4.setMultiplier(new BigDecimal(1));
        channelInfos.add(channelInfo4);

        ChannelInfo channelInfo5 = new ChannelInfo(5, "7.0.43.45.0.255", Unit.get(BaseUnit.CUBICMETERPERHOUR, -3), offlineDevice.getSerialNumber(), false);
        channelInfo5.setMultiplier(new BigDecimal(1));
        channelInfos.add(channelInfo5);

        ChannelInfo channelInfo6 = new ChannelInfo(6, "7.0.43.45.5.255", Unit.get(BaseUnit.SECOND, -3), offlineDevice.getSerialNumber(), false);
        channelInfo6.setMultiplier(new BigDecimal(1));
        channelInfos.add(channelInfo6);
        return channelInfos;
    }
}
