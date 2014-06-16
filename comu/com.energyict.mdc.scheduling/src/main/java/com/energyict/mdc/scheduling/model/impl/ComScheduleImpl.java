package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.Global;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.events.DeleteEventType;
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

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@UniqueName(groups = { Save.Update.class, Save.Create.class }, message = "{"+Constants.NOT_UNIQUE+"}")
@UniqueMRID(groups = { Save.Update.class, Save.Create.class }, message = "{"+Constants.NOT_UNIQUE+"}")
public class ComScheduleImpl implements ComSchedule, HasId {

    private final SchedulingService schedulingService;
    private final EventService eventService;
    private final DataModel dataModel;
    private final ComScheduleExceptionFactory comScheduleExceptionFactory;

    enum Fields {
        NAME("name"),
        NEXT_EXECUTION_SPEC("nextExecutionSpecs"),
        STATUS("schedulingStatus"),
        START_DATE("startDate"),
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
    public ComScheduleImpl(SchedulingService schedulingService, EventService eventService, DataModel dataModel, ComScheduleExceptionFactory comScheduleExceptionFactory) {
        this.schedulingService = schedulingService;
        this.eventService = eventService;
        this.dataModel = dataModel;
        this.comScheduleExceptionFactory = comScheduleExceptionFactory;
    }

    private long id;
    @NotNull(groups = { Save.Update.class, Save.Create.class }, message = "{"+Constants.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Global.DEFAULT_DB_STRING_LENGTH, groups = { Save.Update.class, Save.Create.class }, message = "{"+Constants.TOO_LONG+"}")
    private String name;
    @Size(max= Global.DEFAULT_DB_STRING_LENGTH, groups = { Save.Update.class, Save.Create.class }, message = "{"+Constants.TOO_LONG+"}")
    private String mRID;
    private List<ComTaskInComSchedule> comTaskUsages = new ArrayList<>();
    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();
    private SchedulingStatus schedulingStatus;
    @NotNull(groups = { Save.Update.class, Save.Create.class }, message = "{"+Constants.CAN_NOT_BE_EMPTY+"}")
    private UtcInstant startDate;

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
        this.name = name!=null?name.trim():null;
    }

    // Intentionally not on interface
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
    public UtcInstant getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(UtcInstant startDate) {
        this.startDate = startDate;
    }

    @Override
    public Date getPlannedDate() {
        Calendar calendar = Calendar.getInstance();
        if (this.startDate!=null) {
            calendar.setTime(this.startDate.toDate());
        }
        return SchedulingStatus.PAUSED.equals(this.schedulingStatus)?null:this.getNextTimestamp(calendar);
    }

    @Override
    public void setTemporalExpression(TemporalExpression temporalExpression) {
        if (!this.nextExecutionSpecs.isPresent()) {
            NextExecutionSpecs nextExecutionSpecs = schedulingService.newNextExecutionSpecs(temporalExpression);
            nextExecutionSpecs.save();
            this.nextExecutionSpecs.set(nextExecutionSpecs);
        } else  {
            this.nextExecutionSpecs.get().setTemporalExpression(temporalExpression);
            this.nextExecutionSpecs.get().save();
        }
    }

    @Override
    public void save() {
        Save.action(this.getId()).save(dataModel, this);
        if (Save.UPDATE.equals(Save.action(this.getId()))) {
            this.eventService.postEvent(UpdateEventType.COMSCHEDULES.topic(), this);
        }
    }

    @Override
    public void delete() {
        this.eventService.postEvent(DeleteEventType.COMSCHEDULES.topic(), this);
        this.dataModel.remove(this);
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

}
