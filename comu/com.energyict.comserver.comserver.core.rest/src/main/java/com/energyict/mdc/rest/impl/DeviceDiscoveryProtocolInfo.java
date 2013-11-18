package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.inbound.InboundDeviceProtocolPluggableClass;
import com.energyict.mdw.core.PluggableClassType;
import com.energyict.mdw.shadow.PluggableClassShadow;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:13
 */
@XmlRootElement
public class DeviceDiscoveryProtocolInfo {

    public int id;
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
            this.deviceProtocolVersion = "*** NOT SUPPORTED IN THE CURRENT OSGI BUNDLE ***";
        }
    }

    public PluggableClassShadow asShadow() {
        PluggableClassShadow shadow = new PluggableClassShadow();
        shadow.setName(this.name);
        shadow.setJavaClassName(this.javaClassName);
        shadow.setPluggableType(PluggableClassType.DISCOVERYPROTOCOL);
        return shadow;
    }
}
