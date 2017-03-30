/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

/**
 * Calculates and stores the scores for a {@link DataCollectionKpiImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-15 (17:13)
 */
public interface DataCollectionKpiCalculator {

    public void calculateAndStore();

}