package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerCalculationMode;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * GraphLayer - Device Info: general information about a Device
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 14:34
 */
@Component(name = "com.energyict.mdc.device.topology.DeviceInfoLayer", service = GraphLayer.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceInfoLayer extends AbstractGraphLayer<Device> {

    public enum PropertyNames implements TranslationKey {
        DEVICE_NAME("name", "Name"),
        DEVICE_SERIAL("serialNumber", "Serial number");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat){
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return LayerNames.DeviceInfoLayer.fullName() + ".node." + propertyName;    //topology.graphLayer.deviceInfo.node.xxxx
        }

        public String getPropertyName(){
            return propertyName;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }

    @Override
    public GraphLayerType getType() {
        return GraphLayerType.NODE;
    }

    @Override
    public String getName() {
        return LayerNames.DeviceInfoLayer.fullName();
    }

    @Override
    public GraphLayerCalculationMode getCalculationMode() {
        return GraphLayerCalculationMode.IMMEDIATE;
    }

    public void setDeviceName(String name){
        setProperty(PropertyNames.DEVICE_NAME.getPropertyName(), name);
    }

    public void setSerialNumber(String serialNumber){
        setProperty(PropertyNames.DEVICE_SERIAL.getPropertyName(), serialNumber);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }

    public Map<String, Object> getProperties(NodeInfo<Device> nodeInfo) {
        Device device = ((DeviceNodeInfo) nodeInfo).getDevice();
        this.setDeviceName(device.getName());
        this.setSerialNumber(device.getSerialNumber());
        return propertyMap();
    }

}
