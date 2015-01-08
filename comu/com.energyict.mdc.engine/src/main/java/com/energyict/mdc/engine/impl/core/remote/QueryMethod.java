package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.VoidTransaction;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

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
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            return comServerDAO.getThisComServer();
        }
    },
    GetComServer {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            String hostName = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.HOSTNAME);
            return comServerDAO.getComServer(hostName);
        }
    },
    RefreshComServer {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Long comServerId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            Optional<ComServer> comServer = ServiceProvider.instance.get().engineConfigurationService().findComServer(comServerId);
            if (comServer.isPresent()) {
                Instant modificationDate = this.getModificationDate(parameters);
                if (comServer.get().getModificationDate().isAfter(modificationDate)) {
                    return comServer.get();
                }
            }
            return null;
        }
    },
    RefreshComPort {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Long comportId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMPORT);
            Instant modificationDate = this.getModificationDate(parameters);
            Optional<? extends ComPort> comPort = ServiceProvider.instance.get().engineConfigurationService().findComPort(comportId);
            if (comPort.isPresent() && comPort.get().getModificationDate().isAfter(modificationDate)) {
                return comPort.get();
            } else {
                return null;
            }
        }
    },
    FindExecutableOutboundComTasks,
    FindExecutableInboundComTasks,
    ExecutionStarted {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Long comTaskExecutionId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Long comportId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
                Optional<? extends ComPort> comPort = ServiceProvider.instance.get().engineConfigurationService().findComPort(comportId);
                Optional<ComTaskExecution> comTaskExecution = ServiceProvider.instance.get().communicationTaskService().findComTaskExecution(comTaskExecutionId);
                if (comPort.isPresent() && comTaskExecution.isPresent()) {
                    this.executionStarted(comServerDAO, comPort.get(), comTaskExecution.get());
                }
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                Long comServerId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
                ConnectionTask connectionTask = ServiceProvider.instance.get().connectionTaskService().findConnectionTask(connectionTaskId).get();
                Optional<ComServer> comServer = ServiceProvider.instance.get().engineConfigurationService().findComServer(comServerId);
                if (comServer.isPresent()) {
                    this.executionStarted(comServerDAO, connectionTask, comServer.get());
                }
            }
            return null;
        }
    },
    AttemptLock {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Long comTaskExecutionId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Long comportId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
                Optional<? extends ComPort> comPort = ServiceProvider.instance.get().engineConfigurationService().findComPort(comportId);
                Optional<ComTaskExecution> comTaskExecution = ServiceProvider.instance.get().communicationTaskService().findComTaskExecution(comTaskExecutionId);
                if (comPort.isPresent() && comTaskExecution.isPresent()) {
                    this.attemptLock(comServerDAO, comPort.get(), comTaskExecution.get());
                }
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                Long comServerId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
                OutboundConnectionTask connectionTask = ServiceProvider.instance.get().connectionTaskService().findOutboundConnectionTask(connectionTaskId).get();
                Optional<ComServer> comServer = ServiceProvider.instance.get().engineConfigurationService().findComServer(comServerId);
                if (comServer.isPresent()) {
                    this.attemptLock(comServerDAO, connectionTask, comServer.get());
                }
            }
            return null;
        }
    },
    Unlock {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Long comTaskExecutionId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Optional<ComTaskExecution> comTaskExecution = ServiceProvider.instance.get().communicationTaskService().findComTaskExecution(comTaskExecutionId);
                comTaskExecution.ifPresent(cte -> this.unlock(comServerDAO, cte));
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                OutboundConnectionTask connectionTask = ServiceProvider.instance.get().connectionTaskService().findOutboundConnectionTask(connectionTaskId).get();
                this.unlock(comServerDAO, connectionTask);
            }
            return null;
        }
    },
    ExecutionCompleted {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Long comTaskExecutionId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Optional<ComTaskExecution> comTaskExecution = ServiceProvider.instance.get().communicationTaskService().findComTaskExecution(comTaskExecutionId);
                comTaskExecution.ifPresent(cte -> this.executionCompleted(comServerDAO, cte));
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                ConnectionTask connectionTask = ServiceProvider.instance.get().connectionTaskService().findConnectionTask(connectionTaskId).get();
                this.executionCompleted(comServerDAO, connectionTask);
            }
            return null;
        }
    },
    ExecutionFailed {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Long comTaskExecutionId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Optional<ComTaskExecution> comTaskExecution = ServiceProvider.instance.get().communicationTaskService().findComTaskExecution(comTaskExecutionId);
                comTaskExecution.ifPresent(cte -> this.executionFailed(comServerDAO, cte));
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                ConnectionTask connectionTask = ServiceProvider.instance.get().connectionTaskService().findConnectionTask(connectionTaskId).get();
                this.executionFailed(comServerDAO, connectionTask);
            }
            return null;
        }
    },
    ReleaseInterruptedComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Long comServerId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            Optional<ComServer> comServer = ServiceProvider.instance.get().engineConfigurationService().findComServer(comServerId);
            if (comServer.isPresent()) {
                comServerDAO.releaseInterruptedTasks(comServer.get());
            }
            return null;
        }
    },
    ReleaseTimedOutComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
            Long comServerId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            Optional<ComServer> comServer = ServiceProvider.instance.get().engineConfigurationService().findComServer(comServerId);
            return new TimeDurationXmlWrapper(comServerDAO.releaseTimedOutTasks(comServer.get()));
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

    Long getLong(Map<String, Object> parameters, String jsonPropertyName) {
        Object parameter = parameters.get(jsonPropertyName);
        if (parameter instanceof Long) {
            return (Long) parameter;
        } else {
            return Long.valueOf((Integer) parameter);
        }
    }

    protected void executionStarted(final ComServerDAOImpl comServerDAO, final ConnectionTask connectionTask, final ComServer comServer) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform() {
                comServerDAO.executionStarted(connectionTask, comServer);
            }
        });
    }

    protected void attemptLock(final ComServerDAOImpl comServerDAO, final OutboundConnectionTask connectionTask, final ComServer comServer) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform() {
                comServerDAO.attemptLock(connectionTask, comServer);
            }
        });
    }

    protected void attemptLock(final ComServerDAOImpl comServerDAO, final ComPort comPort, final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform() {
                comServerDAO.attemptLock(comTaskExecution, comPort);
            }
        });
    }

    protected void unlock(final ComServerDAOImpl comServerDAO, final OutboundConnectionTask connectionTask) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform() {
                comServerDAO.unlock(connectionTask);
            }
        });
    }

    protected void unlock(final ComServerDAOImpl comServerDAO, final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform() {
                comServerDAO.unlock(comTaskExecution);
            }
        });
    }

    protected void executionCompleted(final ComServerDAOImpl comServerDAO, final ConnectionTask connectionTask) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform() {
                comServerDAO.executionCompleted(connectionTask);
            }
        });
    }

    protected void executionFailed(final ComServerDAOImpl comServerDAO, final ConnectionTask connectionTask) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform() {
                comServerDAO.executionFailed(connectionTask);
            }
        });
    }

    protected void executionStarted(final ComServerDAOImpl comServerDAO, final ComPort comPort, final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform() {
                comServerDAO.executionStarted(comTaskExecution, comPort);
            }
        });
    }

    protected void executionCompleted(final ComServerDAOImpl comServerDAO, final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform() {
                comServerDAO.executionCompleted(comTaskExecution);
            }
        });
    }

    protected void executionFailed(final ComServerDAOImpl comServerDAO, final ComTaskExecution comTaskExecution) {
        this.executeTransaction(new VoidTransaction() {
            @Override
            public void doPerform() {
                comServerDAO.executionFailed(comTaskExecution);
            }
        });
    }

    private <T> T executeTransaction(Transaction<T> transaction) {
        return ServiceProvider.instance.get().transactionService().execute(transaction);
    }

    protected Instant getModificationDate(Map<String, Object> parameters) {
        Long utcMillis = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE);
        return Instant.ofEpochMilli(utcMillis);
    }

    private boolean nameMatches(String name) {
        return this.name().equalsIgnoreCase(name);
    }

    public static QueryMethod byName(String methodName) {
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
     * @param parameters   The parameters of the query
     * @param comServerDAO The ComServerDAO
     * @return The result of the query
     */
    public Object execute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) throws IOException {
        return this.doExecute(parameters, comServerDAO);
    }

    protected Object doExecute(Map<String, Object> parameters, ComServerDAOImpl comServerDAO) {
        throw new UnsupportedOperationException(this.name() + " does not implement doExecute(Map<String,Object>,ComServerDAOImpl) yet");
    }

}