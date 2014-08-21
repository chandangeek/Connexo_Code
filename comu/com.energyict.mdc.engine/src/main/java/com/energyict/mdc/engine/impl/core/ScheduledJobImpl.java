package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.exceptions.ConnectionFailureException;

import java.util.Calendar;

/**
 * Provides code reuse opportunities for component that
 * want to implement the {@link ScheduledJob} interface.
 * The support is primarily towards creating the physical
 * connection with the device, i.e. the {@link ComChannel},
 * keeping track of the ComSession,
 * the ComTaskExecutionSession
 * and the related statistics.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-14 (16:44)
 */
public abstract class ScheduledJobImpl extends JobExecution {

    protected ScheduledJobImpl(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    /**
     * Provide the {@link OutboundConnectionTask}
     *
     * @return the used OutboundConnectionTask
     */
    public abstract ScheduledConnectionTask getConnectionTask();

    @Override
    protected boolean isConnected() {
        return getExecutionContext().getComPortRelatedComChannel() != null;
    }

    @Override
    protected ComPortRelatedComChannel findOrCreateComChannel() throws ConnectionException {
        return new ComPortRelatedComChannelImpl(
                        this.getConnectionTask().connect(this.getComPort()),
                        this.getComPort(),
                        this.getServiceProvider().hexService());
    }

    @Override
    public boolean isWithinComWindow () {
        return this.isWithinComWindow(this.getConnectionTask().getCommunicationWindow());
    }

    private boolean isWithinComWindow (ComWindow comWindow) {
        if (comWindow == null) {
            return true;
        }
        else {
            Calendar now = Calendar.getInstance();
            now.setTimeInMillis(getServiceProvider().clock().now().getTime());
            return comWindow.includes(now);
        }
    }

    @Override
    public void rescheduleToNextComWindow () {
        this.createExecutionContext(false);
        this.getExecutionContext().getComSessionBuilder().incrementNotExecutedTasks(this.getComTaskExecutions().size());
        this.getExecutionContext().createJournalEntry("Rescheduling to next ComWindow because current timestamp is not " + this.getConnectionTask().getCommunicationWindow());
        this.doReschedule(RescheduleBehavior.RescheduleReason.OUTSIDE_COM_WINDOW);
        this.completeSuccessfulComSession();
    }

    /**
     * Attempts to lock the specified {@link ComTaskExecution}
     * and returns <code>true</code> if the lock succeeded.
     * If the lock did not succeed, this is an indication
     * that another component has already locked it,
     * most likely for executing it.
     *
     * @param comTaskExecution The ComTaskExecution
     * @return A flag that indicates a successful locking of the ComTaskExecution
     */
    boolean attemptLock (ComTaskExecution comTaskExecution) {
        return this.getComServerDAO().attemptLock(comTaskExecution, this.getComPort());
    }

    /**
     * Attempts to lock the specified {@link OutboundConnectionTask}
     * and returns <code>true</code> if the lock succeeded.
     * If the lock did not succeed, this is an indication
     * that another component has already locked it,
     * most likely for executing {@link ComTaskExecution}s against it.
     *
     * @param connectionTask The ConnectionTask
     * @return A flag that indicates a successful locking of the ConnectionTask
     */
    boolean attemptLock (ScheduledConnectionTask connectionTask) {
        return this.getComServerDAO().attemptLock(connectionTask, this.getComPort().getComServer()) != null;
    }

    void unlock (ScheduledConnectionTask connectionTask) {
        this.getComServerDAO().unlock(connectionTask);
    }

    void unlock (ComTaskExecution comTaskExecution) {
        this.getComServerDAO().unlock(comTaskExecution);
    }

    boolean establishConnectionFor() {
        return this.getExecutionContext().connect();
    }

    public void createExecutionContext () {
        this.createExecutionContext(true);
    }

    void createExecutionContext (boolean logConnectionProperties) {
        this.createExecutionContext(this.getComPort(), logConnectionProperties);
    }

    private void createExecutionContext (ComPort comPort, boolean logConnectionProperties) {
        this.setExecutionContext(this.newExecutionContext(this.getConnectionTask(), comPort, logConnectionProperties));
    }

    void completeConnection () {
        if (getExecutionContext() != null) {
            try {
                this.getConnectionTask().disconnect(getExecutionContext().getComPortRelatedComChannel());
            }
            catch (ConnectionException e) {
                throw new ConnectionFailureException(e);
            }
        }
    }

}