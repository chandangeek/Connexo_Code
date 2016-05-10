package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface PassiveEffectiveCalendar extends EffectiveCalendar{

    Instant getActivationDate();
}
