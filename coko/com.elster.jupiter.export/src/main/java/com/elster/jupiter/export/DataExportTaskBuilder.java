/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface DataExportTaskBuilder {

    DataExportTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    DataExportTaskBuilder setNextExecution(Instant nextExecution);

    DataExportTaskBuilder setApplication(String application);

    DataExportTaskBuilder scheduleImmediately();

    DataExportTaskBuilder setName(String string);

    DataExportTaskBuilder setLogLevel(int logLevel);

    DataExportTaskBuilder setDataFormatterFactoryName(String dataFormatter);

    MeterReadingSelectorBuilder selectingMeterReadings();

    MeterReadingSelectorBuilder selectingMeterReadings(String selector);

    UsagePointReadingSelectorBuilder selectingUsagePointReadings();

    EventSelectorBuilder selectingEventTypes();

    CustomSelectorBuilder selectingCustom(String dataSelector);

    PropertyBuilder<DataExportTaskBuilder> addProperty(String name);

    DataExportTaskBuilder pairWith(ExportTask exportTask);

    ExportTask create();

    interface PropertyBuilder<T> {

        T withValue(Object value);

    }

    @ProviderType
    interface CustomSelectorBuilder {

        PropertyBuilder<CustomSelectorBuilder> addProperty(String name);

        DataExportTaskBuilder endSelection();
    }

    @ProviderType
    interface MeterReadingSelectorBuilder {

        MeterReadingSelectorBuilder fromExportPeriod(RelativePeriod relativePeriod);

        MeterReadingSelectorBuilder fromUpdatePeriod(RelativePeriod relativePeriod);

        MeterReadingSelectorBuilder fromReadingType(ReadingType readingType);

        MeterReadingSelectorBuilder withValidatedDataOption(ValidatedDataOption validatedDataOption);

        MeterReadingSelectorBuilder fromEndDeviceGroup(EndDeviceGroup endDeviceGroup);

        MeterReadingSelectorBuilder exportUpdate(boolean exportUpdate);

        MeterReadingSelectorBuilder continuousData(boolean exportContinuousData);

        MeterReadingSelectorBuilder withUpdateWindow(RelativePeriod updateWindow);

        MeterReadingSelectorBuilder exportComplete(MissingDataOption exportComplete);

        DataExportTaskBuilder endSelection();
    }

    @ProviderType
    interface EventSelectorBuilder {

        EventSelectorBuilder fromExportPeriod(RelativePeriod relativePeriod);

        EventSelectorBuilder fromEventType(String filterCode);

        EventSelectorBuilder fromEndDeviceGroup(EndDeviceGroup endDeviceGroup);

        DataExportTaskBuilder endSelection();

    }

    @ProviderType
    interface UsagePointReadingSelectorBuilder {

        UsagePointReadingSelectorBuilder fromExportPeriod(RelativePeriod relativePeriod);

        UsagePointReadingSelectorBuilder fromUpdatePeriod(RelativePeriod relativePeriod);

        UsagePointReadingSelectorBuilder fromUsagePointGroup(UsagePointGroup usagePointGroup);

        UsagePointReadingSelectorBuilder fromMetrologyPurpose(MetrologyPurpose metrologyPurpose);

        UsagePointReadingSelectorBuilder fromReadingType(ReadingType readingType);

        UsagePointReadingSelectorBuilder withValidatedDataOption(ValidatedDataOption validatedDataOption);

        UsagePointReadingSelectorBuilder exportUpdate(boolean exportUpdate);

        UsagePointReadingSelectorBuilder continuousData(boolean exportContinuousData);

        UsagePointReadingSelectorBuilder withUpdateWindow(RelativePeriod updateWindow);

        UsagePointReadingSelectorBuilder exportComplete(MissingDataOption exportComplete);

        DataExportTaskBuilder endSelection();

    }
}
