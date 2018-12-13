/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi;

import aQute.bnd.annotation.ProviderType;

/**
 * Event triggered upon registering a score that does not meet its target.
 */
@ProviderType
public interface KpiMissEvent {

    /**
     * @return the KpiMember for which a score was registered.
     */
    KpiMember getMember();

    /**
     * @return the relevant KpiEntry
     */
    KpiEntry getEntry();
}
