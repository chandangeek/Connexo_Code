/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.profiles;

import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;

import com.energyict.cbo.Utils;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandResponseException;
import com.energyict.protocolimpl.edmi.common.command.TimeInfo;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.edmi.mk6.MK6;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 3/03/2017 - 16:56
 */
public class MK6LoadProfileBuilder implements DeviceLoadProfileSupport {

    private final CommandLineProtocol protocol;

    /**
     * Keeps track of the LoadSurvey object for all the LoadProfiles
     */
    private Map<LoadProfileReader, LoadSurvey> loadSurveyMap;

    /**
     * Keeps track of the list of ChannelInfos for the all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap;

    public MK6LoadProfileBuilder(CommandLineProtocol protocol) {
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
                    LoadSurvey loadSurvey = getCorrespondingLoadSurvey(loadProfileDescription);
                    if (loadSurvey != null) {
                        collectedLoadProfileConfiguration.setProfileInterval(loadSurvey.getProfileInterval());
                        collectedLoadProfileConfiguration.setChannelInfos(constructChannelInfos(loadSurvey, loadProfileReader));
                        getLoadSurveyMap().put(loadProfileReader, loadSurvey);
                    } else {
                        markLoadProfileNotSupported(loadProfileReader, collectedLoadProfileConfiguration);
                    }
                } catch (ProtocolException e) {
                    collectedLoadProfileConfiguration.setFailureInformation(ResultType.InCompatible,
                            MdcManager.getIssueFactory().createProblem(loadProfileReader, "loadProfileXissue", loadProfileReader.getProfileObisCode(), e.getMessage()));
                } catch (CommunicationException e) {
                    if (e.getCause() instanceof CommandResponseException && ((CommandResponseException) e.getCause()).getResponseCANCode() == 3) {
                        collectedLoadProfileConfiguration.setFailureInformation(ResultType.NotSupported,
                        MdcManager.getIssueFactory().createProblem(loadProfileReader.getProfileObisCode(), "loadProfileXnotsupported", loadProfileReader.getProfileObisCode()));
                    } else {
                        throw e; // Rethrow the original communication exception
                    }
                    e.printStackTrace();
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
                markLoadProfileNotSupported(reader, collectedLoadProfile);
            }
            collectedLoadProfiles.add(collectedLoadProfile);
        }
        return collectedLoadProfiles;
    }

    private void readLoadProfileData(LoadProfileReader reader, CollectedLoadProfile collectedLoadProfile) {
        try {
            LoadSurvey loadSurvey = getLoadSurveyMap().get(reader);
            LoadSurveyData loadSurveyData = loadSurvey.readFile(reader.getStartReadingTime());
            collectedLoadProfile.setCollectedIntervalData(loadSurveyData.getCollectedIntervalData(), getChannelInfoMap().get(reader));
        } catch (ProtocolException e) {
            collectedLoadProfile.setFailureInformation(ResultType.DataIncomplete,
                    MdcManager.getIssueFactory().createProblem(reader, "loadProfileXissue", reader.getProfileObisCode(), e.getMessage()));
        }
    }

    private List<ChannelInfo> constructChannelInfos(LoadSurvey loadSurvey, LoadProfileReader reader) throws ProtocolException {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (int channel = 1; channel < loadSurvey.getNrOfChannels(); channel++) {
            LoadSurveyChannel loadSurveyChannel = loadSurvey.getLoadSurveyChannels()[channel];
            LoadProfileChannelDescription loadProfileChannelDescription = LoadProfileChannelDescription.channelDescriptionForName(loadSurveyChannel.getName());
            if (loadProfileChannelDescription == null) {
                throw new ProtocolException(
                        Utils.format(
                                "Encountered unsupported channel ({0}) for load survey {1}",
                                new Object[]{loadSurveyChannel.getName(), reader.getProfileObisCode()}
                        )
                );
            }

            ChannelInfo channelInfo = new ChannelInfo(
                    channelInfos.size(),
                    loadProfileChannelDescription.getObisCode().toString(),
                    loadSurveyChannel.getUnit(),
                    getProtocol().getConfiguredSerialNumber()
            );
            channelInfo.setMultiplier(loadSurveyChannel.getScalingFactor());
            channelInfos.add(channelInfo);
        }
        getChannelInfoMap().put(reader, channelInfos);
        return channelInfos;
    }

    private void markLoadProfileNotSupported(LoadProfileReader reader, CollectedLoadProfileConfiguration collectedLoadProfileConfiguration) {
        collectedLoadProfileConfiguration.setSupportedByMeter(false);
        collectedLoadProfileConfiguration.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(reader, "loadProfileXnotsupported", reader.getProfileObisCode()));
    }

    private void markLoadProfileNotSupported(LoadProfileReader reader, CollectedLoadProfile collectedLoadProfile) {
        collectedLoadProfile.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(reader, "loadProfileXnotsupported", reader.getProfileObisCode()));
    }

    private LoadSurvey getCorrespondingLoadSurvey(LoadProfileDescription loadProfileDescription) {
        try {
            return getExtensionFactory().findLoadSurvey(loadProfileDescription.getExtensionName());
        } catch (IOException e) {
            return null; // Load survey for the given extension name was not found
        }
    }

    public ExtensionFactory getExtensionFactory() {
        return ((MK6) getProtocol()).getExtensionFactory();
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

    public Map<LoadProfileReader, List<ChannelInfo>> getChannelInfoMap() {
        if (channelInfoMap == null) {
            channelInfoMap = new HashMap<>();
        }
        return channelInfoMap;
    }
}