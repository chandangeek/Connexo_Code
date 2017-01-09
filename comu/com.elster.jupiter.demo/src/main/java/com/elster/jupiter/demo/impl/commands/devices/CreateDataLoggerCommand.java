package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.ChannelsOnDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.WebRTUNTASimultationToolPropertyPostBuilder;
import com.elster.jupiter.demo.impl.builders.device.SetDeviceInActiveLifeCycleStatePostBuilder;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.demo.impl.templates.RegisterGroupTpl;
import com.elster.jupiter.demo.impl.templates.SecurityPropertySetTpl;
import com.energyict.mdc.common.Password;
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

    private final static String DATA_LOGGER_MRID = "DemoDataLogger";
    private final static String DATA_LOGGER_SERIAL = "660-05A043-1428";
    private final static String CONNECTION_TASK_PLUGGABLE_CLASS_NAME = "OutboundTcpIp";

    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<DeviceBuilder> deviceBuilderProvider;
    private final Provider<SetDeviceInActiveLifeCycleStatePostBuilder> lifecyclePostBuilder;

    private Map<ComTaskTpl, ComTask> comTasks;
    private String mRID = DATA_LOGGER_MRID;
    private String serialNumber = DATA_LOGGER_SERIAL;

    @Inject
    public CreateDataLoggerCommand(DeviceService deviceService,
                                   ProtocolPluggableService protocolPluggableService,
                                   ConnectionTaskService connectionTaskService,
                                   Provider<DeviceBuilder> deviceBuilderProvider,
                                   Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider,
                                   Provider<SetDeviceInActiveLifeCycleStatePostBuilder> lifecyclePostBuilder) {
        this.deviceService = deviceService;
        this.protocolPluggableService = protocolPluggableService;
        this.connectionTaskService = connectionTaskService;
        this.deviceBuilderProvider = deviceBuilderProvider;
        this.connectionMethodsProvider = connectionMethodsProvider;
        this.lifecyclePostBuilder = lifecyclePostBuilder;
    }

    public void setDataLoggerMrid(String mRID) {
        this.mRID = mRID;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void run() {
        // 1. Some basic checks
        Optional<Device> device = deviceService.findByUniqueMrid(mRID);
        if (device.isPresent()) {
            System.out.println("Nothing was created since a device with MRID '" + this.mRID + "' already exists!");
            return;
        }
        Optional<ConnectionTypePluggableClass> pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName(CONNECTION_TASK_PLUGGABLE_CLASS_NAME);
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
        lifecyclePostBuilder.get().accept(createDataLoggerDevice(configuration));
//
//        //6. Create Device Group "Data loggers"
//        EndDeviceGroup dataLoggerGroup = Builders.from(DeviceGroupTpl.DATA_LOGGERS).get();
//        Builders.from(FavoriteGroupBuilder.class).withGroup(dataLoggerGroup).get();

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
                .withMrid(mRID)
                .withSerialNumber(serialNumber)
                .withDeviceConfiguration(configuration)
                .withYearOfCertification(2015)
                .withPostBuilder(new WebRTUNTASimultationToolPropertyPostBuilder())
                .get();
        addConnectionTasksToDevice(device);
        device = deviceBuilderProvider.get().withMrid(mRID).get();
        addSecurityPropertiesToDevice(device);
        device = deviceBuilderProvider.get().withMrid(mRID).get();
        addComTaskToDevice(device, ComTaskTpl.READ_DATA_LOGGER_REGISTER_DATA);
        addComTaskToDevice(device, ComTaskTpl.READ_DATA_LOGGER_LOAD_PROFILE_DATA);
        return deviceBuilderProvider.get().withMrid(mRID).get();
    }


    private void addConnectionTasksToDevice(Device device) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
        ScheduledConnectionTask deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
                .setComPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get())
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .setNextExecutionSpecsFrom(null)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                .setProperty(ConnectionTypePropertySpecName.OUTBOUND_IP_HOST.propertySpecName(), "localhost")
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
        SecurityPropertySet securityPropertySetHigh =
                configuration
                        .getSecurityPropertySets()
                        .stream()
                        .filter(sps -> SecurityPropertySetTpl.HIGH_LEVEL_NO_ENCRYPTION_MD5.getName().equals(sps.getName()))
                        .findFirst()
                        .orElseThrow(() -> new UnableToCreate("No securityPropertySet with name " + SecurityPropertySetTpl.HIGH_LEVEL_NO_ENCRYPTION_MD5.getName() + "."));
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey(), BigDecimal.ONE);
        typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), new Password("ntaSim"));
        securityPropertySetHigh
                .getPropertySpecs()
                .stream()
                .filter(ps -> SecurityPropertySpecName.AUTHENTICATION_KEY.getKey().equals(ps.getName()))
                .findFirst()
                .ifPresent(ps -> typedProperties.setProperty(ps.getName(), ps.getValueFactory().fromStringValue("00112233445566778899AABBCCDDEEFF")));
        securityPropertySetHigh
                .getPropertySpecs()
                .stream()
                .filter(ps -> SecurityPropertySpecName.ENCRYPTION_KEY.getKey().equals(ps.getName()))
                .findFirst()
                .ifPresent(ps -> typedProperties.setProperty(ps.getName(), ps.getValueFactory().fromStringValue("11223344556677889900AABBCCDDEEFF")));
        device.setSecurityProperties(securityPropertySetHigh, typedProperties);

        SecurityPropertySet securityPropertySetNone =
                configuration
                        .getSecurityPropertySets()
                        .stream()
                        .filter(sps -> SecurityPropertySetTpl.NO_SECURITY.getName().equals(sps.getName()))
                        .findFirst()
                        .orElseThrow(() -> new UnableToCreate("No securityPropertySet with name " + SecurityPropertySetTpl.NO_SECURITY.getName() + "."));
        TypedProperties typedPropertiesNone = TypedProperties.empty();
        typedPropertiesNone.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey(), BigDecimal.ONE);
        device.setSecurityProperties(securityPropertySetNone, typedProperties);
        device.save();
    }

}