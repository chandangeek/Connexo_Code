package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.util.exception.MessageSeed;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    FIELD_IS_REQUIRED(1, "DCR.RequiredField", "Field is required"),
    PROTOCOL_INVALID_NAME(2,"DCR.deviceType.no.such.protocol", "A protocol with name ''{0}'' does not exist"),
    NO_LOGBOOK_TYPE_ID_FOR_ADDING(3,"DCR.NoLogBookTypeIdForAdding", "User should specify ids of LogBook Type for adding"),
    NO_LOGBOOK_TYPE_FOUND(4, "DCR.NoLogBookTypeFound", "No LogBook type with id {0}"),
    NO_LOGBOOK_SPEC_FOUND(5, "DCR.NoLogBookSpecFound", "No LogBook configuration with id {0}"),
    NO_LOAD_PROFILE_TYPE_ID_FOR_ADDING(6, "DCR.NoLoadProfileTypeIdForAdding", "User should specify ids of Load Profile Type for adding"),
    NO_LOAD_PROFILE_TYPE_FOUND(7, "DCR.NoLoadProfileTypeFound", "No Load Profile type with id {0}"),
    NO_PHENOMENON_FOUND(8, "DCR.NoPhenomenonFound", "No Phenomenon with id {0}"),
    NO_CHANNEL_SPEC_FOUND(9, "DCR.NoChannelSpecFound", "No channel specification with id {0}"),
    INVALID_REFERENCE_TO_REGISTER_MAPPING(10, "DCR.NoSuchRegisterMapping", "Register type could not be found"),
    DUPLICATE_OBISCODE(11, "DCR.DuplicateObisCode", "A register mapping with obis code ''{0}'', phenomenon ''{1}'' and time of use ''{2}'' already exists", Layer.UI),
    AS_SOON_AS_POSSIBLE(12, "DCR.asSoonAsPossible", "As soon a possible", Layer.UI),
    MINIMIZE_CONNECTIONS(13, "DCR.minimizeConnections", "Minimize connections", Layer.UI),
    NO_SUCH_DEVICE(14, "DCR.noSuchDevice", "No device with id {0}"),
    DEVICE_DOES_NOT_MATCH_CONFIG(15, "DCR.deviceDoesNotMatchConfig", "Device does not match device configuration"),
    NO_SUCH_CONNECTION_TASK(16, "DCR.NoSuchConnectionTask", "No such connection task"),
    ;

    private final int number;
    private final String key;
    private final String format;
    private final Layer layer;

    private MessageSeeds(int number, String key, String format) {
        this(number, key, format, Layer.REST);
    }

    private MessageSeeds(int number, String key, String format, Layer layer) {
        this.number = number;
        this.key = stripComponentNameIfPresent(key);
        this.format = format;
        this.layer = layer;
    }

    private String stripComponentNameIfPresent(String key) {
        if (key.startsWith(DeviceConfigurationApplication.COMPONENT_NAME+".")) {
            return key.substring(DeviceConfigurationApplication.COMPONENT_NAME.length()+1);
        } else {
            return key;
        }
    }
    
    @Override
    public String getModule() {
        return DeviceConfigurationApplication.COMPONENT_NAME;
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
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    public Layer getLayer() {
        return layer;
    }
}
