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

public class DataSendingStatus {
    private boolean failed;
    private Set<ReadingTypeDataExportItem> failedDataSources;
    private boolean allDataSourcesFailed;

    private DataSendingStatus() {
        failedDataSources = new HashSet<>();
    }

    public static DataSendingStatus success() {
        return new DataSendingStatus();
    }

    public static DataSendingStatusBuilder failure() {
        DataSendingStatus result = new DataSendingStatus();
        result.failed = true;
        return result.new DataSendingStatusBuilder();
    }

    public static DataSendingStatusBuilder builder() {
        return new DataSendingStatus().new DataSendingStatusBuilder();
    }

    public static DataSendingStatus merge(DataSendingStatus result1, DataSendingStatus result2) {
        DataSendingStatus result = new DataSendingStatus();
        result.failed = result1.isFailed() || result2.isFailed();
        result.allDataSourcesFailed = result1.allDataSourcesFailed || result2.allDataSourcesFailed;
        if (!result.allDataSourcesFailed) {
            result.failedDataSources.addAll(result1.failedDataSources);
            result.failedDataSources.addAll(result2.failedDataSources);
        }
        return result;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isFailed(ReadingTypeDataExportItem item) {
        return allDataSourcesFailed || failedDataSources.contains(item);
    }

    public void throwExceptionIfFailed(Thesaurus thesaurus) {
        if (isFailed()) {
            if (allDataSourcesFailed) {
                throw new DestinationFailedException(thesaurus, MessageSeeds.DATA_SENDING_FAILED_ALL_DATA_SOURCES);
            } else {
                String dataSourcesString = failedDataSources.stream()
                        .map(dataSource -> '<' + dataSource.getDescription() + '>')
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining(", "));
                throw new DestinationFailedException(thesaurus, MessageSeeds.DATA_SENDING_FAILED_SPECIFIC_DATA_SOURCES, dataSourcesString);
            }
        }
    }

    public class DataSendingStatusBuilder {
        public DataSendingStatusBuilder withFailedDataSource(ReadingTypeDataExportItem item) {
            failed = true;
            if (!allDataSourcesFailed) {
                failedDataSources.add(item);
            }
            return this;
        }

        public DataSendingStatusBuilder withFailedDataSources(Collection<ReadingTypeDataExportItem> items) {
            if (!items.isEmpty()) {
                failed = true;
                if (!allDataSourcesFailed) {
                    failedDataSources.addAll(items);
                }
            }
            return this;
        }

        public DataSendingStatusBuilder withAllDataSourcesFailed() {
            failed = true;
            failedDataSources.clear();
            allDataSourcesFailed = true;
            return this;
        }

        public DataSendingStatus build() {
            if (failed && failedDataSources.isEmpty()) {
                allDataSourcesFailed = true;
            }
            return DataSendingStatus.this;
        }
    }
}
