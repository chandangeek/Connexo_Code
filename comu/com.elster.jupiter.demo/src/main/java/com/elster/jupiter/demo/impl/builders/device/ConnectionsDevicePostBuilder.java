package com.elster.jupiter.demo.impl.builders.device;

import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.OutboundComPortPool;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.time.TimeDuration;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.function.Consumer;

public class ConnectionsDevicePostBuilder implements Consumer<Device> {
    private final ConnectionTaskService connectionTaskService;

    private OutboundComPortPool comPortPool;
    private String host;

    @Inject
    public ConnectionsDevicePostBuilder(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    public ConnectionsDevicePostBuilder withComPortPool(OutboundComPortPool comPortPool){
        this.comPortPool = comPortPool;
        return this;
    }

    public ConnectionsDevicePostBuilder withHost(String host){
        this.host = host;
        return this;
    }

    @Override
    public void accept(Device device) {
        checkProperties();
        if (device.getScheduledConnectionTasks().isEmpty()) {
            DeviceConfiguration configuration = device.getDeviceConfiguration();
            PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
            int portNumber = 4059;
            ScheduledConnectionTask deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
                    .setComPortPool(this.comPortPool)
                    .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .setNextExecutionSpecsFrom(null)
                    .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                    .setProperty("host", this.host)
                    .setProperty("portNumber", new BigDecimal(portNumber))
                    .setProperty("connectionTimeout", TimeDuration.minutes(1))
                    .setSimultaneousConnectionsAllowed(false)
                    .add();
            connectionTaskService.setDefaultConnectionTask(deviceConnectionTask);
        }
    }

    private void checkProperties() {
        if (this.comPortPool == null){
            throw new UnableToCreate("You must specify the communication port pool for device connection properties");
        }
        if (this.host == null){
            throw new UnableToCreate("You must specify the host property for device connections");
        }
    }
}
