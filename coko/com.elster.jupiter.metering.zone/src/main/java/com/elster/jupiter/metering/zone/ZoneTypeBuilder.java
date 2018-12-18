/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ZoneTypeBuilder {

    ZoneTypeBuilder withName(String name);

    ZoneTypeBuilder withApplication(String application);

    ZoneType create();

}
