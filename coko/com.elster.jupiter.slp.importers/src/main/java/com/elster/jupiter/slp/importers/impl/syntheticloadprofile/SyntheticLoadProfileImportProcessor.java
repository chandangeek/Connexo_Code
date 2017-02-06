/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.syntheticloadprofile;

import com.elster.jupiter.fileimport.csvimport.FileImportLogger;
import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.slp.SyntheticLoadProfile;
import com.elster.jupiter.slp.importers.impl.AbstractImportProcessor;
import com.elster.jupiter.slp.importers.impl.MessageSeeds;
import com.elster.jupiter.slp.importers.impl.SyntheticLoadProfileDataImporterContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SyntheticLoadProfileImportProcessor extends AbstractImportProcessor<SyntheticLoadProfileImportRecord> {

    private Map<String, Map<Instant, BigDecimal>> values = new HashMap<>();
    private Instant previousTimeStamp;

    SyntheticLoadProfileImportProcessor(SyntheticLoadProfileDataImporterContext context) {
        super(context);
    }

    @Override
    public void process(SyntheticLoadProfileImportRecord data, FileImportLogger logger) throws ProcessorException {
        try {
            validateTimeStamps(data);
            data.getSyntheticLoadProfiles().entrySet().forEach(e -> addSyntheticLoadProfile(data.getTimeStamp(), e.getKey(), e.getValue()));
        } catch (Exception e) {
            previousTimeStamp = data.getTimeStamp();
            throw e;
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
            if (findSyntheticLoadProfile(entry.getKey()).getStartTime().isAfter(data.getTimeStamp())) {
                throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_TIMESTAMP_BEFORE_STARTTIME, data.getLineNumber(),
                        DateTimeFormatter.ofPattern("dd/MMM/YYYY-HH:mm", Locale.ENGLISH)
                                .format(LocalDateTime.ofInstant(findSyntheticLoadProfile(entry.getKey()).getStartTime(), ZoneId.systemDefault())));
            }
        }
        //Check for wrong interval of data (current timestamp minus previous timestamp is not equal to 'Interval' of all of the synthetic load profiles specified in the file)
        if (previousTimeStamp != null && !previousTimeStamp.plus(findSyntheticLoadProfile(data.getSyntheticLoadProfiles().keySet().iterator().next()).getInterval().getTemporalAmount()).equals(data.getTimeStamp())) {
            throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_WRONG_INTERVAL, data.getLineNumber());
        } else {
            previousTimeStamp = data.getTimeStamp();
        }
    }

    private void validateDuration() {
        //Check for unexpected end of file (amount of data is less than 'Duration' of synthetic load profile specification)
        for (Map.Entry<String, Map<Instant, BigDecimal>> entry : values.entrySet()) {
            SyntheticLoadProfile syntheticLoadProfile = findSyntheticLoadProfile(entry.getKey());
            Instant startTime = entry.getValue().keySet().stream().min(Instant::compareTo).get();
            Instant endTime = entry.getValue().keySet().stream().max(Instant::compareTo).get();
            if (startTime.plus(syntheticLoadProfile.getDuration().getTemporalAmount()).isAfter(endTime.plus(syntheticLoadProfile.getInterval().getTemporalAmount()))) {
                throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_NOT_ENOUGH_DATA);
            }
        }
    }

    private void addSyntheticLoadProfile(Instant timeStamp, String syntheticLoadProfileName, BigDecimal syntheticLoadProfileValue) {
        if (values.containsKey(syntheticLoadProfileName)) {
            values.get(syntheticLoadProfileName).put(timeStamp, syntheticLoadProfileValue);
        } else {
            Map<Instant, BigDecimal> syntheticLoadProfileValues = new HashMap<>();
            syntheticLoadProfileValues.put(timeStamp, syntheticLoadProfileValue);
            values.put(syntheticLoadProfileName, syntheticLoadProfileValues);
        }
    }

    private SyntheticLoadProfile findSyntheticLoadProfile(String syntheticLoadProfileName) {
        return getContext().getSyntheticLoadProfileService().findSyntheticLoadProfile(syntheticLoadProfileName)
                .orElseThrow(() -> new ProcessorException(MessageSeeds.CORRECTIONFACTOR_HEADER_NOT_FOUND, syntheticLoadProfileName));
    }

}
