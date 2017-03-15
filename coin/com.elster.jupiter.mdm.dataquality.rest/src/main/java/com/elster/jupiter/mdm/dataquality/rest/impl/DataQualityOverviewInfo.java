/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;
import java.util.List;

public class DataQualityOverviewInfo {

    public String usagePointName;
    public String serviceCategory;
    public IdWithNameInfo metrologyConfiguration;
    public IdWithNameInfo metrologyContract;
    public boolean isEffectiveConfiguration;

    // Suspect readings
    public long channelSuspects;
    public long registerSuspects;
    public Instant lastSuspect;

    // Data quality
    public long amountOfSuspects;
    public long amountOfConfirmed;
    public long amountOfEstimates;
    public long amountOfInformatives;
    public long amountOfTotalEdited;

    // Edited values
    public long amountOfAdded;
    public long amountOfEdited;
    public long amountOfRemoved;

    // Type of suspects
    public List<NameValueInfo> suspectsPerValidator;

    // Type of estimates
    public List<NameValueInfo> estimatesPerEstimator;

    public static class NameValueInfo {

        public String name;
        public long value;

        public NameValueInfo(String name, long value) {
            this.name = name;
            this.value = value;
        }
    }
}