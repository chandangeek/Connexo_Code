/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.impl.ServerMeteringService;

/**
 * Provides factory services for {@link InstantTruncater}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-05 (11:59)
 */
public class InstantTruncaterFactory {
    private final ServerMeteringService meteringService;

    public InstantTruncaterFactory(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    InstantTruncater truncaterFor(ReadingType readingType) {
        if (VirtualReadingType.isGas(readingType.getCommodity())) {
            return this.meteringService
                    .getGasDayOptions()
                    .<InstantTruncater>map(GasDayTruncater::new)
                    .orElseGet(SimpleTruncater::new);
        } else {
            return new SimpleTruncater();
        }
    }

}