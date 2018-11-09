/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.ChannelsOnDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.WebRTUNTASimultationToolPropertyPostBuilder;
import com.elster.jupiter.demo.impl.commands.ActivateDevicesCommand;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.demo.impl.templates.RegisterGroupTpl;
import com.elster.jupiter.demo.impl.templates.SecurityPropertySetTpl;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Device Type using WebRTUKP Protocol.
 * 1 LoadProfile with 32 channels
 * Device Configuration is 'DataLogger Enabled'
 */
public class CreateDataLoggerCommand {

    //   private static final String SECURITY_SET_NAME = "High level MD5 authentication - No encryption";

    private final static String DATA_LOGGER_NAME = "DemoDataLogger";
    private final static String DATA_LOGGER_SERIAL = "660-05A043-1428";
    private final static String CONNECTION_TASK_PLUGGABLE_CLASS_NAME = "OutboundTcpIpConnectionType";

    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final SecurityManagementService securityManagementService;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<DeviceBuilder> deviceBuilderProvider;
    private final Provider<ActivateDevicesCommand> lifecyclePostBuilder;

    private Map<ComTaskTpl, ComTask> comTasks;
    private String name = DATA_LOGGER_NAME;
    private String serialNumber = DATA_LOGGER_SERIAL;
    private KeyAccessorValuePersister keyAccessorValuePersister;

    @Inject
    public CreateDataLoggerCommand(DeviceService deviceService,
                                   ProtocolPluggableService protocolPluggableService,
                                   ConnectionTaskService connectionTaskService,
                                   SecurityManagementService securityManagementService,
                                   Provider<DeviceBuilder> deviceBuilderProvider,
                                   Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider,
                                   Provider<ActivateDevicesCommand> lifecyclePostBuilder) {
        this.deviceService = deviceService;
        this.protocolPluggableService = protocolPluggableService;
        this.connectionTaskService = connectionTaskService;
        this.securityManagementService = securityManagementService;
        this.deviceBuilderProvider = deviceBuilderProvider;
        this.connectionMethodsProvider = connectionMethodsProvider;
        this.lifecyclePostBuilder = lifecyclePostBuilder;
    }

