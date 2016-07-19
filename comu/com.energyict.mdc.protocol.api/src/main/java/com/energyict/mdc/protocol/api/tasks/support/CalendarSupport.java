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