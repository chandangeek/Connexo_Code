/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import com.elster.jupiter.util.time.Interval;

/**
 * Serves as the super class for classes that will implement
 * the {@link PersistentDomainExtension} interface for
 * versioned custom property sets, to make sure that
 * all the required fields as described by {@link HardCodedFieldNames}
 * are correctly provided to the {@link CustomPropertySetService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-12 (13:36)
 */
public abstract class AbstractVersionedPersistentDomainExtension extends AbstractPersistentDomainExtension {
    private Interval interval;

    protected Interval getInterval() {
        return interval;
    }

    protected void setInterval(Interval interval) {
        this.interval = interval;
    }

}