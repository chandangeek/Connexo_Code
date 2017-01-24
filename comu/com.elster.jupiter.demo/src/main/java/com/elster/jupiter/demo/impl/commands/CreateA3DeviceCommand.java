package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl;
import com.elster.jupiter.demo.impl.templates.LogBookTypeTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.demo.impl.templates.RegisterTypeTpl;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//TODO reuse code
public class CreateA3DeviceCommand {
    public static final String SECURITY_PROPERTY_NAME = "Read only authentication and Message Encryption/Authentication";

    private final ProtocolPluggableService protocolPluggableService;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<DeviceBuilder> deviceBuilderProvider;
    private final DeviceService deviceService;

    private Map<ComTaskTpl, ComTask> comTasks;
    private Map<LogBookTypeTpl, LogBookType> logBookTypes;
    private Map<LoadProfileTypeTpl, LoadProfileType> loadProfileTypes;
    private Map<RegisterTypeTpl, RegisterType> registerTypes;

    @Inject
    public CreateA3DeviceCommand(
            ProtocolPluggableService protocolPluggableService,
            ConnectionTaskService connectionTaskService,
            Provider<DeviceBuilder> deviceBuilderProvider,
            DeviceService deviceService) {
        this.protocolPluggableService = protocolPluggableService;
        this.connectionTaskService = connectionTaskService;
        this.deviceBuilderProvider = deviceBuilderProvider;
        this.deviceService = deviceService;
    }

    public void run() {
        String a3name = Constants.Device.A3_DEVICE + Constants.Device.A3_SERIAL_NUMBER;
        Optional<Device> device = deviceService.findDeviceByName(a3name);
        if (device.isPresent()) {
            System.out.println("Device with name '" + a3name + "' already exists!");
            return;
        }
        findRequiredObjects();
        DeviceType a3DeviceType = Builders.from(DeviceTypeTpl.Alpha_A3).get();

        String a3DeviceConfName = "Extended config";
        DeviceConfiguration configuration = a3DeviceType.getConfigurations().stream().filter(dc -> a3DeviceConfName.equals(dc.getName())).findFirst()
                .orElseGet(() -> createA3DeviceConfiguration(a3DeviceType, a3DeviceConfName));

        createA3Device(a3name, configuration);
    }

    private void findRequiredObjects() {
        findRegisterTypes();
        findLogBooks();
        findComTasks();
        findLoadProfiles();
    }

    private void findRegisterTypes() {
        registerTypes = new HashMap<>();
        registerTypes.put(RegisterTypeTpl.DELTA_A_PLUS_ALL_PHASES, Builders.from(RegisterTypeTpl.DELTA_A_PLUS_ALL_PHASES).get());
        registerTypes.put(RegisterTypeTpl.DELTA_A_MINUS_ALL_PHASES, Builders.from(RegisterTypeTpl.DELTA_A_MINUS_ALL_PHASES).get());
        registerTypes.put(RegisterTypeTpl.DELRA_REACTIVE_ENERGY_PLUS, Builders.from(RegisterTypeTpl.DELRA_REACTIVE_ENERGY_PLUS).get());
        registerTypes.put(RegisterTypeTpl.DELRA_REACTIVE_ENERGY_MINUS, Builders.from(RegisterTypeTpl.DELRA_REACTIVE_ENERGY_MINUS).get());

        registerTypes.put(RegisterTypeTpl.BULK_A_PLUS_ALL_PHASES, Builders.from(RegisterTypeTpl.BULK_A_PLUS_ALL_PHASES).get());
        registerTypes.put(RegisterTypeTpl.BULK_A_MINUS_ALL_PHASES, Builders.from(RegisterTypeTpl.BULK_A_MINUS_ALL_PHASES).get());
        registerTypes.put(RegisterTypeTpl.BULK_REACTIVE_ENERGY_PLUS, Builders.from(RegisterTypeTpl.BULK_REACTIVE_ENERGY_PLUS).get());
        registerTypes.put(RegisterTypeTpl.BULK_REACTIVE_ENERGY_MINUS, Builders.from(RegisterTypeTpl.BULK_REACTIVE_ENERGY_MINUS).get());
    }

    private void findLogBooks() {
        logBookTypes = new HashMap<>();
        EnumSet.of(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG).stream().forEach(
                tpl->logBookTypes.put(tpl, Builders.from(tpl).get())
        );
    }

    private void findComTasks() {
        comTasks = new HashMap<>();
        // findComTask(ComTaskTpl.READ_ALL);
        findComTask(ComTaskTpl.READ_LOAD_PROFILE_DATA);
        findComTask(ComTaskTpl.READ_LOG_BOOK_DATA);
        findComTask(ComTaskTpl.READ_REGISTER_DATA);
    }

