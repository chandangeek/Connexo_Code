/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.MissingDataOption;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.export.processor.impl.CsvMeterDataFormatterFactory;
import com.elster.jupiter.export.processor.impl.CsvUsagePointDataFormatterFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.Group;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.logging.Level;

public class DataExportTaskBuilder extends NamedBuilder<ExportTask, DataExportTaskBuilder> {
    private static final String DEVICE_DESTINATION_FOLDER_NAME = "DeviceReadings";
    private static final String USAGE_POINT_DESTINATION_FOLDER_NAME = "UsagePointReadings";
    private static final String BULK_A_PLUS_WH = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
    private static final String BULK_A_MINUS_WH = "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0";
    private static final String DAILY_A_PLUS_KWH = "11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String DAILY_A_MINUS_KWH = "11.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";

    private final DataExportService dataExportService;
    private final MeteringService meteringService;
    private final TimeService timeService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final Clock clock;

    private EndDeviceGroup deviceGroup;
    private UsagePointGroup usagePointGroup;
    private MetrologyPurpose purpose;
    private ScheduleExpression scheduleExpression;
    private Instant nextExecution;

    @Inject
    public DataExportTaskBuilder(DataExportService dataExportService,
                                 MeteringService meteringService,
                                 TimeService timeService,
                                 MetrologyConfigurationService metrologyConfigurationService,
                                 Clock clock) {
        super(DataExportTaskBuilder.class);
        this.meteringService = meteringService;
        this.dataExportService = dataExportService;
        this.timeService = timeService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.clock = clock;
    }

    public DataExportTaskBuilder withDeviceGroup(EndDeviceGroup deviceGroup) {
        this.deviceGroup = deviceGroup;
        return this;
    }

    public DataExportTaskBuilder withUsagePointGroup(UsagePointGroup usagePointGroup) {
        this.usagePointGroup = usagePointGroup;
        return this;
    }

    public DataExportTaskBuilder withMetrologyPurpose(DefaultMetrologyPurpose purpose) {
        this.purpose = findMetrologyPurposeOrThrowException(purpose);
        return this;
    }

    public DataExportTaskBuilder withScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    public DataExportTaskBuilder withNextExecution() {
        ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), ZoneId.systemDefault());
        Optional<ZonedDateTime> nextOccurrence = scheduleExpression.nextOccurrence(now);
        this.nextExecution = nextOccurrence.map(ZonedDateTime::toInstant).orElse(null);
        return this;
    }

    @Override
    public Optional<ExportTask> find() {
        return dataExportService.getReadingTypeDataExportTaskByName(getName())
                .map(ExportTask.class::cast);
    }

    @Override
    public ExportTask create() {
        Log.write(this);
        RelativePeriod yesterday = findRelativePeriodOrThrowException(DefaultRelativePeriodDefinition.YESTERDAY);
        com.elster.jupiter.export.DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(getName())
                .setLogLevel(Level.WARNING.intValue())
                .setScheduleExpression(scheduleExpression)
                .setNextExecution(nextExecution);
        String preparedGroupName, folderName;
        if (deviceGroup != null) {
            folderName = DEVICE_DESTINATION_FOLDER_NAME;
            preparedGroupName = prepareName(deviceGroup);
            builder.setApplication("MultiSense")
                    .setDataFormatterFactoryName("standardCsvDataProcessorFactory")
                    .selectingMeterReadings()
                    .fromExportPeriod(yesterday)
                    .fromUpdatePeriod(yesterday)
                    .exportComplete(MissingDataOption.EXCLUDE_INTERVAL)
                    .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                    .fromEndDeviceGroup(deviceGroup)
                    .continuousData(false)
                    .exportUpdate(true)
                    .fromReadingType(findReadingTypeOrThrowException(BULK_A_PLUS_WH))
                    .fromReadingType(findReadingTypeOrThrowException(BULK_A_MINUS_WH))
                    .endSelection();
        } else if (usagePointGroup != null) {
            folderName = USAGE_POINT_DESTINATION_FOLDER_NAME;
            preparedGroupName = prepareName(usagePointGroup);
            builder.setApplication("Insight")
                    .setDataFormatterFactoryName("csvUsagePointDataProcessorFactory")
                    .selectingUsagePointReadings()
                    .fromExportPeriod(yesterday)
                    .fromUpdatePeriod(yesterday)
                    .exportComplete(MissingDataOption.EXCLUDE_INTERVAL)
                    .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                    .fromUsagePointGroup(usagePointGroup)
                    .fromMetrologyPurpose(purpose)
                    .continuousData(false)
                    .exportUpdate(true)
                    .fromReadingType(findReadingTypeOrThrowException(DAILY_A_PLUS_KWH))
                    .fromReadingType(findReadingTypeOrThrowException(DAILY_A_MINUS_KWH))
                    .endSelection();
        } else {
            throw new UnableToCreate("Neither device group nor usage point group is provided.");
        }

        builder.addProperty("formatterProperties.separator").withValue("formatterProperties.separator.semicolon")
                .addProperty("formatterProperties.tag").withValue("new")
                .addProperty("formatterProperties.update.tag").withValue("update");
        ExportTask dataExportTask = builder.create();
        dataExportTask.addFileDestination(folderName, String.format("%s-<identifier>-<date>-<time>", preparedGroupName), "csv");
        return dataExportTask;
    }

    private RelativePeriod findRelativePeriodOrThrowException(DefaultRelativePeriodDefinition period) {
        String name = period.getPeriodName();
        return timeService.findRelativePeriodByName(name)
                .orElseThrow(() -> new UnableToCreate("Unable to find relative period '" + name + "'."));
    }

    private ReadingType findReadingTypeOrThrowException(String mRID) {
        return meteringService.getReadingType(mRID)
                .orElseThrow(() -> new UnableToCreate("Unable to find reading type '" + mRID + "'."));
    }

    private MetrologyPurpose findMetrologyPurposeOrThrowException(DefaultMetrologyPurpose purpose) {
        return metrologyConfigurationService.findMetrologyPurpose(purpose)
                .orElseThrow(() -> new UnableToCreate("Unable to find metrology purpose '" + purpose.getName() + "'."));
    }

    private static String prepareName(Group<?> group) {
        return group.getName().replace(' ', '_');
    }
}
