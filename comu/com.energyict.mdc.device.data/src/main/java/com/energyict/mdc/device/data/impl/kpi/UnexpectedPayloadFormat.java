/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the {@link DataCollectionKpiCalculator} interface
 * for the situation in which the payload does not match the expected
 * format and can therefore not be parsed correctly.
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
public class UnexpectedPayloadFormat implements DataCollectionKpiCalculator {
    private final Logger logger;
    private final String payLoad;

    public UnexpectedPayloadFormat(Logger logger, String payLoad) {
        super();
        this.logger = logger;
        this.payLoad = payLoad;
    }

    @Override
    public void calculateAndStore() {
        this.logger.log(Level.SEVERE, "Payload '" + this.payLoad + "' is not of the expected format");
    }

}