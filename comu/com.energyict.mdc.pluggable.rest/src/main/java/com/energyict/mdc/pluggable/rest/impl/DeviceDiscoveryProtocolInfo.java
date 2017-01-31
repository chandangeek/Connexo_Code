/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceDiscoveryProtocolInfo {

    public long id;
    public String name;
    public String javaClassName;
    public String deviceProtocolVersion;
    public long version;

    public DeviceDiscoveryProtocolInfo() {
    }

    public DeviceDiscoveryProtocolInfo(InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass) {
        this.name = inboundDeviceProtocolPluggableClass.getName();
        this.javaClassName = inboundDeviceProtocolPluggableClass.getJavaClassName();
        this.id = inboundDeviceProtocolPluggableClass.getId();
        try {
            this.deviceProtocolVersion = inboundDeviceProtocolPluggableClass.getVersion();
        } catch (Exception e) {
            //TODO, just logging this as we are working with a protocols OSGI bundle which doesn't contain all protocols yet!
            e.printStackTrace(System.err);
            this.deviceProtocolVersion = "*** PROTOCOL NOT YET SUPPORTED IN THE CURRENT OSGI BUNDLE ***";
        }
        this.version = inboundDeviceProtocolPluggableClass.getEntityVersion();
    }

}