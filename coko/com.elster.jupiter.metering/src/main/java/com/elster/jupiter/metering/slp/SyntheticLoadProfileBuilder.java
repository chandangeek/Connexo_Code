/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.slp;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SyntheticLoadProfileBuilder {
    SyntheticLoadProfileBuilder withDescription(String description);

    SyntheticLoadProfile build();
}