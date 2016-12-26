package com.energyict.protocolimplv2.dlms.g3.profile;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.dlms.g3.G3ProfileType;
import com.energyict.protocolimpl.dlms.g3.profile.G3Profile;
import com.energyict.protocolimplv2.dlms.g3.cache.G3Cache;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2015 - 9:25
 */
public class ProfileDataFactory {

    private static final ObisCode IMPORT_ACTIVE_POWER_PROFILE = ObisCode.fromString("1.1.99.1.0.255");
    private static final ObisCode EXPORT_ACTIVE_POWER_PROFILE = ObisCode.fromString("1.2.99.1.0.255");
    private static final ObisCode DAILY_PROFILE = ObisCode.fromString("1.0.98.1.2.255");
    private static final ObisCode MONTHLY_PROFILE = ObisCode.fromString("1.0.98.1.1.255");

    private final List<ObisCode> supportLoadProfiles;
    private final DlmsSession dlmsSession;
    private final G3Cache g3Cache;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public ProfileDataFactory(DlmsSession dlmsSession, G3Cache g3Cache, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.dlmsSession = dlmsSession;
        this.g3Cache = g3Cache;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        supportLoadProfiles = new ArrayList<>();
        supportLoadProfiles.add(IMPORT_ACTIVE_POWER_PROFILE);
        supportLoadProfiles.add(EXPORT_ACTIVE_POWER_PROFILE);
        supportLoadProfiles.add(DAILY_PROFILE);
        supportLoadProfiles.add(MONTHLY_PROFILE);
    }

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            CollectedLoadProfileConfiguration collectedLoadProfileConfiguration = this.collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
            boolean supported = isSupported(loadProfileReader);
            collectedLoadProfileConfiguration.setSupportedByMeter(supported);
            if (supported) {
                try {
                    G3Profile g3Profile = getG3Profile(loadProfileReader);
                    g3Profile.readChannelInfosFromDevice();
                    collectedLoadProfileConfiguration.setChannelInfos(g3Profile.getChannelInfos());
                    collectedLoadProfileConfiguration.setProfileInterval(g3Profile.getProfileInterval());
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, dlmsSession.getProperties().getRetries() + 1)) {
                        dlmsSession.getLogger().warning(e.toString());
                        collectedLoadProfileConfiguration.setSupportedByMeter(false);
                    }
                }
            }
            result.add(collectedLoadProfileConfiguration);
        }
        return result;
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> result = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfiles) {
            CollectedLoadProfile collectedLoadProfile = this.collectedDataFactory.createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode()));
            if (isSupported(loadProfileReader)) {
                try {
                    ProfileData profileData = getG3Profile(loadProfileReader).getProfileData(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime());
                    collectedLoadProfile.setCollectedIntervalData(profileData.getIntervalDatas(), profileData.getChannelInfos());
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, dlmsSession.getProperties().getRetries() + 1)) {
                        Issue problem = this.issueFactory.createProblem(loadProfileReader, "loadProfileXIssue", loadProfileReader.getProfileObisCode().toString(), e.getMessage());
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue problem = this.issueFactory.createWarning(loadProfileReader, "loadProfileXnotsupported", loadProfileReader.getProfileObisCode().toString());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            result.add(collectedLoadProfile);
        }
        return result;
    }

    private G3Profile getG3Profile(LoadProfileReader loadProfileReader) {
        G3ProfileType g3ProfileType = G3ProfileType.fromObisCode(loadProfileReader.getProfileObisCode());
        return new G3Profile(g3ProfileType, g3Cache, dlmsSession.getLogger(), dlmsSession.getTimeZone(), dlmsSession.getCosemObjectFactory(), loadProfileReader.getMeterSerialNumber());
    }

    private boolean isSupported(LoadProfileReader loadProfileReader) {
        for (ObisCode supportLoadProfile : supportLoadProfiles) {
            if (loadProfileReader.getProfileObisCode().equals(supportLoadProfile)) {
                return true;
            }
        }
        return false;
    }
}