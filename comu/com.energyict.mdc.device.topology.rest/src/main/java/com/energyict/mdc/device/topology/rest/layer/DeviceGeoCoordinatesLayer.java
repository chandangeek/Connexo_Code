/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import org.osgi.service.component.annotations.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.device.topology.DeviceGeoCoordinatesLayer", service = GraphLayer.class, immediate = true)
public class DeviceGeoCoordinatesLayer extends AbstractGraphLayer<Device> {

    private enum PropertyNames implements TranslationKey {
        HAS_COORDONATES("hasCoordonates", "Has coordonates"),
        DEVICE_COORDINATES("deviceCoordinates", "Device coordinates");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat) {
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return LayerNames.DeviceGeoCoordinatesLayer.fullName();    //topology.graphLayer.deviceInfo.node.xxxx
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
    public List<TranslationKey> getKeys() {
        return null;
    }

    @Override
    public GraphLayerType getType() {
        return GraphLayerType.NODE;
    }

    @Override
    public String getName() {
        return LayerNames.DeviceGeoCoordinatesLayer.fullName();
    }

    @Override
    public Map<String, Object> getProperties(NodeInfo<Device> nodeInfo) {
        Device device = ((DeviceNodeInfo) nodeInfo).getDevice();
        Optional<SpatialCoordinates> spatialCoordinates = device.getSpatialCoordinates();
        if (spatialCoordinates.isPresent()) {
            setDeviceCoordinates(spatialCoordinates.get());
            setDeviceCoordinatesPresents(true);
        } else {
            setDeviceCoordinates(nodeInfo.getCoordinates());
            setDeviceCoordinatesPresents(false);
        }
        return propertyMap();
    }

    private void setDeviceCoordinates(SpatialCoordinates deviceCoordinates) {
        setProperty(PropertyNames.DEVICE_COORDINATES.getPropertyName(), deviceCoordinates);
    }

    private void setDeviceCoordinatesPresents(Boolean hasCoordinates) {
        setProperty(PropertyNames.HAS_COORDONATES.getPropertyName(), hasCoordinates);
    }

}
