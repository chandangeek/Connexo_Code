package com.energyict.mdc.tasks;

import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import java.util.List;

/**
 * Models a set of {@link com.energyict.mdc.tasks.ProtocolTask}s which can be scheduled for a Device.
 * Multiple Devices can use the same ComTask.
 *
 * @author gna
 * @since 19/04/12 - 13:52
 */
public interface ComTask extends HasId, HasName {

    /**
     * @return true if collected data can be stored, false otherwise
     */
    public boolean storeData();

    /**
     * @return a List of {@link ProtocolTask ProtocolTasks} for this ComTask
     */
    public List<ProtocolTask> getProtocolTasks();

    /**
     * Create a {@link BasicCheckTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link BasicCheckTask}
     */
    public BasicCheckTask.BasicCheckTaskBuilder createBasicCheckTask();

    /**
     * Create a {@link ClockTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link ClockTask}
     */
    public ClockTask.ClockTaskBuilder createClockTask(ClockTaskType clockTaskType);

    /**
     * Create a {@link LoadProfilesTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link LoadProfilesTask}
     */
    public LoadProfilesTask.LoadProfilesTaskBuilder createLoadProfilesTask();

    /**
     * Create a {@link com.energyict.mdc.tasks.LogBooksTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link com.energyict.mdc.tasks.LogBooksTask}
     */
    public LogBooksTask.LogBooksTaskBuilder createLogbooksTask();

    /**
     * Create a {@link MessagesTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link MessagesTask}
     */
    public MessagesTask.MessagesTaskBuilder createMessagesTask();

    /**
     * Create a {@link RegistersTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link RegistersTask}
     */
    public RegistersTask.RegistersTaskBuilder createRegistersTask();

    /**
     * Create a {@link StatusInformationTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link StatusInformationTask}
     */
    public StatusInformationTask createStatusInformationTask() ;

    /**
     * Create a {@link TopologyTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link TopologyTask}
     */
    public TopologyTask createTopologyTask(TopologyAction topologyAction);

    public void removeTask(ProtocolTask protocolTask);

    /**
     * Keeps track of the maximum number of consecutive failures a comTask can have before marking it as failed.
     *
     * @return the maximum number of consecutive failures that a ComTaskExecution using this ComTask can have
     */
    public int getMaxNumberOfTries();

    public void save();

    public void delete();

    public void setName(String name);

    public int getMaxNrOfTries();

    public void setMaxNrOfTries(int maxNrOfTries);

    public void setStoreData(boolean storeData);

    /**
     * User ComTask should be maintained by the users
     * @return true if this is a User defined ComTask, false otherwise
     */
    boolean isUserComTask();

    /**
     * System ComTasks should be maintained by the system
     * @return true if this is a System defined ComTask, false otherwise
     */
    boolean isSystemComTask();
}
