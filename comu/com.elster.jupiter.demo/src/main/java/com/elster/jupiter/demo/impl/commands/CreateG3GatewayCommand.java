package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CreateG3GatewayCommand {

    private static final String GATEWAY_MRID = "660-05A043-1428";
    private static final String DEVICE_CONFIG_NAME = "Default";
    private static final String SECURITY_PROPERTY_SET_NAME = "High level authentication - No encryption";
    private static final String REQUIRED_PLUGGABLE_CLASS_NAME = "OutboundTcpIp";
    private static final String CONNECTION_METHOD_NAME = "Outbound TCP";

    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final ConnectionTaskService connectionTaskService;
    private ConnectionTypePluggableClass requiredPluggableClass;
    private final Provider<DeviceBuilder> deviceBuilderProvider;

    private Map<ComTaskTpl, ComTask> comTasks;

    @Inject
    public CreateG3GatewayCommand(DeviceService deviceService, ProtocolPluggableService protocolPluggableService,
                                  ConnectionTaskService connectionTaskService, Provider<DeviceBuilder> deviceBuilderProvider) {
        this.deviceService = deviceService;
        this.protocolPluggableService = protocolPluggableService;
        this.connectionTaskService = connectionTaskService;
        this.deviceBuilderProvider = deviceBuilderProvider;
    }

    public void run() {
        // 1. Some basic checks
        Optional<Device> device = deviceService.findByUniqueMrid(GATEWAY_MRID);
        if (device.isPresent()) {
            System.out.println("Nothing was created since a device with MRID '" + GATEWAY_MRID + "' already exists!");
            return;
        }
        Optional<ConnectionTypePluggableClass> pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName(REQUIRED_PLUGGABLE_CLASS_NAME);
        if (!pluggableClass.isPresent()) {
            System.out.println("Nothing was created since the required pluggable class '" + REQUIRED_PLUGGABLE_CLASS_NAME + "' couldn't be found!");
            return;
        }

        // 2. Find or create required objects
        requiredPluggableClass = pluggableClass.get();
        findOrCreateRequiredObjects();

        // 3. Create the device type
        DeviceType g3DeviceType = Builders.from(DeviceTypeTpl.RTU_Plus_G3).get();

        // 4. Create the configuration
        DeviceConfiguration configuration = g3DeviceType.getConfigurations().stream().filter(dc -> DEVICE_CONFIG_NAME.equals(dc.getName())).findFirst()
            .orElseGet(() -> createG3DeviceConfiguration(g3DeviceType, DEVICE_CONFIG_NAME));

        // 5. Create the gateway device
        createG3GatewayDevice(GATEWAY_MRID, configuration);
    }

    private DeviceConfiguration createG3DeviceConfiguration(DeviceType g3DeviceType, String deviceConfigName) {
        DeviceType.DeviceConfigurationBuilder configBuilder = g3DeviceType.newConfiguration(deviceConfigName)
            .canActAsGateway(true)
            .isDirectlyAddressable(true)
            .gatewayType(GatewayType.LOCAL_AREA_NETWORK);
        DeviceConfiguration configuration = configBuilder.add();

        configuration
            .newPartialScheduledConnectionTask(
                CONNECTION_METHOD_NAME,
                requiredPluggableClass,
                new TimeDuration(5, TimeDuration.TimeUnit.MINUTES),
                ConnectionStrategy.AS_SOON_AS_POSSIBLE)
            .comPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get())
            .asDefault(true).build();

        configuration
            .createSecurityPropertySet(SECURITY_PROPERTY_SET_NAME)
            .authenticationLevel(DlmsAuthenticationLevelMessageValues.HIGH_LEVEL_GMAC.getValue())
            .encryptionLevel(DlmsEncryptionLevelMessageValues.NO_ENCRYPTION.getValue())
            .addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1)
            .addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2)
            .addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1)
            .addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2)
            .build();

        addComTasksToDeviceConfiguration(configuration, ComTaskTpl.TOPOLOGY_UPDATE);
        configuration.activate();
        configuration.save();
        return configuration;
    }

    private void findOrCreateRequiredObjects() {
        comTasks = new HashMap<>();
        findOrCreateComTask(ComTaskTpl.TOPOLOGY_UPDATE);
    }

    private ComTask findOrCreateComTask(ComTaskTpl comTaskTpl) {
        return comTasks.put(comTaskTpl, Builders.from(comTaskTpl).get());
    }

    private void addComTasksToDeviceConfiguration(DeviceConfiguration configuration, ComTaskTpl... names) {
        if (names == null) {
            return;
        }
        for (ComTaskTpl comTaskTpl : names) {
            configuration.enableComTask(
                comTasks.get(comTaskTpl),
                configuration.getSecurityPropertySets().get(0),
                configuration.getProtocolDialectConfigurationPropertiesList().get(0))
                    .setIgnoreNextExecutionSpecsForInbound(true)
                    .setPriority(100)
                    .add()
                    .save();
        }
    }

    private void createG3GatewayDevice(String mrid, DeviceConfiguration configuration) {
        Device device = deviceBuilderProvider.get()
            .withMrid(mrid)
            .withSerialNumber(mrid)
            .withDeviceConfiguration(configuration)
            .withYearOfCertification(2015)
            .get();
        addConnectionTasksToDevice(device);
        addSecurityPropertiesToDevice(device);
        addComTaskToDevice(device, ComTaskTpl.TOPOLOGY_UPDATE);
        device.setProtocolProperty("Short_MAC_address", new BigDecimal(0));
        device.save();
    }

    private void addConnectionTasksToDevice(Device device) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
        ScheduledConnectionTask deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
            .setComPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get())
            .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
            .setNextExecutionSpecsFrom(null)
            .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
            .setProperty("host", "10.0.0.135")
            .setProperty("portNumber", new BigDecimal(4059))
            .setSimultaneousConnectionsAllowed(false)
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
        SecurityPropertySet securityPropertySet = configuration.getSecurityPropertySets().stream()
            .filter(sps -> SECURITY_PROPERTY_SET_NAME.equals(sps.getName())).findFirst().orElseThrow(() -> new UnableToCreate(""));
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty("ClientMacAddress", "1");
        securityPropertySet.getPropertySpecs().stream().filter(ps -> ps.getName().equals("AuthenticationKey")).findFirst().ifPresent(
            ps -> typedProperties.setProperty(ps.getName(), ps.getValueFactory().fromStringValue("00112233445566778899AABBCCDDEEFF")));
        securityPropertySet.getPropertySpecs().stream().filter(ps -> ps.getName().equals("EncryptionKey")).findFirst().ifPresent(
            ps -> typedProperties.setProperty(ps.getName(), ps.getValueFactory().fromStringValue("11223344556677889900AABBCCDDEEFF")));
        device.setSecurityProperties(securityPropertySet, typedProperties);
    }
}
