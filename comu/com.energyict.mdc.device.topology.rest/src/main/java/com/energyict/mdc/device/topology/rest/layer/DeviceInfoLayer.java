package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

import java.util.Arrays;
import java.util.List;

/**
 * GraphLayer - Device Info
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 14:34
 */
public class DeviceInfoLayer extends AbstractGraphLayer {

    private final static String NAME = "topology.graphLayer.deviceInfo";

    public enum PropertyNames implements TranslationKey {
        DEVICE_NAME(NAME + "node.name", "Name"),
        DEVICE_SERIAL(NAME + "node.serialNumber", "Serial number"),
        DEVICE_TYPE(NAME + "node.deviceType", "Device type"),
        DEVICE_CONFIGURATION(NAME + "node.deviceConfiguration", "Device configuration");

        private String key;
        private String defaultFormat;

        PropertyNames(String key, String defaultFormat){
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }

    public DeviceInfoLayer(Device device){
        super();
        this.setDeviceName(device.getName());
        this.setSerialNumber(device.getSerialNumber());
        this.setDeviceType(device.getDeviceType().getName());
        this.setDeviceConfiguration(device.getDeviceConfiguration().getName());
    }

    @Override
    public GraphLayerType getType() {
        return GraphLayerType.NODE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void setDeviceName(String name){
        setProperty(PropertyNames.DEVICE_NAME.getDefaultFormat(), name);
    }

    public void setDeviceType(String deviceTypeName){
        setProperty(PropertyNames.DEVICE_TYPE.getDefaultFormat(), deviceTypeName);
    }

    public void setSerialNumber(String serialNumber){
        setProperty(PropertyNames.DEVICE_SERIAL.getDefaultFormat(), serialNumber);
    }

    public void setDeviceConfiguration(String deviceConfigurationName){
        setProperty(PropertyNames.DEVICE_CONFIGURATION.getDefaultFormat(), deviceConfigurationName);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }

}
