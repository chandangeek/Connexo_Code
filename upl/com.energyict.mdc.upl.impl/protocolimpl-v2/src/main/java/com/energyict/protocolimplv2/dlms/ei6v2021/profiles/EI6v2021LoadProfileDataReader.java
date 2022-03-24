package com.energyict.protocolimplv2.dlms.ei6v2021.profiles;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.dlms.a2.profile.A2ProfileDataReader;

import java.util.ArrayList;
import java.util.List;

public class EI6v2021LoadProfileDataReader extends A2ProfileDataReader {

    private static final ObisCode HOURLY_LOAD_PROFILE     = ObisCode.fromString("7.0.99.99.2.255");
    private static final ObisCode DAILY_LOAD_PROFILE      = ObisCode.fromString("7.0.99.99.3.255");
    private static final ObisCode MONTHLY_LOAD_PROFILE    = ObisCode.fromString("7.0.99.99.4.255");
    private static final ObisCode EI6_LOAD_PROFILE_STATUS = ObisCode.fromString("7.0.96.5.1.255");

    private static List<ObisCode> supportedLoadProfiles = new ArrayList<>();

    static {
        supportedLoadProfiles.add(HOURLY_LOAD_PROFILE);
        supportedLoadProfiles.add(DAILY_LOAD_PROFILE);
        supportedLoadProfiles.add(MONTHLY_LOAD_PROFILE);
    }

    public EI6v2021LoadProfileDataReader(A2 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                                         OfflineDevice offlineDevice, long limitMaxNrOfDays, List<ObisCode> supportedLoadProfiles) {
        super(protocol, collectedDataFactory, issueFactory, offlineDevice, limitMaxNrOfDays, supportedLoadProfiles);
    }

    public static List<ObisCode> getSupportedLoadProfiles() {
        return supportedLoadProfiles;
    }

    @Override
    protected boolean isProfileStatus(ObisCode obisCode) {
        return EI6_LOAD_PROFILE_STATUS.equalsIgnoreBChannel(obisCode);
    }

}
