package com.energyict.mdc.device.topology.rest.layer;

/**
 * Copyrights EnergyICT
 * Date: 30/08/2017
 * Time: 11:15
 */
public enum LayerNames {
    CommunciationStatusLayer("topology.GraphLayer.CommunicationStatus"),
    DeviceInfoLayer("topology.GraphLayer.DeviceInfo"),
    DeviceLifeCycleStatusLayer("topology.GraphLayer.DeviceLifeCycleStatus"),
    DeviceSummaryExtraInfoLayer("topology.GraphLayer.DeviceSummary"),
    DeviceTypeLayer("topology.GraphLayer.DeviceType"),
    IssuesAndAlarmLayer("topology.GraphLayer.IssuesAndAlarms"),
    LinkQualityLayer("topology.GraphLayer.linkQuality"),
    DeviceGeoCoordinatesLayer("topology.GraphLayer.DeviceGeoCoordinatesLayer"),
    PLCDetailsLayer("topology.GraphLayer.PLCDetailsLayer");

    private String fullName; //Can be used as translation key

    LayerNames(String fullName){
        this.fullName = fullName;
    }

    public String fullName(){
        return fullName;
    }
}
