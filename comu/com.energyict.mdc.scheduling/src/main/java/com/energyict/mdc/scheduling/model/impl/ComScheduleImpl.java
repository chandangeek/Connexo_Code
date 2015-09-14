package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import java.time.Clock;
import java.time.Instant;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.scheduling.events.DeleteEventType;
import com.energyict.mdc.scheduling.events.EventType;
import com.energyict.mdc.scheduling.events.UpdateEventType;
import com.energyict.mdc.scheduling.events.VetoComTaskAdditionException;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComTaskComScheduleLink;
import com.energyict.mdc.scheduling.model.SchedulingStatus;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TopologyTask;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@UniqueName(groups = { Save.Update.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.NOT_UNIQUE+"}")
@UniqueMRID(groups = { Save.Update.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.NOT_UNIQUE+"}")
public class ComScheduleImpl implements ComSchedule {

    private final Clock clock;
    private final SchedulingService schedulingService;
    private final EventService eventService;
    private final DataModel dataModel;
    private final ComScheduleExceptionFactory comScheduleExceptionFactory;

    enum Fields {
        NAME("name"),
        NEXT_EXECUTION_SPEC("nextExecutionSpecs"),
        STATUS("schedulingStatus"),
        START_DATE("startDate"),
        OBSOLETE_DATE("obsoleteDate"),
        MRID("mRID"),
        COM_TASK_IN_COM_SCHEDULE("comTaskUsages");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public ComScheduleImpl(Clock clock, SchedulingService schedulingService, EventService eventService, DataModel dataModel, ComScheduleExceptionFactory comScheduleExceptionFactory) {
        this.clock = clock;
        this.schedulingService = schedulingService;
        this.eventService = eventService;
        this.dataModel = dataModel;
        this.comScheduleExceptionFactory = comScheduleExceptionFactory;
    }

    private long id;
    @NotNull(groups = { Save.Update.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Update.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.TOO_LONG+"}")
    private String name;
    @Size(max= Table.NAME_LENGTH, groups = { Save.Update.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.TOO_LONG+"}")
    private String mRID;
    @Size(min=1, groups = { NotObsolete.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.COM_TASK_USAGES_NOT_FOUND+"}")
    private List<ComTaskInComSchedule> comTaskUsages = new ArrayList<>();
    @IsPresent
    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();
    private SchedulingStatus schedulingStatus;
    @NotNull(groups = { Save.Update.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    private Instant startDate;
    private Instant obsoleteDate;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = Checks.is(name).emptyOrOnlyWhiteSpace() ? null : name.trim();
    }

    @Valid
    @Override
    public NextExecutionSpecs getNextExecutionSpecs() {
        return nextExecutionSpecs.get();
    }

    @Override
    public TemporalExpression getTemporalExpression() {
        return this.nextExecutionSpecs.get().getTemporalExpression();
    }

    @Override
    public String getmRID() {
        return mRID;
    }

    @Override
    public void setmRID(String mRID) {
        this.mRID = mRID!=null?mRID.trim():null;
    }

    @Override
    public Date getNextTimestamp(Calendar calendar) {
        return this.nextExecutionSpecs.get().getNextTimestamp(calendar);
    }

    @Override
    public Instant getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    @Override
    public Optional<Instant> getPlannedDate() {
        Calendar calendar = Calendar.getInstance();
        if (this.startDate!=null) {
            calendar.setTimeInMillis(this.startDate.toEpochMilli());
        }
        if (SchedulingStatus.PAUSED.equals(this.schedulingStatus)) {
            return Optional.empty();
        }
        else {
            return Optional.of(this.getNextTimestamp(calendar).toInstant());
        }
    }

    @Override
    public void setTemporalExpression(TemporalExpression temporalExpression) {
        if (!this.nextExecutionSpecs.isPresent()) {
            NextExecutionSpecs nextExecutionSpecs = schedulingService.newNextExecutionSpecs(temporalExpression);
            this.nextExecutionSpecs.set(nextExecutionSpecs);
        } else  {
            this.nextExecutionSpecs.get().setTemporalExpression(temporalExpression);
        }
    }

    @Override
    public void save() {
        Save actionToPerform = Save.action(this.getId());
        if (this.isObsolete()) {
            actionToPerform.validate(dataModel, this, Obsolete.class);
        }
        else {
            actionToPerform.validate(dataModel, this, NotObsolete.class);
        }
        this.nextExecutionSpecs.get().save();

        if (Save.UPDATE.equals(actionToPerform)) {
            dataModel.update(this);
            this.eventService.postEvent(UpdateEventType.COMSCHEDULES.topic(), this);
        } else{
            dataModel.persist(this);
        }
    }

    @Override
    public void delete() {
        this.comTaskUsages.clear();
        this.dataModel.remove(this);
        this.eventService.postEvent(DeleteEventType.COMSCHEDULES.topic(), this);
    }

    @Override
    public void makeObsolete() {
        this.comTaskUsages.clear();
        this.eventService.postEvent(EventType.COMSCHEDULES_BEFORE_OBSOLETE.topic(), this);
        this.obsoleteDate = this.clock.instant();
        Save.UPDATE.save(this.dataModel, this, Obsolete.class);
        this.eventService.postEvent(EventType.COMSCHEDULES_OBSOLETED.topic(), this);
    }

    @Override
    public boolean isObsolete() {
        return this.obsoleteDate != null;
    }

    @Override
    public Optional<Instant> getObsoleteDate() {
        return Optional.ofNullable(this.obsoleteDate);
    }

    @Override
    public List<ComTask> getComTasks() {
        List<ComTask> comTasks = new ArrayList<>();
        for (ComTaskInComSchedule comTaskUsage : comTaskUsages) {
            comTasks.add(comTaskUsage.getComTask());
        }
        return comTasks;
    }

    @Override
    public boolean containsComTask(ComTask comTask) {
        for (ComTask other : this.getComTasks()) {
            if (other.getId() == comTask.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addComTask(ComTask comTask) {
        try {
            this.eventService.postEvent(UpdateEventType.COMTASK_WILL_BE_ADDED_TO_SCHEDULE.topic(), new ComTaskAddition(this, comTask));
            comTaskUsages.add(new ComTaskInComScheduleImpl(this, comTask));
        } catch (VetoComTaskAdditionException exception) {
            throw comScheduleExceptionFactory.createCanNotAddComTaskToComScheduleException();
        }
    }

    class ComTaskAddition implements ComTaskComScheduleLink {

        private final ComSchedule comSchedule;
        private final ComTask comTask;

        ComTaskAddition(ComSchedule comSchedule, ComTask comTask) {
            this.comSchedule = comSchedule;
            this.comTask = comTask;
        }

        @Override
        public ComSchedule getComSchedule() {
            return comSchedule;
        }

        @Override
        public ComTask getComTask() {
            return comTask;
        }
    }

    @Override
    public void removeComTask(ComTask comTask) {
        for (java.util.Iterator<ComTaskInComSchedule> iterator = comTaskUsages.iterator(); iterator.hasNext(); ) {
            ComTaskInComSchedule next =  iterator.next();
            if (next.getComTask().getId()==comTask.getId()) {
                iterator.remove();
            }
        }
    }

    @Override
    public SchedulingStatus getSchedulingStatus() {
        return schedulingStatus;
    }

    @Override
    public void setSchedulingStatus(SchedulingStatus schedulingStatus) {
        this.schedulingStatus = schedulingStatus;
    }


    @Override
    public boolean isConfiguredToCollectRegisterData() {
        return isConfiguredToCollectDataOfClass(RegistersTask.class);
    }

    @Override
    public boolean isConfiguredToCollectLoadProfileData() {
        return isConfiguredToCollectDataOfClass(LoadProfilesTask.class);
    }

    @Override
    public boolean isConfiguredToRunBasicChecks() {
        return isConfiguredToCollectDataOfClass(BasicCheckTask.class);
    }

    @Override
    public boolean isConfiguredToCheckClock() {
        return isConfiguredToCollectDataOfClass(ClockTask.class);
    }

    @Override
    public boolean isConfiguredToCollectEvents() {
        return isConfiguredToCollectDataOfClass(LogBooksTask.class);
    }

    @Override
    public boolean isConfiguredToSendMessages() {
        return isConfiguredToCollectDataOfClass(MessagesTask.class);
    }

    @Override
    public boolean isConfiguredToReadStatusInformation() {
        return isConfiguredToCollectDataOfClass(StatusInformationTask.class);
    }

    @Override
    public boolean isConfiguredToUpdateTopology() {
        return isConfiguredToCollectDataOfClass(TopologyTask.class);
    }

    private <T extends ProtocolTask> boolean isConfiguredToCollectDataOfClass (Class<T> protocolTaskClass) {
        for (ComTask comTask : this.getComTasks()) {
            for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
                if (protocolTaskClass.isAssignableFrom(protocolTask.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    private interface NotObsolete {}

    private interface Obsolete {}

}
