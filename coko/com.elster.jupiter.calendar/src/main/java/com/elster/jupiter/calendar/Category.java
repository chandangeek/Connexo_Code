/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

/**
 * A Category is intended as a cheap grouping mechanism for {@link Calendar}s.
 * A UI component that needs to ask the user which
 * holiday Calendar to use, could e.g. group all Calendars
 * of the "Holiday" Category together in a dropdown box.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-07 (12:39)
 */
@ProviderType
public interface Category extends HasId, HasName {
    void save();

    String getDisplayName();
}