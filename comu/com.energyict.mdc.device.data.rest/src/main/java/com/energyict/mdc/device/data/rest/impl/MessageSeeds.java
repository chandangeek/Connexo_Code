package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.configuration.rest.impl.DeviceConfigurationApplication;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_SUCH_DEVICE(14, "DDR.noSuchDevice", "No device with mrId {0}"),
    DEVICE_DOES_NOT_MATCH_CONFIG(15, "DDR.deviceDoesNotMatchConfig", "Device does not match device configuration"),
    NO_SUCH_PARTIAL_CONNECTION_TASK(16, "DDR.NoSuchPartialConnectionTask", "No such connection method on device config"),
    NO_SUCH_CONNECTION_METHOD(17, "DDR.NoSuchConnectionTask" , "Device {0} has no connection method {1}"),
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