    private ComTask findComTask(ComTaskTpl comTaskTpl) {
        return comTasks.put(comTaskTpl, Builders.from(comTaskTpl).get());
    }

    private void findLoadProfiles() {
        this.loadProfileTypes = new HashMap<>();
        this.loadProfileTypes.put(LoadProfileTypeTpl.ELSTER_A3_GENERIC, Builders.from(LoadProfileTypeTpl.ELSTER_A3_GENERIC).get());
    }

    private DeviceConfiguration createA3DeviceConfiguration(DeviceType a3DeviceType, String a3DeviceConfName) {
        DeviceConfiguration configuration;
        DeviceType.DeviceConfigurationBuilder configBuilder = a3DeviceType.newConfiguration(a3DeviceConfName);
        configBuilder.description("Device configuration with delta data loadprofile");
        configBuilder.canActAsGateway(false);
        configBuilder.isDirectlyAddressable(true);

        addNumericRegistersToDeviceConfiguration(configBuilder, RegisterTypeTpl.BULK_A_PLUS_ALL_PHASES, RegisterTypeTpl.BULK_A_MINUS_ALL_PHASES, RegisterTypeTpl.BULK_REACTIVE_ENERGY_PLUS, RegisterTypeTpl.BULK_REACTIVE_ENERGY_MINUS);
        EnumSet.of(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG).stream().forEach(
                tpl->configBuilder.newLogBookSpec(logBookTypes.get(tpl))
        );
        configBuilder.newLoadProfileSpec(loadProfileTypes.get(LoadProfileTypeTpl.ELSTER_A3_GENERIC));
        configuration = configBuilder.add();
        Map<String, String> channels = new HashMap<>(4);
        channels.put(RegisterTypeTpl.DELTA_A_PLUS_ALL_PHASES.getMrid(), "0.1.128.0.0.255");
        channels.put(RegisterTypeTpl.DELTA_A_MINUS_ALL_PHASES.getMrid(), "0.2.128.0.0.255");
        channels.put(RegisterTypeTpl.DELRA_REACTIVE_ENERGY_PLUS.getMrid(), "0.3.128.0.0.255");
        channels.put(RegisterTypeTpl.DELRA_REACTIVE_ENERGY_MINUS.getMrid(), "0.4.128.0.0.255");
        addChannelsOnLoadProfileToDeviceConfiguration(configuration, channels);
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName("OutboundTcpIp").get();
        configuration
                .newPartialScheduledConnectionTask("Outbound TCP", pluggableClass, new TimeDuration(5, TimeDuration.TimeUnit.MINUTES), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get())
                .setNumberOfSimultaneousConnections(1)
                .addProperty("portNumber", new BigDecimal(1153))
                .asDefault(true).build();
        SecurityPropertySet securityPropertySet = configuration.createSecurityPropertySet(SECURITY_PROPERTY_NAME).authenticationLevel(2).encryptionLevel(2).build();
        for (DeviceSecurityUserAction action : DeviceSecurityUserAction.values()) {
            securityPropertySet.addUserAction(action);
        }
        securityPropertySet.update();
        addComTasksToDeviceConfiguration(configuration,
                ComTaskTpl.READ_LOAD_PROFILE_DATA,
                ComTaskTpl.READ_LOG_BOOK_DATA,
                ComTaskTpl.READ_REGISTER_DATA);
        configuration.activate();
        configuration.save();
        return configuration;
    }

    private void addNumericRegistersToDeviceConfiguration(DeviceType.DeviceConfigurationBuilder configurationBuilder, RegisterTypeTpl... registerTypeTpls) {
        if (registerTypeTpls != null) {
            for (RegisterTypeTpl registerTypeName : registerTypeTpls) {
                RegisterType registerType = registerTypes.get(registerTypeName);
                configurationBuilder.newNumericalRegisterSpec(registerType).overflowValue(new BigDecimal(100000000)).numberOfFractionDigits(3)
                        .overruledObisCode(new ObisCode(registerType.getObisCode().getA(), 1, registerType.getObisCode().getC(), registerType.getObisCode().getD(), registerType.getObisCode().getE(), registerType.getObisCode().getF()));
            }
        }
    }

    private void addChannelsOnLoadProfileToDeviceConfiguration(DeviceConfiguration configuration, Map<String, String> channels) {
        for (LoadProfileSpec loadProfileSpec : configuration.getLoadProfileSpecs()) {
            List<ChannelType> availableChannelTypes = loadProfileSpec.getLoadProfileType().getChannelTypes();
            for (ChannelType channelType : availableChannelTypes) {
                String mrid = channelType.getTemplateRegister().getReadingType().getMRID();
                if (channels.containsKey(mrid)) {
                    configuration.createChannelSpec(channelType, loadProfileSpec)
                            .overflow(new BigDecimal(100000000))
                            .nbrOfFractionDigits(3)
                            .overruledObisCode(ObisCode.fromString(channels.get(mrid)))
                            .add();
                }
            }
        }
    }

