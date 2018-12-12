/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.syntheticloadprofile;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.fileimport.csvimport.FileImportLogger;
import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.slp.importers.impl.AbstractImportProcessor;
import com.elster.jupiter.slp.importers.impl.MessageSeeds;
import com.elster.jupiter.slp.importers.impl.SyntheticLoadProfileDataImporterContext;
import com.elster.jupiter.slp.importers.impl.properties.TimeZonePropertySpec;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Map;

public class SyntheticLoadProfileImportProcessor extends AbstractImportProcessor<SyntheticLoadProfileImportRecord> {

    private Map<String, Map<Instant, BigDecimal>> values = new HashMap<>();
    private Map<String, SyntheticLoadProfile> syntheticLoadProfiles = new HashMap<>();
    private LocalDateTime previousTimeStamp;
    private TemporalAmount interval;
    private ZoneId zoneId;

    private final int SECONDS_IN_MINUTE = 60;

    SyntheticLoadProfileImportProcessor(String timeZone, SyntheticLoadProfileDataImporterContext context) {
        super(context);
        this.zoneId = getContext().getClock().getZone();
    }

    @Override
    public void process(SyntheticLoadProfileImportRecord data, FileImportLogger logger) throws ProcessorException {
        try {
            validateTimeStamps(data);
            data.getSyntheticLoadProfiles().entrySet().forEach(e -> addSyntheticLoadProfile(data, e.getKey(), e.getValue()));
        } catch (Exception e) {
            throw e;
        } finally {
            previousTimeStamp = LocalDateTime.ofInstant(data.getTimeStamp(), zoneId);
        }
    }

    @Override
    public void complete(FileImportLogger logger) {
        validateDuration();
        for (Map.Entry<String, Map<Instant, BigDecimal>> entry : values.entrySet()) {
            SyntheticLoadProfile syntheticLoadProfile = findSyntheticLoadProfile(entry.getKey());
            syntheticLoadProfile.addValues(entry.getValue());
        }
        if (logger instanceof SyntheticLoadProfileImportLogger) {
            ((SyntheticLoadProfileImportLogger) logger).addImportedSyntheticLoadProfiles(values.keySet());
        }
    }

    private void validateTimeStamps(SyntheticLoadProfileImportRecord data) {
        //Check if timestamp in the file is before 'Start time' of synthetic load profile specification
        for (Map.Entry<String, BigDecimal> entry : data.getSyntheticLoadProfiles().entrySet()) {
            SyntheticLoadProfile syntheticLoadProfile = findSyntheticLoadProfile(entry.getKey());
            if (syntheticLoadProfile.getStartTime().isAfter(data.getTimeStamp())) {
                throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_TIMESTAMP_BEFORE_STARTTIME, data.getLineNumber(),
                        DefaultDateTimeFormatters.shortDate().withShortTime().build().format(syntheticLoadProfile.getStartTime().atZone(zoneId)));
            }
            //Check for wrong interval of data (current timestamp minus previous timestamp is not equal to 'Interval' of all of the synthetic load profiles specified in the file)
            if (previousTimeStamp != null && !previousTimeStamp.plus(getInterval(data)).equals(LocalDateTime.ofInstant(data.getTimeStamp(), zoneId))) {
                throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_WRONG_INTERVAL, data.getLineNumber());
            } else if (previousTimeStamp == null) {
                String expectedDateTime = DefaultDateTimeFormatters.shortDate().withShortTime().build().format(syntheticLoadProfile.getStartTime().atZone(zoneId));
                String actualDateTime = DefaultDateTimeFormatters.shortDate().withShortTime().build().format(data.getTimeStamp().atZone(zoneId));
                if (syntheticLoadProfile.getReadingType().getMacroPeriod().equals(MacroPeriod.DAILY) && data.getTimeStamp().atZone(zoneId).getHour() != 0) {
                    throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_WRONG_FIRST_TIMESTAMP, data.getLineNumber(), expectedDateTime, actualDateTime);
                } else if (syntheticLoadProfile.getInterval().getUnits().contains(ChronoUnit.SECONDS)
                        && data.getTimeStamp().atZone(zoneId).getMinute() % ((syntheticLoadProfile.getInterval().get(ChronoUnit.SECONDS)) / SECONDS_IN_MINUTE) != 0) {
                    throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_WRONG_FIRST_TIMESTAMP, data.getLineNumber(), expectedDateTime, actualDateTime);
                }
                if (data.getTimeStamp().atZone(zoneId).getSecond() != 0
                        && data.getTimeStamp().atZone(zoneId).getNano() != 0) {
                    throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_WRONG_FIRST_TIMESTAMP, data.getLineNumber(), expectedDateTime, actualDateTime);
                }
            }
        }
    }

    private void validateDuration() {
        //Check for unexpected end of file (amount of data is less than 'Duration' of synthetic load profile specification)
        for (Map.Entry<String, Map<Instant, BigDecimal>> entry : values.entrySet()) {
            SyntheticLoadProfile syntheticLoadProfile = findSyntheticLoadProfile(entry.getKey());
            LocalDateTime startTime = LocalDateTime.ofInstant(entry.getValue().keySet().stream().min(Instant::compareTo).get(), zoneId);
            LocalDateTime endTime = LocalDateTime.ofInstant(entry.getValue().keySet().stream().max(Instant::compareTo).get(), zoneId);
            if (startTime.plus(syntheticLoadProfile.getDuration()).isAfter(endTime.plus(syntheticLoadProfile.getInterval()))) {
                throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_NOT_ENOUGH_DATA);
            }
        }
    }

    private void addSyntheticLoadProfile(SyntheticLoadProfileImportRecord data, String syntheticLoadProfileName, BigDecimal syntheticLoadProfileValue) {
        if (syntheticLoadProfileValue == null) {
            throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_WRONG_VALUE, data.getLineNumber());
        }
        if (values.containsKey(syntheticLoadProfileName)) {
            values.get(syntheticLoadProfileName).put(data.getTimeStamp(), syntheticLoadProfileValue);
        } else {
            Map<Instant, BigDecimal> syntheticLoadProfileValues = new HashMap<>();
            syntheticLoadProfileValues.put(data.getTimeStamp(), syntheticLoadProfileValue);
            values.put(syntheticLoadProfileName, syntheticLoadProfileValues);
        }
    }

    private SyntheticLoadProfile findSyntheticLoadProfile(String syntheticLoadProfileName) {
        if (!syntheticLoadProfiles.containsKey(syntheticLoadProfileName)) {
            syntheticLoadProfiles.put(syntheticLoadProfileName, getContext().getSyntheticLoadProfileService().findSyntheticLoadProfile(syntheticLoadProfileName)
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.CORRECTIONFACTOR_HEADER_NOT_FOUND, syntheticLoadProfileName)));
        }
        return syntheticLoadProfiles.get(syntheticLoadProfileName);
    }

    private TemporalAmount getInterval(SyntheticLoadProfileImportRecord data) {
        if (interval == null) {
            interval = findSyntheticLoadProfile(data.getSyntheticLoadProfiles().keySet().iterator().next()).getInterval();
        }
        return interval;
    }
}