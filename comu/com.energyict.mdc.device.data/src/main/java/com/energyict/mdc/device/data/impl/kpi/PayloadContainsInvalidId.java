/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the {@link DataCollectionKpiCalculator} interface
 * for the situation in which the payload could not be parsed
 * to extract the id of the {@link DataCollectionKpiImpl}.
 * <br>
 * This situation is an indication that the DataCollectionKpiImpl
 * component is putting the wrong payload in the recurrent task
 * at creation time.
 * <br>
 * Instead of actually calculating and storing,
 * it will log the failure to extract the id.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-15 (17:29)
 */
public class PayloadContainsInvalidId implements DataCollectionKpiCalculator {
    private final Logger logger;
    private final String payLoad;
    private final NumberFormatException exception;

    public PayloadContainsInvalidId(Logger logger, String payLoad, NumberFormatException exception) {
        super();
        this.logger = logger;
        this.payLoad = payLoad;
        this.exception = exception;
    }

    @Override
    public void calculateAndStore() {
        this.logger.log(Level.SEVERE, "The data collection kpi identifier in the payload '" + this.payLoad + "' could not be parsed to long", this.exception);
    }

}