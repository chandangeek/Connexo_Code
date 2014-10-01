package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.RelativePeriod;

public interface DataExportTaskBuilder {

    DataExportTaskBuilder setCronExpression(String expression);

    DataExportTaskBuilder scheduleImmediately();

    DataExportTask build();

	DataExportTaskBuilder setName(String string);

    DataExportTaskBuilder setDataFormatter(DataFormatterFactory dataFormatterFactory);

    DataExportTaskBuilder setRelativePeriod(RelativePeriod relativePeriod);

    DataExportTaskBuilder addReadingType(ReadingType readingType);

    PropertyBuilder addProperty(String name);

    interface PropertyBuilder {

        DataExportTaskBuilder withValue(Object value);

    }
}
