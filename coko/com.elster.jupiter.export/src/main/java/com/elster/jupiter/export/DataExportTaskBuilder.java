/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

public interface DataExportTaskBuilder {

    DataExportTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    DataExportTaskBuilder setNextExecution(Instant nextExecution);

    DataExportTaskBuilder setApplication(String application);

    DataExportTaskBuilder scheduleImmediately();

    DataExportTaskBuilder setName(String string);

    DataExportTaskBuilder setDataFormatterFactoryName(String dataFormatter);

    MeterReadingSelectorBuilder selectingMeterReadings();

    UsagePointReadingSelectorBuilder selectingUsagePointReadings();

    EventSelectorBuilder selectingEventTypes();

    CustomSelectorBuilder selectingCustom(String dataSelector);

    PropertyBuilder<DataExportTaskBuilder> addProperty(String name);

    ExportTask create();

    interface PropertyBuilder<T> {

        T withValue(Object value);

    }

    interface CustomSelectorBuilder {

        PropertyBuilder<CustomSelectorBuilder> addProperty(String name);

        DataExportTaskBuilder endSelection();
    }

    interface MeterReadingSelectorBuilder {

        MeterReadingSelectorBuilder fromExportPeriod(RelativePeriod relativePeriod);

        MeterReadingSelectorBuilder fromUpdatePeriod(RelativePeriod relativePeriod);

        MeterReadingSelectorBuilder fromReadingType(ReadingType readingType);

        MeterReadingSelectorBuilder withValidatedDataOption(ValidatedDataOption validatedDataOption);

        MeterReadingSelectorBuilder fromEndDeviceGroup(EndDeviceGroup endDeviceGroup);

        MeterReadingSelectorBuilder exportUpdate(boolean exportUpdate);

        MeterReadingSelectorBuilder continuousData(boolean exportContinuousData);

        MeterReadingSelectorBuilder withUpdateWindow(RelativePeriod updateWindow);

        MeterReadingSelectorBuilder exportComplete(boolean exportComplete);

        DataExportTaskBuilder endSelection();
    }

    interface EventSelectorBuilder {

        EventSelectorBuilder fromExportPeriod(RelativePeriod relativePeriod);

        EventSelectorBuilder fromEventType(String filterCode);

        EventSelectorBuilder fromEndDeviceGroup(EndDeviceGroup endDeviceGroup);

        DataExportTaskBuilder endSelection();

    }

    interface UsagePointReadingSelectorBuilder {

        UsagePointReadingSelectorBuilder fromExportPeriod(RelativePeriod relativePeriod);

        UsagePointReadingSelectorBuilder fromUsagePointGroup(UsagePointGroup usagePointGroup);

        UsagePointReadingSelectorBuilder fromReadingType(ReadingType readingType);

        UsagePointReadingSelectorBuilder withValidatedDataOption(ValidatedDataOption validatedDataOption);

        UsagePointReadingSelectorBuilder continuousData(boolean exportContinuousData);

        UsagePointReadingSelectorBuilder exportComplete(boolean exportComplete);

        DataExportTaskBuilder endSelection();

    }
}
