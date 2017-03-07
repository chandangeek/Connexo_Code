/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DeviceDiscoveryProtocolInfo {

    public long id;
    public String name;
    public String javaClassName;
    public String deviceProtocolVersion;
    public long version;
    public List<PropertyInfo> properties;

    public DeviceDiscoveryProtocolInfo() {
    }

    public DeviceDiscoveryProtocolInfo(InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
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
        this.properties = new ArrayList<>();
        List<PropertySpec> propertySpecs = inboundDeviceProtocolPluggableClass.getInboundDeviceProtocol().getPropertySpecs();
        TypedProperties typedProperties = inboundDeviceProtocolPluggableClass.getProperties(propertySpecs);
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, this.properties);
    }

}