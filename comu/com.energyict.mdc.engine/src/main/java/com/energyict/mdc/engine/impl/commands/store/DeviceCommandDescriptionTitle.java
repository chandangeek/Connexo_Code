package com.energyict.mdc.engine.impl.commands.store;

/**
 * Enum containing an entry for all possible {@link DeviceCommand DeviceCommands}.
 * This enum can be used to provide a readable description for each of the {@link DeviceCommand DeviceCommands}
 *
 * @author sva
 * @since 5/12/13 - 15:19
 */
public enum DeviceCommandDescriptionTitle {

    CollectedDeviceCacheCommand         ("Collected device cache"),
    CollectedLogBookDeviceCommand       ("Collected logbook data"),
    CollectedRegisterListDeviceCommand  ("Collected register data"),
    CollectedMessageListDeviceCommand   ("Collected message data"),
    CollectedLoadProfileDeviceCommand   ("Collected load profile data"),
    CollectedDeviceTopologyDeviceCommand("Collected device topology"),
    UpdateDeviceIpAddress               ("Update device IP address"),
    UpdateDeviceProtocolProperty        ("Update device protocol property"),
    StoreConfigurationUserFile          ("Store configuration user file"),
    CreateNoLogBooksForDeviceEvent      ("No logbooks for device event"),
    UpdateDeviceMessage                 ("Update device message"),
    CTRUpdateDeviceMessage              ("Update device message"),
    CreateOutboundComSession            ("Create outbound comSession command"),
    CreateInboundComSession             ("Create inbound comSession command"),
    NoopDeviceCommand                   ("No operations device command"),
    ComSessionRootDeviceCommand         ("ComSession device command"),
    ComTaskExecutionRootDeviceCommand   ("ComTask device command"),
    CompositeDeviceCommandImpl          ("Aggregated device command"),
    UnknownCommand                      ("Unknown device command");

    private String description;

    private DeviceCommandDescriptionTitle(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static <T extends DeviceCommand> DeviceCommandDescriptionTitle from(Class<T> commandClass) {
        for (DeviceCommandDescriptionTitle each : values()) {
            if (each.name().equals(commandClass.getSimpleName())) {
                return each;
            }
        }
        return UnknownCommand;
    }

}