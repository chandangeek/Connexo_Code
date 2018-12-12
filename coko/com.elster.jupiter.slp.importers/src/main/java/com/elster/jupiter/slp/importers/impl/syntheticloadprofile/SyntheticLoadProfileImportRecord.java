/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.syntheticloadprofile;

import com.elster.jupiter.fileimport.csvimport.FileImportRecord;
import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.slp.importers.impl.MessageSeeds;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SyntheticLoadProfileImportRecord extends FileImportRecord {

    public SyntheticLoadProfileImportRecord() {
    }

    public SyntheticLoadProfileImportRecord(long lineNumber) {
        super(lineNumber);
    }

    private Optional<Instant> timeStamp;
    private Map<String, BigDecimal> syntheticLoadProfiles = new HashMap<>();

    public void addSyntheticLoadProfileValue(String syntheticLoadProfileName, BigDecimal value) {
        syntheticLoadProfiles.put(syntheticLoadProfileName, value);
    }

    public Map<String, BigDecimal> getSyntheticLoadProfiles() {
        return syntheticLoadProfiles;
    }

    public Instant getTimeStamp() {
        return timeStamp.orElseThrow(() -> new ProcessorException(MessageSeeds.CORRECTIONFACTOR_WRONG_TIMESTAMP, this.getLineNumber()));
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = Optional.ofNullable(timeStamp);
    }
}
