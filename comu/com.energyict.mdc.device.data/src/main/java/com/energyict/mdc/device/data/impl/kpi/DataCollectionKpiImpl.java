package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiMember;
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
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.Range;

import com.energyict.mdc.device.data.exceptions.CannotReplaceExistingKPI;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link DataCollectionKpi} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-06 (10:25)
 */
@MustHaveEitherConnectionSetupOrComTaskExecution(groups = {Save.Update.class})
public class DataCollectionKpiImpl implements DataCollectionKpi, PersistenceAware {

    public enum Fields {
        CONNECTION_KPI("connectionKpi"),
        COMMUNICATION_KPI("communicationKpi"),
        CONNECTION_RECURRENT_TASK("connectionKpiTask"),
        COMMUNICATION_RECURRENT_TASK("communicationKpiTask"),
        END_DEVICE_GROUP("endDeviceGroup");

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
    private Reference<Kpi> connectionKpi = ValueReference.absent();
    private Reference<Kpi> communicationKpi = ValueReference.absent();
    private CompositeKpiSaveStrategy kpiSaveStrategy = new KpiSaveStrategyAtCreationTime();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_GROUP_IS_REQUIRED + "}")
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();
    private Reference<RecurrentTask> connectionKpiTask = ValueReference.absent();
    private Reference<RecurrentTask> communicationKpiTask = ValueReference.absent();
    private RecurrentTaskSaveStrategy recurrentTaskSaveStrategy = new CreateAllSchedules();

    @Inject
    public DataCollectionKpiImpl(DataModel dataModel, TaskService taskService, MessageService messageService, KpiService kpiService, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.messageService = messageService;
        this.kpiService = kpiService;
        this.thesaurus = thesaurus;
    }

    DataCollectionKpiImpl initialize(EndDeviceGroup group) {
        this.endDeviceGroup.set(group);
        return this;
    }

    public void save() {
        if (this.getId() == 0) {
            // Save myself first (without validation) so that the payload of the recurrent task can contain my ID
            Save.CREATE.save(this.dataModel, this);
        }
        // Now save the KPIs and the recurrent task
        this.kpiSaveStrategy.save();
        this.recurrentTaskSaveStrategy.save();
        // Update myself (with validation this time) to set the KPIs and the recurrent task
        Save.UPDATE.save(this.dataModel, this);
    }

