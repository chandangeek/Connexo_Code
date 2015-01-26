package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.factories.DeviceFactory;
import com.elster.jupiter.demo.impl.factories.ReadingTypeFactory;
import com.elster.jupiter.demo.impl.finders.ComTaskFinder;
import com.elster.jupiter.demo.impl.finders.LogBookFinder;
import com.elster.jupiter.demo.impl.finders.OutboundComPortPoolFinder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateA3DeviceCommand {
    public static final String DELTA_A_PLUS_ALL_PHASES = "0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String DELTA_A_MINUS_ALL_PHASES = "0.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String DELRA_REACTIVE_ENERGY_PLUS = "0.0.0.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0";
    public static final String DELRA_REACTIVE_ENERGY_MINUS = "0.0.0.4.3.1.12.0.0.0.0.0.0.0.0.3.73.0";

    public static final String BULK_A_PLUS_ALL_PHASES = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String BULK_A_MINUS_ALL_PHASES = "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String BULK_REACTIVE_ENERGY_PLUS = "0.0.0.1.2.1.12.0.0.0.0.0.0.0.0.3.73.0";
    public static final String BULK_REACTIVE_ENERGY_MINUS = "0.0.0.1.3.1.12.0.0.0.0.0.0.0.0.3.73.0";
    public static final String SECURITY_PROPERTY_NAME = "Read only authentication and Message Encryption/Authentication";

    private final ProtocolPluggableService protocolPluggableService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<LogBookFinder> logBookFinderProvider;
    private final Provider<OutboundComPortPoolFinder> outboundComPortPoolFinderProvider;
    private final Provider<DeviceFactory> deviceFactoryProvider;
    private final Provider<ComTaskFinder> comTaskFinderProvider;
    private final MasterDataService masterDataService;
    private final MeteringService meteringService;
    private final Provider<ReadingTypeFactory> readingTypeFactoryProvider;
    private final DeviceService deviceService;

    private Map<String, ComTask> comTasks;
    private Map<String, LogBookType> logBookTypes;
    private Map<String, LoadProfileType> loadProfileTypes;
    private Map<String, RegisterType> registerTypes;

    @Inject
    public CreateA3DeviceCommand(
             ProtocolPluggableService protocolPluggableService,
             DeviceConfigurationService deviceConfigurationService,
             ConnectionTaskService connectionTaskService,
             Provider<LogBookFinder> logBookFinderProvider,
             Provider<OutboundComPortPoolFinder> outboundComPortPoolFinderProvider,
             Provider<DeviceFactory> deviceFactoryProvider,
             Provider<ComTaskFinder> comTaskFinderProvider,
             MasterDataService masterDataService,
             MeteringService meteringService,
             Provider<ReadingTypeFactory> readingTypeFactoryProvider,
             DeviceService deviceService) {
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.connectionTaskService = connectionTaskService;
        this.logBookFinderProvider = logBookFinderProvider;
        this.outboundComPortPoolFinderProvider = outboundComPortPoolFinderProvider;
        this.deviceFactoryProvider = deviceFactoryProvider;
        this.comTaskFinderProvider = comTaskFinderProvider;
        this.masterDataService = masterDataService;
        this.meteringService = meteringService;
        this.readingTypeFactoryProvider = readingTypeFactoryProvider;
        this.deviceService = deviceService;
    }

    public void run(){
        String a3mrid = Constants.Device.A3_DEVICE + Constants.Device.A3_SERIAL_NUMBER;
        Device device = deviceService.findByUniqueMrid(a3mrid);
        if (device != null){
            System.out.println("Device with mrid '" + a3mrid + "' already exists!");
            return;
        }
        findRequiredObjects();
        String a3DeviceTypeName = Constants.DeviceType.Alpha_A3.getName();
        DeviceType a3DeviceType = deviceConfigurationService.findDeviceTypeByName(a3DeviceTypeName)
                .orElseGet(() -> createA3DeviceType(a3DeviceTypeName));

        String a3DeviceConfName = "Extended config";
        DeviceConfiguration configuration =  a3DeviceType.getConfigurations().stream().filter(dc -> a3DeviceConfName.equals(dc.getName())).findFirst()
                .orElseGet(() -> createA3DeviceConfiguration(a3DeviceType, a3DeviceConfName));

        createA3Device(a3mrid, configuration);
    }

    private void findRequiredObjects(){
        findReadingTypes();
        findPhenomenons();
        findRegisterTypes();
        findLogBooks();
        findComTasks();
        findLoadProfiles();
    }

    private void findReadingTypes(){
        findOrCreateReadingType("0.0.2.1.2.1.12.0.0.0.0.0.0.0.0.0.73.0", "Bulk Reactive Energy + all phases");
        findOrCreateReadingType("0.0.2.1.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "Bulk Reactive Energy + all phases");
        findOrCreateReadingType("0.0.2.4.2.1.12.0.0.0.0.0.0.0.0.0.73.0", "Delta Reactive Energy + all phases");
        findOrCreateReadingType("0.0.2.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "Delta Reactive Energy + all phases");
        findOrCreateReadingType("0.0.2.4.2.1.12.0.0.0.0.0.0.0.0.6.73.0", "Delta Reactive Energy + all phases");
        findOrCreateReadingType("0.0.2.1.2.1.12.0.0.0.0.0.0.0.0.6.73.0", "Bulk Reactive Energy + all phases");
        findOrCreateReadingType("0.0.2.1.3.1.12.0.0.0.0.0.0.0.0.0.73.0", "Bulk Reactive Energy - all phases");
        findOrCreateReadingType("0.0.2.1.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "Bulk Reactive Energy - all phases");
        findOrCreateReadingType("0.0.2.1.3.1.12.0.0.0.0.0.0.0.0.6.73.0", "Bulk Reactive Energy - all phases");
        findOrCreateReadingType("0.0.2.4.3.1.12.0.0.0.0.0.0.0.0.0.73.0", "Delta Reactive Energy - all phases");
        findOrCreateReadingType("0.0.2.4.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "Delta Reactive Energy - all phases");
        findOrCreateReadingType("0.0.2.4.3.1.12.0.0.0.0.0.0.0.0.6.73.0", "Delta Reactive Energy - all phases");
        findOrCreateReadingType("0.0.0.1.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "Bulk Reactive Energy + all phases");
        findOrCreateReadingType("0.0.0.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "Delta Reactive Energy + all phases");
        findOrCreateReadingType("0.0.0.4.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "Delta Reactive Energy - all phases");
        findOrCreateReadingType("0.0.0.1.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "Bulk Reactive Energy - all phases");
    }

    private ReadingType findOrCreateReadingType(String mrid, String description){
        return meteringService.getReadingType(mrid).orElseGet(() -> createReadingType(mrid, description));
    }

    private ReadingType createReadingType(String mrid, String description){
        return readingTypeFactoryProvider.get().withMrid(mrid).withAlias(description).get();
    }

    private void findPhenomenons(){

    }

    private void findRegisterTypes(){
        registerTypes = new HashMap<>();
        registerTypes.put(DELTA_A_PLUS_ALL_PHASES, findOrCreateRegisterType(DELTA_A_PLUS_ALL_PHASES, "1.0.1.6.0.255"));
        registerTypes.put(DELTA_A_MINUS_ALL_PHASES, findOrCreateRegisterType(DELTA_A_MINUS_ALL_PHASES, "1.0.2.6.0.255"));
        registerTypes.put(DELRA_REACTIVE_ENERGY_PLUS, findOrCreateRegisterType(DELRA_REACTIVE_ENERGY_PLUS, "1.0.3.6.0.255"));
        registerTypes.put(DELRA_REACTIVE_ENERGY_MINUS, findOrCreateRegisterType(DELRA_REACTIVE_ENERGY_MINUS, "1.0.4.6.0.255"));

        registerTypes.put(BULK_A_PLUS_ALL_PHASES, findOrCreateRegisterType(BULK_A_PLUS_ALL_PHASES, "1.0.1.8.0.255"));
        registerTypes.put(BULK_A_MINUS_ALL_PHASES, findOrCreateRegisterType(BULK_A_MINUS_ALL_PHASES, "1.0.2.8.0.255"));
        registerTypes.put(BULK_REACTIVE_ENERGY_PLUS, findOrCreateRegisterType(BULK_REACTIVE_ENERGY_PLUS, "1.0.3.8.0.255"));
        registerTypes.put(BULK_REACTIVE_ENERGY_MINUS, findOrCreateRegisterType(BULK_REACTIVE_ENERGY_MINUS, "1.0.4.8.0.255"));
    }

    private RegisterType findOrCreateRegisterType(String mRid, String obisCode){
        ReadingType readingType = meteringService.getReadingType(mRid).orElseThrow( () -> new UnableToCreate("Unable to find reading type with mrid = " + mRid));
        return masterDataService.findRegisterTypeByReadingType(readingType).orElseGet(() -> createRegisterType(readingType, obisCode));
    }

    private RegisterType createRegisterType(ReadingType readingType, String obisCode) {
        String multiplier = readingType.getMultiplier().getSymbol();
        String symbol = readingType.getUnit().getUnit().getSymbol();
        Unit unit = Unit.get(multiplier + symbol);
        if (unit == null){
            // try unit in lower case
            unit = Unit.get(multiplier + symbol.toLowerCase());
            if (unit == null){
                unit = Unit.get(BaseUnit.UNITLESS);
            }
        }
        RegisterType registerType = masterDataService.newRegisterType(
                readingType.getName(),
                ObisCode.fromString(obisCode),
                unit,
                readingType,
                readingType.getTou());
        registerType.save();
        return registerType;
    }

    private void findLogBooks(){
        logBookTypes = new HashMap<>();
        String logBookName = Constants.LogBookType.GENERIC_LOGBOOK;
        logBookTypes.put(logBookName, logBookFinderProvider.get().withName(logBookName).find());
    }

    private void findComTasks(){
        comTasks = new HashMap<>();
        findComTask(Constants.CommunicationTask.READ_ALL);
        findComTask(Constants.CommunicationTask.READ_LOAD_PROFILE_DATA);
        findComTask(Constants.CommunicationTask.READ_LOG_BOOK_DATA);
        findComTask(Constants.CommunicationTask.READ_REGISTER_DATA);
    }

    private ComTask findComTask(String comTaskName) {
        return comTasks.put(comTaskName, comTaskFinderProvider.get().withName(comTaskName).find());
    }

    private void findLoadProfiles(){
        this.loadProfileTypes = new HashMap<>();
        String loadProfileName = Constants.LoadProfileType.ELSTER_A3_GENERIC;
        String obisCode = "0.15.99.1.0.255";
        TimeDuration loadProfileInterval = TimeDuration.minutes(15);
        List<String> registerTypes = Arrays.asList(
            DELTA_A_PLUS_ALL_PHASES, DELTA_A_MINUS_ALL_PHASES, DELRA_REACTIVE_ENERGY_PLUS, DELRA_REACTIVE_ENERGY_MINUS
        );

        this.loadProfileTypes.put(loadProfileName, findOrCreateLoadProfileType(loadProfileName, obisCode, loadProfileInterval, registerTypes));
    }

    private LoadProfileType findOrCreateLoadProfileType(String loadProfileName, String obisCode, TimeDuration loadProfileInterval, List<String> registerTypes) {
        List<LoadProfileType> loadProfileTypes = masterDataService.findLoadProfileTypesByName(loadProfileName);
        LoadProfileType loadProfileType = null;
        if (loadProfileTypes.isEmpty()){
            loadProfileType = masterDataService.newLoadProfileType(loadProfileName, ObisCode.fromString(obisCode), loadProfileInterval,
                    registerTypes.stream().map(mrid -> CreateA3DeviceCommand.this.registerTypes.get(mrid)).collect(Collectors.toList()));
            loadProfileType.save();
        } else {
            loadProfileType = loadProfileTypes.get(0);
        }
        return loadProfileType;
    }

    private DeviceType createA3DeviceType(String a3DeviceTypeName) {
        System.out.println("==> Unable to find a device type with name = " + a3DeviceTypeName + ". Create a new one");
        DeviceType a3DeviceType;List<DeviceProtocolPluggableClass> alphaA3Protocols = protocolPluggableService.findDeviceProtocolPluggableClassesByClassName("com.energyict.protocolimpl.elster.a3.AlphaA3");
        if (alphaA3Protocols.isEmpty()){
            throw new IllegalStateException("Unable to retrieve the ALPHA_A3 protocol. Please check that license was correctly installed and that indexing process was finished for protocols.");
        }
        a3DeviceType = deviceConfigurationService.newDeviceType(a3DeviceTypeName, alphaA3Protocols.get(0));
        a3DeviceType.addLogBookType(logBookTypes.get(Constants.LogBookType.GENERIC_LOGBOOK));
        a3DeviceType.addLoadProfileType(loadProfileTypes.get(Constants.LoadProfileType.ELSTER_A3_GENERIC));
        a3DeviceType.addRegisterType(registerTypes.get(BULK_A_PLUS_ALL_PHASES));
        a3DeviceType.addRegisterType(registerTypes.get(BULK_A_MINUS_ALL_PHASES));
        a3DeviceType.addRegisterType(registerTypes.get(BULK_REACTIVE_ENERGY_PLUS));
        a3DeviceType.addRegisterType(registerTypes.get(BULK_REACTIVE_ENERGY_MINUS));
        a3DeviceType.save();
        return a3DeviceType;
    }

    private DeviceConfiguration createA3DeviceConfiguration(DeviceType a3DeviceType, String a3DeviceConfName) {
        DeviceConfiguration configuration;
        DeviceType.DeviceConfigurationBuilder configBuilder = a3DeviceType.newConfiguration(a3DeviceConfName);
        configBuilder.description("Device configuration with delta data loadprofile");
        configBuilder.canActAsGateway(false);
        configBuilder.isDirectlyAddressable(true);

        addNumericRegistersToDeviceConfiguration(configBuilder, BULK_A_PLUS_ALL_PHASES, BULK_A_MINUS_ALL_PHASES, BULK_REACTIVE_ENERGY_PLUS, BULK_REACTIVE_ENERGY_MINUS);
        configBuilder.newLogBookSpec(logBookTypes.get(Constants.LogBookType.GENERIC_LOGBOOK));
        configBuilder.newLoadProfileSpec(loadProfileTypes.get(Constants.LoadProfileType.ELSTER_A3_GENERIC));
        configuration = configBuilder.add();
        Map<String, String> channels = new HashMap<>(4);
        channels.put(DELTA_A_PLUS_ALL_PHASES, "0.1.128.0.0.255");
        channels.put(DELTA_A_MINUS_ALL_PHASES, "0.2.128.0.0.255");
        channels.put(DELRA_REACTIVE_ENERGY_PLUS, "0.3.128.0.0.255");
        channels.put(DELRA_REACTIVE_ENERGY_MINUS, "0.4.128.0.0.255");
        addChannelsOnLoadProfileToDeviceConfiguration(configuration, channels);
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName("OutboundTcpIp").get();
        configuration
                .newPartialScheduledConnectionTask("Outbound TCP", pluggableClass, new TimeDuration(5, TimeDuration.TimeUnit.MINUTES), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPoolFinderProvider.get().withName(Constants.ComPortPool.ORANGE).find())
                .addProperty("portNumber", new BigDecimal(1153))
                .asDefault(true).build();
        SecurityPropertySet securityPropertySet = configuration.createSecurityPropertySet(SECURITY_PROPERTY_NAME).authenticationLevel(2).encryptionLevel(2).build();
        for (DeviceSecurityUserAction action : DeviceSecurityUserAction.values()) {
            securityPropertySet.addUserAction(action);
        }
        securityPropertySet.update();
        addComTasksToDeviceConfiguration(configuration,
               Constants.CommunicationTask.READ_LOAD_PROFILE_DATA,
                Constants.CommunicationTask.READ_LOG_BOOK_DATA,
                Constants.CommunicationTask.READ_REGISTER_DATA);
        configuration.activate();
        configuration.save();
        return configuration;
    }

    private void addNumericRegistersToDeviceConfiguration(DeviceType.DeviceConfigurationBuilder configurationBuilder, String... registerTypeNames) {
        if (registerTypeNames != null){
            for (String registerTypeName : registerTypeNames) {
                RegisterType registerType = registerTypes.get(registerTypeName);
                configurationBuilder.newNumericalRegisterSpec(registerType).setOverflowValue(new BigDecimal(100000000)).setNumberOfFractionDigits(3).setNumberOfDigits(8).setMultiplier(new BigDecimal(1))
                    .setOverruledObisCode(new ObisCode(registerType.getObisCode().getA(), 1, registerType.getObisCode().getC(), registerType.getObisCode().getD(), registerType.getObisCode().getE(), registerType.getObisCode().getF()));
            }
        }
    }

    private void addChannelsOnLoadProfileToDeviceConfiguration(DeviceConfiguration configuration, Map<String, String> channels) {
        for (LoadProfileSpec loadProfileSpec : configuration.getLoadProfileSpecs()) {
            List<ChannelType> availableChannelTypes = loadProfileSpec.getLoadProfileType().getChannelTypes();
            for (ChannelType channelType : availableChannelTypes) {
                String mrid = channelType.getTemplateRegister().getReadingType().getMRID();
                if (channels.containsKey(mrid)){
                    configuration.createChannelSpec(channelType, channelType.getPhenomenon(), loadProfileSpec)
                            .setMultiplier(new BigDecimal(1))
                            .setOverflow(new BigDecimal(100000000))
                            .setNbrOfFractionDigits(3)
                            .setOverruledObisCode(ObisCode.fromString(channels.get(mrid)))
                            .add();
                }
            }
        }
    }

    private void addComTasksToDeviceConfiguration(DeviceConfiguration configuration, String... names){
        if (names != null) {
            for (String name : names) {
                configuration.enableComTask(comTasks.get(name), configuration.getSecurityPropertySets().get(0))
                        .setIgnoreNextExecutionSpecsForInbound(true)
                        .setPriority(100)
                        .setProtocolDialectConfigurationProperties(configuration.getProtocolDialectConfigurationPropertiesList().get(0)).add().save();
            }
        }
    }

    private void createA3Device(String a3mrid, DeviceConfiguration configuration) {
        Device device = deviceFactoryProvider.get()
                .withMrid(a3mrid)
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
                .setComPortPool(outboundComPortPoolFinderProvider.get().withName(Constants.ComPortPool.ORANGE).find())
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .setNextExecutionSpecsFrom(null)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                .setProperty("host", "166.150.217.174")
                .setProperty("portNumber", new BigDecimal(1153))
                .setProperty("connectionTimeout", TimeDuration.minutes(1))
                .setSimultaneousConnectionsAllowed(false)
                .add();
        connectionTaskService.setDefaultConnectionTask(deviceConnectionTask);
    }

    private void addComTasksToDevice(Device device) {
        addComTaskToDevice(device, Constants.CommunicationTask.READ_LOG_BOOK_DATA, TimeDuration.hours(1));
        addComTaskToDevice(device, Constants.CommunicationTask.READ_LOAD_PROFILE_DATA, TimeDuration.hours(1));
        addComTaskToDevice(device, Constants.CommunicationTask.READ_REGISTER_DATA, TimeDuration.minutes(15));
    }

    private void addComTaskToDevice(Device device, String comTask, TimeDuration every) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        ComTaskEnablement taskEnablement = configuration.getComTaskEnablementFor(comTasks.get(comTask)).get();
        device.newManuallyScheduledComTaskExecution(taskEnablement, configuration.getProtocolDialectConfigurationPropertiesList().get(0), new TemporalExpression(every)).add();
    }

    private void addSecurityPropertiesToDevice(Device device) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        SecurityPropertySet securityPropertySet = configuration.getSecurityPropertySets().stream().filter(sps -> SECURITY_PROPERTY_NAME.equals(sps.getName())).findFirst().orElseThrow(() -> new UnableToCreate(""));
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty("C12UserId", "0");
        typedProperties.setProperty("C12User", "          ");
        securityPropertySet.getPropertySpecs().stream().filter(ps -> ps.getName().equals("EncryptionKey")).findFirst().ifPresent(
                ps -> typedProperties.setProperty(ps.getName(), ps.getValueFactory().fromStringValue("F2FE78E33DF19786BBD2E56F9E93BE88")));
        typedProperties.setProperty("AnsiCalledAPTitle", "1.3.6.1.4.1.33507.1919.42327");
        typedProperties.setProperty("Password", new Password("00000000000000000000"));
        device.setSecurityProperties(securityPropertySet, typedProperties);
    }

    private void addProtocolPropertiesToDevice(Device device) {
        //device.setProtocolDialectProperty(device.getProtocolDialects().get(0).getDeviceProtocolDialectName(), "CalledAPTitle", "1.3.6.1.4.1.33507.1919.29674");
        //device.setProtocolDialectProperty(device.getProtocolDialects().get(0).getDeviceProtocolDialectName(), "SecurityKey", "93B6F29D64C9AD7331DCCAABBB7D4680");
        //device.setProtocolDialectProperty(device.getProtocolDialects().get(0).getDeviceProtocolDialectName(), "SecurityMode", "2");
        //device.setProtocolDialectProperty(device.getProtocolDialects().get(0).getDeviceProtocolDialectName(), "SecurityLevel", "2");
    }
}
