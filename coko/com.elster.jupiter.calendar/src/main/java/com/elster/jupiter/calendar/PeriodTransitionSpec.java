/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ProviderType;

/**
 * Specifies when a transition from one {@link Period} to another occurs.
 * Transitions will automatically abut to avoid gaps in timeline of a full year.
 * A PeriodTransitionSpec therefore only specifies the Period to which it
 * transitions and not from which it is transitioning.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-18 (10:35)
 */
@ProviderType
public interface PeriodTransitionSpec {

    /**
     * The {@link Period} to which is being transitioned.
     *
     * @return The Period to which is being transitioned
     */
    Period getPeriod();

    Calendar getCalendar();

}