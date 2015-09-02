package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;
import com.energyict.protocolimplv2.abnt.common.structure.LoadProfileReadoutResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParameterFields;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParametersResponse;
import com.energyict.protocolimplv2.abnt.common.structure.field.UnitField;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 28/08/2014 - 9:34
 */
public class LoadProfileBuilder implements DeviceLoadProfileSupport {

    private static final int NR_OF_WORDS_PER_INTERVAL = 166;
    private static final ObisCode LOAD_PROFILE_OBIS = ObisCode.fromString("1.0.99.1.0.255");

    private static final int MINUTES_PER_HOUR = 60;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int CHANNELS_PER_GROUP = 3;
    private static final int CHANNELS_PER_CHANNEL_GROUP = 3;

    /**
     * Keeps track of the list of <CODE>ChannelInfo</CODE> objects for all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap;

    /**
     * Keeps track of the channel group number for the UnitMapping
     */
    private Map<UnitField.UnitMapping, Integer> unitMappingMappedToChannelGroup;

    /**
     * Keeps track of the List of ChannelInfos for each channel group
     */
    private Map<Integer, List<ChannelInfo>> channelInfosForChannelGroup;

    private final AbstractAbntProtocol meterProtocol;

    public LoadProfileBuilder(AbstractAbntProtocol meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>(loadProfilesToRead.size());
        for (LoadProfileReader reader : loadProfilesToRead) {
            CollectedLoadProfileConfiguration config = MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(reader.getProfileObisCode(), reader.getMeterSerialNumber());
            if (reader.getProfileObisCode().equals(LOAD_PROFILE_OBIS)) {
                fetchLoadProfileConfiguration(reader, config);
            } else {
                config.setSupportedByMeter(false);
            }
            loadProfileConfigurations.add(config);
        }
        return loadProfileConfigurations;
    }

