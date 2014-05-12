package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import java.util.Date;
import java.util.List;

/**
 * Models a task that is able to establish a connection
 * with a device that is designed for communication.<br>
 * A ConnectionTask can be used by multiple devices,
 * allowing those devices to all use the same communication channel.
 * In that case, the device will most likely be a gateway-like device.
 * <p/>
 * A ConnectionTask of type X cannot be created against a device if the
 * {@link com.energyict.mdc.device.config.DeviceConfiguration} does not support X.
 * Remember that support for type X on the configuration level is
 * managed with a {@link PartialConnectionTask}. Every ConnectionTask
 * will therefore be linked to the PartialConnectionTask from which
 * it was created and from which it can inherit connection properties.
 * <p/>
 * When the {@link ConnectionType} does not support {@link com.energyict.mdc.common.ComWindow}s
 * then the ConnectionTask is NOT required to have a ComWindow.
 * In case there is a ComWindow, the ConnectionTask will only
 * establish a connection when the time is within the ComWindow.
 * This ComWindow is computed against the device's time zone.
 * <p/>
 * Each time a ConnectionTask is executed, a ComSession
 * is created that captures all the details of the communication with the device.
 * That communication overview is very imported and should not be deleted
 * easily. Therefore, ConnectionTasks are never deleted but made obsolete.
 * Obsolete ConnectionTasks will not return from {@link DeviceDataService}
 * finder methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (09:59)
 */
public interface ConnectionTask<CPPT extends ComPortPool, PCTT extends PartialConnectionTask>
       extends ConnectionTaskPropertyProvider, ConnectionTaskExecutionAspects, HasId {

    public enum Type {
        /**
         * For inbound connections
         */
        INBOUND,
        /**
         * For outbound connections
         */
        OUTBOUND,
        /**
         * For initiation of a connection
         */
        CONNECTION_INITIATION}


    /**
     * Returns the object's unique identifier.
     *
     * @return the id
     */
    public long getId();

    /**
     * Gets the ConnectionTask's name - this will be the name inherited from the {@link PartialConnectionTask}
     *
     * @return the name
     */
    public String getName();

    /**
     * Gets the {@link Device} that this ConnectionTask is going
     * to connect to when executing.
     *
     * @return The Device that will be connected to
     */
    public Device getDevice ();

    /**
     * Gets the {@link PartialConnectionTask} from which this ConnectionTask
     * was created and from which it will inherit properties.
     * Note that this is read-only and will typically be set at creation time.
     *
     * @return The PartialConnectionTask
     */
    public PCTT getPartialConnectionTask ();

    /**
     * Gets the list of {@link ConnectionTaskProperty ConnectionTaskProperties}
     * that will currently be used to connect to devices.<br>
     * Note that properties that are inherited from higher levels
     * such as e.g. the {@link ConnectionTypePluggableClass}
     * are also returned. Use the {@link ConnectionTaskProperty#isInherited()} method
     * to distinguish between local and inherited values should that be necessary.
     *
     * @return The List of ConnectionTaskProperties
     */
    public List<ConnectionTaskProperty> getProperties ();

    /**
     * Gets the list of {@link ConnectionTaskProperty ConnectionTaskProperties}
     * that are active on the specified Date to connect to devices.<br>
     * Note that properties that are inherited from higher levels
     * such as e.g. the {@link ConnectionTypePluggableClass}
     * are also returned. Use the {@link ConnectionTaskProperty#isInherited()} method
     * to distinguish between local and inherited values should that be necessary.
     *
     * @param date The Date on which the ConnectionTaskProperty should be active
     * @return The List of ConnectionTaskProperties
     */
    public List<ConnectionTaskProperty> getProperties (Date date);

    /**
     * Sets the value of the property with the specified name.
     *
     * @param propertyName the name of the property
     * @param value The property value
     */
    public void setProperty (String propertyName, Object value);

    /**
     * Removes the property with the specified name.
     * In case the property was inherited from the {@link ConnectionTypePluggableClass}
     * then this actually means that this ConnectionTask is reverting
     * the setting of the property back to the ConnectionTypePluggableClass level.
     *
     * @param propertyName The name of the property
     */
    public void removeProperty (String propertyName);

    /**
     * Tests if this ConnectionTask is paused.
     * A ConnectionTask that is paused, will not be executed
     * by the ComServer. In addition, all {@link ComTaskExecution}s
     * that are linked to it will not be executed.
     * Remember that a ScheduledComTask can be linked directly to a ConnectionTask
     * or it can be linked to the default ConnectionTask of the device hieararchy.
     *
     * @return A flag that indicates if this ConnectionTask is paused
     */
    public boolean isPaused ();

    /**
     * Makes this ConnectionTask obsolete, i.e. it will no longer execute
     * nor will it be returned by {@link DeviceDataService} finder methods.
     */
    public void makeObsolete ();

    /**
     * Tests if this ConnectionTask is obsolete.
     *
     * @return A flag that indicates if this ConnectionTask is obsolete
     */
    public boolean isObsolete ();

    /**
     * Gets the date on which this ConnectionTask was made obsolete.
     *
     * @return The date when this ConnectionTask was made obsolete
     *         or <code>null</code> when this ConnectionTask is not obsolete at all.
     */
    public Date getObsoleteDate ();

    /**
     * Gets the date on which this ConnectionTask was created or last modified.
     *
     * @return The date when this ConnectionTask was created or last modified
     */
    public Date getModificationDate ();

    /**
     * Tests if this ConnectionTask is the default that should be used
     * by {@link ComTaskExecution}s that are configured to use the default.
     *
     * @return A flag that indicates if this is the default
     */
    public boolean isDefault ();

    /**
     * Gets the ComPortPool from which a {@link com.energyict.mdc.engine.model.ComPort} will
     * be pulled when a connection needs to be established for an outbound task
     * or the ComPortPool that contains the ComPorts that will be used by
     * the physical device to connect to the platform for an inbound task.
     *
     * @return The ComPortPool
     */
    public CPPT getComPortPool ();

    public void setComPortPool (CPPT comPortPool);

    /**
     * Keeps track of the last time a connection was started for this ConnectionTask.
     *
     * @return the date of the last connection start.
     */

    public Date getLastCommunicationStart();

    /**
     * Keeps track of the last time a SUCCESSFUL connection was ended, independent whether or not the ConnectionTask has failed.
     *
     * @return the date of the last SUCCESSFUL connection end.
     */
    public Date getLastSuccessfulCommunicationEnd();

    /**
     * Gets the {@link ConnectionType} that knows exactly how to connect
     * to a device and the properties it needs to do that.
     *
     * @return The ConnectionType
     */
    public ConnectionType getConnectionType();

    /**
     * Tests if this ConnectionTask is currently executing.
     * Convenience (and possibly faster) for <code>getExecutingComServer() != null</code>.
     *
     * @return <code>true</code> iff this ConnectionTask is executing, i.e. if the executing ComServer is not null
     */
    public boolean isExecuting ();

    /**
     * Gets the {@link ComServer} that is currently
     * executing this ConnectionTask or <code>null</code>
     * if this ConnectionTask is not executing at this moment.
     *
     * @return The ComServer or <code>null</code>
     */
    public ComServer getExecutingComServer ();

    /**
     * Pauses this connectionTask, i.e. temporarily disables its execution.
     * The reverse operation is {@link #resume()}
     */
    public void pause ();

    /**
     * Resumes the ability to execute this ConnectionTask.
     * This is the reverse operation of {@link #pause()}
     */
    public void resume();

    public void save();

}