/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-23 (11:54)
 */
public class UsagePointHasMeterOnThisRole extends LocalizedException {
    private Meter meterActiveOnRole;
    private MeterRole meterRole;
    private Range<Instant> conflictActivationRange;

    public UsagePointHasMeterOnThisRole (Thesaurus thesaurus, MessageSeed messageSeed, Meter meterActiveOnRole, MeterRole meterRole, Range<Instant> conflictActivationRange) {
        super(thesaurus, messageSeed, meterActiveOnRole.getName(), meterRole.getDisplayName());
        this.meterActiveOnRole = meterActiveOnRole;
        this.meterRole = meterRole;
        this.conflictActivationRange = conflictActivationRange;
    }

    public Meter getMeter() {
        return this.meterActiveOnRole;
    }

    public MeterRole getMeterRole() {
        return this.meterRole;
    }

    public Range<Instant> getConflictActivationRange() {
        return this.conflictActivationRange;
    }

}