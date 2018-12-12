/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.kpi.DataCollectionKpi;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the {@link DataCollectionKpiCalculator} interface
 * for the situation in which the id of the {@link DataCollectionKpiImpl}
 * that was contained in the payload is not the id
 * of an existing DataCollectionKpiImpl.
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
public class DataCollectionKpiDoesNotExist implements DataCollectionKpiCalculator {
    private final Logger logger;
    private final String payLoad;

    public DataCollectionKpiDoesNotExist(Logger logger, String payLoad) {
        super();
        this.logger = logger;
        this.payLoad = payLoad;
    }

    @Override
    public void calculateAndStore() {
        this.logger.log(Level.SEVERE, "Payload '" + this.payLoad + "' does not contain the unique identifier of a " + DataCollectionKpi.class.getSimpleName());
    }

}