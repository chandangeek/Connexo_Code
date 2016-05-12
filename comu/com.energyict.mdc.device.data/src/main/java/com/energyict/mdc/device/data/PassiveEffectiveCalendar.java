package com.energyict.mdc.device.data;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface PassiveEffectiveCalendar extends HasId{

    Instant getActivationDate();

    AllowedCalendar getAllowedCalendar();

}
