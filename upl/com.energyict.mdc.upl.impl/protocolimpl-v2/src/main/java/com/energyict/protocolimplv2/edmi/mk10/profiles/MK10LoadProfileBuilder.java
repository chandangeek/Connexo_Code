package com.energyict.protocolimplv2.edmi.mk10.profiles;

import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandResponseException;
import com.energyict.protocolimpl.edmi.common.command.TimeInfo;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 27/02/2017 - 13:29
 */
public class MK10LoadProfileBuilder implements DeviceLoadProfileSupport {

    private final CommandLineProtocol protocol;

    /**
     * Keeps track of the LoadSurvey object for all the LoadProfiles
     */
    private Map<LoadProfileReader, LoadSurvey> loadSurveyMap;

    public MK10LoadProfileBuilder(CommandLineProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            CollectedLoadProfileConfiguration collectedLoadProfileConfiguration = MdcManager.getCollectedDataFactory()
                    .createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
            LoadProfileDescription loadProfileDescription = LoadProfileDescription.fromObisCode(loadProfileReader.getProfileObisCode());
            if (!loadProfileDescription.equals(LoadProfileDescription.UNKNOWN)) {
                try {
                    LoadSurvey loadSurvey = new LoadSurvey(getProtocol(), loadProfileDescription);
                    collectedLoadProfileConfiguration.setProfileInterval(loadSurvey.getProfileInterval());
                    collectedLoadProfileConfiguration.setChannelInfos(loadSurvey.getChannelInfos());
                    getLoadSurveyMap().put(loadProfileReader, loadSurvey);
                } catch (ProtocolException e) {
                    collectedLoadProfileConfiguration.setFailureInformation(ResultType.NotSupported,
                                                     MdcManager.getIssueFactory().createProblem(loadProfileReader.getProfileObisCode(), "loadProfileXIssue", loadProfileReader.getProfileObisCode(), e.getMessage()));
                } catch (CommunicationException e) {
                    if (e.getCause() instanceof CommandResponseException && ((CommandResponseException) e.getCause()).getResponseCANCode() == 3) {
                        collectedLoadProfileConfiguration.setFailureInformation(ResultType.NotSupported,
                                MdcManager.getIssueFactory().createProblem(loadProfileReader.getProfileObisCode(), "loadProfileXnotsupported", loadProfileReader.getProfileObisCode()));
                    } else {
                        throw e; // Rethrow the original communication exception
                    }
                }
            } else {
                collectedLoadProfileConfiguration.setSupportedByMeter(false);
            }
            result.add(collectedLoadProfileConfiguration);
        }
        return result;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> collectedLoadProfiles = new ArrayList<>(loadProfiles.size());
        for (LoadProfileReader reader : loadProfiles) {
            CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory()
                    .createCollectedLoadProfile(new LoadProfileIdentifierById(reader.getLoadProfileId(), reader.getProfileObisCode()));
            if (getLoadSurveyMap().containsKey(reader)) {
                readLoadProfileData(reader, collectedLoadProfile);
            } else {
                loadProfileNotSupported(reader, collectedLoadProfile);
            }
            collectedLoadProfiles.add(collectedLoadProfile);
        }
        return collectedLoadProfiles;
    }

    private void readLoadProfileData(LoadProfileReader reader, CollectedLoadProfile collectedLoadProfile) {
        try {
            LoadSurvey loadSurvey = getLoadSurveyMap().get(reader);
            LoadSurveyData loadSurveyData = loadSurvey.readFile(reader.getStartReadingTime());
            collectedLoadProfile.setCollectedIntervalData(loadSurveyData.getCollectedIntervalData(), loadSurvey.getChannelInfos());
        } catch (ProtocolException e) {
            collectedLoadProfile.setFailureInformation(ResultType.DataIncomplete,
                    MdcManager.getIssueFactory().createProblem(reader, "loadProfileXissue", reader.getProfileObisCode(), e.getMessage()));
        }
    }

    private void loadProfileNotSupported(LoadProfileReader reader, CollectedLoadProfile collectedLoadProfile) {
        collectedLoadProfile.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(reader, "loadProfileXnotsupported", reader.getProfileObisCode()));
    }

    @Override
    public Date getTime() {
        return new TimeInfo(getProtocol()).getTime();
    }

    public CommandLineProtocol getProtocol() {
        return protocol;
    }

    public Map<LoadProfileReader, LoadSurvey> getLoadSurveyMap() {
        if (loadSurveyMap == null) {
            loadSurveyMap = new HashMap<>();
        }
        return loadSurveyMap;
    }
}