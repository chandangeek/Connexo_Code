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

    DataExportTaskBuilder exportUpdate(boolean exportUpdate);

    DataExportTaskBuilder exportContinuousData(boolean exportContinuousData);

    ReadingTypeDataExportTask build();

    DataExportTaskBuilder setName(String string);

    DataExportTaskBuilder setDataProcessorName(String dataProcessor);

    DataExportTaskBuilder setDataSelectorName(String dataSelector);

    DataExportTaskBuilder setExportPeriod(RelativePeriod relativePeriod);

    DataExportTaskBuilder setUpdatePeriod(RelativePeriod relativePeriod);

    DataExportTaskBuilder addReadingType(ReadingType readingType);

    DataExportTaskBuilder addReadingType(String readingType);

    DataExportTaskBuilder setValidatedDataOption(ValidatedDataOption validatedDataOption);

    PropertyBuilder addProperty(String name);

    DataExportTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    interface PropertyBuilder {

        DataExportTaskBuilder withValue(Object value);

    }
}
