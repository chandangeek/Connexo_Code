package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.DeviceLoadProfileConfiguration;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;
import com.energyict.protocolimplv2.abnt.common.structure.LoadProfileReadoutResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParameterFields;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParametersResponse;
import com.energyict.protocolimplv2.abnt.common.structure.field.LoadProfileDataSelector;
import com.energyict.protocolimplv2.abnt.common.structure.field.UnitField;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva                      //TODO >> After billing info is not correct!!! - Should re-establish the next interval (within 5 min) - check how to handle <<<
 * @since 28/08/2014 - 9:34
 */
public class LoadProfileBuilder implements DeviceLoadProfileSupport {       //TODO: maybe apply power failure event log information -> channel status short/long

    private static final int NR_OF_WORDS_PER_INTERVAL = 166;
    private static final ObisCode LOAD_PROFILE_OBIS = ObisCode.fromString("1.0.99.1.0.255");

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int CHANNELS_PER_GROUP = 3;
    private static final int MILLIS_PER_HOUR = 3600 * 1000;
    private static final int CHANNELS_PER_CHANNEL_GROUP = 3;

    /**
     * Keeps track of the list of <CODE>ChannelInfo</CODE> objects for all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap;

    private final AbstractAbntProtocol meterProtocol;

    public LoadProfileBuilder(AbstractAbntProtocol meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>(loadProfilesToRead.size());
        for (LoadProfileReader reader : loadProfilesToRead) {
            DeviceLoadProfileConfiguration config = (DeviceLoadProfileConfiguration) MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(LOAD_PROFILE_OBIS, reader.getMeterSerialNumber());
            if (reader.getProfileObisCode().equals(LOAD_PROFILE_OBIS)) {
                fetchLoadProfileConfiguration(reader, config);
            } else {
                config.setSupportedByMeter(false);
            }
            loadProfileConfigurations.add(config);
        }
        return loadProfileConfigurations;
    }

    private void fetchLoadProfileConfiguration(LoadProfileReader reader, DeviceLoadProfileConfiguration loadProfileConfig) {
        try {
            int numberOfLoadProfileChannelGroups = (int) ((BcdEncodedField) getRequestFactory().getDefaultParameters().getField(ReadParameterFields.numberOfLoadProfileChannelGroups)).getValue();
            loadProfileConfig.setProfileInterval((int) (((BcdEncodedField) getRequestFactory().getDefaultParameters().getField(ReadParameterFields.loadProfileInterval)).getValue() * SECONDS_PER_MINUTE));
            List<ChannelInfo> channelInfos = new ArrayList<>(numberOfLoadProfileChannelGroups * CHANNELS_PER_GROUP);
            for (int i = 0; i < numberOfLoadProfileChannelGroups; i++) {
                ReadParametersResponse parameters = getRequestFactory().readParameters(LoadProfileDataSelector.newFullProfileDataSelector(), i);
                addChannelInfoForChannel(reader, channelInfos, parameters, ReadParameterFields.unitChn1);
                addChannelInfoForChannel(reader, channelInfos, parameters, ReadParameterFields.unitChn2);
                addChannelInfoForChannel(reader, channelInfos, parameters, ReadParameterFields.unitChn3);
            }
            loadProfileConfig.setChannelInfos(channelInfos);
            getChannelInfoMap().put(reader, channelInfos);
        } catch (ParsingException e) {
            loadProfileConfig.setSupportedByMeter(false);
            loadProfileConfig.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(reader, "CouldNotParseLoadProfileData")); //TODO: add exception as parameter
        }
    }

    private void addChannelInfoForChannel(LoadProfileReader reader, List<ChannelInfo> channelInfos, ReadParametersResponse parameters, ReadParameterFields unitFieldEnum) {
        UnitField unitField = (UnitField) parameters.getField(unitFieldEnum);
        channelInfos.add(
                new ChannelInfo(
                        channelInfos.size(),
                        unitField.getCorrespondingObisCode().toString(),
                        unitField.getEisUnit(),
                        reader.getMeterSerialNumber(),
                        unitField.isCumulative()
                )
        );
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> collectedLoadProfiles = new ArrayList<>(loadProfiles.size());
        for (LoadProfileReader reader : loadProfiles) {
            CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(reader.getLoadProfileId()));
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
        List<ChannelInfo> channelInfos = getChannelInfoMap().get(reader);   //TODO: should this list be shortened to only contain actual groups?
        int numberOfLoadProfileChannelGroups = (int) Math.ceil((double) reader.getChannelInfos().size() / CHANNELS_PER_CHANNEL_GROUP);
        numberOfLoadProfileChannelGroups = 7;   //TODO: hard-coded overrule here

        LoadProfileDataSelector dataSelector = createLoadProfileDataSelectorFor(reader);
        Map<Date, IntervalData> intervalDataMap = new HashMap<>();
        for (int channelGroup = 0; channelGroup < numberOfLoadProfileChannelGroups; channelGroup++) { //TODO: warning - groups can be noon-adjacent!!!
            readLoadProfilePart(collectedLoadProfile, intervalDataMap, dataSelector, channelGroup);
        }

        List<IntervalData> intervalData = composeAndFilterIntervalDataList(reader, intervalDataMap, numberOfLoadProfileChannelGroups);
        collectedLoadProfile.setCollectedIntervalData(intervalData, channelInfos);
    }

    private LoadProfileDataSelector createLoadProfileDataSelectorFor(LoadProfileReader reader) {
        long timeDifferenceInMillis = reader.getEndReadingTime().getTime() - reader.getStartReadingTime().getTime();
        int nrOfHoursToRead = (int) Math.ceil((double) timeDifferenceInMillis / MILLIS_PER_HOUR);
        return LoadProfileDataSelector.newHoursToReadDataSelector(nrOfHoursToRead < 99 ? nrOfHoursToRead : 99); //TODO: limited to 0x99 hours - is it possible to read out earlier data?
    }

    private void readLoadProfilePart(CollectedLoadProfile collectedLoadProfile, Map<Date, IntervalData> intervalDataMap, LoadProfileDataSelector dataSelector, int channelGroup) {
        try {
            ReadParametersResponse parameters = getRequestFactory().readParameters(dataSelector, channelGroup);
            int nrOfLoadProfileWords = (int) ((BcdEncodedField)parameters.getField(ReadParameterFields.numberOfLoadProfileWords)).getValue();
            int maxNrOfSegments = (int) Math.ceil((double) nrOfLoadProfileWords / NR_OF_WORDS_PER_INTERVAL);
            LoadProfileReadoutResponse loadProfileData = getRequestFactory().readLoadProfileData(maxNrOfSegments);

            Calendar intervalCalendar = Calendar.getInstance(getMeterProtocol().getRequestFactory().getTimeZone());
            intervalCalendar.setTime(((DateTimeField) parameters.getField(ReadParameterFields.dateTimeOfLastDemandInterval)).getDate(getMeterProtocol().getTimeZone()));
            for (int i = nrOfLoadProfileWords; i > 0; i -= 3) {
                IntervalData intervalData = intervalDataMap.containsKey(intervalCalendar.getTime())
                        ? intervalDataMap.get(intervalCalendar.getTime())   // If the intervalData already exists (and thus contains already data for other channel groups) then re-use it
                        : new IntervalData(intervalCalendar.getTime());     // else make a new IntervalData object (note: this should only be the case for channel group 0)

                intervalData.addValue((double) loadProfileData.getLoadProfileWords().getLoadProfileWords().get(i - 3) * ((BcdEncodedField) parameters.getField(ReadParameterFields.numeratorChn1)).getValue() / ((BcdEncodedField) parameters.getField(ReadParameterFields.denominatorChn1)).getValue());
                intervalData.addValue((double) loadProfileData.getLoadProfileWords().getLoadProfileWords().get(i - 2) * ((BcdEncodedField) parameters.getField(ReadParameterFields.numeratorChn2)).getValue() / ((BcdEncodedField) parameters.getField(ReadParameterFields.denominatorChn2)).getValue());
                intervalData.addValue((double) loadProfileData.getLoadProfileWords().getLoadProfileWords().get(i - 1) * ((BcdEncodedField) parameters.getField(ReadParameterFields.numeratorChn3)).getValue() / ((BcdEncodedField) parameters.getField(ReadParameterFields.denominatorChn3)).getValue());

                intervalDataMap.put(intervalCalendar.getTime(), intervalData); // Create or update intervalData entry
                intervalCalendar.add(Calendar.MINUTE, (int) (-1 * ((BcdEncodedField) parameters.getField(ReadParameterFields.loadProfileInterval)).getValue()));    // Subtract load profile interval from calendar
            }
        } catch (ParsingException e) {
            collectedLoadProfile.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(collectedLoadProfile, "CouldNotParseLoadProfileData")); //TODO: add exception as parameter
        }
    }

    private List<IntervalData> composeAndFilterIntervalDataList(LoadProfileReader reader, Map<Date, IntervalData> intervalDataMap, int numberOfLoadProfileChannelGroups) {
        List<IntervalData> intervalDatas = new ArrayList<>(intervalDataMap.size());

        for (IntervalData intervalData : intervalDataMap.values()) {
            if (intervalData.getEndTime().before(reader.getStartReadingTime()) ||
                    intervalData.getEndTime().after(reader.getEndReadingTime())) {
                // Entry outside of requested time frame, no need to add it
            } else if (intervalData.getValueCount() != (numberOfLoadProfileChannelGroups * CHANNELS_PER_CHANNEL_GROUP)) {
                // Entry has an invalid number of channels, definitely do not add it
                System.out.println(intervalData.getEndTime() + " - CHN: " + intervalData.getIntervalValues().size());   //TODO: remove system out statement
            } else {
                intervalDatas.add(intervalData);
            }
        }
        Collections.sort(intervalDatas, new IntervalDataComparator());
        return intervalDatas;
    }

    private void loadProfileNotSupported(LoadProfileReader reader, CollectedLoadProfile collectedLoadProfile) {
        collectedLoadProfile.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(reader, "loadProfileXnotsupported", reader.getProfileObisCode()));
    }

    @Override
    public Date getTime() {
        return getMeterProtocol().getTime();
    }

    public AbstractAbntProtocol getMeterProtocol() {
        return meterProtocol;
    }

    public Map<LoadProfileReader, List<ChannelInfo>> getChannelInfoMap() {
        if (this.channelInfoMap == null) {
            this.channelInfoMap = new HashMap<>();
        }
        return this.channelInfoMap;
    }

    public RequestFactory getRequestFactory() {
        return getMeterProtocol().getRequestFactory();
    }

    private class IntervalDataComparator implements java.util.Comparator<IntervalData> {

        @Override
        public int compare(IntervalData o1, IntervalData o2) {
            return o1.getEndTime().compareTo(o2.getEndTime());
        }
    }
}