package com.energyict.mdc.device.data.impl.kpi;


import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiService;
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
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.constraintvalidators.MustHaveUniqueEndDeviceGroup;
import com.energyict.mdc.device.data.kpi.DataValidationKpi;
import com.energyict.mdc.device.data.kpi.DataValidationKpiScore;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@MustHaveUniqueEndDeviceGroup(message=MessageSeeds.Keys.DEVICE_GROUP_MUST_BE_UNIQUE, groups={Save.Create.class, Save.Update.class})
public class DataValidationKpiImpl implements DataValidationKpi, PersistenceAware {

    public enum Fields {
        DATA_VALIDATION_KPI("dataValidationKpi"),
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

    private long id;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    private Reference<Kpi> dataValidationKpi = ValueReference.absent();
    private Reference<RecurrentTask> dataValidationKpiTask = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<EndDeviceGroup> deviceGroup = ValueReference.absent();
    @NotNull(message = MessageSeeds.Keys.FIELD_REQUIRED, groups={Save.Create.class, Save.Update.class})
    private transient TemporalAmount frequency;


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
        Save.UPDATE.save(this.dataModel, this);
    }

    void dataValidationKpiBuilder(KpiBuilder builder){
        Kpi kpi = builder.create();
        this.dataValidationKpi.set(kpi);
    }

    public boolean hasDeviceGroup() {
        return this.deviceGroup.isPresent();
    }

    @Override
    public void postLoad() {
        //TODO add support to postLoad
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
        //TODO getDataValidationKpiScores
        return null;
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
}
