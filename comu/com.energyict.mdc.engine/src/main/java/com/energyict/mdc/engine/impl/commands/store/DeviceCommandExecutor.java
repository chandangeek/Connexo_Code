package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.core.ServerProcess;
import com.energyict.mdc.engine.config.ComServer;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Models the behavior of a component that will execute {@link DeviceCommand}s.
 * Note that implementations may delay the execution of a DeviceCommand
 * until there are enough system resources available.
 * <p>
 * To ensure that the rate at which new DeviceCommands are generated
 * is more or less in line with the rate at which these DeviceCommands
 * can be executed, it is necessary to prepare the execution.
 * Creating a DeviceCommand is usually a time consuming process
 * and it would be a shame to find out after the DeviceCommand
 * is created that there are actually not enough resources to execute it.
 * It is only when the preparation succeeds that new DeviceCommands
 * can be presented for execution. If despite of preparation failure,
 * DeviceCommands are executed, that execution will immediately fail
 * with a {@link NoResourcesAcquiredException}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-17 (16:11)
 */
public interface DeviceCommandExecutor extends ServerProcess {

    /**
     * Gets the {@link ComServer.LogLevel} this is used by this DeviceCommandExecutor.
     *
     * @return The LogLevel
     */
    public ComServer.LogLevel getLogLevel ();

    /**
     * Prepares the execution of the specified number of {@link DeviceCommand}s
     * by acquiring the necessary {@link DeviceCommandExecutionToken resources}
     * for the number of DeviceCommands that was specified but only if this DeviceCommandExecutor
     * has enough resources available to execute that number of DeviceCommands.
     * In case not enough resources are available, the returned List is empty.
     * Note that this will throw an IllegalStateException if this DeviceCommandExecutor
     * is shutting down or has already shutdown.
     *
     * @param numberOfCommands The number of DeviceCommands that will be created
     *                         and presented for execution later
     * @return Returns {@link DeviceCommandExecutionToken}s for every DeviceCommand,
     *         i.e. the size of the List will be equal to the numberOfCommands parameter value
     */
    public List<DeviceCommandExecutionToken> tryAcquireTokens (int numberOfCommands);

    /**
     * Prepares the execution of the specified number of {@link DeviceCommand}s
     * by acquiring the necessary {@link DeviceCommandExecutionToken resources}
     * for the number of DeviceCommands that was specified.
     * This call will block until this DeviceCommandExecutor has enough resources
     * available to execute that number of DeviceCommands.
     * Note that this will throw an IllegalStateException if this DeviceCommandExecutor
     * is shutting down or has already shutdown.
     *
     * @param numberOfCommands The number of DeviceCommands that will be created
     *                         and presented for execution later
     * @return Returns {@link DeviceCommandExecutionToken}s for every DeviceCommand,
     *         i.e. the size of the List will be equal to the numberOfCommands parameter value
     * @throws InterruptedException if the current thread is interrupted
     */
    public List<DeviceCommandExecutionToken> acquireTokens (int numberOfCommands) throws InterruptedException;

    /**
     * Executes the {@link DeviceCommand} or delays its execution.
     * Note that resources must have been acquired prior to the execution,
     * to be able to execute this DeviceCommand.
     * The provided {@link DeviceCommandExecutionToken}
     * is proof that the execution was previously prepared.
     * A {@link NoResourcesAcquiredException} will be thrown if
     * this DeviceCommandExecutor failed to prepare these resources
     * earlier or if the client code failed to acquire these resources
     * with the prepareExecution method.
     * Note that this will throw an IllegalStateException if this DeviceCommandExecutor
     * is shutting down or has already shutdown.
     *
     * @param command The DeviceCommand
     * @param token A DeviceCommandExecutionToken that was previously returned by a prepareExecution call
     * @see #tryAcquireTokens(int)
     * @see #acquireTokens(int)
     */
    public Future<Boolean> execute (DeviceCommand command, DeviceCommandExecutionToken token);

    /**
     * Returns a previously created {@link DeviceCommandExecutionToken}
     * and frees the associated reserved resources.
     * Note that freeing tokens while this DeviceCommandExecutor
     * is shutting down is allowed. The resources will be properly
     * released but cannot be claimed by another process
     * because preparing or executing is no longer allowed.
     * Note however that when the DeviceCommandExecutionToken
     * was not returned by this DeviceCommandExecutor,
     * an {@link NoResourcesAcquiredException} will be thrown.
     *
     * @param unusedToken The unused DeviceCommandExecutionToken
     * @throws NoResourcesAcquiredException Indicates that the specified DeviceCommandExecutionToken
     *                                      was not returned by this DeviceCommandExecutor
     */
    public void free (DeviceCommandExecutionToken unusedToken);

    public int getCapacity ();

    public int getCurrentSize ();

    public int getCurrentLoadPercentage ();

    public int getNumberOfThreads ();

    public int getThreadPriority ();

}