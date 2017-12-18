package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.geo.Latitude;
import com.elster.jupiter.util.geo.Longitude;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import org.osgi.service.component.annotations.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GraphLayer - Device Type Info
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 14:34
 */
@Component(name = "com.energyict.mdc.device.topology.DeviceType", service = GraphLayer.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceTypeLayer extends AbstractGraphLayer<Device> {

    public final static String NAME = "topology.GraphLayer.DeviceType";

    private BigDecimal latitude;
    private BigDecimal longitude;
    private SpatialCoordinates parentCoordinates;

    public enum PropertyNames implements TranslationKey {
        DEVICE_TYPE("deviceType", "Device type"),
        DEVICE_CONFIGURATION("deviceConfiguration", "Device configuration"),
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
            return LayerNames.DeviceTypeLayer.fullName() + ".node." + propertyName;    //topology.graphLayer.deviceInfo.node.xxxx
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
        return LayerNames.DeviceTypeLayer.fullName();
    }

    public void setDeviceType(String deviceTypeName) {
        setProperty(PropertyNames.DEVICE_TYPE.getPropertyName(), deviceTypeName);
    }

    public void setDeviceConfiguration(String deviceConfigurationName) {
        setProperty(PropertyNames.DEVICE_CONFIGURATION.getPropertyName(), deviceConfigurationName);
    }

    private void setDeviceCoordinates(SpatialCoordinates deviceCoordinates) {
        setProperty(PropertyNames.DEVICE_COORDINATES.getPropertyName(), deviceCoordinates);
    }

    private void setDeviceCoordinatesPresents(Boolean hasCoordinates) {
        setProperty(PropertyNames.HAS_COORDONATES.getPropertyName(), hasCoordinates);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }

    public Map<String, Object> getProperties(NodeInfo<Device> nodeInfo) {
        Device device = ((DeviceNodeInfo) nodeInfo).getDevice();
        this.setDeviceType(device.getDeviceType().getName());
        this.setDeviceConfiguration(device.getDeviceConfiguration().getName());
        Device parent = nodeInfo.getParent();
        if (parent != null && parentCoordinates == null) {
            parentCoordinates = parent.getSpatialCoordinates().get();
        }
        Optional<SpatialCoordinates> spatialCoordinates = device.getSpatialCoordinates();
        if (spatialCoordinates.isPresent()) {
            setDeviceCoordinates(spatialCoordinates.get());
            setDeviceCoordinatesPresents(true);
        } else {
            setDeviceCoordinates(getCoordinatesInCloseProximityWithParentNode(parentCoordinates));
            setDeviceCoordinatesPresents(false);
        }
        return propertyMap();
    }

    private SpatialCoordinates getCoordinatesInCloseProximityWithParentNode(SpatialCoordinates coordinates) {
        if (latitude == null) {
            latitude = coordinates.getLatitude().getValue();
        }
        if (longitude == null) {
            longitude = coordinates.getLongitude().getValue();
        }
        latitude = latitude.add(new BigDecimal(0.5));
        longitude = longitude.subtract(new BigDecimal(0.5));
        return new SpatialCoordinates(new Latitude(latitude), new Longitude(longitude), coordinates.getElevation());
    }

}
