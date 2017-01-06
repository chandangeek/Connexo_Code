package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 6/01/2017
 * Time: 10:48
 */
@Component(name = "com.energyict.mdc.device.topology.DeviceLifeCycleStatusLayer", service = GraphLayer.class, immediate = true)
public class DeviceLifeCycleStatusLayer extends AbstractGraphLayer {

    private DeviceService deviceService;
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    private final static String NAME = "topology.GraphLayer.DeviceLifeCycleStatu";

    public enum PropertyNames implements TranslationKey {
        ISSUE_COUNT("deviceLifecycleStatus", "Device life cycle status");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat) {
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return NAME + ".node." + propertyName;    //topology.graphLayer.DeviceLifeCycleStatu.node.xxxx
        }

        public String getPropertyName() {
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
        return NAME;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Override
    public Map<String, Object> getProperties(NodeInfo info) {
        Optional<Device> device = deviceService.findDeviceById(info.getId());
        if (device.isPresent())
            setDeviceLifecycle(new DeviceLifeCycleStateInfo(deviceLifeCycleConfigurationService, null, device.get().getState()));
        return propertyMap();
    }

    private void setDeviceLifecycle(DeviceLifeCycleStateInfo deviceLifeCycleStateInfo) {
        if (deviceLifeCycleStateInfo != null)
            this.setProperty(PropertyNames.ISSUE_COUNT.getPropertyName(), deviceLifeCycleStateInfo);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }
}