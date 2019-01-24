/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ZoneBuilder {

    ZoneBuilder withName(String name);

    ZoneBuilder withZoneType(ZoneType zoneType);

    Zone create();

}
