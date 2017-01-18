package com.energyict.mdc.tasks;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.upl.tasks.TopologyAction;

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
    boolean storeData();

    /**
     * @return a List of {@link ProtocolTask ProtocolTasks} for this ComTask
     */
    List<ProtocolTask> getProtocolTasks();

    /**
     * Create a {@link BasicCheckTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link BasicCheckTask}
     */
    BasicCheckTask.BasicCheckTaskBuilder createBasicCheckTask();

    /**
     * Create a {@link ClockTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link ClockTask}
     */
    ClockTask.ClockTaskBuilder createClockTask(ClockTaskType clockTaskType);

    /**
     * Create a {@link LoadProfilesTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link LoadProfilesTask}
     */
    LoadProfilesTask.LoadProfilesTaskBuilder createLoadProfilesTask();

    /**
     * Create a {@link com.energyict.mdc.tasks.LogBooksTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link com.energyict.mdc.tasks.LogBooksTask}
     */
    LogBooksTask.LogBooksTaskBuilder createLogbooksTask();

    /**
     * Create a {@link MessagesTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link MessagesTask}
     */
    MessagesTask.MessagesTaskBuilder createMessagesTask();

    /**
     * Create a {@link RegistersTask} for this {@link ComTask}
     *
     * @return the newly created {@link RegistersTask}
     */
    RegistersTask.RegistersTaskBuilder createRegistersTask();

    /**
     * Create a {@link StatusInformationTask} for this {@link ComTask}
     *
     * @return the newly created {@link StatusInformationTask}
     */
    StatusInformationTask createStatusInformationTask() ;

    /**
     * Create a {@link FirmwareManagementTask} for this {@link ComTask}
     *
     * @return the newly created {@link FirmwareManagementTask}
     */
    FirmwareManagementTask createFirmwareManagementTask();

    /**
     * Create a {@link TopologyTask} for this {@link ComTask}
     *
     * @return the newly created {@link TopologyTask}
     */
    TopologyTask createTopologyTask(TopologyAction topologyAction);

    void removeTask(ProtocolTask protocolTask);

    /**
     * Keeps track of the maximum number of consecutive failures a comTask can have before marking it as failed.
     *
     * @return the maximum number of consecutive failures that a ComTaskExecution using this ComTask can have
     */
    int getMaxNumberOfTries();

    void save();

    void delete();

    void setName(String name);

    int getMaxNrOfTries();

    void setMaxNrOfTries(int maxNrOfTries);

    void setStoreData(boolean storeData);

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

    long getVersion();
}
