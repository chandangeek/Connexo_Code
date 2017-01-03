package com.energyict.mdc.device.topology.rest.info;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

/**
 * GraphLayer - Device Info
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 14:34
 */
public class DeviceInfoLayer extends AbstractGraphLayer {

    private final static String NAME = "topology.graphLayer.Nodes.deviceInfo";

    public enum PropertyNames implements TranslationKey {
        DEVICE_NAME("topology.graphLayer.Nodes.deviceInfo.name", "Name"),
        DEVICE_SERIAL("topology.graphLayer.Nodes.deviceInfo.serialNumber", "Serial number"),
        DEVICE_TYPE("topology.graphLayer.Nodes.deviceInfo.deviceType", "Device type"),
        DEVICE_CONFIGURATION("topology.graphLayer.Nodes.deviceInfo.deviceConfiguration", "Device configuration");

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

    public DeviceInfoLayer(){}

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

}
