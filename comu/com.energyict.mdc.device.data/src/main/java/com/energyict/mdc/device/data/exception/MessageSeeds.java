package com.energyict.mdc.device.data.exception;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.DeviceDataService;

import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 03/03/14
 * Time: 16:01
 */
public enum MessageSeeds implements MessageSeed {

    DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY(101, Constants.PHYSICAL_GATEWAY_STILL_IN_USE,"You can not delete device '{0}' because it is still used as a physical gateway for '{1}'", Level.SEVERE),
    DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY(102, Constants.COMMUNICATION_GATEWAY_STILL_IN_USE,"You can not delete device '{0}' because it is still used as a communication gateway for '{1}'", Level.SEVERE),
    DEVICE_PROPERTY_INFO_TYPE_DOENST_EXIST(103, Constants.INFOTYPE_DOESNT_EXIST,"The intotype for property value '{0}' does not exist.", Level.SEVERE),
    DEVICE_PROPERTY_NOT_ON_DEVICE_PROTOCOL(104, Constants.PROPERTY_NOT_ON_DEVICE_PROTOCOL,"The property '{0}' is not defined by the device protocol '{1}' of device '{2}'", Level.SEVERE),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getModule() {
        return DeviceDataService.COMPONENTNAME;
    }

    public static class Constants {
        public static final String NAME_REQUIRED_KEY = "DDC.X.name.required";
        public static final String VALUE_IS_REQUIRED_KEY = "DDC.X.value.required";
        public static final String DUPLICATE_DEVICE_EXTERNAL_KEY = "DDC.device.duplicateExternalName";
        public static final String GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY = "DDC.gateway.not.origin";
        public static final String PHYSICAL_GATEWAY_STILL_IN_USE = "DDC.device.delete.linked.physical.gateway";
        public static final String COMMUNICATION_GATEWAY_STILL_IN_USE = "DDC.device.delete.linked.communication.gateway";
        public static final String INFOTYPE_DOESNT_EXIST = "DDC.device.property.infotype.required";
        public static final String PROPERTY_NOT_ON_DEVICE_PROTOCOL = "DDC.not.deviceprotocol.property";

    }
}
