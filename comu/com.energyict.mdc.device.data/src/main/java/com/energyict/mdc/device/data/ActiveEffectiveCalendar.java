package com.energyict.mdc.device.data;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface ActiveEffectiveCalendar extends Effectivity {
    Instant getLastVerifiedDate();

    AllowedCalendar getAllowedCalendar();

}
