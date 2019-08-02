/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
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
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            return serviceProvider.comServerDAO().getThisComServer();
        }
    },
    GetComServer {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            String hostName = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.HOSTNAME);
            return serviceProvider.comServerDAO().getComServer(hostName);
        }
    },
    RefreshComServer {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comServerId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
            if (comServer.isPresent()) {
                Instant modificationDate = this.getModificationDate(parameters);
                if (comServer.get().getModificationDate().isAfter(modificationDate)) {
                    return comServer.get();
                }
            }
            return null;
        }
    },
    FindExecutableOutboundComTasks,
    FindExecutableInboundComTasks,
    ExecutionStarted {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Long comportId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
                Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comportId);
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                if (comPort.isPresent() && comTaskExecution.isPresent()) {
                    this.executionStarted(serviceProvider, comPort.get(), comTaskExecution.get());
                }
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                Long comServerId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
                ConnectionTask connectionTask = serviceProvider.connectionTaskService().findConnectionTask(connectionTaskId).get();
                Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
                if (comServer.isPresent()) {
                    this.executionStarted(serviceProvider, connectionTask, comServer.get());
                }
            }
            return null;
        }
    },
    AttemptLock {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Long comportId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
                Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comportId);
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                if (comPort.isPresent() && comTaskExecution.isPresent()) {
                    this.attemptLock(serviceProvider, comPort.get(), comTaskExecution.get());
                }
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                Long comServerId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
                OutboundConnectionTask connectionTask = serviceProvider.connectionTaskService().findOutboundConnectionTask(connectionTaskId).get();
                Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
                if (comServer.isPresent()) {
                    this.attemptLock(serviceProvider, connectionTask, comServer.get());
                }
            }
            return null;
        }
    },
    Unlock {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                comTaskExecution.ifPresent(cte -> this.unlock(serviceProvider, cte));
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                OutboundConnectionTask connectionTask = serviceProvider.connectionTaskService().findOutboundConnectionTask(connectionTaskId).get();
                this.unlock(serviceProvider, connectionTask);
            }
            return null;
        }
    },
    ExecutionCompleted {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                comTaskExecution.ifPresent(cte -> this.executionCompleted(serviceProvider, cte));
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                ConnectionTask connectionTask = serviceProvider.connectionTaskService().findConnectionTask(connectionTaskId).get();
                this.executionCompleted(serviceProvider, connectionTask);
            }
            return null;
        }
    },
    ExecutionRescheduled {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION)) {
                Integer comTaskExecutionId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.RESCHEDULE_DATE)) {
                    Date rescheduleDate = new Date((Long) parameters.get(RemoteComServerQueryJSonPropertyNames.RESCHEDULE_DATE));
                    if(comTaskExecution.isPresent()){
                        this.executionRescheduled(serviceProvider, comTaskExecution.get(), rescheduleDate.toInstant());
                    }
                }
            }
            return null;
        }
    },
    ExecutionFailed {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                comTaskExecution.ifPresent(cte -> this.executionFailed(serviceProvider, cte));
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                ConnectionTask connectionTask = serviceProvider.connectionTaskService().findConnectionTask(connectionTaskId).get();
                this.executionFailed(serviceProvider, connectionTask);
            }
            return null;
        }
    },
    ReleaseInterruptedComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comServerId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
            if (comServer.isPresent()) {
                serviceProvider.comServerDAO().releaseInterruptedTasks(comServer.get());
            }
            return null;
        }
    },
    ReleaseTimedOutComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comServerId = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
            return new TimeDurationXmlWrapper(serviceProvider.comServerDAO().releaseTimedOutTasks(comServer.get()));
        }
    },
    ReleaseComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Integer comPortId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
            Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
            if (comPort.isPresent()) {
                serviceProvider.comServerDAO().releaseTasksFor(comPort.get());
            }
            return null;
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

    /**
     * Defines the services that are required by all QueryMethods.
     */
    public interface ServiceProvider {

        public ComServerDAO comServerDAO();

        public EngineConfigurationService engineConfigurationService();

        public ConnectionTaskService connectionTaskService();

        public CommunicationTaskService communicationTaskService();

        public TransactionService transactionService();
    }

    Long getLong(Map<String, Object> parameters, String jsonPropertyName) {
        Object parameter = parameters.get(jsonPropertyName);
        if (parameter instanceof Long) {
            return (Long) parameter;
        } else {
            return Long.valueOf((Integer) parameter);
        }
    }

    protected void executionStarted(ServiceProvider serviceProvider, ConnectionTask connectionTask, ComServer comServer) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionStarted(connectionTask, comServer);
            }
        });
    }

    protected void attemptLock(ServiceProvider serviceProvider, OutboundConnectionTask connectionTask, ComServer comServer) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().attemptLock(connectionTask, comServer);
            }
        });
    }

    protected void attemptLock(ServiceProvider serviceProvider, ComPort comPort, ComTaskExecution comTaskExecution) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().attemptLock(comTaskExecution, comPort);
            }
        });
    }

    protected void unlock(ServiceProvider serviceProvider, OutboundConnectionTask connectionTask) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().unlock(connectionTask);
            }
        });
    }

    protected void unlock(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().unlock(comTaskExecution);
            }
        });
    }

    protected void executionCompleted(ServiceProvider serviceProvider, ConnectionTask connectionTask) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionCompleted(connectionTask);
            }
        });
    }

    protected void executionFailed(ServiceProvider serviceProvider, ConnectionTask connectionTask) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionFailed(connectionTask);
            }
        });
    }

    protected void executionRescheduled(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionRescheduled(comTaskExecution, rescheduleDate);
            }
        });
    }

    protected void executionStarted(ServiceProvider serviceProvider, ComPort comPort, ComTaskExecution comTaskExecution) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionStarted(comTaskExecution, comPort, true);
            }
        });
    }

    protected void executionCompleted(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionCompleted(comTaskExecution);
            }
        });
    }

    protected void executionFailed(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionFailed(comTaskExecution);
            }
        });
    }

    private <T> T executeTransaction(ServiceProvider serviceProvider, ExceptionThrowingSupplier<T, RuntimeException> transaction) {
        return serviceProvider.transactionService().execute(transaction);
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
     * @param serviceProvider The ServiceProvider
     * @return The result of the query
     */
    public Object execute(Map<String, Object> parameters, ServiceProvider serviceProvider) throws IOException {
        return this.doExecute(parameters, serviceProvider);
    }

    protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
        throw new UnsupportedOperationException(this.name() + " does not implement doExecute(Map<String,Object>,ComServerDAO) yet");
    }

}
