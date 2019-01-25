package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class ZoneInUseLocalizedException extends LocalizedException {
    public ZoneInUseLocalizedException (Thesaurus thesaurus, Zone zone) {
        super(thesaurus, MessageSeeds.ZONE_IN_USE, zone.getName());
    }
}