    private void addComTasksToDeviceConfiguration(DeviceConfiguration configuration, ComTaskTpl... names) {
        if (names != null) {
            for (ComTaskTpl comTaskTpl : names) {
                configuration.enableComTask(comTasks.get(comTaskTpl), configuration.getSecurityPropertySets().get(0), configuration.getProtocolDialectConfigurationPropertiesList().get(0))
                        .setIgnoreNextExecutionSpecsForInbound(false)
                        .setPriority(100).add().save();
            }
        }
    }

    private void createA3Device(String a3mrid, DeviceConfiguration configuration) {
        Device device = deviceBuilderProvider.get()
                .withName(a3mrid)
                .withSerialNumber(Constants.Device.A3_SERIAL_NUMBER)
                .withDeviceConfiguration(configuration)
                .withYearOfCertification(2014)
                .get();
        addConnectionTasksToDevice(device);
        addSecurityPropertiesToDevice(device);
        addComTasksToDevice(device);
        addProtocolPropertiesToDevice(device);
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
                .setProperty(ConnectionTypePropertySpecName.OUTBOUND_IP_HOST.propertySpecName(), "166.150.217.174")
                .setProperty(ConnectionTypePropertySpecName.OUTBOUND_IP_PORT_NUMBER.propertySpecName(), new BigDecimal(1153))
                .setProperty(ConnectionTypePropertySpecName.OUTBOUND_IP_CONNECTION_TIMEOUT.propertySpecName(), TimeDuration.minutes(1))
                .setNumberOfSimultaneousConnections(1)
                .add();
        connectionTaskService.setDefaultConnectionTask(deviceConnectionTask);
    }

    private void addComTasksToDevice(Device device) {
        addComTaskToDevice(device, ComTaskTpl.READ_LOG_BOOK_DATA, TimeDuration.hours(1));
        addComTaskToDevice(device, ComTaskTpl.READ_LOAD_PROFILE_DATA, TimeDuration.hours(1));
        addComTaskToDevice(device, ComTaskTpl.READ_REGISTER_DATA, TimeDuration.minutes(15));
    }

    private void addComTaskToDevice(Device device, ComTaskTpl comTask, TimeDuration every) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        ComTaskEnablement taskEnablement = configuration.getComTaskEnablementFor(comTasks.get(comTask)).get();
        device.newManuallyScheduledComTaskExecution(taskEnablement, new TemporalExpression(every)).add();
    }

    private void addSecurityPropertiesToDevice(Device device) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        SecurityPropertySet securityPropertySet = configuration.getSecurityPropertySets().stream().filter(sps -> SECURITY_PROPERTY_NAME.equals(sps.getName())).findFirst().orElseThrow(() -> new UnableToCreate(""));
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(SecurityPropertySpecName.ANSI_C12_USER_ID.getKey(), "0");
        typedProperties.setProperty(SecurityPropertySpecName.ANSI_C12_USER.getKey(), "          ");
        securityPropertySet.getPropertySpecs().stream().filter(ps -> SecurityPropertySpecName.ENCRYPTION_KEY.getKey().equals(ps.getName())).findFirst().ifPresent(
                ps -> typedProperties.setProperty(ps.getName(), ps.getValueFactory().fromStringValue("F2FE78E33DF19786BBD2E56F9E93BE88")));
        typedProperties.setProperty(SecurityPropertySpecName.ANSI_CALLED_AP_TITLE.getKey(), "1.3.6.1.4.1.33507.1919.42327");
        typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.getKey(), new Password("00000000000000000000"));
        device.setSecurityProperties(securityPropertySet, typedProperties);
    }

    private void addProtocolPropertiesToDevice(Device device) {
        //device.setProtocolDialectProperty(device.getProtocolDialects().get(0).getDeviceProtocolDialectName(), "CalledAPTitle", "1.3.6.1.4.1.33507.1919.29674");
        //device.setProtocolDialectProperty(device.getProtocolDialects().get(0).getDeviceProtocolDialectName(), "SecurityKey", "93B6F29D64C9AD7331DCCAABBB7D4680");
        //device.setProtocolDialectProperty(device.getProtocolDialects().get(0).getDeviceProtocolDialectName(), "SecurityMode", "2");
        //device.setProtocolDialectProperty(device.getProtocolDialects().get(0).getDeviceProtocolDialectName(), "SecurityLevel", "2");
    }
}
