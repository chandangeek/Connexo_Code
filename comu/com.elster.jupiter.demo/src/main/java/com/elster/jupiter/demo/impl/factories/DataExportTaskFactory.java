package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class DataExportTaskFactory extends NamedFactory<DataExportTaskFactory, ReadingTypeDataExportTask> {
    private final DataExportService dataExportService;
    private final MeteringGroupsService meteringGroupsService;
    private final TimeService timeService;

    @Inject
    public DataExportTaskFactory(DataExportService dataExportService, MeteringGroupsService meteringGroupsService, TimeService timeService) {
        super(DataExportTaskFactory.class);
        this.meteringGroupsService = meteringGroupsService;
        this.dataExportService = dataExportService;
        this.timeService = timeService;
    }

    @Override
    public ReadingTypeDataExportTask get() {
        Log.write(this);
        Optional<RelativePeriod> yesterday = timeService.findRelativePeriodByName("Yesterday");
        if (!yesterday.isPresent()){
            throw new UnableToCreate("Unable to find the relative yesterday period");
        }
        Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(getName());
        if (!endDeviceGroup.isPresent()){
            throw new UnableToCreate("Unable to find the device group with name " + getName());
        }
        LocalDateTime startOn = LocalDateTime.now();
        startOn = startOn.withSecond(0).withMinute(0).withHour(11);
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(getName())
                .setDataProcessorName("standardCsvDataProcessorFactory")
                .setScheduleExpression(new TemporalExpression(TimeDuration.days(1), TimeDuration.hours(11)))
                .setNextExecution(startOn.toInstant(ZoneOffset.UTC))
                .setExportPeriod(yesterday.get())
                .setUpdatePeriod(yesterday.get())
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .setEndDeviceGroup(endDeviceGroup.get())
                .exportContinuousData(false)
                .exportUpdate(false);
        builder.addProperty("fileFormat.filenamePrefix").withValue("ConsumptionDataExporter_" + getName().replace(" ", ""));
        builder.addProperty("fileFormat.fileExtension").withValue("csv");
        builder.addProperty("formatterProperties.separator").withValue("comma");
        builder.addProperty("fileFormat.path").withValue("");
        builder.addReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        builder.addReadingType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingTypeDataExportTask dataExportTask = builder.build();
        dataExportTask.save();
        return dataExportTask;
    }
}
