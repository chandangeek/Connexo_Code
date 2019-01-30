/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.ChannelsOnDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.WebRTUNTASimultationToolPropertyPostBuilder;
import com.elster.jupiter.demo.impl.commands.ActivateDevicesCommand;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.RegisterGroupTpl;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Device Type using WebRTUKP Protocol.
 * 1 LoadProfile with 32 channels
 * Device Configuration is 'MultiElement Enabled'
 */
public class CreateMultiElementDeviceCommand {

    //   private static final String SECURITY_SET_NAME = "High level MD5 authentication - No encryption";

    private final static String MULTI_ELEMENT_DEVICE_NAME = "Demo Multi Element Device";
    private final static String MULTI_ELEMENT_SERIAL = "660-05A043-1428b";
    private final static String CONNECTION_TASK_PLUGGABLE_CLASS_NAME = "OutboundTcpIpConnectionType";

    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<DeviceBuilder> deviceBuilderProvider;
    private final Provider<ActivateDevicesCommand> lifecyclePostBuilder;

    private Map<ComTaskTpl, ComTask> comTasks;
    private String name = MULTI_ELEMENT_DEVICE_NAME;
    private String serialNumber = MULTI_ELEMENT_SERIAL;

    @Inject
    public CreateMultiElementDeviceCommand(DeviceService deviceService,
                                           ProtocolPluggableService protocolPluggableService,
                                           ConnectionTaskService connectionTaskService,
                                           Provider<DeviceBuilder> deviceBuilderProvider,
                                           Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider,
                                           Provider<ActivateDevicesCommand> lifecyclePostBuilder) {
        this.deviceService = deviceService;
        this.protocolPluggableService = protocolPluggableService;
        this.connectionTaskService = connectionTaskService;
        this.deviceBuilderProvider = deviceBuilderProvider;
        this.connectionMethodsProvider = connectionMethodsProvider;
        this.lifecyclePostBuilder = lifecyclePostBuilder;
    }

    public void setMultiElementDeviceName(String name) {
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
        DeviceType deviceType = (Builders.from(DeviceTypeTpl.MULTI_ELEMENT).get());

        // 4. Create the configuration
        DeviceConfiguration configuration = createMultiElementDeviceConfiguration(deviceType);

        createMultiElementDevice(configuration);
        // 5. Create the gateway device (and set it to the 'Active' life cycle state)
//        lifecyclePostBuilder.get()
//                .setDevices(Collections.singletonList(createMultiElementDevice(configuration)))
//                .run();

    }

    private DeviceConfiguration createMultiElementDeviceConfiguration(DeviceType deviceType) {
        DeviceConfiguration config = Builders.from(DeviceConfigurationTpl.MULTI_ELEMENT_DEVICE).withDeviceType(deviceType)
                .withCanActAsGateway(true)
                .withDirectlyAddressable(true)
                .withMultiElementEnabled(true)
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

    private Device createMultiElementDevice(DeviceConfiguration configuration) {
        Device device = deviceBuilderProvider.get()
                .withName(name)
                .withSerialNumber(serialNumber)
                .withDeviceConfiguration(configuration)
                .withYearOfCertification(2015)
                .withPostBuilder(new WebRTUNTASimultationToolPropertyPostBuilder())
                .get();
        addComTaskToDevice(device, ComTaskTpl.READ_DATA_LOGGER_REGISTER_DATA);
        addComTaskToDevice(device, ComTaskTpl.READ_DATA_LOGGER_LOAD_PROFILE_DATA);
        return deviceBuilderProvider.get().withName(name).get();
    }

    private void addComTaskToDevice(Device device, ComTaskTpl comTask) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        ComTaskEnablement taskEnablement = configuration.getComTaskEnablementFor(comTasks.get(comTask)).get();
        device.newManuallyScheduledComTaskExecution(taskEnablement, null).add();
    }
}