/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterreadings;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;

import ch.iec.tc57._2011.schema.message.ErrorType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SyncReplyIssue {
    private static final String READING = "GetMeterReadings.Reading";
    private static final String READING_ITEM = READING + "[%s]";
    private static final String TIME_PERIOD = READING + ".timePeriod";
    private static final String CONNECTION_METHOD = READING + ".connectionMethod";
    private static final String DATA_SOURCE = READING + ".dataSource";

    private final ReplyTypeFactory replyTypeFactory;

    // Meters aka EndDevices
    private Set<String> notFoundMRIDs;
    private Set<String> notFoundNames;
    private Set<Meter> existedMeters;

    // ReadingType
    private Set<String> notFoundRTMRIDs;
    private Set<String> notFoundRTNames;
    private Set<ReadingType> existedReadingTypes;
    private Map<String, Set<String>> notFoundReadingTypesOnDevices;

    // Reading aka Source
    private Set<Integer> notUsedReadingsDueToTimeStamp;
    private Set<Integer> notUsedReadingsDueToConnectionMethod;
    private Set<Integer> notUsedReadingsDueToDataSources;
    private Set<Integer> existedReadingsIndexes;

    // Source.dataSource
    private Map<String, Set<String>> notFoundLoadProfilesOnDevices;
    private Map<Integer, Set<String>> readingExistedLoadProfilesMap;
    private Map<Integer, Set<String>> readingExistedRegisterGroupsMap;

    // additional errors to be sent synchroneously
    private List<ErrorType> errorTypes;

    // not injectable in order to create this object on each request
    public SyncReplyIssue(ReplyTypeFactory replyTypeFactory) {
        this.replyTypeFactory = replyTypeFactory;
    }

    public ReplyTypeFactory getReplyTypeFactory() {
        return replyTypeFactory;
    }

    public Set<String> getNotFoundMRIDs() {
        if (notFoundMRIDs == null) {
            notFoundMRIDs = new HashSet<>();
        }
        return notFoundMRIDs;
    }

    public void setNotFoundMRIDs(Set<String> notFoundMRIDs) {
        this.notFoundMRIDs = notFoundMRIDs;
    }

    public Set<String> getNotFoundNames() {
        if (notFoundNames == null) {
            notFoundNames = new HashSet<>();
        }
        return notFoundNames;
    }

    public void setNotFoundNames(Set<String> notFoundNames) {
        this.notFoundNames = notFoundNames;
    }

    public Set<Meter> getExistedMeters() {
        if (existedMeters == null) {
            existedMeters = new HashSet<>();
        }
        return existedMeters;
    }

    public void setExistedMeters(Set<Meter> existedMeters) {
        this.existedMeters = existedMeters;
    }

    public Set<String> getNotFoundRTMRIDs() {
        if (notFoundRTMRIDs == null) {
            notFoundRTMRIDs = new HashSet<>();
        }
        return notFoundRTMRIDs;
    }

    public void setNotFoundRTMRIDs(Set<String> notFoundRTMRIDs) {
        this.notFoundRTMRIDs = notFoundRTMRIDs;
    }

    public Set<String> getNotFoundRTNames() {
        if (notFoundRTNames == null) {
            notFoundRTNames = new HashSet<>();
        }
        return notFoundRTNames;
    }

    public void setNotFoundRTNames(Set<String> notFoundRTNames) {
        this.notFoundRTNames = notFoundRTNames;
    }

    public void addExistedReadingType(ReadingType readingType) {
        getExistedReadingTypes().add(readingType);
    }

    public void addExistedReadingTypes(Set<ReadingType> readingTypes) {
        getExistedReadingTypes().addAll(readingTypes);
    }

    public Set<ReadingType> getExistedReadingTypes() {
        if (existedReadingTypes == null) {
            existedReadingTypes = new HashSet<>();
        }
        return existedReadingTypes;
    }

    public void setExistedReadingTypes(Set<ReadingType> existedReadingTypes) {
        this.existedReadingTypes = existedReadingTypes;
    }

    public Map<String, Set<String>> getNotFoundReadingTypesOnDevices() {
        if (notFoundReadingTypesOnDevices == null) {
            notFoundReadingTypesOnDevices = new HashMap<>();
        }
        return notFoundReadingTypesOnDevices;
    }

    public void setNotFoundReadingTypesOnDevices(Map<String, Set<String>> notFoundReadingTypesOnDevices) {
        this.notFoundReadingTypesOnDevices = notFoundReadingTypesOnDevices;
    }

    public Set<Integer> getNotUsedReadingsDueToTimeStamp() {
        if (notUsedReadingsDueToTimeStamp == null) {
            notUsedReadingsDueToTimeStamp = new HashSet<>();
        }
        return notUsedReadingsDueToTimeStamp;
    }

    public void setNotUsedReadingsDueToTimeStamp(Set<Integer> notUsedReadingsDueToTimeStamp) {
        this.notUsedReadingsDueToTimeStamp = notUsedReadingsDueToTimeStamp;
    }

    public void addNotUsedReadingsDueToTimeStamp(int index) {
        if (notUsedReadingsDueToTimeStamp == null) {
            notUsedReadingsDueToTimeStamp = new HashSet<>();
        }
        notUsedReadingsDueToTimeStamp.add(index);
    }

    public void addNotUsedReadingsDueToConnectionMethod(int index) {
        getNotUsedReadingsDueToConnectionMethod().add(index);
    }

    public Set<Integer> getNotUsedReadingsDueToConnectionMethod() {
        if (notUsedReadingsDueToConnectionMethod == null) {
            notUsedReadingsDueToConnectionMethod = new HashSet<>();
        }
        return notUsedReadingsDueToConnectionMethod;
    }

    public void setNotUsedReadingsDueToConnectionMethod(Set<Integer> notUsedReadingsDueToConnectionMethod) {
        this.notUsedReadingsDueToConnectionMethod = notUsedReadingsDueToConnectionMethod;
    }


    public Set<Integer> getNotUsedReadingsDueToDataSources() {
        if (notUsedReadingsDueToDataSources == null) {
            notUsedReadingsDueToDataSources = new HashSet<>();
        }
        return notUsedReadingsDueToDataSources;
    }

    public void setNotUsedReadingsDueToDataSources(Set<Integer> notUsedReadingsDueToDataSources) {
        this.notUsedReadingsDueToDataSources = notUsedReadingsDueToDataSources;
    }

    public void addNotUsedReadingsDueToDataSources(int index) {
        getNotUsedReadingsDueToDataSources().add(index);
    }

    public void addExistedReadingsIndexes(int index) {
        getExistedReadingsIndexes().add(index);
    }

    public Set<Integer> getExistedReadingsIndexes() {
        if (existedReadingsIndexes == null) {
            existedReadingsIndexes = new HashSet<>();
        }
        return existedReadingsIndexes;
    }

    public void setExistedReadingsIndexes(Set<Integer> existedReadingsIndexes) {
        this.existedReadingsIndexes = existedReadingsIndexes;
    }

    public void addNotFoundOnDeviceLoadProfiles(String deviceName, Set<String> notFoundLoadProfiles) {
        getNotFoundLoadProfilesOnDevices().put(deviceName, notFoundLoadProfiles);
    }

    public Map<String, Set<String>> getNotFoundLoadProfilesOnDevices() {
        if (notFoundLoadProfilesOnDevices == null) {
            notFoundLoadProfilesOnDevices = new HashMap<>();
        }
        return notFoundLoadProfilesOnDevices;
    }

    public void setNotFoundLoadProfilesOnDevices(Map<String, Set<String>> notFoundLoadProfilesOnDevices) {
        this.notFoundLoadProfilesOnDevices = notFoundLoadProfilesOnDevices;
    }

    public Map<Integer, Set<String>> getReadingExistedLoadProfilesMap() {
        if (readingExistedLoadProfilesMap == null) {
            readingExistedLoadProfilesMap = new HashMap<>();
        }
        return readingExistedLoadProfilesMap;
    }

    public void setReadingsExistedLoadProfilesMap(Map<Integer, Set<String>> readingExistedLoadProfilesMap) {
        this.readingExistedLoadProfilesMap = readingExistedLoadProfilesMap;
    }

    public void addReadingsExistedLoadProfilesMap(int index, Set<String> existedLoadProfiles) {
        getReadingExistedLoadProfilesMap().put(index, existedLoadProfiles);
    }

    public void addReadingExistedRegisterGroupMap(int index, Set<String> existedRegisterGroups) {
        getReadingExistedRegisterGroupsMap().put(index, existedRegisterGroups);
    }

    public Map<Integer, Set<String>> getReadingExistedRegisterGroupsMap() {
        if (readingExistedRegisterGroupsMap == null) {
            readingExistedRegisterGroupsMap = new HashMap<>();
        }
        return readingExistedRegisterGroupsMap;
    }

    public void setReadingExistedRegisterGroupsMap(Map<Integer, Set<String>> readingExistedRegisterGroupsMap) {
        this.readingExistedRegisterGroupsMap = readingExistedRegisterGroupsMap;
    }

    public void addErrorType(ErrorType errorType) {
        getErrorTypes().add(errorType);
    }

    public void addErrorTypes(Set<ErrorType> errorTypes) {
        getErrorTypes().addAll(errorTypes);
    }

    public List<ErrorType> getErrorTypes() {
        if (errorTypes == null) {
            errorTypes = new ArrayList<>();
        }
        return errorTypes;
    }

    public List<ErrorType> getResultErrorTypes() {
        // reading types issue
        List<ErrorType> errorTypes = getErrorTypes();
        if (!notFoundRTMRIDs.isEmpty() && !notFoundRTNames.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_TYPES_NOT_FOUND_IN_THE_SYSTEM, null,
                    combineNotFoundElementMessage(notFoundRTMRIDs),
                    combineNotFoundElementMessage(notFoundRTNames)));
        } else if (!notFoundRTMRIDs.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_TYPES_WITH_MRID_NOT_FOUND, null,
                    combineNotFoundElementMessage(notFoundRTMRIDs)));
        } else if (!notFoundRTNames.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_TYPES_WITH_NAME_NOT_FOUND, null,
                    combineNotFoundElementMessage(notFoundRTNames)));
        }

        // devices issue
        if (!notFoundMRIDs.isEmpty() && !notFoundNames.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.END_DEVICES_NOT_FOUND, null,
                    combineNotFoundElementMessage(notFoundMRIDs),
                    combineNotFoundElementMessage(notFoundNames)));
        } else if (!notFoundMRIDs.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.END_DEVICES_WITH_MRID_NOT_FOUND, null,
                    combineNotFoundElementMessage(notFoundMRIDs)));
        } else if (!notFoundNames.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.END_DEVICES_WITH_NAME_NOT_FOUND, null,
                    combineNotFoundElementMessage(notFoundNames)));
        }

        getNotFoundReadingTypesOnDevices().forEach((deviceName, readingTypeNames) ->
                errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_TYPES_NOT_FOUND_ON_DEVICE, null,
                        deviceName, combineNotFoundElementMessage(readingTypeNames)))
        );
        getNotFoundLoadProfilesOnDevices().forEach((deviceName, loadProfileNames) ->
                errorTypes.add(replyTypeFactory.errorType(MessageSeeds.LOAD_PROFILES_NOT_FOUND_ON_DEVICE, null,
                        deviceName, combineNotFoundElementMessage(loadProfileNames)))
        );

        if (!getNotUsedReadingsDueToTimeStamp().isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_NOT_APPLICABLE, null,
                    String.format(READING_ITEM, combineNotFoundElementIndexes(getNotUsedReadingsDueToTimeStamp())), TIME_PERIOD));
        }

        if (!getNotUsedReadingsDueToConnectionMethod().isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_NOT_APPLICABLE, null,
                    String.format(READING_ITEM, combineNotFoundElementIndexes(getNotUsedReadingsDueToConnectionMethod())), CONNECTION_METHOD));
        }

        if (!getNotUsedReadingsDueToDataSources().isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_NOT_APPLICABLE, null,
                    String.format(READING_ITEM, combineNotFoundElementIndexes(getNotUsedReadingsDueToDataSources())), DATA_SOURCE));
        }

        if (getExistedReadingsIndexes().isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.NO_READINGS_TO_READ, null));
        }

        return errorTypes;
    }

    private String combineNotFoundElementMessage(Set<String> notFoundElements) {
        return notFoundElements.stream().sorted().collect(Collectors.joining(", "));
    }

    private String combineNotFoundElementIndexes(Set<Integer> notFoundElements) {
        return notFoundElements.stream().sorted().map(Objects::toString).collect(Collectors.joining(", "));
    }
}
