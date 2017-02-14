/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.time.Instant;

public class DataSourceInfo {

    @JsonUnwrapped
    public DataSource details;
    public ReadingTypeInfo readingType;
    public Instant lastExportedDate;
    public Long occurrenceId;

    public static class DataSource {

        public String name;

    }

    public static class MeterDataSource extends DataSource {

        public String serialNumber;

    }

    public static class UsagePointDataSource extends DataSource {

        public String connectionState;

    }
}
