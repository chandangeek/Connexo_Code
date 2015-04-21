package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
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
    private ConnectionTypePluggableClass requiredPluggableClass;

    private Map<ComTaskTpl, ComTask> comTasks;

    @Inject
    public CreateG3GatewayCommand(DeviceService deviceService, ProtocolPluggableService protocolPluggableService) {
        this.deviceService = deviceService;
        this.protocolPluggableService = protocolPluggableService;
    }

    public void run() {
        // 1. Some basic checks
        Optional<Device> device = deviceService.findByUniqueMrid(GATEWAY_MRID);
        if (device.isPresent()) {
            System.out.println("Nothing was created since a device with MRID '" + GATEWAY_MRID + "' already exists!");
            return;
        }
        Optional<ConnectionTypePluggableClass> pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName(REQUIRED_PLUGGABLE_CLASS_NAME);
        if (pluggableClass.isPresent()) {
            System.out.println("Nothing was created since the required pluggable class '" + REQUIRED_PLUGGABLE_CLASS_NAME + "' couldn't be found!");
            return;
        }
        requiredPluggableClass = pluggableClass.get();
        // 2. Create the device type
        DeviceType g3DeviceType = Builders.from(DeviceTypeTpl.RTU_Plus_G3).get();
        // 3. Create the configuration
        DeviceConfiguration configuration = g3DeviceType.getConfigurations().stream().filter(dc -> DEVICE_CONFIG_NAME.equals(dc.getName())).findFirst()
            .orElseGet(() -> createG3DeviceConfiguration(g3DeviceType, DEVICE_CONFIG_NAME));
    }

    private DeviceConfiguration createG3DeviceConfiguration(DeviceType g3DeviceType, String deviceConfigName) {
        DeviceType.DeviceConfigurationBuilder configBuilder = g3DeviceType.newConfiguration(deviceConfigName)
            .canActAsGateway(true)
            .isDirectlyAddressable(true)
            .gatewayType(GatewayType.LOCAL_AREA_NETWORK);
        DeviceConfiguration configuration = configBuilder.add();

        findOrCreateRequiredObjects();
//        Map<String, String> channels = new HashMap<>(4);
//        channels.put(RegisterTypeTpl.DELTA_A_PLUS_ALL_PHASES.getMrid(), "0.1.128.0.0.255");
//        channels.put(RegisterTypeTpl.DELTA_A_MINUS_ALL_PHASES.getMrid(), "0.2.128.0.0.255");
//        channels.put(RegisterTypeTpl.DELRA_REACTIVE_ENERGY_PLUS.getMrid(), "0.3.128.0.0.255");
//        channels.put(RegisterTypeTpl.DELRA_REACTIVE_ENERGY_MINUS.getMrid(), "0.4.128.0.0.255");
//        addChannelsOnLoadProfileToDeviceConfiguration(configuration, channels);

        configuration
            .newPartialScheduledConnectionTask(
                CONNECTION_METHOD_NAME,
                requiredPluggableClass,
                new TimeDuration(5, TimeDuration.TimeUnit.MINUTES),
                ConnectionStrategy.AS_SOON_AS_POSSIBLE)
            .comPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get())
            .asDefault(true).build();

        SecurityPropertySet securityPropertySet = configuration.createSecurityPropertySet(SECURITY_PROPERTY_SET_NAME)
            .authenticationLevel(DlmsAuthenticationLevelMessageValues.HIGH_LEVEL_GMAC.getValue())
            .encryptionLevel(DlmsEncryptionLevelMessageValues.NO_ENCRYPTION.getValue())
            .addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1)
            .addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2)
            .addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1)
            .addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2)
            .build();

        addComTasksToDeviceConfiguration(configuration,
            ComTaskTpl.TOPOLOGY_UPDATE/*, ComTaskTpl.READ_LOAD_PROFILE_DATA, ComTaskTpl.READ_LOG_BOOK_DATA, ComTaskTpl.READ_REGISTER_DATA*/);
        configuration.activate();
        configuration.save();
        return configuration;
    }

    private void findOrCreateRequiredObjects() {
        comTasks = new HashMap<>();
        findOrCreateComTask(ComTaskTpl.TOPOLOGY_UPDATE);
//        findOrCreateComTask(ComTaskTpl.READ_LOAD_PROFILE_DATA);
//        findOrCreateComTask(ComTaskTpl.READ_LOG_BOOK_DATA);
//        findOrCreateComTask(ComTaskTpl.READ_REGISTER_DATA);
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

}
