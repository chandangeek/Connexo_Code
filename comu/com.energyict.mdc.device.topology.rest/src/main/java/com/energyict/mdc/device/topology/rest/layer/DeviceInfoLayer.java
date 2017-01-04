package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GraphLayer - Device Info
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 14:34
 */
@Component(name = "com.energyict.mdc.device.topology.DeviceInfoLayer", service = GraphLayer.class, immediate = true)
public class DeviceInfoLayer extends AbstractGraphLayer {

    private DeviceService deviceService;
    private final static String NAME = "topology.graphLayer.deviceInfo";

    public enum PropertyNames implements TranslationKey {
        DEVICE_NAME("name", "Name"),
        DEVICE_SERIAL("serialNumber", "Serial number"),
        DEVICE_TYPE("deviceType", "Device type"),
        DEVICE_CONFIGURATION("deviceConfiguration", "Device configuration");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat){
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return NAME + ".node." + propertyName;    //topology.graphLayer.deviceInfo.node.xxxx
        }

        public String getPropertyName(){
            return propertyName;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }

    public DeviceInfoLayer(){
        super();
    }

    @Reference
    public void setDeviceService(DeviceService deviceService){
        this.deviceService = deviceService;
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
        setProperty(PropertyNames.DEVICE_NAME.getPropertyName(), name);
    }

    public void setDeviceType(String deviceTypeName){
        setProperty(PropertyNames.DEVICE_TYPE.getPropertyName(), deviceTypeName);
    }

    public void setSerialNumber(String serialNumber){
        setProperty(PropertyNames.DEVICE_SERIAL.getPropertyName(), serialNumber);
    }

    public void setDeviceConfiguration(String deviceConfigurationName){
        setProperty(PropertyNames.DEVICE_CONFIGURATION.getPropertyName(), deviceConfigurationName);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }

    public Map<String, Object> getProperties(NodeInfo nodeInfo) {
        Optional<Device> device = deviceService.findDeviceById(nodeInfo.getId());
        if (device.isPresent()) {
            this.setDeviceName(device.get().getName());
            this.setSerialNumber(device.get().getSerialNumber());
            this.setDeviceType(device.get().getDeviceType().getName());
            this.setDeviceConfiguration(device.get().getDeviceConfiguration().getName());
        }
        return propertyMap();
    }

}
