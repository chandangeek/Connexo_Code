package com.energyict.mdc.device.topology.rest.impl;


import com.energyict.mdc.device.topology.rest.info.GeoLocationConfigurationInfo;
import org.osgi.framework.BundleContext;

import java.util.Objects;

public class GeoLocationConfigurationProperties {

    private final String GEOLOCATION_IS_ENABLED = "com.elster.jupiter.geolocation.isGeolocationEnabled";
    private final String GEOLOCATION_TILE_LAYER = "com.elster.jupiter.geolocation.tileLayer";
    private final String GEOLOCATION_TYPE = "com.elster.jupiter.geolocation.type";
    private final String GEOLOCATION_MAX_ZOOM = "com.elster.jupiter.geolocation.maxZoom";
    private final String EMPTY_STRING = "";

    private BundleContext bundleContext;

    public GeoLocationConfigurationProperties(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public GeoLocationConfigurationInfo buildGeoLocationConfigurationInfo() {
        GeoLocationConfigurationInfo geoLocationConfigurationInfo = new GeoLocationConfigurationInfo();
        geoLocationConfigurationInfo.setGeolocationEnabled(parseToBoolean(getProperty(GEOLOCATION_IS_ENABLED)));
        geoLocationConfigurationInfo.setGeolocationTileLayer(getProperty(GEOLOCATION_TILE_LAYER));
        geoLocationConfigurationInfo.setMaxZoom(getProperty(GEOLOCATION_MAX_ZOOM));
        geoLocationConfigurationInfo.setGeolocationType(getProperty(GEOLOCATION_TYPE));
        return geoLocationConfigurationInfo;
    }

    private String getProperty(String propertyName) {
        return parseProperty(propertyName);
    }

    private String parseProperty(String propertyName) {
        String property = bundleContext.getProperty(propertyName);
        return Objects.isNull(property) ? EMPTY_STRING : property;
    }

    private boolean parseToBoolean(String propertyValue) {
        return Boolean.parseBoolean(propertyValue);
    }
}
