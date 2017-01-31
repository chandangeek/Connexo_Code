/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.offline;

import com.energyict.mdc.common.Offline;

/**
 * Represents the offline version of a {@link com.elster.jupiter.calendar.Calendar}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-25 (16:27)
 */
public interface OfflineCalendar extends Offline {

    long getId();

    String getMRID();

    String getName();

    boolean isGhost();

}