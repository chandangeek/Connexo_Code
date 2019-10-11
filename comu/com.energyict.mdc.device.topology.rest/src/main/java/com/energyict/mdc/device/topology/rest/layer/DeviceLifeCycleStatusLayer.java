package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.device.data.Device;
import com.elster.jupiter.metering.DefaultState;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Gathers the device life cycle state of a DeviceNodeInfo
 * Copyrights EnergyICT
 * Date: 6/01/2017
 * Time: 10:48
 */
@Component(name = "com.energyict.mdc.device.topology.DeviceLifeCycleStatusLayer", service = GraphLayer.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceLifeCycleStatusLayer extends AbstractGraphLayer<Device> {

    private MeteringTranslationService meteringTranslationService;

    public enum PropertyNames implements TranslationKey {
        LIFECYCLE_STATUS("deviceLifecycleStatus", "Device life cycle status");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat) {
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return LayerNames.DeviceLifeCycleStatusLayer.fullName() + ".node." + propertyName;    //topology.graphLayer.DeviceLifeCycleStatu.node.xxxx
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
        return LayerNames.DeviceLifeCycleStatusLayer.fullName();
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    @Override
    public Map<String, Object> getProperties(NodeInfo<Device> info) {
        Device device = ((DeviceNodeInfo) info).getDevice();
        State state = device.getState();
        setDeviceLifecycleState(DefaultState.from(state).map(meteringTranslationService::getDisplayName).orElseGet(state::getName));
        return propertyMap();
    }

    private void setDeviceLifecycleState(String stateName) {
        if (stateName != null)
            this.setProperty(PropertyNames.LIFECYCLE_STATUS.getPropertyName(), stateName);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }
}