package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.impl.MessageSeeds;
import com.elster.jupiter.validation.kpi.DataValidationKpi;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Predicates.not;

@MustHaveUniqueEndDeviceGroup(message = MessageSeeds.Constants.DEVICE_GROUP_MUST_BE_UNIQUE, groups = {Save.Create.class, Save.Update.class})
public class DataValidationKpiImpl implements DataValidationKpi, PersistenceAware, Cloneable {

    public enum Fields {
        CHILDREN_KPIS("childrenKpis"),
        DATA_VALIDATION_KPI_TASK("dataValidationKpiTask"),
        END_DEVICE_GROUP("deviceGroup");

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
    private final Clock clock;

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;

    private List<DataValidationKpiChild> childrenKpis = new ArrayList<>();
    private Reference<RecurrentTask> dataValidationKpiTask = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private Reference<EndDeviceGroup> deviceGroup = ValueReference.absent();
    @NotNull(message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}", groups = {Save.Create.class, Save.Update.class})
    private transient TemporalAmount frequency;
    private RecurrentTaskSaveStrategy recurrentTaskSaveStrategy = new CreateRecurrentTask(dataValidationKpiTask, KpiType.VALIDATION);

    @Inject
    public DataValidationKpiImpl(DataModel dataModel, TaskService taskService, MessageService messageService, KpiService kpiService, Thesaurus thesaurus, Clock clock) {
        super();
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.messageService = messageService;
        this.kpiService = kpiService;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    DataValidationKpiImpl initialize(EndDeviceGroup group) {
        if (group != null) {
            this.deviceGroup.set(group);
        } else {
            this.deviceGroup.setNull();
        }
        return this;
    }

    public void save() {
        if (this.getId() == 0) {
            Save.CREATE.save(this.dataModel, this);
        }
        this.recurrentTaskSaveStrategy.save();
        Save.UPDATE.save(this.dataModel, this);
    }

    private void createKpiChildren(EndDeviceGroup endDeviceGroup) {
        endDeviceGroup.getMembers(Instant.now(clock))
                .forEach(device -> createValidationKpiMember(device.getId()));
    }

    private void createValidationKpiMember(long id) {
        KpiBuilder builder = kpiService.newKpi();
        builder.interval(frequency);
        Stream.of(DataValidationKpiMemberTypes.values())
                .map(DataValidationKpiMemberTypes::fieldName)
                .forEach(member ->
                        builder.member()
                                .named(member + id)
                                .add()
                );
        builder.named("ValidationKpi"+id);
        childrenKpis.add(DataValidationKpiChildImpl.from(dataModel, this, builder.create()));
    }

    boolean hasDeviceGroup() {
        return this.deviceGroup.isPresent();
    }

    @Override
    public void postLoad() {
        updateFrequency();
        this.recurrentTaskSaveStrategy = new UpdateRecurrentTask(this.dataValidationKpiTask, KpiType.VALIDATION);
        Stream.of(this.dataValidationKpiCalculationIntervalLength())
                .flatMap(Functions.asStream())
                .findFirst()
                .ifPresent(temporalAmount -> this.frequency = temporalAmount);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Optional<TemporalAmount> dataValidationKpiCalculationIntervalLength() {
        return frequency != null ? Optional.of(frequency) : Optional.empty();
    }

    @Override
    public void setFrequency(TemporalAmount intervalLength) {
        if (this.frequency != null) {
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
        this.dataValidationKpiTask.getOptional().ifPresent(found -> {
                    if ("TaskService".equals(found.getUserName())) {
                        // else name will be creation user or batch executor
                        found.setNextExecution(null);
                        found.save();
                    } else {
                        dropDataValidationKpi();
                    }
                }
        );
    }

    @Override
    protected final DataValidationKpiImpl clone() {
        try {
            return (DataValidationKpiImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // should never occur, we are implementing Cloneable
        }
    }

    @Override
    public Optional<Instant> getLatestCalculation() {
        Optional<RecurrentTask> task = this.dataValidationKpiTask.getOptional();
        if (task.isPresent()) {
            return task.get().getLastRun();
        }
        return Optional.empty();

    }

    @Override
    public boolean isCancelled() {
        Optional<RecurrentTask> validationTask = this.dataValidationKpiTask.getOptional();
        if (validationTask.isPresent()) {
            return taskService.getRecurrentTask(validationTask.get().getId()).get().getNextExecution() == null;
        } else {
            return true;
        }
    }

    @Override
    public void dropDataValidationKpi() {
        this.dataModel.remove(this);
        this.dataValidationKpiTask.getOptional().ifPresent(task -> {
            task.suspend();
            task.delete();
        });
        this.childrenKpis.forEach(DataValidationKpiChild::remove);
    }

    List<DataValidationKpiChild> getDataValidationKpiChildren() {
        return Collections.unmodifiableList(childrenKpis);
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    private void updateFrequency() {
        if (frequency == null) {
            Optional<RecurrentTask> recurrentTask = dataValidationKpiTask.getOptional();
            if (recurrentTask.isPresent()) {
                frequency = ((TemporalExpression) (recurrentTask.get().getScheduleExpression())).getEvery()
                        .asTemporalAmount();
            }
        }
    }

    void updateMembers() {
        if (childrenKpis == null) {
            createKpiChildren(getDeviceGroup());
        }
        updateFrequency();
        Set<Long> deviceGroupDeviceIds = deviceIdsInGroup();
        Set<Long> dataValidationKpiMembers = deviceIdsInKpiMembers();
        if (deviceGroupDeviceIds.equals(dataValidationKpiMembers)) {
            return;
        }
        Set<Long> commonElements = intersection(deviceGroupDeviceIds, dataValidationKpiMembers);
        deviceGroupDeviceIds.stream()
                .filter(not(commonElements::contains))
                .forEach(this::createValidationKpiMember);
        dataValidationKpiMembers.stream()
                .filter(not(commonElements::contains))
                .forEach(id -> {
                            List<Kpi> obsoleteKpiList = childrenKpis.stream()
                                    .map(DataValidationKpiChild::getChildKpi)
                                    .map(Kpi::getMembers)
                                    .flatMap(List::stream)
                                    .filter(member -> member.getName().endsWith("_" + id))
                                    .map(KpiMember::getKpi)
                                    .distinct()
                                    .collect(Collectors.toList());
                            obsoleteKpiList.forEach(Kpi::remove);
                        }
                );
    }

    private Set<Long> intersection(Set<Long> first, Set<Long> second) {
        return first.stream()
                .filter(second::contains)
                .collect(Collectors.toSet());
    }

    private Set<Long> deviceIdsInKpiMembers() {
        return childrenKpis.stream()
                .map(DataValidationKpiChild::getChildKpi)
                .map(Kpi::getMembers)
                .flatMap(List::stream)
                .map(this::deviceIdAsString)
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    private Set<Long> deviceIdsInGroup() {
        EndDeviceGroup endDeviceGroup = getDeviceGroup();
        return endDeviceGroup.getMembers(Instant.now(clock))
                .stream()
                .map(EndDevice::getId)
                .collect(Collectors.toSet());
    }

    private String deviceIdAsString(KpiMember member) {
        return member.getName().substring(member.getName().indexOf("_") + 1);
    }

    private interface RecurrentTaskSaveStrategy {
        void save();
    }

    private class CreateRecurrentTask implements RecurrentTaskSaveStrategy {
        private final Reference<RecurrentTask> recurrentTask;
        private final KpiType kpiType;

        CreateRecurrentTask(Reference<RecurrentTask> recurrentTask, KpiType kpiType) {
            super();
            this.recurrentTask = recurrentTask;
            this.kpiType = kpiType;
        }

        public void save() {
            updateFrequency();
            DestinationSpec destination = messageService.getDestinationSpec(DataValidationKpiCalculatorHandlerFactory.TASK_DESTINATION)
                    .get();
            RecurrentTask recurrentTask;
            Optional<RecurrentTask> defaultTask = taskService.getRecurrentTask(taskName());
            if (defaultTask.isPresent()) {
                recurrentTask = defaultTask.get();
                recurrentTask.setScheduleExpression(this.toScheduleExpression());
                recurrentTask.save();
            } else {
                recurrentTask = taskService.newBuilder()
                        .setApplication("MultiSense")
                        .setName(taskName())
                        .setScheduleExpression(this.toScheduleExpression())
                        .setDestination(destination)
                        .setPayLoad(scheduledExcutionPayload())
                        .scheduleImmediately(true)
                        .build();
            }
            this.setRecurrentTask(recurrentTask);

        }

        protected Optional<RecurrentTask> getRecurrentTask() {
            return this.recurrentTask.getOptional();
        }

        protected void setRecurrentTask(RecurrentTask recurrentTask) {
            this.recurrentTask.set(recurrentTask);
        }

        private ScheduleExpression toScheduleExpression() {
            if (frequency instanceof Duration) {
                Duration duration = (Duration) frequency;
                return new TemporalExpression(new TimeDurationFromDurationFactory().from(duration));
            } else {
                Period period = (Period) frequency;
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
        UpdateRecurrentTask(Reference<RecurrentTask> recurrentTask, KpiType kpiType) {
            super(recurrentTask, kpiType);
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
            } else {
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
            } else {
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
            } else {
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
            } else {
                return null;
            }
        }

        private TimeDuration noDays(Period period) {
            if (period.getDays() != 0) {
                throw new IllegalArgumentException("Years and days or months and days are not supported");
            } else {
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
            } else {
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
            } else {
                return null;
            }
        }
    }
}
