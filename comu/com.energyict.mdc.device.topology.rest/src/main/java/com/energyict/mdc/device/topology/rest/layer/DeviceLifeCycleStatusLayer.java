package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerCalculationMode;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 6/01/2017
 * Time: 10:48
 */
@Component(name = "com.energyict.mdc.device.topology.DeviceLifeCycleStatusLayer", service = GraphLayer.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceLifeCycleStatusLayer extends AbstractGraphLayer<Device> {

    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    private final static String NAME = "topology.GraphLayer.DeviceLifeCycleStatus";
    private final static String DEFAULT_FORMAT = "Status of device life cycle";

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

    public String getDisplayName(Thesaurus thesaurus){
        return thesaurus.getFormat(getTranslatedName()).format();
    }

    private TranslationKey getTranslatedName(){
        return new TranslationKey() {
                    @Override
                    public String getKey() {
                        return NAME;
                    }

                    @Override
                    public String getDefaultFormat() {
                        return DEFAULT_FORMAT;
                    }
                };
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Override
    public Map<String, Object> getProperties(NodeInfo<Device> info) {
        Device device = ((DeviceNodeInfo) info).getDevice();
        State state = device.getState();
        setDeviceLifecycleState(DefaultState.from(state).map(deviceLifeCycleConfigurationService::getDisplayName).orElseGet(state::getName));

        return propertyMap();
    }

    private void setDeviceLifecycleState(String stateName) {
        if (stateName != null)
            this.setProperty(PropertyNames.LIFECYCLE_STATUS.getPropertyName(), stateName);
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.add(getTranslatedName());
        keys.addAll(Arrays.asList(PropertyNames.values()));
        return keys;
    }
}