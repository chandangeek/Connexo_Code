package com.energyict.mdc.device.data;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.AllowedCalendar;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface EffectiveCalendar extends HasId, Effectivity{

    AllowedCalendar getAllowedCalendar();
}
