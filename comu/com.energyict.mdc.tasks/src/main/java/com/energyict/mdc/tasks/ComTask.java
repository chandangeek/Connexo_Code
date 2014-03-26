package com.energyict.mdc.tasks;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import java.sql.SQLException;
import java.util.List;

/**
 * Models a set of {@link com.energyict.mdc.tasks.ProtocolTask}s which can be scheduled for a Device.
 * Multiple Devices can use the same ComTask.
 *
 * @author gna
 * @since 19/04/12 - 13:52
 */
public interface ComTask {

    public long getId();

    public String getName();
    /**
     * @return true if collected data can be stored, false otherwise
     */
    public boolean storeData();

    /**
     * @return a List of {@link ProtocolTask ProtocolTasks} for this ComTask
     */
    public List<? extends ProtocolTask> getProtocolTasks();

    /**
     * Create a {@link BasicCheckTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link BasicCheckTask}
     * @throws BusinessException if some validation of the {@link BasicCheckTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
    public BasicCheckTask.BasicCheckTaskBuilder createBasicCheckTask() throws BusinessException, SQLException;

    /**
     * Create a {@link ClockTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link ClockTask}
     * @throws BusinessException if some validation of the {@link ClockTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
    public ClockTask.ClockTaskBuilder createClockTask(ClockTaskType clockTaskType) throws BusinessException, SQLException;

    /**
     * Create a {@link LoadProfilesTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link LoadProfilesTask}
     * @throws BusinessException if some validation of the {@link LoadProfilesTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
    public LoadProfilesTask.LoadProfilesTaskBuilder createLoadProfilesTask() throws BusinessException, SQLException;

    /**
     * Create a {@link com.energyict.mdc.tasks.LogBooksTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link com.energyict.mdc.tasks.LogBooksTask}
     * @throws BusinessException if some validation of the {@link com.energyict.mdc.tasks.LogBooksTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
    public LogBooksTask.LogBooksTaskBuilder createLogbooksTask() throws BusinessException, SQLException;

    /**
     * Create a {@link MessagesTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link MessagesTask}
     * @throws BusinessException if some validation of the {@link MessagesTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
    public MessagesTask.MessagesTaskBuilder createMessagesTask() throws BusinessException, SQLException;

    /**
     * Create a {@link RegistersTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link RegistersTask}
     * @throws BusinessException if some validation of the {@link RegistersTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
    public RegistersTask.RegistersTaskBuilder createRegistersTask() throws BusinessException, SQLException;

    /**
     * Create a {@link StatusInformationTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link StatusInformationTask}
     * @throws BusinessException if some validation of the {@link StatusInformationTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
    public StatusInformationTask createStatusInformationTask() throws BusinessException, SQLException;

    /**
     * Create a {@link TopologyTask} based on the given shadow for this {@link ComTask}
     *
     * @return the newly created {@link TopologyTask}
     * @throws BusinessException if some validation of the {@link TopologyTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
    public TopologyTask createTopologyTask(TopologyAction topologyAction) throws BusinessException, SQLException;

    /**
     * Keeps track of the maximum number of consecutive failures a comTask can have before marking it as failed.
     *
     * @return the maximum number of consecutive failures that a ComTaskExecution using this ComTask can have
     */
    public int getMaxNumberOfTries();

    public String getType();

    public void save();

    public void delete();

    void setName(String name);

    int getMaxNrOfTries();

    void setMaxNrOfTries(int maxNrOfTries);

    void setStoreData(boolean storeData);
}
