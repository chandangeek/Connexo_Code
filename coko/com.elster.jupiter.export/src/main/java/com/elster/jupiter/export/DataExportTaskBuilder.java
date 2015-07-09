package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

public interface DataExportTaskBuilder {

    DataExportTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    DataExportTaskBuilder setNextExecution(Instant nextExecution);

    DataExportTaskBuilder scheduleImmediately();

    ExportTask build();

    DataExportTaskBuilder setName(String string);

    DataExportTaskBuilder setDataFormatterName(String dataFormatter);

    StandardSelectorBuilder selectingStandard();

    CustomSelectorBuilder selectingCustom(String dataSelector);

    PropertyBuilder<DataExportTaskBuilder> addProperty(String name);

    interface PropertyBuilder<T> {

        T withValue(Object value);

    }

    interface CustomSelectorBuilder {
        PropertyBuilder<CustomSelectorBuilder> addProperty(String name);

        DataExportTaskBuilder endSelection();
    }

    interface StandardSelectorBuilder {

        StandardSelectorBuilder fromExportPeriod(RelativePeriod relativePeriod);

        StandardSelectorBuilder fromUpdatePeriod(RelativePeriod relativePeriod);

        StandardSelectorBuilder fromReadingType(ReadingType readingType);

        StandardSelectorBuilder fromReadingType(String readingType);

        StandardSelectorBuilder withValidatedDataOption(ValidatedDataOption validatedDataOption);

        StandardSelectorBuilder fromEndDeviceGroup(EndDeviceGroup endDeviceGroup);

        StandardSelectorBuilder exportUpdate(boolean exportUpdate);

        StandardSelectorBuilder continuousData(boolean exportContinuousData);

        DataExportTaskBuilder endSelection();
    }
}
