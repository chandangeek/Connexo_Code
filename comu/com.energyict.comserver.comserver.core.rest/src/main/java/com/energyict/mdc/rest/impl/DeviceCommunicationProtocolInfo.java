package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdw.core.PluggableClassType;
import com.energyict.mdw.shadow.PluggableClassShadow;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 11:21
 */
@XmlRootElement
public class DeviceCommunicationProtocolInfo {

    public int id;
    public String name;
    public String javaClassName;
    public String deviceProtocolVersion;

    public DeviceCommunicationProtocolInfo() {
    }

    public DeviceCommunicationProtocolInfo(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.name = deviceProtocolPluggableClass.getName();
        this.javaClassName = deviceProtocolPluggableClass.getJavaClassName();
        this.id = deviceProtocolPluggableClass.getId();
        try {
            this.deviceProtocolVersion = deviceProtocolPluggableClass.getDeviceProtocol().getVersion();
        } catch (Exception e) {
            //TODO, just logging this as we are working with a protocols OSGI bundle which doesn't contain all protocols yet!
            e.printStackTrace(System.err);
            this.deviceProtocolVersion = "*** PROTOCOL NOT SUPPORTED IN THE CURRENT OSGI BUNDLE YET ***";
        }
    }

    public PluggableClassShadow asShadow() {
        PluggableClassShadow shadow = new PluggableClassShadow();
        shadow.setName(this.name);
        shadow.setJavaClassName(this.javaClassName);
        shadow.setPluggableType(PluggableClassType.DEVICEPROTOCOL);
        return shadow;
    }
}
