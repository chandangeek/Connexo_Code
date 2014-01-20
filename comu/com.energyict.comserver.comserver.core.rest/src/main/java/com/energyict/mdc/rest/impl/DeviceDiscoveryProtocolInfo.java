package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:13
 */
@XmlRootElement
public class DeviceDiscoveryProtocolInfo {

    public long id;
    public String name;
    public String javaClassName;
    public String deviceProtocolVersion;

    public DeviceDiscoveryProtocolInfo() {
    }

    public DeviceDiscoveryProtocolInfo(InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass) {
        this.name = inboundDeviceProtocolPluggableClass.getName();
        this.javaClassName = inboundDeviceProtocolPluggableClass.getJavaClassName();
        this.id = inboundDeviceProtocolPluggableClass.getId();
        try {
            this.deviceProtocolVersion = inboundDeviceProtocolPluggableClass.getInboundDeviceProtocol().getVersion();
        } catch (Exception e) {
            //TODO, just logging this as we are working with a protocols OSGI bundle which doesn't contain all protocols yet!
            e.printStackTrace(System.err);
            this.deviceProtocolVersion = "*** DISCOVERY PROTOCOL NOT SUPPORTED IN THE CURRENT OSGI BUNDLE YET ***";
        }
    }

}