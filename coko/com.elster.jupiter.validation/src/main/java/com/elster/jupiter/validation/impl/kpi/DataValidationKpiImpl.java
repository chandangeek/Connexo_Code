package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.impl.MessageSeeds;
import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.DataValidationKpiScore;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@MustHaveUniqueEndDeviceGroup(message=MessageSeeds.Constants.DEVICE_GROUP_MUST_BE_UNIQUE, groups={Save.Create.class, Save.Update.class})
public class DataValidationKpiImpl implements DataValidationKpi, PersistenceAware {

    public enum Fields {
        DATA_VALIDATION_KPI("dataValidationKpi"),
        DATA_VALIDATION_KPI_TASK("dataValidationKpiTask"),
        END_DEVICE_GROUP("deviceGroup"),
        SUSPECT("SUSPECT_"),
        REGISTER("REGISTER_"),
        CHANNELS("CHANNELS_");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private final TaskService taskService;
    private final MessageService messageService;
    private final KpiService kpiService;
    private final Thesaurus thesaurus;

    private long id;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    private Reference<Kpi> dataValidationKpi = ValueReference.absent();
    private Reference<RecurrentTask> dataValidationKpiTask = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private Reference<EndDeviceGroup> deviceGroup = ValueReference.absent();
    @NotNull(message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private transient TemporalAmount frequency;
    private RecurrentTaskSaveStrategy recurrentTaskSaveStrategy = new CreateRecurrentTask(dataValidationKpi, dataValidationKpiTask, KpiType.VALIDATION);


    @Inject
    public DataValidationKpiImpl(DataModel dataModel, TaskService taskService, MessageService messageService, KpiService kpiService, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.messageService = messageService;
        this.kpiService = kpiService;
        this.thesaurus = thesaurus;
    }


    DataValidationKpiImpl initialize(EndDeviceGroup group) {
        if (group != null) {
            this.deviceGroup.set(group);
        } else {
            this.deviceGroup.setNull();
        }
        return this;
    }

    public void save(){
        if (this.getId() == 0) {
            Save.CREATE.save(this.dataModel, this);
        }
        this.recurrentTaskSaveStrategy.save();
        Save.UPDATE.save(this.dataModel, this);
    }

    void dataValidationKpiBuilder(KpiBuilder builder, String mRID){
        builder.interval(this.frequency);
        Stream.of(DataValidationKpiMembers.values())
                .map(DataValidationKpiMembers::fieldName)
                .forEach(member -> builder.member()
                        .named(member)
                        .add());
        builder.named(mRID);

        dataValidationKpi.set(builder.create());

    }

    public boolean hasDeviceGroup() {
        return this.deviceGroup.isPresent();
    }

    @Override
    public void postLoad() {
        this.recurrentTaskSaveStrategy = new UpdateRecurrentTask(this.dataValidationKpi, this.dataValidationKpiTask, KpiType.VALIDATION);
        Stream.of(this.dataValidationKpiCalculationIntervalLength())
                .flatMap(Functions.asStream())
                .findFirst()
                .ifPresent(temporalAmount -> this.frequency = temporalAmount);
    }

    @Override
    public long getId(){
        return this.id;
    }

    @Override
    public Optional<TemporalAmount> dataValidationKpiCalculationIntervalLength() {
        if (this.dataValidationKpi.isPresent()){
            return Optional.of(this.dataValidationKpi.get().getIntervalLength());
        }else{
            return Optional.empty();
        }
    }

    @Override
    public List<DataValidationKpiScore> getDataValidationKpiScores() {
        //FixMe will be implemented in next story CXO-1611;
        return Collections.emptyList();
    }

    @Override
    public void setFrequency(TemporalAmount intervalLength) {
        if (this.frequency!=null) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.CAN_NOT_CHANGE_FREQUENCY);
        }
        this.frequency = intervalLength;
    }

    @Override
    public TemporalAmount getFrequency() {
        return this.frequency;
    }

    @Override
    public EndDeviceGroup getDeviceGroup() {
        return this.deviceGroup.get();
    }

