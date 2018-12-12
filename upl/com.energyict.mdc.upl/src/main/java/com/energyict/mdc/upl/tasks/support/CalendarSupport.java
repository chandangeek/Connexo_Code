package com.energyict.mdc.upl.tasks.support;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;

/**
 * Defines functionality related to Calendar information.
 */
@ProviderType
public interface CalendarSupport {

    CollectedCalendar getCollectedCalendar();

}