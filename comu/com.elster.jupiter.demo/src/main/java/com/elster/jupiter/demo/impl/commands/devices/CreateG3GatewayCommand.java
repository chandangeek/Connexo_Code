package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.commands.ActivateDevicesCommand;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CreateG3GatewayCommand {

    static final String GATEWAY_NAME = "Demo board RTU+Server G3";
    static final String GATEWAY_SERIAL = "660-05A043-1428";
    static final String SECURITY_PROPERTY_SET_NAME = "High level authentication - No encryption";
    static final String REQUIRED_PLUGGABLE_CLASS_NAME = "OutboundTcpIp";

    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<DeviceBuilder> deviceBuilderProvider;
    private final Provider<ActivateDevicesCommand> lifecyclePostBuilder;

    private Map<ComTaskTpl, ComTask> comTasks;
    private String name = GATEWAY_NAME;
    private String serialNumber = GATEWAY_SERIAL;

    @Inject
    public CreateG3GatewayCommand(DeviceService deviceService,
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

    public void setGatewayName(String name) {
        this.name = name;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void run() {
        // 1. Some basic checks
        Optional<Device> device = deviceService.findDeviceByName(name);
        if (device.isPresent()) {
            System.out.println("Nothing was created since a device with name '" + GATEWAY_NAME + "' already exists!");
            return;
        }
        Optional<ConnectionTypePluggableClass> pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName(REQUIRED_PLUGGABLE_CLASS_NAME);
        if (!pluggableClass.isPresent()) {
            System.out.println("Nothing was created since the required pluggable class '" + REQUIRED_PLUGGABLE_CLASS_NAME + "' couldn't be found!");
            return;
        }

        // 2. Find or create required objects
        findOrCreateRequiredObjects();

        // 3. Create the device type
        DeviceType deviceType = (Builders.from(DeviceTypeTpl.RTU_Plus_G3).get());

        // 4. Create the configuration
        DeviceConfiguration configuration = createG3DeviceConfiguration(deviceType);

        // 5. Create the gateway device (and set it to the 'Active' life cycle state)
        lifecyclePostBuilder.get()
                .setDevices(Collections.singletonList(createG3GatewayDevice(configuration)))
                .run();
    }

    private DeviceConfiguration createG3DeviceConfiguration(DeviceType deviceType) {
        DeviceConfiguration config = Builders.from(DeviceConfigurationTpl.RTU_Plus_G3).withDeviceType(deviceType)
                .withCanActAsGateway(true)
                .withDirectlyAddressable(true)
                .withPostBuilder(this.connectionMethodsProvider.get().withRetryDelay(5))
                .get();
        if (!config.isActive()) {
            config.activate();
        }
        return config;
    }

    private void findOrCreateRequiredObjects() {
        comTasks = new HashMap<>();
        findOrCreateComTask(ComTaskTpl.TOPOLOGY_UPDATE);
    }

    private ComTask findOrCreateComTask(ComTaskTpl comTaskTpl) {
        return comTasks.put(comTaskTpl, Builders.from(comTaskTpl).get());
    }

    private Device createG3GatewayDevice(DeviceConfiguration configuration) {
        Device device = deviceBuilderProvider.get()
                .withName(name)
            .withSerialNumber(serialNumber)
            .withDeviceConfiguration(configuration)
            .withYearOfCertification(2015)
            .get();
        addConnectionTasksToDevice(device);
        addSecurityPropertiesToDevice(device);
        addComTaskToDevice(device, ComTaskTpl.TOPOLOGY_UPDATE);
        device.setProtocolProperty("Short_MAC_address", BigDecimal.ZERO);
        device.save();
        return device;
    }


    private void addConnectionTasksToDevice(Device device) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
        ScheduledConnectionTask deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
            .setComPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get())
            .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
            .setNextExecutionSpecsFrom(null)
            .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
            .setProperty(ConnectionTypePropertySpecName.OUTBOUND_IP_HOST.propertySpecName(), "10.0.0.135")
            .setProperty(ConnectionTypePropertySpecName.OUTBOUND_IP_PORT_NUMBER.propertySpecName(), new BigDecimal(4059))
            .setNumberOfSimultaneousConnections(1)
            .add();
        connectionTaskService.setDefaultConnectionTask(deviceConnectionTask);
    }

    private void addComTaskToDevice(Device device, ComTaskTpl comTask) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        ComTaskEnablement taskEnablement = configuration.getComTaskEnablementFor(comTasks.get(comTask)).get();
        device.newManuallyScheduledComTaskExecution(taskEnablement, null).add();
    }

    private void addSecurityPropertiesToDevice(Device device) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        SecurityPropertySet securityPropertySet =
                configuration
                        .getSecurityPropertySets()
                        .stream()
                        .filter(sps -> SECURITY_PROPERTY_SET_NAME.equals(sps.getName()))
                        .findFirst()
                        .orElseThrow(() -> new UnableToCreate("No securityPropertySet with name" + SECURITY_PROPERTY_SET_NAME + "."));
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey(), BigDecimal.ONE);
        securityPropertySet
                .getPropertySpecs()
                .stream()
                .filter(ps -> SecurityPropertySpecName.AUTHENTICATION_KEY.getKey().equals(ps.getName()))
                .findFirst()
                .ifPresent(ps -> typedProperties.setProperty(ps.getName(), ps.getValueFactory().fromStringValue("00112233445566778899AABBCCDDEEFF")));
        securityPropertySet
                .getPropertySpecs()
                .stream()
                .filter(ps -> SecurityPropertySpecName.ENCRYPTION_KEY.getKey().equals(ps.getName()))
                .findFirst()
                .ifPresent(ps -> typedProperties.setProperty(ps.getName(), ps.getValueFactory().fromStringValue("11223344556677889900AABBCCDDEEFF")));
        device.setSecurityProperties(securityPropertySet, typedProperties);
    }
}