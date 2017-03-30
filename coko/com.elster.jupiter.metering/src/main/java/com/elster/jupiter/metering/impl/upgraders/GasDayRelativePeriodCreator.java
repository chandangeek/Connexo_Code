/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.time.TimeService;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Creates relative periods that take the {@link GasDayOptions} into account.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-17 (11:47)
 */
public class GasDayRelativePeriodCreator {
    private static final Logger LOGGER = Logger.getLogger(GasDayRelativePeriodCreator.class.getName());
    private final ServerMeteringService meteringService;
    private final TimeService timeService;

    public static void createAll(ServerMeteringService meteringService, TimeService timeService) {
        new GasDayRelativePeriodCreator(meteringService, timeService).createAll();
    }

    public GasDayRelativePeriodCreator(ServerMeteringService meteringService, TimeService timeService) {
        this.meteringService = meteringService;
        this.timeService = timeService;
    }

    public void createAll() {
        Optional<GasDayOptions> gasDayOptions = this.meteringService.getGasDayOptions();
        if (gasDayOptions.isPresent()) {
            this.createDefaultRelativePeriods(gasDayOptions.get());
        } else {
            LOGGER.info(() -> "Gas day options missing, skipping creation of gas day specific relative periods");
        }
    }

    private void createDefaultRelativePeriods(GasDayOptions gasDayOptions) {
        Arrays
                .stream(DefaultRelativePeriodDefinition.values())
                .forEach(definition -> definition.create(this.timeService, gasDayOptions));
    }

}