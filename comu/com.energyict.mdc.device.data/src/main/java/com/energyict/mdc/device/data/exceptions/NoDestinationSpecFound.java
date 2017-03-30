/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.util.function.Supplier;

/**
 * Models the situation where one tries to get a DestinationSpec which is unknown to the system ...
 */
public class NoDestinationSpecFound extends LocalizedException implements Supplier<NoDestinationSpecFound>{

    public NoDestinationSpecFound(Thesaurus thesaurus, String destinationSpec) {
        super(thesaurus, MessageSeeds.NO_DESTINATION_SPEC_FOUND, destinationSpec);
        this.set("destinationSpec", destinationSpec);
    }

    @Override
    public NoDestinationSpecFound get() {
        return this;
    }
}
