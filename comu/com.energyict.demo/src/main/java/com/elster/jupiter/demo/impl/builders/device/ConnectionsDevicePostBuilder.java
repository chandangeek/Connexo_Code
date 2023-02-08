/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders.device;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.function.Consumer;

public class ConnectionsDevicePostBuilder implements Consumer<Device> {
    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;

    private OutboundComPortPool comPortPool;
    private String host;

    @Inject
    public ConnectionsDevicePostBuilder(ConnectionTaskService connectionTaskService,
                                        CommunicationTaskService communicationTaskService) {
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
    }

    public ConnectionsDevicePostBuilder withComPortPool(OutboundComPortPool comPortPool) {
        this.comPortPool = comPortPool;
        return this;
    }

    public ConnectionsDevicePostBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public void accept(Device device) {
        checkProperties();
        if (device.getScheduledConnectionTasks().isEmpty()) {
            DeviceConfiguration configuration = device.getDeviceConfiguration();
            int portNumber = 4059;
            configuration.getPartialOutboundConnectionTasks().stream()
                    .findFirst()
                    .map(connectionTask -> {
                        ScheduledConnectionTask deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
                                .setComPortPool(this.comPortPool)
                                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                                .setNextExecutionSpecsFrom(null)
                                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                                .setProperty("host", this.host)
                                .setProperty("portNumber", new BigDecimal(portNumber))
                                .setProperty("connectionTimeout", TimeDuration.minutes(1))
                                .setNumberOfSimultaneousConnections(1)
                                .add();
                        connectionTaskService.setDefaultConnectionTask(deviceConnectionTask);
                        return connectionTask;
                    });
        }
        device.getComTaskExecutions().stream()
                .map(ComTaskExecution::getId)
                .sorted()
                .map(communicationTaskService::findAndLockComTaskExecutionById)
                .flatMap(Functions.asStream())
                .forEach(comTaskExecution -> device.getComTaskExecutionUpdater(comTaskExecution).useDefaultConnectionTask(true).update());
    }

    private void checkProperties() {
        if (this.comPortPool == null) {
            throw new UnableToCreate("You must specify the communication port pool for device connection properties");
        }
        if (this.host == null) {
            throw new UnableToCreate("You must specify the host property for device connections");
        }
    }
}
