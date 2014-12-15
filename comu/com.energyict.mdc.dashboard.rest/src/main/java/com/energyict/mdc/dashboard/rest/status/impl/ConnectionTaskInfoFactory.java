package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.Optional;

import javax.inject.Inject;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfoFactory;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;

public class ConnectionTaskInfoFactory extends DeviceConnectionTaskInfoFactory {

    @Inject
    public ConnectionTaskInfoFactory(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public ConnectionTaskInfo from(ConnectionTask<?, ?> connectionTask, Optional<ComSession> lastComSessionOptional) {
        ConnectionTaskInfo info = super.from(connectionTask, lastComSessionOptional, ConnectionTaskInfo::new);
        Device device = connectionTask.getDevice();
        info.device = new IdWithNameInfo(device.getmRID(), device.getName());
        info.deviceType = new IdWithNameInfo(device.getDeviceType());
        info.deviceConfiguration = new DeviceConfigurationIdInfo(device.getDeviceConfiguration());
        if (connectionTask.isDefault()) {
            info.connectionMethod.name += " (" + getThesaurus().getString(MessageSeeds.DEFAULT.getKey(), "default") + ")";
        }
        return info;
    }

}
