package com.energyict.mdc.device.topology.rest.impl;


import com.energyict.mdc.device.topology.rest.info.GeoLocationConfigurationInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

@RunWith(MockitoJUnitRunner.class)
public class GeoLocationConfigurationPropertiesTest {

    private final String GEOLOCATION_IS_ENABLED = "com.elster.jupiter.geolocation.isGeolocationEnabled";
    private final String GEOLOCATION_TILE_LAYER = "com.elster.jupiter.geolocation.tileLayer";
    private final String GEOLOCATION_TYPE = "com.elster.jupiter.geolocation.type";
    private final String GEOLOCATION_MAX_ZOOM = "com.elster.jupiter.geolocation.maxZoom";

    @Mock
    private BundleContext bundleContext;

    @Test
    public void testGeoLocationConfigurationInfoIsBuildFromProperties() {
        String tileLayerProperty = "tileLayerProperty";
        String geoLocationTypeProperty = "geoLocationTypeProperty";
        String maxZoomProperty = "18";
        String trueProperty = "true";
        when(bundleContext.getProperty(GEOLOCATION_IS_ENABLED)).thenReturn(trueProperty);
        when(bundleContext.getProperty(GEOLOCATION_TYPE)).thenReturn(geoLocationTypeProperty);
        when(bundleContext.getProperty(GEOLOCATION_TILE_LAYER)).thenReturn(tileLayerProperty);
        when(bundleContext.getProperty(GEOLOCATION_MAX_ZOOM)).thenReturn(maxZoomProperty);
        GeoLocationConfigurationProperties geoLocationConfigurationProperties = new GeoLocationConfigurationProperties(bundleContext);
        GeoLocationConfigurationInfo geoLocationConfigurationInfo = geoLocationConfigurationProperties.buildGeoLocationConfigurationInfo();
        assertEquals("Geolocation layer should be set", tileLayerProperty, geoLocationConfigurationInfo.getGeolocationTileLayer());
        assertEquals("Geolocation type property should be set", geoLocationTypeProperty, geoLocationConfigurationInfo.getGeolocationType());
        assertEquals("Geolocation maxZoom property should be set", maxZoomProperty, geoLocationConfigurationInfo.getMaxZoom());
        assertEquals("Geolocation isLocationEnabled should be enabled", true, geoLocationConfigurationInfo.isGeolocationEnabled());

    }

    @Test
    public void testGeoLocationConfigInfoShouldReturnEmptyStringIfPropertyDoesNotExist() {
        String tileLayerProperty = "tileLayerProperty";
        when(bundleContext.getProperty(GEOLOCATION_IS_ENABLED)).thenReturn(null);
        when(bundleContext.getProperty(GEOLOCATION_TYPE)).thenReturn(null);
        when(bundleContext.getProperty(GEOLOCATION_TILE_LAYER)).thenReturn(tileLayerProperty);
        when(bundleContext.getProperty(GEOLOCATION_MAX_ZOOM)).thenReturn(null);
        GeoLocationConfigurationProperties geoLocationConfigurationProperties = new GeoLocationConfigurationProperties(bundleContext);
        GeoLocationConfigurationInfo geoLocationConfigurationInfo = geoLocationConfigurationProperties.buildGeoLocationConfigurationInfo();
        assertFalse(geoLocationConfigurationInfo.getGeolocationTileLayer().isEmpty());
        assertTrue(geoLocationConfigurationInfo.getGeolocationType().isEmpty());
        assertTrue(geoLocationConfigurationInfo.getMaxZoom().isEmpty());
        assertFalse(geoLocationConfigurationInfo.isGeolocationEnabled());
    }
}
