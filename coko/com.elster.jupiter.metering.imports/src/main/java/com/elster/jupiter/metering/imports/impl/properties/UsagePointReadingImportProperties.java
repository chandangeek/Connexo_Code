/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.properties;

import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;

public class UsagePointReadingImportProperties {

    private MeteringDataImporterContext context;
    private DataAggregationService dataAggregationService;
    private String delimiter;
    private String dateFormat;
    private String timeZone;
    private SupportedNumberFormat numberFormat;

    private UsagePointReadingImportProperties() {

    }

    public MeteringDataImporterContext getContext() {
        return context;
    }

    public DataAggregationService getDataAggregationService() {
        return dataAggregationService;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public SupportedNumberFormat getNumberFormat() {
        return numberFormat;
    }

    public static class UsagePointReadingImportPropertiesBuilder {
        private MeteringDataImporterContext context;
        private DataAggregationService dataAggregationService;
        private String delimiter;
        private String dateFormat;
        private String timeZone;
        private SupportedNumberFormat numberFormat;

        public UsagePointReadingImportPropertiesBuilder withMeteringDataImpContext(MeteringDataImporterContext context) {
            this.context = context;
            return this;
        }

        public UsagePointReadingImportPropertiesBuilder withDataAggregServ(DataAggregationService dataAggregationService) {
            this.dataAggregationService = dataAggregationService;
            return this;
        }

        public UsagePointReadingImportPropertiesBuilder withDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public UsagePointReadingImportPropertiesBuilder withDateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public UsagePointReadingImportPropertiesBuilder withTimeZone(String timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public UsagePointReadingImportPropertiesBuilder withNumberFormat(SupportedNumberFormat numberFormat) {
            this.numberFormat = numberFormat;
            return this;
        }

        public UsagePointReadingImportProperties build() {
            UsagePointReadingImportProperties usagePointReadingImportProperties = new UsagePointReadingImportProperties();
            usagePointReadingImportProperties.context = context;
            usagePointReadingImportProperties.dataAggregationService = dataAggregationService;
            usagePointReadingImportProperties.delimiter = delimiter;
            usagePointReadingImportProperties.dateFormat = dateFormat;
            usagePointReadingImportProperties.timeZone = timeZone;
            usagePointReadingImportProperties.numberFormat = numberFormat;
            return usagePointReadingImportProperties;
        }
    }
}