    public void setDataLoggerName(String name) {
        this.name = name;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void run() {
        // 1. Some basic checks
        Optional<Device> device = deviceService.findDeviceByName(name);
        if (device.isPresent()) {
            System.out.println("Nothing was created since a device with name '" + this.name + "' already exists!");
            return;
        }
        Optional<ConnectionTypePluggableClass> pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByNameTranslationKey(CONNECTION_TASK_PLUGGABLE_CLASS_NAME);
        if (!pluggableClass.isPresent()) {
            System.out.println("Nothing was created since the required pluggable class '" + CONNECTION_TASK_PLUGGABLE_CLASS_NAME + "' couldn't be found!");
            return;
        }

        // 2. Find or create required objects
        findOrCreateRequiredObjects();

        // 3. Create the device type
        DeviceType deviceType = (Builders.from(DeviceTypeTpl.WEBRTU_Z2).get());

        // 4. Create the configuration
        DeviceConfiguration configuration = createDataLoggerDeviceConfiguration(deviceType);

        // 5. Create the gateway device (and set it to the 'Active' life cycle state)
        lifecyclePostBuilder.get()
                .setDevices(Collections.singletonList(createDataLoggerDevice(configuration)))
                .run();
//
//        //6. Create Device Group "Data loggers"
//        EndDeviceGroup dataLoggerGroup = Builders.from(DeviceGroupTpl.DATA_LOGGERS).get();
//        Builders.from(FavoriteDeviceGroupBuilder.class).withGroup(dataLoggerGroup).get();

    }

    private DeviceConfiguration createDataLoggerDeviceConfiguration(DeviceType deviceType) {
        DeviceConfiguration config = Builders.from(DeviceConfigurationTpl.DATA_LOGGER).withDeviceType(deviceType)
                .withCanActAsGateway(true)
                .withDirectlyAddressable(true)
                .withDataLoggerEnabled(true)
                .withPostBuilder(this.connectionMethodsProvider.get().withRetryDelay(5))
                .withPostBuilder(new ChannelsOnDevConfPostBuilder())
                .get();
        if (!config.isActive()) {
            config.activate();
        }
        return config;
    }

    private void findOrCreateRequiredObjects() {
        Builders.from(RegisterGroupTpl.DATA_LOGGER_REGISTER_DATA).get();

        comTasks = new HashMap<>();
        findOrCreateComTask(ComTaskTpl.READ_DATA_LOGGER_REGISTER_DATA);
        findOrCreateComTask(ComTaskTpl.READ_DATA_LOGGER_LOAD_PROFILE_DATA);
    }

    private ComTask findOrCreateComTask(ComTaskTpl comTaskTpl) {
        return comTasks.put(comTaskTpl, Builders.from(comTaskTpl).get());
    }

    private Device createDataLoggerDevice(DeviceConfiguration configuration) {
        Device device = deviceBuilderProvider.get()
                .withName(name)
                .withSerialNumber(serialNumber)
                .withDeviceConfiguration(configuration)
                .withYearOfCertification(2015)
                .withPostBuilder(new WebRTUNTASimultationToolPropertyPostBuilder())
                .get();
        addConnectionTasksToDevice(device);
        device = deviceBuilderProvider.get().withName(name).get();
        addSecurityPropertiesToDevice(device);
        device = deviceBuilderProvider.get().withName(name).get();
        addComTaskToDevice(device, ComTaskTpl.READ_DATA_LOGGER_REGISTER_DATA);
        addComTaskToDevice(device, ComTaskTpl.READ_DATA_LOGGER_LOAD_PROFILE_DATA);
        return deviceBuilderProvider.get().withName(name).get();
    }


    private void addConnectionTasksToDevice(Device device) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
        device
                .getScheduledConnectionTasks().stream()
                .filter(scheduledConnectionTask -> scheduledConnectionTask.getName().compareToIgnoreCase(connectionTask.getName()) == 0)
                .findFirst()
                .ifPresent(
                        scheduledConnectionTask -> {
                            scheduledConnectionTask.setComPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get());
                            scheduledConnectionTask.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
                            scheduledConnectionTask.setNextExecutionSpecsFrom(null);
                            //scheduledConnectionTask.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
                            scheduledConnectionTask.setProperty("host", "localhost");
                            scheduledConnectionTask.setProperty("portNumber", new BigDecimal(4059));
                            scheduledConnectionTask.setNumberOfSimultaneousConnections(1);
                            scheduledConnectionTask.save();
                            connectionTaskService.setDefaultConnectionTask(scheduledConnectionTask);
                        }
                );
        /*ScheduledConnectionTask deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
                .setComPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get())
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .setNextExecutionSpecsFrom(null)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                .setProperty("host", "localhost")
                .setProperty("portNumber", new BigDecimal(4059))
                .setNumberOfSimultaneousConnections(1)
                .add();
        connectionTaskService.setDefaultConnectionTask(deviceConnectionTask);*/
    }

    private void addComTaskToDevice(Device device, ComTaskTpl comTask) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        ComTaskEnablement taskEnablement = configuration.getComTaskEnablementFor(comTasks.get(comTask)).get();
        device.newManuallyScheduledComTaskExecution(taskEnablement, null).add();
    }

    private void addSecurityPropertiesToDevice(Device device) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        SecurityPropertySet securityPropertySetHigh =
                configuration
                        .getSecurityPropertySets()
                        .stream()
                        .filter(sps -> SecurityPropertySetTpl.HIGH_LEVEL_NO_ENCRYPTION_MD5.getName().equals(sps.getName()))
                        .findFirst()
                        .orElseThrow(() -> new UnableToCreate("No securityPropertySet with name " + SecurityPropertySetTpl.HIGH_LEVEL_NO_ENCRYPTION_MD5.getName() + "."));
        securityPropertySetHigh
                .getPropertySpecs()
                .stream()
                .filter(ps -> "Password".equals(ps.getName()))
                .findFirst()
                .ifPresent(ps -> getKeyAccessorValuePersister().persistKeyAccessorValue(device, "Password", "ntaSim"));
        securityPropertySetHigh
                .getPropertySpecs()
                .stream()
                .filter(ps -> "AuthenticationKey".equals(ps.getName()))
                .findFirst()
                .ifPresent(ps -> getKeyAccessorValuePersister().persistKeyAccessorValue(device, "AuthenticationKey", "00112233445566778899AABBCCDDEEFF"));
        securityPropertySetHigh
                .getPropertySpecs()
                .stream()
                .filter(ps -> "EncryptionKey".equals(ps.getName()))
                .findFirst()
                .ifPresent(ps -> getKeyAccessorValuePersister().persistKeyAccessorValue(device, "EncryptionKey", "11223344556677889900AABBCCDDEEFF"));
    }

    private KeyAccessorValuePersister getKeyAccessorValuePersister() {
        if (keyAccessorValuePersister == null) {
            keyAccessorValuePersister = new KeyAccessorValuePersister(securityManagementService);
        }
        return keyAccessorValuePersister;
    }

}