/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.tasks.support;

import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;

import aQute.bnd.annotation.ProviderType;

/**
 * Defines functionality related to Calendar information.
 */
@ProviderType
public interface CalendarSupport {

    CollectedCalendar getCollectedCalendar();

}