package com.energyict.mdc.device.topology.rest.demo.layer;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.layer.LayerNames;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;

/**
 * Copyrights EnergyICT
 * Date: 5/09/2017
 * Time: 15:22
 */
public class CommunicationStatusLayerBuilder implements GraphLayerBuilder {

    private final ConnectionTaskService connectionTaskService;
    private final Clock clock;

    private ComSessionBuilder comSessionBuilder;

    public CommunicationStatusLayerBuilder(ConnectionTaskService connectionTaskService, Clock clock){
        this.connectionTaskService = connectionTaskService;
        this.clock = clock;
    }

    @Override
    public boolean isGraphLayerCompatible(GraphLayer layer) {
        return LayerNames.CommunciationStatusLayer.fullName().equals(layer.getName());
    }

    @Override
    public void buildLayer(Device device) {
        // Approximately 9% of the devices should have at least 1 comtask Execution with a lastCommunicationSessionFailed
        int randomizer = new Random().nextInt() + 1;
        if (randomizer % 11 == 0) {
            Optional<ConnectionTask<?, ?>> connectionTask = device.getConnectionTasks().stream().filter(ct -> ct.isDefault() || ct.isActive()).findFirst();
            ComPortPool comportPool = connectionTask.get().getComPortPool();
            ComPort comPort = comportPool.getComPorts().get(0);
            if (connectionTask.isPresent()) {
                comSessionBuilder = connectionTaskService.buildComSession(connectionTask.get(), comportPool, comPort, clock.instant());
                // At least 1 ComTask
                device.getComTaskExecutions().stream().filter(this::isActiveAndNotSystemComTaskExecution).forEach(this::doFailComTaskExecution);
            }
        }
     }

    private boolean isActiveAndNotSystemComTaskExecution(ComTaskExecution comTaskExecution ){
        return !comTaskExecution.isFirmware() && !comTaskExecution.isOnHold() && !comTaskExecution.isObsolete() && comTaskExecution.getComTask().isUserComTask();
    }

    private void doFailComTaskExecution(ComTaskExecution comTaskExecution){
        Instant failedOn = clock.instant().plusSeconds(3L);
        ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder = comSessionBuilder.addComTaskExecutionSession(comTaskExecution, comTaskExecution.getComTask(), clock.instant());
        comSessionBuilder = comTaskExecutionSessionBuilder.add(failedOn, ComTaskExecutionSession.SuccessIndicator.Failure);
        comSessionBuilder.addJournalEntry(failedOn, ComServer.LogLevel.ERROR, "For demo purposes this communication session was meant to fail");
    }


}