    @Override
    public void postLoad() {
        this.kpiSaveStrategy = new KpiSaveStrategyForUpdate();
        this.recurrentTaskSaveStrategy = new UpdateAllSchedules();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public EndDeviceGroup getDeviceGroup() {
        return this.endDeviceGroup.get();
    }

    public boolean hasDeviceGroup() {
        return this.endDeviceGroup.isPresent();
    }

    @Override
    public boolean calculatesConnectionSetupKpi() {
        return this.connectionKpi.isPresent();
    }

    @Override
    public Optional<TemporalAmount> connectionSetupKpiCalculationIntervalLength() {
        if (this.connectionKpi.isPresent()) {
            return Optional.of(this.connectionKpi.get().getIntervalLength());
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public void calculateComTaskExecutionKpi(BigDecimal staticTarget) {
        KpiBuilder kpiBuilder = newKpi(this.connectionKpi.get().getIntervalLength(), staticTarget);
        this.communicationKpiBuilder(kpiBuilder);
        this.save();
    }

    @Override
    public void calculateConnectionKpi(BigDecimal staticTarget) {
        KpiBuilder kpiBuilder = newKpi(this.communicationKpi.get().getIntervalLength(), staticTarget);
        this.connectionKpiBuilder(kpiBuilder);
        this.save();
    }

    private KpiBuilder newKpi(TemporalAmount intervalLength, BigDecimal staticTarget) {
        KpiBuilder builder = kpiService.newKpi();
        new DataCollectionKpiServiceImpl.KpiTargetBuilderImpl(builder, intervalLength).expectingAsMaximum(staticTarget);
        return builder;
    }

    Optional<Kpi> connectionKpi() {
        if (this.connectionKpi.isPresent()) {
            return Optional.of(this.connectionKpi.get());
        }
        else {
            return Optional.empty();
        }
    }

    Optional<RecurrentTask> connectionKpiTask() {
        return connectionKpiTask.getOptional();
    }

    @Override
    public Optional<BigDecimal> getStaticConnectionKpiTarget() {
        if (this.connectionKpi.isPresent()) {
            List<? extends KpiMember> members = this.connectionKpi.get().getMembers();
            if (!members.isEmpty()) {
                return Optional.of(members.get(0).getTarget(Instant.now()));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<BigDecimal> getStaticCommunicationKpiTarget() {
        if (this.communicationKpi.isPresent()) {
            List<? extends KpiMember> members = this.communicationKpi.get().getMembers();
            if (!members.isEmpty()) {
                return Optional.of(members.get(0).getTarget(Instant.now()));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<DataCollectionKpiScore> getConnectionSetupKpiScores(Range<Instant> interval) {
        if (this.connectionKpi.isPresent()) {
            return new KpiMembers(this.connectionKpi.get().getMembers()).getScores(interval);
        }
        else {
            return Collections.emptyList();
        }
    }

    void connectionKpiBuilder(KpiBuilder builder) {
        this.kpiSaveStrategy.connectionKpiBuilder(builder);
    }

    @Override
    public boolean calculatesComTaskExecutionKpi() {
        return this.communicationKpi.isPresent();
    }

    @Override
    public Optional<TemporalAmount> comTaskExecutionKpiCalculationIntervalLength() {
        if (this.communicationKpi.isPresent()) {
            return Optional.of(this.communicationKpi.get().getIntervalLength());
        }
        else {
            return Optional.empty();
        }
    }

    Optional<Kpi> communicationKpi() {
        if (this.communicationKpi.isPresent()) {
            return Optional.of(this.communicationKpi.get());
        }
        else {
            return Optional.empty();
        }
    }

    Optional<RecurrentTask> communicationKpiTask() {
        return communicationKpiTask.getOptional();
    }

    @Override
    public List<DataCollectionKpiScore> getComTaskExecutionKpiScores(Range<Instant> interval) {
        if (this.communicationKpi.isPresent()) {
            return new KpiMembers(this.communicationKpi.get().getMembers()).getScores(interval);
        }
        else {
            return Collections.emptyList();
        }
    }

    void communicationKpiBuilder(KpiBuilder builder) {
        this.kpiSaveStrategy.communicationKpiBuilder(builder);
    }

    @Override
    public void delete() {
        this.deleteKPIs();
        this.dataModel.remove(this);
        this.deleteRecurrentTasks();
    }

    @Override
    public Optional<Instant> getLatestCalculation() {
        return Stream.of(connectionKpiTask, communicationKpiTask).
            map(Reference::getOptional).
            flatMap(Functions.asStream()).
            map(RecurrentTask::getLastOccurrence).
            flatMap(Functions.asStream()).
            map(TaskOccurrence::getTriggerTime).
                max(Comparator.nullsLast(Comparator.<Instant>naturalOrder()));
    }

    private void deleteKPIs () {
       /* Todo: Kpi has no delete method yet due to problems with journalling
                Remove the @Ignore from com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiImplTest.testDeleteAlsoDeletesKoreKPIs
                once this is implemented properly
        if (this.connectionKpi.isPresent()) {
            this.connectionKpi.get().delete();
        }
        if (this.communicationKpi.isPresent()) {
            this.communicationKpi.get().delete();
        }
        */
    }

    private void deleteRecurrentTasks() {
        if (this.connectionKpiTask.isPresent()) {
            this.connectionKpiTask.get().delete();
        }
        if (this.communicationKpiTask.isPresent()) {
            this.communicationKpiTask.get().delete();
        }
    }

    private interface KpiSaveStrategy {
        void save();
    }

    private interface CompositeKpiSaveStrategy {
        void connectionKpiBuilder(KpiBuilder builder);
        void communicationKpiBuilder(KpiBuilder builder);
        void save();
    }

    private class KpiSaveStrategyAtCreationTime implements CompositeKpiSaveStrategy {
        private KpiSaveStrategy connectionKpi = new StubKpiSaveStrategy();
        private KpiSaveStrategy communicationKpi = new StubKpiSaveStrategy();

        @Override
        public void connectionKpiBuilder(KpiBuilder builder) {
            if (builder != null) {
                this.connectionKpi = new BuildAndSave(DataCollectionKpiImpl.this.connectionKpi, builder);
            }
        }

        @Override
        public void communicationKpiBuilder(KpiBuilder builder) {
            if (builder != null) {
                this.communicationKpi = new BuildAndSave(DataCollectionKpiImpl.this.communicationKpi, builder);
            }
        }

        @Override
        public void save() {
            this.connectionKpi.save();
            this.communicationKpi.save();
        }
    }

    private class BuildAndSave implements KpiSaveStrategy {
        private final Reference<Kpi> kpiReference;
        private final KpiBuilder builder;

        private BuildAndSave(Reference<Kpi> kpiReference, KpiBuilder builder) {
            super();
            this.kpiReference = kpiReference;
            this.builder = builder;
        }

        @Override
        public void save() {
            Kpi kpi = this.builder.build();
            kpi.save();
            this.kpiReference.set(kpi);
        }
    }

    private class StubKpiSaveStrategy implements KpiSaveStrategy {
        @Override
        public void save() {
            // Stubs do not save
        }
    }

    private class KpiSaveStrategyForUpdate implements CompositeKpiSaveStrategy {
        private KpiSaveStrategy connectionKpi = new StubKpiSaveStrategy();
        private KpiSaveStrategy communicationKpi = new StubKpiSaveStrategy();

        @Override
        public void connectionKpiBuilder(KpiBuilder builder) {
            if (builder != null) {
                if (DataCollectionKpiImpl.this.connectionKpi.isPresent()) {
                    throw new CannotReplaceExistingKPI(thesaurus);
                }
                this.connectionKpi = new BuildAndSave(DataCollectionKpiImpl.this.connectionKpi, builder);
            }
        }

        @Override
        public void communicationKpiBuilder(KpiBuilder builder) {
            if (builder != null) {
                if (DataCollectionKpiImpl.this.communicationKpi.isPresent()) {
                    throw new CannotReplaceExistingKPI(thesaurus);
                }
                this.communicationKpi = new BuildAndSave(DataCollectionKpiImpl.this.communicationKpi, builder);
            }
        }

        @Override
        public void save() {
            this.connectionKpi.save();
            this.communicationKpi.save();
        }
    }

    private interface RecurrentTaskSaveStrategy {
        void save();
    }

    private class CreateAllSchedules implements RecurrentTaskSaveStrategy {
        private final RecurrentTaskSaveStrategy connectionKpi = new CreateOneRecurrentTask(DataCollectionKpiImpl.this.connectionKpi, DataCollectionKpiImpl.this.connectionKpiTask, KpiType.CONNECTION);
        private final RecurrentTaskSaveStrategy communicationKpi = new CreateOneRecurrentTask(DataCollectionKpiImpl.this.communicationKpi, DataCollectionKpiImpl.this.communicationKpiTask, KpiType.COMMUNICATION);

        @Override
        public void save() {
            this.connectionKpi.save();
            this.communicationKpi.save();
        }
    }

    private class UpdateAllSchedules implements RecurrentTaskSaveStrategy {
        private final RecurrentTaskSaveStrategy connectionKpi = new UpdateOneRecurrentTask(DataCollectionKpiImpl.this.connectionKpi, DataCollectionKpiImpl.this.connectionKpiTask, KpiType.CONNECTION);
        private final RecurrentTaskSaveStrategy communicationKpi = new UpdateOneRecurrentTask(DataCollectionKpiImpl.this.communicationKpi, DataCollectionKpiImpl.this.communicationKpiTask, KpiType.COMMUNICATION);

        @Override
        public void save() {
            this.connectionKpi.save();
            this.communicationKpi.save();
        }
    }

    private class CreateOneRecurrentTask implements RecurrentTaskSaveStrategy {
        private final Reference<Kpi> kpi;
        private final Reference<RecurrentTask> recurrentTask;
        private final KpiType kpiType;

        protected CreateOneRecurrentTask(Reference<Kpi> kpi, Reference<RecurrentTask> recurrentTask, KpiType kpiType) {
            super();
            this.kpi = kpi;
            this.recurrentTask = recurrentTask;
            this.kpiType = kpiType;
        }

        @Override
        public void save() {
            if (this.kpi.isPresent()) {
                DestinationSpec destination = messageService.getDestinationSpec(DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION).get();
                RecurrentTaskBuilder taskBuilder = taskService.newBuilder();
                taskBuilder.setName(taskName());
                taskBuilder.setScheduleExpression(this.toScheduleExpression(this.kpi.get()));
                taskBuilder.setDestination(destination);
                taskBuilder.setPayLoad(scheduledExcutionPayload());
                taskBuilder.scheduleImmediately();
                RecurrentTask recurrentTask = taskBuilder.build();
                recurrentTask.save();
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
                // Must be a Period
                Period period = (Period) kpi.getIntervalLength();
                return new TemporalExpression(new TimeDurationFromPeriodFactory().from(period));
            }
        }

        private String scheduledExcutionPayload() {
            return this.kpiType.recurrentPayload(DataCollectionKpiImpl.this);
        }

        private String taskName() {
            return this.kpiType.recurrentTaskName(DataCollectionKpiImpl.this);
        }

    }

    private interface TimeDurationFactory {
        public TimeDuration from(TemporalAmount temporalAmount);
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

    private class UpdateOneRecurrentTask extends CreateOneRecurrentTask {
        protected UpdateOneRecurrentTask(Reference<Kpi> kpi, Reference<RecurrentTask> recurrentTask, KpiType kpiType) {
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

}