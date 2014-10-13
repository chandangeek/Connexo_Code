package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link DataCollectionKpi} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-06 (10:25)
 */
@MustHaveEitherConnectionSetupOrComTaskExecution(groups = {Save.Create.class, Save.Update.class})
public class DataCollectionKpiImpl implements DataCollectionKpi, PersistenceAware {

    private static final String CONNECTION_KPI_CALCULATOR_TASK_NAME_PATTERN = "ConnectionKpiCalculator({0})";
    private static final String COMMUNICATION_KPI_CALCULATOR_TASK_NAME_PATTERN = "CommunicationKpiCalculator({0})";

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

    private long id;
    private Reference<Kpi> connectionKpi = ValueReference.absent();
    private Reference<Kpi> communicationKpi = ValueReference.absent();
    private CompositeKpiSaveStrategy kpiSaveStrategy = new KpiSaveStrategyAtCreationTime();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_GROUP_IS_REQUIRED + "}")
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();
    private Reference<RecurrentTask> connectionKpiTask = ValueReference.absent();
    private Reference<RecurrentTask> communicationKpiTask = ValueReference.absent();
    private RecurrentTaskSaveStrategy recurrentTaskSaveStrategy = new CreateAllSchedules();

    @Inject
    public DataCollectionKpiImpl(DataModel dataModel, TaskService taskService, MessageService messageService) {
        super();
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.messageService = messageService;
    }

    DataCollectionKpiImpl initialize(EndDeviceGroup group) {
        this.endDeviceGroup.set(group);
        return this;
    }

    public void save() {
        this.kpiSaveStrategy.save();
        this.recurrentTaskSaveStrategy.save();
        Save.action(this.getId()).save(this.dataModel, this);
    }

    @Override
    public void postLoad() {
        this.kpiSaveStrategy = new KpiSaveStrategyForUpdate();
        this.recurrentTaskSaveStrategy = new UpdateAllSchedules();
    }

    /**
     * Returns the payload that should be passed to the
     * {@link DataCollectionKpiCalculator} to calculate this DataCollectionKpi.
     *
     * @return The payload
     */
    private String scheduledExcutionPayload () {
        return DataCollectionKpiCalculator.expectedPayloadFor(this);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public EndDeviceGroup getDeviceGroup() {
        return this.endDeviceGroup.get();
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

    Optional<Kpi> connectionKpi() {
        if (this.connectionKpi.isPresent()) {
            return Optional.of(this.connectionKpi.get());
        }
        else {
            return Optional.empty();
        }
    }

    Optional<RecurrentTask> connectionKpiTask() {
        return Optional.ofNullable(this.connectionKpiTask.getOptional().orNull());
    }

    @Override
    public List<DataCollectionKpiScore> getConnectionSetupKpiScores(Interval interval) {
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
        return Optional.ofNullable(this.communicationKpiTask.getOptional().orNull());
    }

    @Override
    public List<DataCollectionKpiScore> getComTaskExecutionKpiScores(Interval interval) {
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
        @Override
        public void connectionKpiBuilder(KpiBuilder builder) {
            // Todo: support updates
        }

        @Override
        public void communicationKpiBuilder(KpiBuilder builder) {
            // Todo: support updates
        }

        @Override
        public void save() {
            // Todo: support updates
        }
    }

    private interface RecurrentTaskSaveStrategy {
        void save();
    }

    private class CreateAllSchedules implements RecurrentTaskSaveStrategy {
        private final RecurrentTaskSaveStrategy connectionKpi = new CreateOneRecurrentTask(DataCollectionKpiImpl.this.connectionKpi, DataCollectionKpiImpl.this.connectionKpiTask, CONNECTION_KPI_CALCULATOR_TASK_NAME_PATTERN);
        private final RecurrentTaskSaveStrategy communicationKpi = new CreateOneRecurrentTask(DataCollectionKpiImpl.this.communicationKpi, DataCollectionKpiImpl.this.communicationKpiTask, COMMUNICATION_KPI_CALCULATOR_TASK_NAME_PATTERN);

        @Override
        public void save() {
            this.connectionKpi.save();
            this.communicationKpi.save();
        }
    }

    private class UpdateAllSchedules implements RecurrentTaskSaveStrategy {
        private final RecurrentTaskSaveStrategy connectionKpi = new UpdateOneRecurrentTask(DataCollectionKpiImpl.this.connectionKpi, DataCollectionKpiImpl.this.connectionKpiTask, CONNECTION_KPI_CALCULATOR_TASK_NAME_PATTERN);
        private final RecurrentTaskSaveStrategy communicationKpi = new UpdateOneRecurrentTask(DataCollectionKpiImpl.this.communicationKpi, DataCollectionKpiImpl.this.communicationKpiTask, COMMUNICATION_KPI_CALCULATOR_TASK_NAME_PATTERN);

        @Override
        public void save() {
            this.connectionKpi.save();
            this.communicationKpi.save();
        }
    }

    private class CreateOneRecurrentTask implements RecurrentTaskSaveStrategy {
        private final Reference<Kpi> kpi;
        private final Reference<RecurrentTask> recurrentTask;
        private final String namePattern;

        protected CreateOneRecurrentTask(Reference<Kpi> kpi, Reference<RecurrentTask> recurrentTask, String namePattern) {
            super();
            this.kpi = kpi;
            this.recurrentTask = recurrentTask;
            this.namePattern = namePattern;
        }

        @Override
        public void save() {
            if (this.kpi.isPresent()) {
                DestinationSpec destination = messageService.getDestinationSpec(DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION).get();
                RecurrentTaskBuilder taskBuilder = taskService.newBuilder();
                taskBuilder.setName(taskName());
                taskBuilder.setCronExpression(cronExpression());
                taskBuilder.setDestination(destination);
                taskBuilder.setPayLoad(scheduledExcutionPayload());
                taskBuilder.scheduleImmediately();
                RecurrentTask recurrentTask = taskBuilder.build();
                recurrentTask.save();
                this.setRecurrentTask(recurrentTask);
            }
        }

        protected RecurrentTask getRecurrentTask() {
            return this.recurrentTask.get();
        }

        protected void setRecurrentTask(RecurrentTask recurrentTask) {
            this.recurrentTask.set(recurrentTask);
        }

        private String cronExpression() {
            // Todo: wait for new api that use TemporalExpression, currently hardcodes every hour starting at midnight of every day
            return "0 0 0/1 * * ? *";
        }

        private String taskName() {
            return MessageFormat.format(this.namePattern, endDeviceGroup.get().getName());
        }

    }

    private class UpdateOneRecurrentTask extends CreateOneRecurrentTask {
        protected UpdateOneRecurrentTask(Reference<Kpi> kpi, Reference<RecurrentTask> recurrentTask, String namePattern) {
            super(kpi, recurrentTask, namePattern);
        }

        @Override
        public void save() {
            /* Todo: updating a RecurrentTask is not supported yet,
             *       delete the existing one and recreate it. */
            this.getRecurrentTask().delete();
            super.save();
        }
    }

}