    @Override
    public void delete() {
        this.dataModel.remove(this);
        this.dataValidationKpiTask.getOptional().ifPresent(RecurrentTask::delete);
        this.dataValidationKpi.getOptional().ifPresent(Kpi::remove);
    }

    @Override
    public Optional<Instant> getLatestCalculation() {
        return Stream.of(dataValidationKpiTask).
                map(Reference::getOptional).
                flatMap(Functions.asStream()).
                map(RecurrentTask::getLastOccurrence).
                flatMap(Functions.asStream()).
                map(TaskOccurrence::getTriggerTime).
                max(Comparator.nullsLast(Comparator.<Instant>naturalOrder()));
    }

    @Override
    public void dropDataValidationKpi() {
        deleteDataValidationKpi();
        this.save();
    }

    @Override
    public Kpi getDataValidationKpi(){
        return this.dataValidationKpi.get();
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    private void deleteDataValidationKpi() {
        Optional<Kpi> kpi = dataValidationKpi.getOptional();
        kpi.ifPresent(x->dataValidationKpi.setNull());
        Optional<RecurrentTask> recurrentTask = dataValidationKpiTask.getOptional();
        recurrentTask.ifPresent(x->dataValidationKpiTask.setNull());
        this.save();
        kpi.ifPresent (Kpi::remove);
        recurrentTask.ifPresent(RecurrentTask::delete);
    }


    private interface RecurrentTaskSaveStrategy {
        void save();
    }

    private class CreateRecurrentTask implements RecurrentTaskSaveStrategy {
        private final Reference<Kpi> kpi;
        private final Reference<RecurrentTask> recurrentTask;
        private final KpiType kpiType;

        protected CreateRecurrentTask(Reference<Kpi> kpi, Reference<RecurrentTask> recurrentTask, KpiType kpiType) {
            super();
            this.kpi = kpi;
            this.recurrentTask = recurrentTask;
            this.kpiType = kpiType;
        }

        @Override
        public void save() {
            //
            if (this.kpi.isPresent()) {
                DestinationSpec destination = messageService.getDestinationSpec(DataValidationKpiCalculatorHandlerFactory.TASK_DESTINATION).get();
                RecurrentTask recurrentTask = taskService.newBuilder()
                        .setApplication("MultiSense")
                        .setName(taskName())
                        .setScheduleExpression(this.toScheduleExpression(this.kpi.get()))
                        .setDestination(destination)
                        .setPayLoad(scheduledExcutionPayload())
                        .scheduleImmediately(true)
                        .build();
                this.setRecurrentTask(recurrentTask);
            }
        }

        protected Optional<RecurrentTask> getRecurrentTask() {
            return this.recurrentTask.getOptional();
        }

        protected void setRecurrentTask(RecurrentTask recurrentTask) {
            this.recurrentTask.set(recurrentTask);
        }

        private ScheduleExpression toScheduleExpression(Kpi kpi) {
            if (kpi.getIntervalLength() instanceof Duration) {
                Duration duration = (Duration) kpi.getIntervalLength();
                return new TemporalExpression(new TimeDurationFromDurationFactory().from(duration));
            }
            else {
                Period period = (Period) kpi.getIntervalLength();
                return new TemporalExpression(new TimeDurationFromPeriodFactory().from(period));
            }
        }

        private String scheduledExcutionPayload() {
            return this.kpiType.recurrentPayload(DataValidationKpiImpl.this.getId());
        }

        private String taskName() {
            return this.kpiType.recurrentTaskName(deviceGroup.get().getId());
        }

    }

    private class UpdateRecurrentTask extends CreateRecurrentTask {
        protected UpdateRecurrentTask(Reference<Kpi> kpi, Reference<RecurrentTask> recurrentTask, KpiType kpiType) {
            super(kpi, recurrentTask, kpiType);
        }

        @Override
        public void save() {
            /* Todo: updating a RecurrentTask is not supported yet,
             * Deleting and re-creating results in a constraint violation, so looks like that is not an option.
              * Will only support adding a recurrentTask for newly added KPI at this point
             */
            if (!this.getRecurrentTask().isPresent()) {
                super.save();
            }
        }
    }


    private interface TimeDurationFactory {
        TimeDuration from(TemporalAmount temporalAmount);
    }

    private class TimeDurationFromDurationFactory implements TimeDurationFactory {
        private Stream<TimeDurationFactory> factories;

        private TimeDurationFromDurationFactory() {
            super();
            this.factories = Stream.of(
                    new TimeDurationFromDurationInHoursFactory(),
                    new TimeDurationFromDurationInMinutesFactory(),
                    new TimeDurationFromDurationInSecondsFactory());
        }

        @Override
        public TimeDuration from(TemporalAmount temporalAmount) {
            return this.factories
                    .map(f -> f.from(temporalAmount))
                    .filter(t -> t != null)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unable to convert Duration '" + temporalAmount + "' to TemporalExpression"));
        }

    }
    private class TimeDurationFromDurationInHoursFactory implements TimeDurationFactory {
        @Override
        public TimeDuration from(TemporalAmount temporalAmount) {
            return this.from((Duration) temporalAmount);
        }

        private TimeDuration from(Duration duration) {
            if (duration.toHours() != 0) {
                return TimeDuration.hours(Math.toIntExact(duration.toHours()));
            }
            else {
                return null;
            }
        }
    }

    private class TimeDurationFromDurationInMinutesFactory implements TimeDurationFactory {
        @Override
        public TimeDuration from(TemporalAmount temporalAmount) {
            return this.from((Duration) temporalAmount);
        }

        private TimeDuration from(Duration duration) {
            if (duration.toMinutes() != 0) {
                return TimeDuration.minutes(Math.toIntExact(duration.toMinutes()));
            }
            else {
                return null;
            }
        }
    }

    private class TimeDurationFromDurationInSecondsFactory implements TimeDurationFactory {
        @Override
        public TimeDuration from(TemporalAmount temporalAmount) {
            return this.from((Duration) temporalAmount);
        }

        private TimeDuration from(Duration duration) {
            if (duration.getSeconds() != 0) {
                return TimeDuration.seconds(Math.toIntExact(duration.getSeconds()));
            }
            else {
                return null;
            }
        }
    }

    private class TimeDurationFromPeriodFactory implements TimeDurationFactory {
        private Stream<TimeDurationFactory> factories;

        private TimeDurationFromPeriodFactory() {
            super();
            this.factories = Stream.of(
                    new TimeDurationFromPeriodValidatingFactory(),
                    new TimeDurationFromPeriodInMonthsFactory(),
                    new TimeDurationFromPeriodInDaysFactory());
        }

        @Override
        public TimeDuration from(TemporalAmount temporalAmount) {
            return this.factories
                    .map(f -> f.from(temporalAmount))
                    .filter(t -> t != null)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unable to convert Period '" + temporalAmount + "' to TemporalExpression"));
        }

    }

    private class TimeDurationFromPeriodValidatingFactory implements TimeDurationFactory {

        @Override
        public TimeDuration from(TemporalAmount temporalAmount) {
            return this.from((Period) temporalAmount);
        }

        private TimeDuration from(Period period) {
            if (period.getYears() != 0 || period.getMonths() != 0) {
                return this.noDays(period);
            }
            else {
                return null;
            }
        }

        private TimeDuration noDays(Period period) {
            if (period.getDays() != 0) {
                throw new IllegalArgumentException("Years and days or months and days are not supported");
            }
            else {
                return null;
            }
        }
    }

    private class TimeDurationFromPeriodInMonthsFactory implements TimeDurationFactory {
        @Override
        public TimeDuration from(TemporalAmount temporalAmount) {
            return this.from((Period) temporalAmount);
        }

        private TimeDuration from(Period period) {
            if (period.toTotalMonths() != 0) {
                return TimeDuration.months(Math.toIntExact(period.toTotalMonths()));
            }
            else {
                return null;
            }
        }
    }

    private class TimeDurationFromPeriodInDaysFactory implements TimeDurationFactory {
        @Override
        public TimeDuration from(TemporalAmount temporalAmount) {
            return this.from((Period) temporalAmount);
        }

        private TimeDuration from(Period period) {
            if (period.getDays() != 0) {
                return TimeDuration.days(Math.toIntExact(period.getDays()));
            }
            else {
                return null;
            }
        }
    }

}
