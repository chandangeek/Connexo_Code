/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.protocol.api.device.offline.OfflineCalendar;

/**
 * Provides an implementation for the {@link OfflineCalendar} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-25 (16:42)
 */
public class OfflineCalendarImpl implements OfflineCalendar {
    private final boolean ghost;
    private final long id;
    private final String name;
    private final String mRID;

    public static OfflineCalendarImpl from(AllowedCalendar allowedCalendar) {
        if (allowedCalendar.isGhost()) {
            return fromGhost(allowedCalendar);
        } else {
            return fromActual(allowedCalendar);
        }
    }

    private static OfflineCalendarImpl fromActual(AllowedCalendar ghost) {
        Calendar calendar = ghost.getCalendar().get();
        return new OfflineCalendarImpl(false, calendar.getId(), ghost.getName(), calendar.getMRID());
    }

    private static OfflineCalendarImpl fromGhost(AllowedCalendar ghost) {
        return new OfflineCalendarImpl(true, ghost.getId(), ghost.getName(), ghost.getName());
    }

    private OfflineCalendarImpl(boolean ghost, long id, String name, String mRID) {
        this.ghost = ghost;
        this.id = id;
        this.name = name;
        this.mRID = mRID;
    }

    @Override
    public boolean isGhost() {
        return ghost;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

}