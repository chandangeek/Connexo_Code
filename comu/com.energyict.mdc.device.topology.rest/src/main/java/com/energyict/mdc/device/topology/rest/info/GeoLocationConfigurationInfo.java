package com.energyict.mdc.device.topology.rest.info;


public class GeoLocationConfigurationInfo {


    private boolean hasGeolocationEnabled;
    private String geolocationTileLayer;
    private String geolocationType;
    private String maxZoom;

    public String getGeolocationTileLayer() {
        return geolocationTileLayer;
    }

    public void setGeolocationTileLayer(String geolocationTileLayer) {
        this.geolocationTileLayer = geolocationTileLayer;
    }

    public String getGeolocationType() {
        return geolocationType;
    }

    public void setGeolocationType(String geolocationType) {
        this.geolocationType = geolocationType;
    }

    public String getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(String maxZoom) {
        this.maxZoom = maxZoom;
    }

    public boolean isGeolocationEnabled() {
        return hasGeolocationEnabled;
    }

    public void setGeolocationEnabled(boolean hasGeolocationEnabled) {
        this.hasGeolocationEnabled = hasGeolocationEnabled;
    }
}
