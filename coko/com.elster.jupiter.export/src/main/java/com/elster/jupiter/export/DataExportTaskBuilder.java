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

    ExportTask create();

    DataExportTaskBuilder setName(String string);

    DataExportTaskBuilder setDataFormatterFactoryName(String dataFormatter);

    ReadingTypeSelectorBuilder selectingReadingTypes();

    EventSelectorBuilder selectingEventTypes();

    AggregatedDataSelectorBuilder selectingAggregatedData();

    CustomSelectorBuilder selectingCustom(String dataSelector);

    PropertyBuilder<DataExportTaskBuilder> addProperty(String name);

    interface PropertyBuilder<T> {

        T withValue(Object value);

    }

    interface CustomSelectorBuilder {
        PropertyBuilder<CustomSelectorBuilder> addProperty(String name);

        DataExportTaskBuilder endSelection();
    }

    interface ReadingTypeSelectorBuilder {

        ReadingTypeSelectorBuilder fromExportPeriod(RelativePeriod relativePeriod);

        ReadingTypeSelectorBuilder fromUpdatePeriod(RelativePeriod relativePeriod);

        ReadingTypeSelectorBuilder fromReadingType(ReadingType readingType);

        ReadingTypeSelectorBuilder fromReadingType(String readingType);

        ReadingTypeSelectorBuilder withValidatedDataOption(ValidatedDataOption validatedDataOption);

        ReadingTypeSelectorBuilder fromEndDeviceGroup(EndDeviceGroup endDeviceGroup);

        ReadingTypeSelectorBuilder exportUpdate(boolean exportUpdate);

        ReadingTypeSelectorBuilder continuousData(boolean exportContinuousData);

        DataExportTaskBuilder endSelection();

        ReadingTypeSelectorBuilder withUpdateWindow(RelativePeriod updateWindow);

        ReadingTypeSelectorBuilder exportComplete(boolean exportComplete);
    }

    interface EventSelectorBuilder {

        EventSelectorBuilder fromExportPeriod(RelativePeriod relativePeriod);

        EventSelectorBuilder fromEventType(String filterCode);

        EventSelectorBuilder fromEndDeviceGroup(EndDeviceGroup endDeviceGroup);

        DataExportTaskBuilder endSelection();

    }

    interface AggregatedDataSelectorBuilder {

        AggregatedDataSelectorBuilder fromExportPeriod(RelativePeriod relativePeriod);

        AggregatedDataSelectorBuilder fromUsagePointGroup(UsagePointGroup usagePointGroup);

        AggregatedDataSelectorBuilder fromReadingType(ReadingType readingType);

        AggregatedDataSelectorBuilder withValidatedDataOption(ValidatedDataOption validatedDataOption);

        AggregatedDataSelectorBuilder continuousData(boolean exportContinuousData);

        AggregatedDataSelectorBuilder exportComplete(boolean exportComplete);

        DataExportTaskBuilder endSelection();

    }
}