    private void fetchLoadProfileConfiguration(LoadProfileReader reader, CollectedLoadProfileConfiguration loadProfileConfig) {
        try {
            int numberOfLoadProfileChannelGroups = (int) ((BcdEncodedField) getRequestFactory().getDefaultParameters().getField(ReadParameterFields.numberOfLoadProfileChannelGroups)).getValue();
            loadProfileConfig.setProfileInterval((int) (((BcdEncodedField) getRequestFactory().getDefaultParameters().getField(ReadParameterFields.loadProfileInterval)).getValue() * SECONDS_PER_MINUTE));
            List<ChannelInfo> channelInfos = new ArrayList<>(numberOfLoadProfileChannelGroups * CHANNELS_PER_GROUP);
            for (int i = 0; i < numberOfLoadProfileChannelGroups; i++) {
                List<ChannelInfo> channelGroupChannelInfos = new ArrayList<>();
                ReadParametersResponse parameters = getRequestFactory().readParameters(i);
                addChannelInfoForChannel(reader, channelInfos, channelGroupChannelInfos, i, parameters, ReadParameterFields.unitChn1);
                addChannelInfoForChannel(reader, channelInfos, channelGroupChannelInfos, i, parameters, ReadParameterFields.unitChn2);
                addChannelInfoForChannel(reader, channelInfos, channelGroupChannelInfos, i, parameters, ReadParameterFields.unitChn3);
                getChannelInfosForChannelGroup().put(i, channelGroupChannelInfos);
            }
            loadProfileConfig.setChannelInfos(channelInfos);
            getChannelInfoMap().put(reader, channelInfos);
        } catch (ParsingException e) {
            loadProfileConfig.setSupportedByMeter(false);
            loadProfileConfig.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createProblem(reader, "CouldNotParseLoadProfileData"));
        }
    }

    private void addChannelInfoForChannel(LoadProfileReader reader, List<ChannelInfo> channelInfos, List<ChannelInfo> channelGroupChannelInfos, int channelGroup, ReadParametersResponse parameters, ReadParameterFields unitFieldEnum) {
        UnitField unitField = (UnitField) parameters.getField(unitFieldEnum);
        getUnitMappingMappedToChannelGroup().put(unitField.getUnitMapping(), channelGroup);
        ChannelInfo channelInfo = new ChannelInfo(
                channelInfos.size(),
                unitField.getCorrespondingObisCode().toString(),
                unitField.getEisUnit(),
                reader.getMeterSerialNumber(),
                unitField.isCumulative()
        );
        channelInfos.add(channelInfo);
        channelGroupChannelInfos.add(channelInfo);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> collectedLoadProfiles = new ArrayList<>(loadProfiles.size());
        for (LoadProfileReader reader : loadProfiles) {
            CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(reader.getLoadProfileId(), reader.getProfileObisCode()));
            if (getChannelInfoMap().containsKey(reader)) {
                readLoadProfileData(reader, collectedLoadProfile);
            } else {
                loadProfileNotSupported(reader, collectedLoadProfile);
            }
            collectedLoadProfiles.add(collectedLoadProfile);
        }
        return collectedLoadProfiles;
    }

    private void readLoadProfileData(LoadProfileReader reader, CollectedLoadProfile collectedLoadProfile) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        Map<Date, IntervalData> intervalDataMap = new HashMap<>();

        try {
            List<Integer> channelGroupsWhoShouldBeRead = findChannelGroupsWhoShouldBeRead(reader);
            DateTimeField lastDemandResetDateTime = (DateTimeField) getRequestFactory().getDefaultParameters().getField(ReadParameterFields.dateTimeOfLastDemandReset);
            boolean shouldReadPreviousBillingLoadProfileData = lastDemandResetDateTime.getDate(getMeterProtocol().getTimeZone()).after(reader.getStartReadingTime());
            for (Integer channelGroup : channelGroupsWhoShouldBeRead) {
                channelInfos.addAll(getChannelInfosForChannelGroup().get(channelGroup));
                readLoadProfilePart(intervalDataMap, channelGroup, shouldReadPreviousBillingLoadProfileData);
            }

            List<IntervalData> intervalData = composeAndFilterIntervalDataList(reader, intervalDataMap, channelGroupsWhoShouldBeRead.size());
            collectedLoadProfile.setCollectedIntervalData(intervalData, channelInfos);
        } catch (ParsingException e) {
            collectedLoadProfile.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createProblem(collectedLoadProfile, "CouldNotParseLoadProfileData"));
        }
    }

    private List<Integer> findChannelGroupsWhoShouldBeRead(LoadProfileReader loadProfileReader) {
        List<Integer> channelGroupsWhoShouldBeRead = new ArrayList<>();
        for (ChannelInfo channelInfo : loadProfileReader.getChannelInfos()) {
            try {
                UnitField.UnitMapping unitMapping = UnitField.UnitMapping.fromObisCode(channelInfo.getChannelObisCode());
                if (!channelGroupsWhoShouldBeRead.contains(getUnitMappingMappedToChannelGroup().get(unitMapping))) {
                    channelGroupsWhoShouldBeRead.add(getUnitMappingMappedToChannelGroup().get(unitMapping));
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(channelInfo + e.getMessage());
            }
        }
        return channelGroupsWhoShouldBeRead;
    }

    private void readLoadProfilePart(Map<Date, IntervalData> intervalDataMap, Integer channelGroup, boolean shouldReadPreviousLoadProfile) throws ParsingException {
        if (shouldReadPreviousLoadProfile) {
            readPreviousBillingLoadProfileData(intervalDataMap, channelGroup);
        }
        readCurrentBillingLoadProfileData(intervalDataMap, channelGroup);
    }

    private void readPreviousBillingLoadProfileData(Map<Date, IntervalData> intervalDataMap, Integer channelGroup) throws ParsingException {
        ReadParametersResponse parameters = getRequestFactory().readPreviousParameters(channelGroup);
        int nrOfLoadProfileWords = (int) ((BcdEncodedField) parameters.getField(ReadParameterFields.numberOfLoadProfileWordsInPreviousBilling)).getValue();
        int maxNrOfSegments = (int) Math.ceil((double) nrOfLoadProfileWords / NR_OF_WORDS_PER_INTERVAL);

        LoadProfileReadoutResponse loadProfileData = getRequestFactory().readPreviousBillingLoadProfileData(maxNrOfSegments);
        doReadLoadProfileData(intervalDataMap, parameters, nrOfLoadProfileWords, loadProfileData, channelGroup);
    }

    private void readCurrentBillingLoadProfileData(Map<Date, IntervalData> intervalDataMap, Integer channelGroup) throws ParsingException {
        ReadParametersResponse parameters = getRequestFactory().readParameters(channelGroup);
        int nrOfLoadProfileWords = (int) ((BcdEncodedField) parameters.getField(ReadParameterFields.numberOfLoadProfileWords)).getValue();
        int maxNrOfSegments = (int) Math.ceil((double) nrOfLoadProfileWords / NR_OF_WORDS_PER_INTERVAL);

        LoadProfileReadoutResponse loadProfileData = getRequestFactory().readCurrentBillingLoadProfileData(maxNrOfSegments);
        doReadLoadProfileData(intervalDataMap, parameters, nrOfLoadProfileWords, loadProfileData, channelGroup);
    }


    private void doReadLoadProfileData(Map<Date, IntervalData> intervalDataMap, ReadParametersResponse parameters, int nrOfLoadProfileWords, LoadProfileReadoutResponse loadProfileData, Integer channelGroup) throws ParsingException {
        Calendar intervalCalendar = Calendar.getInstance(getMeterProtocol().getRequestFactory().getTimeZone());
        intervalCalendar.setTime(getDateOfLastDemandInterval(parameters));
        for (int i = nrOfLoadProfileWords; i > 0; i -= 3) {
            IntervalData intervalData = intervalDataMap.containsKey(intervalCalendar.getTime())
                    ? intervalDataMap.get(intervalCalendar.getTime())   // If the intervalData already exists (and thus contains already data for other channel groups) then re-use it
                    : new IntervalData(intervalCalendar.getTime());     // else make a new IntervalData object (note: this should only be the case for channel group 0)


            int nrOfIntervalsPerHour = MINUTES_PER_HOUR / (int) (((BcdEncodedField) parameters.getField(ReadParameterFields.loadProfileInterval)).getValue());
            List<ChannelInfo> channelInfos = getChannelInfosForChannelGroup().get(channelGroup);
            intervalData.addValue(
                    (double) loadProfileData.getLoadProfileWords().getLoadProfileWords().get(i - 3)
                            * ((BcdEncodedField) parameters.getField(ReadParameterFields.numeratorChn1)).getValue()
                            / ((BcdEncodedField) parameters.getField(ReadParameterFields.denominatorChn1)).getValue()
                            * (!channelInfos.get(0).getUnit().isUndefined() && channelInfos.get(0).getUnit().isVolumeUnit() ? 1 : nrOfIntervalsPerHour)
            );
            intervalData.addValue(
                    (double) loadProfileData.getLoadProfileWords().getLoadProfileWords().get(i - 2)
                            * ((BcdEncodedField) parameters.getField(ReadParameterFields.numeratorChn2)).getValue()
                            /  ((BcdEncodedField) parameters.getField(ReadParameterFields.denominatorChn2)).getValue()
                            * (!channelInfos.get(1).getUnit().isUndefined() && channelInfos.get(0).getUnit().isVolumeUnit() ? 1 : nrOfIntervalsPerHour)
            );
            intervalData.addValue(
                    (double) loadProfileData.getLoadProfileWords().getLoadProfileWords().get(i - 1)
                            * ((BcdEncodedField) parameters.getField(ReadParameterFields.numeratorChn3)).getValue()
                            /  ((BcdEncodedField) parameters.getField(ReadParameterFields.denominatorChn3)).getValue()
                            * (!channelInfos.get(0).getUnit().isUndefined() && channelInfos.get(2).getUnit().isVolumeUnit() ? 1 : nrOfIntervalsPerHour)
            ) ;

            intervalDataMap.put(intervalCalendar.getTime(), intervalData); // Create or update intervalData entry
            intervalCalendar.add(Calendar.MINUTE, (int) (-1 * ((BcdEncodedField) parameters.getField(ReadParameterFields.loadProfileInterval)).getValue()));    // Subtract load profile interval from calendar
        }
    }

    private Date getDateOfLastDemandInterval(ReadParametersResponse parameters) throws ParsingException {
        Date date = ((DateTimeField) parameters.getField(ReadParameterFields.dateTimeOfLastDemandInterval)).getDate(getMeterProtocol().getTimeZone());
        long loadProfileInterval = ((BcdEncodedField) parameters.getField(ReadParameterFields.loadProfileInterval)).getValue();
        return ProtocolTools.roundUpToNearestInterval(date, (int) loadProfileInterval);
    }

    private List<IntervalData> composeAndFilterIntervalDataList(LoadProfileReader reader, Map<Date, IntervalData> intervalDataMap, int numberOfLoadProfileChannelGroups) {
        List<IntervalData> intervalDatas = new ArrayList<>(intervalDataMap.size());

        for (IntervalData intervalData : intervalDataMap.values()) {
            if (intervalData.getEndTime().before(reader.getStartReadingTime()) ||
                    intervalData.getEndTime().after(reader.getEndReadingTime())) {
                // Entry outside of requested time frame, no need to add it
            } else if (intervalData.getValueCount() != (numberOfLoadProfileChannelGroups * CHANNELS_PER_CHANNEL_GROUP)) {
                // Entry has an invalid number of channels, definitely do not add it
            } else {
                intervalDatas.add(intervalData);
            }
        }
        Collections.sort(intervalDatas,
                new Comparator<IntervalData>() {
                    @Override
                    public int compare(IntervalData o1, IntervalData o2) {
                        return o1.getEndTime().compareTo(o2.getEndTime());
                    }
                });
        return intervalDatas;
    }

    private void loadProfileNotSupported(LoadProfileReader reader, CollectedLoadProfile collectedLoadProfile) {
        collectedLoadProfile.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(reader, "loadProfileXnotsupported", reader.getProfileObisCode()));
    }

    public AbstractAbntProtocol getMeterProtocol() {
        return meterProtocol;
    }

    public RequestFactory getRequestFactory() {
        return getMeterProtocol().getRequestFactory();
    }

    @Override
    public Date getTime() {
        return getMeterProtocol().getTime();
    }

    public Map<LoadProfileReader, List<ChannelInfo>> getChannelInfoMap() {
        if (this.channelInfoMap == null) {
            this.channelInfoMap = new HashMap<>();
        }
        return this.channelInfoMap;
    }

    public Map<UnitField.UnitMapping, Integer> getUnitMappingMappedToChannelGroup() {
        if (this.unitMappingMappedToChannelGroup == null) {
            this.unitMappingMappedToChannelGroup = new HashMap<>();
        }
        return this.unitMappingMappedToChannelGroup;
    }

    public Map<Integer, List<ChannelInfo>> getChannelInfosForChannelGroup() {
        if (this.channelInfosForChannelGroup == null) {
            this.channelInfosForChannelGroup = new HashMap<>();
        }
        return this.channelInfosForChannelGroup;
    }
}