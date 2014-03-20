package com.energyict.mdc.task;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.protocol.tasks.BasicCheckTask;
import com.energyict.mdc.protocol.tasks.LoadProfilesTask;
import com.energyict.mdc.protocol.tasks.LogBooksTask;
import com.energyict.mdc.protocol.tasks.MessagesTask;
import com.energyict.mdc.protocol.tasks.RegistersTask;
import com.energyict.mdc.protocol.tasks.StatusInformationTask;
import com.energyict.mdc.protocol.tasks.TopologyTask;
import com.energyict.mdc.shadow.protocol.task.ClockTaskShadow;
import java.sql.SQLException;
import java.util.List;

/**
 * Models a set of {@link com.energyict.mdc.task.ProtocolTask}s which can be scheduled for a Device.
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
    public List<ProtocolTask> getProtocolTasks();

    /**
     * Create a {@link BasicCheckTask} based on the given shadow for this {@link ComTask}
     *
     * @param basicCheckTaskShadow to shadow to use for modeling the new {@link BasicCheckTask}
     * @return the newly created {@link BasicCheckTask}
     * @throws BusinessException if some validation of the {@link BasicCheckTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
//    public BasicCheckTask createBasicCheckTask(final BasicCheckTaskShadow basicCheckTaskShadow) throws BusinessException, SQLException;

    /**
     * Create a {@link ClockTask} based on the given shadow for this {@link ComTask}
     *
     * @param clockTask the shadow to use for modeling the new {@link ClockTask}
     * @return the newly created {@link ClockTask}
     * @throws BusinessException if some validation of the {@link ClockTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
    public ClockTask createClockTask(final ClockTaskShadow clockTask) throws BusinessException, SQLException;

    /**
     * Create a {@link LoadProfilesTask} based on the given shadow for this {@link ComTask}
     *
     * @param loadProfilesTaskShadow the shadow to use for modeling the new {@link LoadProfilesTask}
     * @return the newly created {@link LoadProfilesTask}
     * @throws BusinessException if some validation of the {@link LoadProfilesTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
//    public LoadProfilesTask createLoadProfilesTask(final LoadProfilesTaskShadow loadProfilesTaskShadow) throws BusinessException, SQLException;

    /**
     * Create a {@link com.energyict.mdc.protocol.tasks.LogBooksTask} based on the given shadow for this {@link ComTask}
     *
     * @param logBooksTaskShadow the shadow to use for modeling the new {@link com.energyict.mdc.protocol.tasks.LogBooksTask}
     * @return the newly created {@link com.energyict.mdc.protocol.tasks.LogBooksTask}
     * @throws BusinessException if some validation of the {@link com.energyict.mdc.protocol.tasks.LogBooksTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
//    public LogBooksTask createLogbooksTask(final LogBooksTaskShadow logBooksTaskShadow) throws BusinessException, SQLException;

    /**
     * Create a {@link MessagesTask} based on the given shadow for this {@link ComTask}
     *
     * @param messagesTaskShadow the shadow to use for modeling the new {@link MessagesTask}
     * @return the newly created {@link MessagesTask}
     * @throws BusinessException if some validation of the {@link MessagesTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
//    public MessagesTask createMessagesTask(final MessagesTaskShadow messagesTaskShadow) throws BusinessException, SQLException;

    /**
     * Create a {@link RegistersTask} based on the given shadow for this {@link ComTask}
     *
     * @param registersTaskShadow the shadow to use for modeling the new {@link RegistersTask}
     * @return the newly created {@link RegistersTask}
     * @throws BusinessException if some validation of the {@link RegistersTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
//    public RegistersTask createRegistersTask(final RegistersTaskShadow registersTaskShadow) throws BusinessException, SQLException;

    /**
     * Create a {@link StatusInformationTask} based on the given shadow for this {@link ComTask}
     *
     * @param statusInformationTaskShadow the shadow to use for modeling the new {@link StatusInformationTask}
     * @return the newly created {@link StatusInformationTask}
     * @throws BusinessException if some validation of the {@link StatusInformationTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
//    public StatusInformationTask createStatusInformationTask(final StatusInformationTaskShadow statusInformationTaskShadow) throws BusinessException, SQLException;

    /**
     * Create a {@link TopologyTask} based on the given shadow for this {@link ComTask}
     *
     * @param topologyTaskShadow the shadow to use for modeling the new {@link TopologyTask}
     * @return the newly created {@link TopologyTask}
     * @throws BusinessException if some validation of the {@link TopologyTask} failed or another business related error occurred
     * @throws SQLException      if a database related error occurred
     */
//    public TopologyTask createTopologyTask(final TopologyTaskShadow topologyTaskShadow) throws BusinessException, SQLException;

    /**
     * Keeps track of the maximum number of consecutive failures a comTask can have before marking it as failed.
     *
     * @return the maximum number of consecutive failures that a ComTaskExecution using this ComTask can have
     */
    public int getMaxNumberOfTries();

}
