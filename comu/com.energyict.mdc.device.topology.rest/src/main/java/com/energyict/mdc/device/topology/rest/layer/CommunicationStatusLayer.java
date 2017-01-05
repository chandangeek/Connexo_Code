package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 5/01/2017
 * Time: 10:44
 */
@Component(name = "com.energyict.mdc.device.topology.CommunicationStatusLayer", service = GraphLayer.class, immediate = true)
public class CommunicationStatusLayer extends AbstractGraphLayer {

    private Clock clock;
    private DeviceService deviceService;
    private TopologyService topologyService;

    private final static String NAME = "topology.GraphLayer.Links.linkQuality";

    public enum PropertyNames implements TranslationKey {
        COMMUNICATION_STATUS("failedCommunications", "Failed communications");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat){
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return NAME + propertyName;    //topology.graphLayer.deviceInfo.node.xxxx
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
        return NAME;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void checkCommunicationStatus(NodeInfo info){
        Optional<Device> device = deviceService.findDeviceById(info.getId());
        device.ifPresent((d) -> setFailedCommunications(this.topologyService.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType.COMMUNICATION_FAILURE, d,
                Interval.of(Range.lessThan(clock.instant())))));
    }

    @Override
    public Map<String, Object> getProperties(NodeInfo info) {
        checkCommunicationStatus(info);
        return propertyMap();
    }

    public void setFailedCommunications(int errors){
        this.setProperty(PropertyNames.COMMUNICATION_STATUS.getPropertyName(), ""+errors);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }

}
