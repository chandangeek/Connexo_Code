package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedData;

/**
 * Extends {@link CollectedData} to add behavior that is private
 * to server based components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-22 (16:07)
 */
public interface ServerCollectedData extends CollectedData {

    /**
     * Converts this CollectedData to a {@link DeviceCommand}
     * that will allow to store the collected data
     * once the communication session is complete.
     *
     * @return The DeviceCommand
     * @param issueService
     */
    public DeviceCommand toDeviceCommand(IssueService issueService);

    /**
     * Injects the {@link ConnectionTask} that was used to communicate
     * as part of post processing of this CollectedData.
     * There may be CollectedData created during the discovery process
     * that requires the ConnectionTask while it was obviously not available
     * during discovery.
     *
     * @param connectionTask The ConnectionTask
     */
    public void postProcess (ConnectionTask connectionTask);

}