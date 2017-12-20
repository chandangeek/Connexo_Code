/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.energyict.mdc.device.topology.impl.MessageSeeds;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiScore;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@MustHaveUniqueEndDeviceGroup(message= "{" + MessageSeeds.Keys.DEVICE_GROUP_MUST_BE_UNIQUE + "}", groups={Save.Create.class, Save.Update.class})
@MustHaveValidFrequency(message= "{" + MessageSeeds.Keys.FREQUENCY_MUST_BE_VALID + "}", groups={Save.Update.class})
public class RegisteredDevicesKpiImpl implements RegisteredDevicesKpi {

    private static final String REGISTERED_DEVICES_KPI_NAME_SUFFIX = " - Registered devices KPI";
    public static final String KPI_TOTAL_NAME = "total";
    public static final String KPI_REGISTERED_NAME = "registered";

    public enum Fields {
        KPI("kpi"),
        KPI_TASK("kpiTask"),
        END_DEVICE_GROUP("deviceGroup"),
        TARGET("target");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TaskService taskService;
    private final KpiService kpiService;

    private long id;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Min(value = 0, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.TARGET_MUST_BE_VALID + "}")
    @Max(value = 100, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.TARGET_MUST_BE_VALID + "}")
    private long target = 0;
    private Reference<Kpi> kpi = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<EndDeviceGroup> deviceGroup = ValueReference.absent();
    @NotNull(groups = {Save.Create.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private transient TemporalAmount frequency;
    private Reference<RecurrentTask> kpiTask = ValueReference.absent();
    private List<RecurrentTask> nextRecurrentTasks = new ArrayList<>();

    @Inject
    public RegisteredDevicesKpiImpl(DataModel dataModel, MessageService messageService, TaskService taskService, KpiService kpiService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.taskService = taskService;
        this.kpiService = kpiService;
    }

    RegisteredDevicesKpiImpl initialize(EndDeviceGroup group) {
        if (group != null) {
            this.deviceGroup.set(group);
        } else {
            this.deviceGroup.setNull();
        }
        return this;
    }

    @Override
    public void setTarget(long target) {
        this.target = target;
    }

    @Override
    public void save() {
        if (this.getId() == 0) {
            Save.CREATE.save(this.dataModel, this);
            newKpi();
            this.saveKpiAndTask();
        } else {
            this.saveTask();
        }
        Save.UPDATE.save(this.dataModel, this);
    }

    private void newKpi() {
        KpiBuilder builder = kpiService.newKpi();
        builder.interval(frequency);
        builder.member().named(KPI_TOTAL_NAME).add();
        builder.member().named(KPI_REGISTERED_NAME).add();
        this.kpi.set(builder.create());
    }

    private void saveKpiAndTask() {
        if (this.kpi.isPresent()) {
            DestinationSpec destination = messageService.getDestinationSpec(RegisteredDevicesKpiCalculatorFactory.TASK_DESTINATION).get();
            RecurrentTask recurrentTask = taskService.newBuilder()
                    .setApplication("MultiSense")
                    .setName(deviceGroup.get().getName() + REGISTERED_DEVICES_KPI_NAME_SUFFIX)
                    .setScheduleExpression(this.toScheduleExpression())
                    .setDestination(destination)
                    .setPayLoad(String.valueOf(this.getId()))
                    .scheduleImmediately(true)
                    .setNextRecurrentTasks(nextRecurrentTasks)
                    .build();
            kpiTask.set(recurrentTask);
        }
    }

    private void saveTask() {
        kpiTask.get().setNextRecurrentTasks(nextRecurrentTasks);
        kpiTask.get().save();
    }

    public Kpi kpi() {
        return kpi.get();
    }

    private ScheduleExpression toScheduleExpression() {
        TemporalAmount frequency = getFrequency();
        if (frequency instanceof Duration) {
            Duration duration = (Duration) frequency;
            return new TemporalExpression(new TimeDurationFactory.TimeDurationFromDurationFactory().from(duration));
        } else {
            Period period = (Period) frequency;
            return new TemporalExpression(new TimeDurationFactory.TimeDurationFromPeriodFactory().from(period));
        }
    }

    public void setFrequency(TemporalAmount frequency) {
        this.frequency = frequency;
    }

    @Override
    public TemporalAmount getFrequency() {
        return kpi().getIntervalLength();
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public EndDeviceGroup getDeviceGroup() {
        return deviceGroup.get();
    }

    @Override
    public long getTarget() {
        return target;
    }

    @Override
    public void delete() {
        this.dataModel.remove(this);
        this.kpiTask.getOptional().ifPresent(RecurrentTask::delete);
        this.kpi.getOptional().ifPresent(Kpi::remove);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public List<RegisteredDevicesKpiScore> getScores(Range<Instant> interval) {
        List<? extends KpiEntry> total;
        List<? extends KpiEntry> registered;
        boolean firstIsTotal = this.kpi().getMembers().get(0).getName().equals(KPI_TOTAL_NAME);
        total = this.kpi().getMembers().get(firstIsTotal ? 0 : 1).getScores(interval);
        registered = this.kpi().getMembers().get(firstIsTotal ? 1 : 0).getScores(interval);

        List<RegisteredDevicesKpiScore> scores = new ArrayList<>();
        for (int i = 0; i < total.size(); i++) {
            Instant timestamp = total.get(i).getTimestamp();
            scores.add(this.newScore(timestamp, total.get(i), registered.get(i)));
        }
        return scores;
    }

    @Override
    public Optional<Instant> getLatestCalculation() {
        return kpiTask.get().getLastOccurrence().map(TaskOccurrence::getTriggerTime);
    }

    @Override
    public void updateTarget(long target) {
        this.target = target;
        this.save();
    }

    @Override
    public List<RecurrentTask> getNextRecurrentTasks() {
        return kpiTask.getOptional()
                .map(recurrentTask -> recurrentTask.getNextRecurrentTasks())
                .orElse(Collections.emptyList());
    }

    @Override
    public List<RecurrentTask> getPrevRecurrentTasks() {
        return kpiTask.getOptional()
                .map(recurrentTask -> recurrentTask.getPrevRecurrentTasks())
                .orElse(Collections.emptyList());
    }

    @Override
    public void setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks) {
        this.nextRecurrentTasks = nextRecurrentTasks;

    }

    public boolean hasDeviceGroup() {
        return this.deviceGroup.isPresent();
    }

    private RegisteredDevicesKpiScore newScore(Instant timestamp, KpiEntry total, KpiEntry registered) {
        return new RegisteredDevicesKpiScoreImpl(timestamp, total.getScore(), registered.getScore());
    }
}
