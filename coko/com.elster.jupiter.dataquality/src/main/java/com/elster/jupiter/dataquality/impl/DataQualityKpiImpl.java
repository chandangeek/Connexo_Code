/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.DataQualityKpi;
import com.elster.jupiter.dataquality.impl.calc.DataQualityKpiCalculatorHandlerFactory;
import com.elster.jupiter.dataquality.impl.calc.DataQualityKpiMemberType;
import com.elster.jupiter.dataquality.impl.calc.KpiType;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.kpi.KpiUpdater;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Predicates.not;

public abstract class DataQualityKpiImpl implements HasId, DataQualityKpi, PersistenceAware {

    public enum Fields {
        KPI_MEMBERS("kpiMembers"),
        DATA_QUALITY_KPI_TASK("dataQualityKpiTask"),
        OBSOLETE_TIME("obsoleteTime");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused") // Managed by ORM
    private long id;

    private Instant obsoleteTime;

    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;

    private List<DataQualityKpiMember> kpiMembers = new ArrayList<>();

    private Reference<RecurrentTask> dataQualityKpiTask = ValueReference.absent();

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private transient TemporalAmount frequency;

    private final DataModel dataModel;
    private final MeteringService meteringService;
    private final ValidationService validationService;
    private final EstimationService estimationService;
    private final MessageService messageService;
    private final TaskService taskService;
    private final KpiService kpiService;
    private final Clock clock;

    @Inject
    public DataQualityKpiImpl(DataModel dataModel, MeteringService meteringService, ValidationService validationService,
                              EstimationService estimationService, MessageService messageService, TaskService taskService,
                              KpiService kpiService, Clock clock) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.messageService = messageService;
        this.taskService = taskService;
        this.kpiService = kpiService;
        this.clock = clock;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public TemporalAmount getFrequency() {
        return frequency;
    }

    List<DataQualityKpiMember> getKpiMembers() {
        return kpiMembers;
    }

    void setFrequency(TemporalAmount frequency) {
        this.frequency = frequency;
    }

    @Override
    public void postLoad() {
        this.frequency = this.dataQualityKpiTask.getOptional()
                .map(RecurrentTask::getScheduleExpression)
                .map(TemporalExpression.class::cast)
                .map(TemporalExpression::getEvery)
                .map(TimeDuration::asTemporalAmount)
                .orElse(null);
    }

    public void save() {
        if (this.getId() != 0) {
            throw new IllegalStateException("Update is not supported");
        }
        Save.CREATE.save(this.dataModel, this);
        this.dataQualityKpiTask.set(createNewRecurrentTask());
        this.dataModel.update(this, Fields.DATA_QUALITY_KPI_TASK.fieldName());
    }

    @Override
    public void delete() {
        this.kpiMembers.forEach(DataQualityKpiMember::remove);
        this.dataModel.remove(this);
        this.dataQualityKpiTask.get().delete();
    }

    abstract KpiType getKpiType();

    abstract String getRecurrentTaskName();

    @Override
    public Optional<Instant> getLatestCalculation() {
        return this.dataQualityKpiTask.map(RecurrentTask::getLastRun).orElse(Optional.empty());
    }

    public boolean isCancelled() {
        Instant nextExecution = dataQualityKpiTask.getOptional()
                .map(RecurrentTask::getId)
                .flatMap(taskService::getRecurrentTask)
                .map(RecurrentTask::getNextExecution)
                .orElse(null);
        return nextExecution == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataQualityKpiImpl that = (DataQualityKpiImpl) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    DataModel getDataModel() {
        return dataModel;
    }

    KpiService getKpiService() {
        return kpiService;
    }

    MeteringService getMeteringService() {
        return meteringService;
    }

    ValidationService getValidationService() {
        return validationService;
    }

    EstimationService getEstimationService() {
        return estimationService;
    }

    Clock getClock() {
        return clock;
    }

    private RecurrentTask createNewRecurrentTask() {
        DestinationSpec destination = this.messageService.getDestinationSpec(DataQualityKpiCalculatorHandlerFactory.TASK_DESTINATION).get();
        return this.taskService.newBuilder()
                .setApplication(getKpiType().recurrentTaskApplicationName())
                .setName(getRecurrentTaskName())
                .setScheduleExpression(toScheduleExpression())
                .setDestination(destination)
                .setPayLoad(scheduledExecutionPayload())
                .scheduleImmediately(true)
                .build();
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

    private String scheduledExecutionPayload() {
        return getKpiType().recurrentPayload(getId());
    }

    DataQualityKpiMemberImpl createDataQualityKpiMember(String identifier, ZoneId zoneId) {
        KpiBuilder kpiBuilder = this.kpiService.newKpi();
        kpiBuilder.interval(getFrequency());
        kpiBuilder.timeZone(zoneId);

        actualKpiMemberTypes()
                .map(DataQualityKpiMemberType::getName)
                .map(member -> member.toUpperCase() + "_" + identifier)
                .forEach(member -> kpiBuilder.member().named(member).add());

        DataQualityKpiMemberImpl dataQualityKpiMember = DataQualityKpiMemberImpl.from(this.dataModel, this, kpiBuilder.create());
        this.kpiMembers.add(dataQualityKpiMember);
        return dataQualityKpiMember;
    }

    void updateKpiMemberIfNeeded(DataQualityKpiMember dataQualityKpiMember) {
        Kpi kpi = dataQualityKpiMember.getChildKpi();
        Set<String> existingKpiMemberNames = kpi.getMembers().stream().map(KpiMember::getName).collect(Collectors.toSet());
        String identifier = dataQualityKpiMember.getTargetIdentifier();
        List<String> membersToCreate = actualKpiMemberTypes()
                .map(DataQualityKpiMemberType::getName)
                .map(member -> member.toUpperCase() + "_" + identifier)
                .filter(not(existingKpiMemberNames::contains))
                .collect(Collectors.toList());
        if (!membersToCreate.isEmpty()) {
            KpiUpdater kpiUpdater = kpi.startUpdate();
            membersToCreate.forEach(name -> kpiUpdater.member().named(name).add());
            kpiUpdater.update();
        }
    }

    Stream<DataQualityKpiMemberType> actualKpiMemberTypes() {
        Stream<DataQualityKpiMemberType> predefined = Stream.of(DataQualityKpiMemberType.PredefinedKpiMemberType.values());
        Stream<DataQualityKpiMemberType> validators = getValidationService().getAvailableValidators(QualityCodeSystem.MDC)
                .stream().map(DataQualityKpiMemberType.ValidatorKpiMemberType::new);
        Stream<DataQualityKpiMemberType> estimators = getEstimationService().getAvailableEstimators(QualityCodeSystem.MDC)
                .stream().map(DataQualityKpiMemberType.EstimatorKpiMemberType::new);
        return Stream.of(predefined, estimators, validators).flatMap(Function.identity());
    }

    @Override
    public void makeObsolete() {
        RecurrentTask recurrentTask = this.dataQualityKpiTask.get();
        this.dataQualityKpiTask.setNull();
        this.obsoleteTime = clock.instant();
        this.dataModel.update(this,
                Fields.OBSOLETE_TIME.fieldName(),
                Fields.DATA_QUALITY_KPI_TASK.fieldName());
        recurrentTask.delete();
    }

    @Override
    public Optional<Instant> getObsoleteTime() {
        return Optional.ofNullable(this.obsoleteTime);
    }
}
