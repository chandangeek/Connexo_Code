package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
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

import static com.elster.jupiter.util.conditions.Where.where;

public class DataExportTaskBuilder extends NamedBuilder<ExportTask, DataExportTaskBuilder> {
    private final DataExportService dataExportService;
    private final MeteringGroupsService meteringGroupsService;
    private final TimeService timeService;

    private String group;

    @Inject
    public DataExportTaskBuilder(DataExportService dataExportService, MeteringGroupsService meteringGroupsService, TimeService timeService) {
        super(DataExportTaskBuilder.class);
        this.meteringGroupsService = meteringGroupsService;
        this.dataExportService = dataExportService;
        this.timeService = timeService;
    }

    public DataExportTaskBuilder withGroup(String group){
        this.group = group;
        return this;
    }

    @Override
    public Optional<ExportTask> find() {
        Optional<? extends ExportTask> task = dataExportService.getReadingTypeDataExportTaskQuery().select(where("name").isEqualTo(getName())).stream().findFirst();
        return Optional.ofNullable((ExportTask) task.orElse(null));
    }

    @Override
    public ExportTask create() {
        Log.write(this);
        Optional<RelativePeriod> yesterday = timeService.findRelativePeriodByName("Yesterday");
        if (!yesterday.isPresent()){
            throw new UnableToCreate("Unable to find the relative yesterday period");
        }
        Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(group);
        if (!endDeviceGroup.isPresent()){
            throw new UnableToCreate("Unable to find the device group with name " + group);
        }
        LocalDateTime startOn = LocalDateTime.now();
        startOn = startOn.withSecond(0).withMinute(0).withHour(11);
        com.elster.jupiter.export.DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(getName())
                .setDataFormatterName("standardCsvDataProcessorFactory")
                .setScheduleExpression(new TemporalExpression(TimeDuration.days(1), TimeDuration.hours(11)))
                .setNextExecution(startOn.toInstant(ZoneOffset.UTC))
                .selectingStandard()
                .fromExportPeriod(yesterday.get())
                .fromUpdatePeriod(yesterday.get())
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .fromEndDeviceGroup(endDeviceGroup.get())
                .continuousData(false)
                .exportUpdate(false)
                .fromReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .fromReadingType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .endSelection();
        builder.addProperty("formatterProperties.separator").withValue("comma");
        ExportTask dataExportTask = builder.build();
        dataExportTask.save();
        return dataExportTask;
    }
}
