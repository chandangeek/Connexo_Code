package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTask;

/**
 * Adds behavior to {@link DeviceDataService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-28 (11:24)
 */
public interface ServerDeviceDataService extends DeviceDataService {

    /**
     * Tests if the specified {@link ComTaskEnablement} is used
     * by at least one {@link ComTaskExecution}.
     *
     * @param comTaskEnablement The ComTaskEnablement
     * @return A flag that indicates if the ComTaskEnablement is used or not
     */
    public boolean hasComTaskExecutions(ComTaskEnablement comTaskEnablement);

    /**
     * Disconnects all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration} from the
     * {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
     * that relates to the {@link PartialConnectionTask}.
     * Note that this will cause the execution to be put on hold
     * because it will no longer show up in the comserver task query.
     *
     * @param comTask The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     * @param partialConnectionTask The PartialConnectionTask
     */
    public void removePreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask);

    /**
     * Switches all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration} that were using
     * the default connection task to the specific
     * {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
     * that relates to the {@link PartialConnectionTask}.
     *
     * @param comTask The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     * @param partialConnectionTask The PartialConnectionTask
     */
    public void switchFromDefaultConnectionTaskToPreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask);

    /**
     * Switches all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration}
     * to use the default connection task from now on.
     *
     * @param comTask The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     */
    public void switchOnDefault(ComTask comTask, DeviceConfiguration deviceConfiguration);

    /**
     * Switches all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration}
     * to stop using the default connection task from now on.
     * Note that they will continue to use the one that they are connected to,
     * which coincidently may be the default ConnectionTask of the Device.
     *
     * @param comTask The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     */
    public void switchOffDefault(ComTask comTask, DeviceConfiguration deviceConfiguration);

    /**
     * Switches all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration}, that were using
     * a specific {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
     * that relates to the {@link PartialConnectionTask},
     * to use the default connection task from now on.
     *
     * @param comTask The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     * @param previousPartialConnectionTask The PartialConnectionTask
     */
    public void switchFromPreferredConnectionTaskToDefault (ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask);

    /**
     * Updates the {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
     * of all the executions of the specified {@link ComTask}
     * that are scheduled against {@link com.energyict.mdc.device.data.Device}s
     * of the specified {@link DeviceConfiguration} that have not overruled
     * the previous preferred connection task.
     *
     * @param comTask The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     * @param previousPartialConnectionTask The previous preferred {@link PartialConnectionTask}
     * @param newPartialConnectionTask The new preferred PartialConnectionTask
     */
    public void preferredConnectionTaskChanged (ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask);

}