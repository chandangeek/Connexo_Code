package com.energyict.mdc.rest.impl;

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

    public DeviceCommunicationProtocolInfo(SimpleDeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.name = deviceProtocolPluggableClass.getPluggableClass().getName();
        this.javaClassName = deviceProtocolPluggableClass.getPluggableClass().getJavaClassName();
        this.id = deviceProtocolPluggableClass.getPluggableClass().getId();
        this.deviceProtocolVersion = deviceProtocolPluggableClass.getDeviceProtocol().getVersion();
    }

    public PluggableClassShadow asShadow() {
        PluggableClassShadow shadow = new PluggableClassShadow();
        shadow.setName(this.name);
        shadow.setJavaClassName(this.javaClassName);
        shadow.setPluggableType(PluggableClassType.DEVICEPROTOCOL);
        return shadow;
    }
}
