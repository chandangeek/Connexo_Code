/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DeviceCommunicationProtocolInfo {

    public long id;
    public String name;
    public LicensedProtocolInfo licensedProtocol;
    public String deviceProtocolVersion;
    public PropertyInfo[] properties;
    public long version;

    public DeviceCommunicationProtocolInfo() {
    }

    public DeviceCommunicationProtocolInfo(final UriInfo uriInfo, DeviceProtocolPluggableClass deviceProtocolPluggableClass, LicensedProtocol licensedProtocol, boolean embedProperties, MdcPropertyUtils mdcPropertyUtils) {
        this.name = deviceProtocolPluggableClass.getName();
        this.id = deviceProtocolPluggableClass.getId();
        try {
            this.deviceProtocolVersion = deviceProtocolPluggableClass.getVersion();
        } catch (Exception e) {
            //TODO, just logging this as we are working with a protocols OSGI bundle which doesn't contain all protocols yet!
            e.printStackTrace(System.err);
            this.deviceProtocolVersion = "*** PROTOCOL NOT YET SUPPORTED IN THE CURRENT OSGI BUNDLE ***";
        }
        if (licensedProtocol != null) {
            this.licensedProtocol = new LicensedProtocolInfo(licensedProtocol, deviceProtocolPluggableClass.getDeviceProtocol().getProtocolDescription());
        }
        if (embedProperties) {
            List<PropertyInfo> propertyInfoList = createPropertyInfoList(uriInfo, deviceProtocolPluggableClass, mdcPropertyUtils);
            this.properties = propertyInfoList.toArray(new PropertyInfo[propertyInfoList.size()]);
        }
        this.version = deviceProtocolPluggableClass.getEntityVersion();
    }

    private List<PropertyInfo> createPropertyInfoList(UriInfo uriInfo, DeviceProtocolPluggableClass deviceProtocolPluggableClass, MdcPropertyUtils mdcPropertyUtils) {
        List<PropertySpec> propertySpecs = deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs();
        TypedProperties properties = deviceProtocolPluggableClass.getProperties(propertySpecs);
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, properties, propertyInfoList);
        return propertyInfoList;
    }

    public void copyProperties(DeviceProtocolPluggableClass deviceProtocolPluggableClass, MdcPropertyUtils mdcPropertyUtils) {
        List<PropertySpec> propertySpecs = deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs();
        for (PropertySpec propertySpec : propertySpecs) {
            Object value = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
            if (value == null || "".equals(value)) {
                deviceProtocolPluggableClass.removeProperty(propertySpec);
            } else {
                deviceProtocolPluggableClass.setProperty(propertySpec, value);
            }
        }
    }

}
