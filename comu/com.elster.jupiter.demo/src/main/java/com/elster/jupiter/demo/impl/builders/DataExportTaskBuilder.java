package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.MeteringService;
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
import java.util.logging.Level;

public class DataExportTaskBuilder extends NamedBuilder<ExportTask, DataExportTaskBuilder> {
    private final DataExportService dataExportService;
    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
    private final TimeService timeService;

    private String group;

    @Inject
    public DataExportTaskBuilder(DataExportService dataExportService, MeteringService meteringService, MeteringGroupsService meteringGroupsService, TimeService timeService) {
        super(DataExportTaskBuilder.class);
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.dataExportService = dataExportService;
        this.timeService = timeService;
    }

    public DataExportTaskBuilder withGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public Optional<ExportTask> find() {
        Optional<? extends ExportTask> task = dataExportService.getReadingTypeDataExportTaskByName(getName());
        return Optional.ofNullable(task.orElse(null));
    }

    @Override
    public ExportTask create() {
        Log.write(this);
        Optional<RelativePeriod> yesterday = timeService.findRelativePeriodByName("Yesterday");
        if (!yesterday.isPresent()) {
            throw new UnableToCreate("Unable to find the relative yesterday period");
        }
        Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(group);
        if (!endDeviceGroup.isPresent()) {
            throw new UnableToCreate("Unable to find the device group with name " + group);
        }
        LocalDateTime startOn = LocalDateTime.now();
        startOn = startOn.withSecond(0).withMinute(0).withHour(11);
        com.elster.jupiter.export.DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(getName())
                .setLogLevel(Level.WARNING.intValue())
                .setApplication("MultiSense")
                .setDataFormatterFactoryName("standardCsvDataProcessorFactory")
                .setScheduleExpression(new TemporalExpression(TimeDuration.days(1), TimeDuration.hours(11)))
                .setNextExecution(startOn.toInstant(ZoneOffset.UTC))
                .selectingMeterReadings()
                .fromExportPeriod(yesterday.get())
                .fromUpdatePeriod(yesterday.get())
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .fromEndDeviceGroup(endDeviceGroup.get())
                .continuousData(false)
                .exportUpdate(true)
                .fromReadingType(meteringService.getReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0").get())
                .fromReadingType(meteringService.getReadingType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0").get())
                .endSelection();

        builder.addProperty("formatterProperties.separator").withValue("formatterProperties.separator.semicolon");
        builder.addProperty("formatterProperties.tag").withValue("new");
        builder.addProperty("formatterProperties.update.tag").withValue("update");
        ExportTask dataExportTask = builder.create();
        dataExportTask.addFileDestination("readings", String.format("%s-<identifier>-<date>-<time>", group.substring(0, group.indexOf(" "))), "csv");
        return dataExportTask;
    }
}
