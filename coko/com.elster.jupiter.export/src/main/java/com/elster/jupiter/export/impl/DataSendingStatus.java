/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataSendingStatus {
    private boolean failed;
    private Set<ReadingTypeDataExportItem> failedDataSourcesWithNewData;
    private Set<ReadingTypeDataExportItem> failedDataSourcesWithChangedData;
    private boolean allDataSourcesWithNewDataFailed;
    private boolean allDataSourcesWithChangedDataFailed;

    private DataSendingStatus() {
        failedDataSourcesWithNewData = new HashSet<>();
        failedDataSourcesWithChangedData = new HashSet<>();
    }

    public static DataSendingStatus success() {
        return new DataSendingStatus();
    }

    public static Builder failure() {
        DataSendingStatus result = new DataSendingStatus();
        result.failed = true;
        return result.new Builder();
    }

    public static Builder builder() {
        return new DataSendingStatus().new Builder();
    }

    public static DataSendingStatus merge(DataSendingStatus result1, DataSendingStatus result2) {
        DataSendingStatus result = new DataSendingStatus();
        result.failed = result1.isFailed() || result2.isFailed();
        result.allDataSourcesWithNewDataFailed = result1.allDataSourcesWithNewDataFailed || result2.allDataSourcesWithNewDataFailed;
        result.allDataSourcesWithChangedDataFailed = result1.allDataSourcesWithChangedDataFailed || result2.allDataSourcesWithChangedDataFailed;
        if (!result.allDataSourcesWithNewDataFailed) {
            result.failedDataSourcesWithNewData.addAll(result1.failedDataSourcesWithNewData);
            result.failedDataSourcesWithNewData.addAll(result2.failedDataSourcesWithNewData);
        }
        if (!result.allDataSourcesWithChangedDataFailed) {
            result.failedDataSourcesWithChangedData.addAll(result1.failedDataSourcesWithChangedData);
            result.failedDataSourcesWithChangedData.addAll(result2.failedDataSourcesWithChangedData);
        }
        return result;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isFailedForNewData(ReadingTypeDataExportItem item) {
        return allDataSourcesWithNewDataFailed || failedDataSourcesWithNewData.contains(item);
    }

    public boolean isFailedForChangedData(ReadingTypeDataExportItem item) {
        return allDataSourcesWithChangedDataFailed || failedDataSourcesWithChangedData.contains(item);
    }

    public void throwExceptionIfFailed(Thesaurus thesaurus) {
        if (isFailed()) {
            if (allDataSourcesWithNewDataFailed || allDataSourcesWithChangedDataFailed) {
                throw new DestinationFailedException(thesaurus, MessageSeeds.DATA_SENDING_FAILED_ALL_DATA_SOURCES);
            } else {
                int maxNumber = 10;
                Set<ReadingTypeDataExportItem> failedDataSources = new HashSet<>();
                failedDataSources.addAll(failedDataSourcesWithNewData);
                failedDataSources.addAll(failedDataSourcesWithChangedData);
                String dataSourcesString = failedDataSources.stream()
                        .map(dataSource -> '<' + dataSource.getDescription() + '>')
                        .sorted()
                        .limit(maxNumber)
                        .collect(Collectors.joining(", "));
                if (failedDataSources.size() > maxNumber) {
                    dataSourcesString = dataSourcesString + ", ...";
                }
                throw new DestinationFailedException(thesaurus, MessageSeeds.DATA_SENDING_FAILED_SPECIFIC_DATA_SOURCES, failedDataSources.size(), dataSourcesString);
            }
        }
    }

    public class Builder {
        public Builder withFailedDataSource(ReadingTypeDataExportItem item) {
            return withFailedDataSourceForNewData(item).withFailedDataSourceForChangedData(item);
        }

        public Builder withFailedDataSourceForNewData(ReadingTypeDataExportItem item) {
            failed = true;
            if (!allDataSourcesWithNewDataFailed) {
                failedDataSourcesWithNewData.add(item);
            }
            return this;
        }

        public Builder withFailedDataSourceForChangedData(ReadingTypeDataExportItem item) {
            failed = true;
            if (!allDataSourcesWithChangedDataFailed) {
                failedDataSourcesWithChangedData.add(item);
            }
            return this;
        }

        public Builder withFailedDataSources(Collection<ReadingTypeDataExportItem> items) {
            return withFailedDataSourcesForNewData(items).withFailedDataSourcesForChangedData(items);
        }

        public Builder withFailedDataSourcesForNewData(Collection<ReadingTypeDataExportItem> items) {
            if (!items.isEmpty()) {
                failed = true;
                if (!allDataSourcesWithNewDataFailed) {
                    failedDataSourcesWithNewData.addAll(items);
                }
            }
            return this;
        }

        public Builder withFailedDataSourcesForChangedData(Collection<ReadingTypeDataExportItem> items) {
            if (!items.isEmpty()) {
                failed = true;
                if (!allDataSourcesWithChangedDataFailed) {
                    failedDataSourcesWithChangedData.addAll(items);
                }
            }
            return this;
        }

        public Builder withAllDataSourcesFailed() {
            return withAllDataSourcesFailedForNewData().withAllDataSourcesFailedForChangedData();
        }

        public Builder withAllDataSourcesFailedForNewData() {
            failed = true;
            failedDataSourcesWithNewData.clear();
            allDataSourcesWithNewDataFailed = true;
            return this;
        }

        public Builder withAllDataSourcesFailedForChangedData() {
            failed = true;
            failedDataSourcesWithChangedData.clear();
            allDataSourcesWithChangedDataFailed = true;
            return this;
        }

        public DataSendingStatus build() {
            if (failed && !allDataSourcesWithNewDataFailed && !allDataSourcesWithChangedDataFailed
                    && failedDataSourcesWithNewData.isEmpty() && failedDataSourcesWithChangedData.isEmpty()) {
                allDataSourcesWithNewDataFailed = true;
                allDataSourcesWithChangedDataFailed = true;
            }
            return DataSendingStatus.this;
        }
    }
}
