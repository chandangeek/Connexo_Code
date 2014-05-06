package com.energyict.mdc.engine.impl.core.devices;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.FreeUnusedTokenDeviceCommand;

import java.util.List;

/**
 * Defines pointcuts and advice that will do logging for the
 * {@link DeviceCommandExecutor} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (15:57)
 */
public abstract aspect AbstractDeviceCommandExecutorLogging {

    private pointcut starting (DeviceCommandExecutor deviceCommandExecutor):
            execution(void DeviceCommandExecutor.start())
         && target(deviceCommandExecutor);

    after (DeviceCommandExecutor deviceCommandExecutor): starting(deviceCommandExecutor) {
        this.getLogger(deviceCommandExecutor).started(deviceCommandExecutor);
    }

    private pointcut shuttingDown (DeviceCommandExecutor deviceCommandExecutor):
            execution(void DeviceCommandExecutor.shutdown())
         && target(deviceCommandExecutor);

    before (DeviceCommandExecutor deviceCommandExecutor): shuttingDown(deviceCommandExecutor) {
        this.getLogger(deviceCommandExecutor).shuttingDown(deviceCommandExecutor);
    }

    private pointcut tryAcquireTokens (DeviceCommandExecutor deviceCommandExecutor, int numberOfCommands):
            execution(List<DeviceCommandExecutionToken> DeviceCommandExecutor.tryAcquireTokens(int))
         && target(deviceCommandExecutor)
         && args(numberOfCommands);

    after (DeviceCommandExecutor deviceCommandExecutor, int numberOfCommands) returning (List<DeviceCommandExecutionToken> tokens) : tryAcquireTokens(deviceCommandExecutor, numberOfCommands) {
        if (tokens.isEmpty()) {
            this.getLogger(deviceCommandExecutor).preparationFailed(deviceCommandExecutor, numberOfCommands);
        }
        else {
            this.getLogger(deviceCommandExecutor).preparationCompleted(deviceCommandExecutor, numberOfCommands);
        }
        this.getLogger(deviceCommandExecutor).logCurrentQueueSize(deviceCommandExecutor.getCurrentSize(), deviceCommandExecutor.getCapacity());
    }

    after (DeviceCommandExecutor deviceCommandExecutor, int numberOfCommands) throwing (IllegalStateException e) : tryAcquireTokens(deviceCommandExecutor, numberOfCommands) {
        this.getLogger(deviceCommandExecutor).cannotPrepareWhenNotRunning(e,  deviceCommandExecutor);
    }

    private pointcut acquireTokens (DeviceCommandExecutor deviceCommandExecutor, int numberOfCommands):
            execution(List<DeviceCommandExecutionToken> DeviceCommandExecutor.acquireTokens(int))
         && target(deviceCommandExecutor)
         && args(numberOfCommands);

    before (DeviceCommandExecutor deviceCommandExecutor, int numberOfCommands): acquireTokens(deviceCommandExecutor, numberOfCommands) {
        this.getLogger(deviceCommandExecutor).logCurrentQueueSize(deviceCommandExecutor.getCurrentSize(), deviceCommandExecutor.getCapacity());
        if (deviceCommandExecutor.getCurrentSize() == deviceCommandExecutor.getCapacity()) {
            this.getLogger(deviceCommandExecutor).preparationFailed(deviceCommandExecutor, numberOfCommands);
        }
    }

    after (DeviceCommandExecutor deviceCommandExecutor, int numberOfCommands) throwing (IllegalStateException e) : acquireTokens(deviceCommandExecutor, numberOfCommands) {
        this.getLogger(deviceCommandExecutor).cannotPrepareWhenNotRunning(e,  deviceCommandExecutor);
    }

    private pointcut executeCommand (DeviceCommandExecutor deviceCommandExecutor, DeviceCommand command, DeviceCommandExecutionToken token):
            execution(void DeviceCommandExecutor.execute(DeviceCommand, DeviceCommandExecutionToken))
         && target(deviceCommandExecutor)
         && args(command, token);

    after (DeviceCommandExecutor deviceCommandExecutor, DeviceCommand command, DeviceCommandExecutionToken token) : executeCommand(deviceCommandExecutor, command, token) {
        this.getLogger(deviceCommandExecutor).executionQueued(deviceCommandExecutor, command);
    }

    after (DeviceCommandExecutor deviceCommandExecutor, DeviceCommand command, DeviceCommandExecutionToken token) throwing (IllegalStateException e) : executeCommand(deviceCommandExecutor, command, token) {
        this.getLogger(deviceCommandExecutor).cannotExecuteWhenNotRunning(e, deviceCommandExecutor, command);
    }

    private pointcut commandCompleted (DeviceCommandExecutorImpl deviceCommandExecutor, DeviceCommand command):
            execution(void com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorImpl.commandCompleted(DeviceCommand))
         && target(deviceCommandExecutor)
         && args(command);

    before (DeviceCommandExecutorImpl deviceCommandExecutor, DeviceCommand command) : commandCompleted (deviceCommandExecutor, command) {
        if (command instanceof FreeUnusedTokenDeviceCommand) {
            this.getLogger(deviceCommandExecutor).tokenReleased(deviceCommandExecutor);
        }
        else {
            this.getLogger(deviceCommandExecutor).commandCompleted(deviceCommandExecutor, command);
        }
    }

    after (DeviceCommandExecutorImpl deviceCommandExecutor, DeviceCommand command) : commandCompleted (deviceCommandExecutor, command) {
        this.getLogger(deviceCommandExecutor).logCurrentQueueSize(deviceCommandExecutor.getCurrentSize(), deviceCommandExecutor.getCapacity());
    }

    private pointcut commandFailed (DeviceCommandExecutorImpl deviceCommandExecutor, DeviceCommand command, Throwable t):
            execution(void com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorImpl.commandFailed(DeviceCommand, java.lang.Throwable))
         && target(deviceCommandExecutor)
         && args(command, t);

    before (DeviceCommandExecutorImpl deviceCommandExecutor, DeviceCommand command, Throwable t) : commandFailed (deviceCommandExecutor, command, t) {
        this.getLogger(deviceCommandExecutor).commandFailed(t,  deviceCommandExecutor, command);
    }

    after (DeviceCommandExecutorImpl deviceCommandExecutor, DeviceCommand command, Throwable t) : commandFailed (deviceCommandExecutor, command, t) {
        this.getLogger(deviceCommandExecutor).logCurrentQueueSize(deviceCommandExecutor.getCurrentSize(), deviceCommandExecutor.getCapacity());
    }

    private pointcut changingThreadPriority (DeviceCommandExecutorImpl listener, int newPriority):
            execution(void com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorImpl.changeThreadPriority(int))
         && target(listener)
         && args(newPriority);

    before (DeviceCommandExecutorImpl listener, int newPriority) : changingThreadPriority (listener,  newPriority) {
        if (listener.getThreadPriority() != newPriority) {
            this.getLogger(listener).threadPriorityChanged(listener, listener.getThreadPriority(), newPriority);
        }
    }

    private pointcut changingNumberOfThreads (DeviceCommandExecutorImpl listener, int newNumberOfThreads):
            execution(void com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorImpl.changeNumberOfThreads(int))
         && target(listener)
         && args(newNumberOfThreads);

    before (DeviceCommandExecutorImpl listener, int newNumberOfThreads) : changingNumberOfThreads (listener,  newNumberOfThreads) {
        if (listener.getNumberOfThreads() != newNumberOfThreads) {
            this.getLogger(listener).numberOfThreadsChanged(listener, listener.getNumberOfThreads(), newNumberOfThreads);
        }
    }

    private pointcut changingQueueCapacity (DeviceCommandExecutorImpl listener, int newCapacity):
            execution(void com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorImpl.changeQueueCapacity(int))
         && target(listener)
         && args(newCapacity);

    before (DeviceCommandExecutorImpl listener, int newCapacity) : changingQueueCapacity (listener,  newCapacity) {
        if (listener.getQueueCapacity() != newCapacity) {
            this.getLogger(listener).queueCapacityChanged(listener, listener.getQueueCapacity(), newCapacity);
        }
    }

    protected abstract DeviceCommandExecutorLogger getLogger (DeviceCommandExecutor deviceCommandExecutor);

}