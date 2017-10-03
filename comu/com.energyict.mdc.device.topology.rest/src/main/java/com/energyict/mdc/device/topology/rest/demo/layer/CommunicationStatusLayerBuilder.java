package com.energyict.mdc.device.topology.rest.demo.layer;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.layer.LayerNames;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;

/**
 * Populating the database with failed communications so to demo the communication status layer of the network topology management
 * Copyrights EnergyICT
 * Date: 5/09/2017
 * Time: 15:22
 */
public class CommunicationStatusLayerBuilder implements GraphLayerBuilder {

    private final EngineConfigurationService engineConfigurationService;
    private final SchedulingService schedulingService;
    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;
    private final Clock clock;

    private ComSessionBuilder comSessionBuilder;

    public CommunicationStatusLayerBuilder(EngineConfigurationService engineConfigurationService,
                                           SchedulingService schedulingService,
                                           ConnectionTaskService connectionTaskService,
                                           CommunicationTaskService communicationTaskService,
                                           Clock clock) {
        this.engineConfigurationService = engineConfigurationService;
        this.schedulingService = schedulingService;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.clock = clock;
    }

    @Override
    public boolean isGraphLayerCompatible(GraphLayer layer) {
        return LayerNames.CommunciationStatusLayer.fullName().equals(layer.getName());
    }

    @Override
    public void buildLayer(Device device) {
        // Approximately 9% of the devices should have at least 1 comtask Execution with a lastCommunicationSessionFailed
        int randomizer = new Random().nextInt(101) + 1;
        if (randomizer % 11 == 0) {
            Optional<ConnectionTask<?, ?>> connectionTask = device.getConnectionTasks().stream().filter(ct -> ct.isDefault() || ct.isActive()).findFirst();
            if (!connectionTask.isPresent()) {
                connectionTask = addDefaultConnectionTask(device);
            }
            if (device.getComTaskExecutions().isEmpty()) {
                addScheduledComTaskExections(device);
            }
            if (connectionTask.isPresent()) {
                ComPortPool comportPool = connectionTask.get().getComPortPool();
                ComPort comPort = comportPool.getComPorts().get(0);
                comSessionBuilder = connectionTaskService.buildComSession(connectionTask.get(), comportPool, comPort, clock.instant());
                device.getComTaskExecutions().stream().filter(this::isActiveAndNotSystemComTaskExecution).forEach(this::doFailComTaskExecution);
                Instant failedOn = clock.instant().plusSeconds(3L);
                comSessionBuilder.addJournalEntry(failedOn, ComServer.LogLevel.ERROR, "For demo purposes this communication session was meant to fail");
                comSessionBuilder.endSession(failedOn.plusSeconds(1L), ComSession.SuccessIndicator.SetupError).create();

            }
        }
    }

    private Optional<ConnectionTask<?, ?>> addDefaultConnectionTask(Device device) {
        ScheduledConnectionTask deviceConnectionTask = null;
        Optional<OutboundComPortPool> comPortPool = engineConfigurationService.findAllComPortPools().stream().filter((cpp) -> !cpp.isInbound()).map(OutboundComPortPool.class::cast).findFirst();
        if (comPortPool.isPresent()) {
            DeviceConfiguration configuration = device.getDeviceConfiguration();
            if (!configuration.getPartialOutboundConnectionTasks().isEmpty()) {
                PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
                deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
                        .setComPortPool(comPortPool.get())
                        .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                        .setNextExecutionSpecsFrom(null)
                        .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                        .setProperty("host", "localhost")
                        .setProperty("portNumber", new BigDecimal(4059))
                        .setProperty("connectionTimeout", TimeDuration.minutes(1))
                        .setNumberOfSimultaneousConnections(1)
                        .add();
                System.out.println(String.format("Device '%1s' (%d): set connectiontask '%2s' as default connection task", device.getName(), device.getId(), deviceConnectionTask.getName()));
                System.out.println(String.format("ConnectionTask status: %s", deviceConnectionTask.getStatus()));
                connectionTaskService.setDefaultConnectionTask(deviceConnectionTask);
            }
        }
        return Optional.ofNullable(deviceConnectionTask);
    }

    private void addScheduledComTaskExections(Device device) {
        Optional<ComSchedule> daily = getDailyReadAllSchedule();
        if (daily.isPresent()) {
            long commonComTasksCount = daily.get().getComTasks().stream().map(ct -> device.getDeviceConfiguration().getComTaskEnablementFor(ct)).filter(Optional::isPresent).count();
            // Avoid an IndexOutOfBoundException when no common comtasks present
            if (commonComTasksCount > 0) {
                device.newScheduledComTaskExecution(daily.get()).add();
            }
        }
    }

    private boolean isActiveAndNotSystemComTaskExecution(ComTaskExecution comTaskExecution) {
        return !comTaskExecution.isFirmware() && !comTaskExecution.isOnHold() && !comTaskExecution.isObsolete() && comTaskExecution.getComTask().isUserComTask();
    }

    private void doFailComTaskExecution(ComTaskExecution comTaskExecution) {
        Instant failedOn = clock.instant().plusSeconds(3L);
        ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder = comSessionBuilder.addComTaskExecutionSession(comTaskExecution, comTaskExecution.getComTask(), clock.instant());
        comTaskExecutionSessionBuilder.addComTaskExecutionMessageJournalEntry(failedOn, ComServer.LogLevel.ERROR, "FailedForDemoPurposes", "For demo purposes this communication session was meant to fail");
        comTaskExecutionSessionBuilder.addComCommandJournalEntry(failedOn, CompletionCode.ConnectionError /* Highest Priority */, "Forced error for demo purposes", "No real command executed: demo!");
        comTaskExecutionSessionBuilder.add(failedOn, ComTaskExecutionSession.SuccessIndicator.Failure);
        comSessionBuilder.incrementFailedTasks(1);
        communicationTaskService.executionFailedFor(comTaskExecution);
    }

    private Optional<ComSchedule> getDailyReadAllSchedule() {
        // Created by createDemoData
        return schedulingService.findAllSchedules().find().stream().filter(cs -> cs.getName().equals("Daily read all")).findFirst();
    }
}
