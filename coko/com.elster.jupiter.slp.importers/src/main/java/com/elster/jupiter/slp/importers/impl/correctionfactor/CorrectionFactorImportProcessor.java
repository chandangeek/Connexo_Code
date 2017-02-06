/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.correctionfactor;

import com.elster.jupiter.slp.CorrectionFactor;
import com.elster.jupiter.slp.importers.impl.AbstractImportProcessor;
import com.elster.jupiter.slp.importers.impl.FileImportLogger;
import com.elster.jupiter.slp.importers.impl.MessageSeeds;
import com.elster.jupiter.slp.importers.impl.SyntheticLoadProfileDataImporterContext;
import com.elster.jupiter.slp.importers.impl.exceptions.ProcessorException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CorrectionFactorImportProcessor extends AbstractImportProcessor<CorrectionFactorImportRecord> {

    private Map<String, Map<Instant, BigDecimal>> values = new HashMap<>();
    private Instant previousTimeStamp;

    CorrectionFactorImportProcessor(SyntheticLoadProfileDataImporterContext context) {
        super(context);
    }

    @Override
    public void process(CorrectionFactorImportRecord data, FileImportLogger logger) throws ProcessorException {
        try {
            validateTimeStamps(data);
            data.getCorrectionFactors().entrySet().forEach(e -> addCorrectionFactor(data.getTimeStamp(), e.getKey(), e.getValue()));
        } catch (Exception e) {
            previousTimeStamp = data.getTimeStamp();
            throw e;
        }
    }

    @Override
    public void complete(FileImportLogger logger) {
        validateDuration();
        for (Map.Entry<String, Map<Instant, BigDecimal>> entry : values.entrySet()) {
            CorrectionFactor correctionFactor = findCorrectionFactor(entry.getKey());
            correctionFactor.addValues(entry.getValue());
        }
        if (logger instanceof CorrectionFactorImportLogger) {
            ((CorrectionFactorImportLogger) logger).addImportedCorrectionFactors(values.keySet());
        }
    }

    private void validateTimeStamps(CorrectionFactorImportRecord data) {
        //Check if timestamp in the file is before 'Start time' of correction factor specification
        for (Map.Entry<String, BigDecimal> entry : data.getCorrectionFactors().entrySet()) {
            if (findCorrectionFactor(entry.getKey()).getStartTime().isAfter(data.getTimeStamp())) {
                throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_TIMESTAMP_BEFORE_STARTTIME, data.getLineNumber(),
                        DateTimeFormatter.ofPattern("dd/MMM/YYYY-HH:mm", Locale.ENGLISH)
                                .format(LocalDateTime.ofInstant(findCorrectionFactor(entry.getKey()).getStartTime(), ZoneId.systemDefault())));
            }
        }
        //Check for wrong interval of data (current timestamp minus previous timestamp is not equal to 'Interval' of all of the correction factors specified in the file)
        if (previousTimeStamp != null && !previousTimeStamp.plus(findCorrectionFactor(data.getCorrectionFactors().keySet().iterator().next()).getInterval().getTemporalAmount()).equals(data.getTimeStamp())) {
            throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_WRONG_INTERVAL, data.getLineNumber());
        } else {
            previousTimeStamp = data.getTimeStamp();
        }
    }

    private void validateDuration() {
        //Check for unexpected end of file (amount of data is less than 'Duration' of correction factor specification)
        for (Map.Entry<String, Map<Instant, BigDecimal>> entry : values.entrySet()) {
            CorrectionFactor correctionFactor = findCorrectionFactor(entry.getKey());
            Instant startTime = entry.getValue().keySet().stream().min(Instant::compareTo).get();
            Instant endTime = entry.getValue().keySet().stream().max(Instant::compareTo).get();
            if (startTime.plus(correctionFactor.getDuration().getTemporalAmount()).isAfter(endTime.plus(correctionFactor.getInterval().getTemporalAmount()))) {
                throw new ProcessorException(MessageSeeds.CORRECTIONFACTOR_NOT_ENOUGH_DATA);
            }
        }
    }

    private void addCorrectionFactor(Instant timeStamp, String correctionFactorName, BigDecimal correctionFactorValue) {
        if (values.containsKey(correctionFactorName)) {
            values.get(correctionFactorName).put(timeStamp, correctionFactorValue);
        } else {
            Map<Instant, BigDecimal> correctionFactorValues = new HashMap<>();
            correctionFactorValues.put(timeStamp, correctionFactorValue);
            values.put(correctionFactorName, correctionFactorValues);
        }
    }

    private CorrectionFactor findCorrectionFactor(String correctionFactorName) {
        return getContext().getSyntheticLoadProfileService().findCorrectionFactor(correctionFactorName)
                .orElseThrow(() -> new ProcessorException(MessageSeeds.CORRECTIONFACTOR_HEADER_NOT_FOUND, correctionFactorName));
    }

}
