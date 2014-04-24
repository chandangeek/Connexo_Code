package com.energyict.mdc.protocol.api.timezones;

import com.energyict.mdc.common.ApplicationComponent;

import java.util.List;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link TimeZoneInUse}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (10:42)
 */
public interface TimeZoneInUseFactory {

    public List<TimeZoneInUse> findAllTimeZoneInUses();

}