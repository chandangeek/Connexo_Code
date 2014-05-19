package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Elements of the QueryMethod enum correspond to every method that is defined
 * in the ComServerDAO interface.
 * The last element represents the fact that a remote client executes
 * a method that does not actually exist on the ComServerDAO interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (15:18)
 */
public enum QueryMethod {

    ConfirmSentMessagesAndGetPending,
    GetThisComServer {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            return comServerDAO.getThisComServer();
        }
    },
    GetComServer {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            String hostName = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.HOSTNAME);
            return comServerDAO.getComServer(hostName);
        }
    },
    RefreshComServer {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Integer comServerId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            ComServer comServer = ServiceProvider.instance.get().engineModelService().findComServer(comServerId);
            Date modificationDate = this.getModificationDate(parameters);
            if (comServer.getModificationDate().after(modificationDate)) {
                return comServer;
            }
            else {
                return null;
            }
        }
    },
    RefreshComPort {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Integer comportId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
            Date modificationDate = this.getModificationDate(parameters);
            ComPort comPort = ServiceProvider.instance.get().engineModelService().findComPort(comportId);
            if (comPort.getModificationDate().after(modificationDate)) {
                return comPort;
            }
            else {
                return null;
            }
        }
    },
    FindExecutableOutboundComTasks,
    FindExecutableInboundComTasks,
    ExecutionStarted {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Integer comTaskExecutionId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Integer comportId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
                ComPort comPort = ServiceProvider.instance.get().engineModelService().findComPort(comportId);
                ComTaskExecution comTaskExecution = ServiceProvider.instance.get().deviceDataService().findComTaskExecution(comTaskExecutionId);
                this.executionStarted(comServerDAO, comPort, comTaskExecution);
            }
            else {
                // Must be a ConnectionTask
                Integer connectionTaskId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                Integer comServerId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
                ConnectionTask connectionTask = ServiceProvider.instance.get().deviceDataService().findConnectionTask(connectionTaskId).get();
                ComServer comServer = ServiceProvider.instance.get().engineModelService().findComServer(comServerId);
                this.executionStarted(comServerDAO, connectionTask, comServer);
            }
            return null;
        }
    },
    AttemptLock {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Integer comTaskExecutionId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Integer comportId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
                ComPort comPort = ServiceProvider.instance.get().engineModelService().findComPort(comportId);
                ComTaskExecution comTaskExecution = ServiceProvider.instance.get().deviceDataService().findComTaskExecution(comTaskExecutionId);
                this.attemptLock(comServerDAO, comPort, comTaskExecution);
            }
            else {
                // Must be a ConnectionTask
                Integer connectionTaskId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                Integer comServerId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
                ScheduledConnectionTask connectionTask = ServiceProvider.instance.get().deviceDataService().findScheduledConnectionTask(connectionTaskId).get();
                ComServer comServer = ServiceProvider.instance.get().engineModelService().findComServer(comServerId);
                this.attemptLock(comServerDAO, connectionTask, comServer);
            }
            return null;
        }
    },
    Unlock {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Integer comTaskExecutionId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                ComTaskExecution comTaskExecution = ServiceProvider.instance.get().deviceDataService().findComTaskExecution(comTaskExecutionId);
                this.unlock(comServerDAO, comTaskExecution);
            }
            else {
                // Must be a ConnectionTask
                Integer connectionTaskId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                OutboundConnectionTask connectionTask = ServiceProvider.instance.get().deviceDataService().findScheduledConnectionTask(connectionTaskId).get();
                this.unlock(comServerDAO, connectionTask);
            }
            return null;
        }
    },
    ExecutionCompleted {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Integer comTaskExecutionId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                ComTaskExecution comTaskExecution = ServiceProvider.instance.get().deviceDataService().findComTaskExecution(comTaskExecutionId);
                this.executionCompleted(comServerDAO, comTaskExecution);
            }
            else {
                // Must be a ConnectionTask
                Integer connectionTaskId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                OutboundConnectionTask connectionTask = ServiceProvider.instance.get().deviceDataService().findScheduledConnectionTask(connectionTaskId).get();
                this.executionCompleted(comServerDAO, connectionTask);
            }
            return null;
        }
    },
    ExecutionFailed {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Integer comTaskExecutionId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                ComTaskExecution comTaskExecution = ServiceProvider.instance.get().deviceDataService().findComTaskExecution(comTaskExecutionId);
                this.executionFailed(comServerDAO, comTaskExecution);
            }
            else {
                // Must be a ConnectionTask
                Integer connectionTaskId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                ConnectionTask connectionTask = ServiceProvider.instance.get().deviceDataService().findConnectionTask(connectionTaskId).get();
                this.executionFailed(comServerDAO, connectionTask);
            }
            return null;
        }
    },
    SetMaxNrOfTries {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Integer connectionTaskId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
            Integer maxNrOfTries = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.MAX_NR_OF_TRIES);
            ScheduledConnectionTask connectionTask = ServiceProvider.instance.get().deviceDataService().findScheduledConnectionTask(connectionTaskId).get();
            this.setMaxNrOfTries(comServerDAO, connectionTask, maxNrOfTries);
            return null;
        }
    },
    ReleaseInterruptedComTasks {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Integer comServerId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            ComServer comServer = ServiceProvider.instance.get().engineModelService().findComServer(comServerId);
            comServerDAO.releaseInterruptedTasks(comServer);
            return null;
        }
    },
    ReleaseTimedOutComTasks {
        @Override
        protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Integer comServerId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            ComServer comServer = ServiceProvider.instance.get().engineModelService().findComServer(comServerId);
            return new TimeDurationXmlWrapper(comServerDAO.releaseTimedOutTasks(comServer));
        }
    },
    CreateOutboundComSession,
    CreateInboundComSession,
    CreateOrUpdateDeviceCache,
    StoreLoadProfile,
    FindDevice,
    UpdateIpAddress,
    UpdateGateway,
    StoreConfigurationFile,
    CreateDeviceEvent,
    SignalEvent,
    IsStillPending,
    AreStillPending,
    MessageNotUnderstood;

    protected void executionStarted (final ComServerDAOImpl comServerDAO, final ConnectionTask connectionTask, final ComServer comServer) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.executionStarted(connectionTask, comServer);
            }
        });
    }

    protected void attemptLock (final ComServerDAOImpl comServerDAO, final OutboundConnectionTask connectionTask, final ComServer comServer) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.attemptLock(connectionTask, comServer);
            }
        });
    }

    protected void attemptLock (final ComServerDAOImpl comServerDAO, final ComPort comPort, final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.attemptLock(comTaskExecution, comPort);
            }
        });
    }

    protected void unlock (final ComServerDAOImpl comServerDAO, final OutboundConnectionTask connectionTask) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.unlock(connectionTask);
            }
        });
    }

    protected void unlock (final ComServerDAOImpl comServerDAO, final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.unlock(comTaskExecution);
            }
        });
    }

    protected void executionCompleted (final ComServerDAOImpl comServerDAO, final ConnectionTask connectionTask) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.executionCompleted(connectionTask);
            }
        });
    }

    protected void executionFailed (final ComServerDAOImpl comServerDAO, final ConnectionTask connectionTask) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.executionFailed(connectionTask);
            }
        });
    }

    protected void setMaxNrOfTries (final ComServerDAOImpl comServerDAO, final ScheduledConnectionTask connectionTask, final int maxNrOfTries) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.setMaxNumberOfTries(connectionTask, maxNrOfTries);
            }
        });
    }

    protected void executionStarted (final ComServerDAOImpl comServerDAO, final ComPort comPort, final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.executionStarted(comTaskExecution, comPort);
            }
        });
    }

    protected void executionCompleted (final ComServerDAOImpl comServerDAO, final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.executionCompleted(comTaskExecution);
            }
        });
    }

    protected void executionFailed (final ComServerDAOImpl comServerDAO, final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform () {
                comServerDAO.executionFailed(comTaskExecution);
            }
        });
    }

    private <T> T executeTransaction (Transaction<T> transaction) {
        return ServiceProvider.instance.get().transactionService().execute(transaction);
    }

    protected Date getModificationDate (Map<String, Object> parameters) {
        Long utcMillis = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE);
        return new Date(utcMillis);
    }

    private boolean nameMatches (String name) {
        return this.name().equalsIgnoreCase(name);
    }

    public static QueryMethod byName (String methodName) {
        for (QueryMethod queryMethod : values()) {
            if (queryMethod.nameMatches(methodName)) {
                return queryMethod;
            }
        }
        return MessageNotUnderstood;
    }

    /**
     * Executes the query with the specified parameters
     * and uses the Writer to marshall the result to JSON.
     *
     * @param parameters The parameters of the query
     * @param comServerDAO The ComServerDAO
     * @return The result of the query
     */
    public Object execute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) throws IOException {
        return this.doExecute(parameters, comServerDAO);
    }

    protected Object doExecute (Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
        throw new UnsupportedOperationException(this.name() + " does not implement doExecute(Map<String,Object>,ComServerDAOImpl) yet");
    }

}