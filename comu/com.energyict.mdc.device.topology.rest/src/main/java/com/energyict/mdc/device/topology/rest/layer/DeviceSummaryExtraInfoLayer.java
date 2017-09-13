package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;
import com.energyict.mdc.tasks.ComTask;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Layer used to initialize additional properties shown in the 'Device Summary' panel of the graph
 * Copyrights EnergyICT
 * Date: 25/08/2017
 * Time: 12:03
 */
@Component(name = "com.energyict.mdc.device.topology.DeviceSummaryExtraInfo", service = GraphLayer.class, immediate = true)
public class DeviceSummaryExtraInfoLayer extends AbstractGraphLayer<Device> {

    public enum PropertyNames implements TranslationKey {
        FAILED_COM_TASKS("failedComTasks", "Failed Communication tasks");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat) {
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return LayerNames.DeviceSummaryExtraInfoLayer.fullName() + ".node." + propertyName;    //topology.graphLayer.deviceInfo.node.xxxx
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
        return LayerNames.DeviceSummaryExtraInfoLayer.fullName();
    }

    @Override
    public Map<String, Object> getProperties(NodeInfo<Device> info) {
        Device device = ((DeviceNodeInfo) info).getDevice();
        List<String> failedComTaskNames = device.getComTaskExecutions().stream().filter(ComTaskExecution::isLastExecutionFailed).map(ComTaskExecution::getComTask).map(ComTask::getName).collect(Collectors.toList());
        if (!failedComTaskNames.isEmpty()) {
            setProperty(PropertyNames.FAILED_COM_TASKS.getPropertyName(), failedComTaskNames);
        } else {
            setProperty(PropertyNames.FAILED_COM_TASKS.getPropertyName(), null);
        }
        return propertyMap();
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }
}
