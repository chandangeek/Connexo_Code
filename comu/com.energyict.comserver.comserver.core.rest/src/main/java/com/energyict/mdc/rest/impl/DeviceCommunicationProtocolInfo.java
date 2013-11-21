package com.energyict.mdc.rest.impl;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dynamicattributes.AttributeValueSelectionMode;
import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.rest.impl.properties.MdcPropertyUtils;
import com.energyict.mdc.rest.impl.properties.PredefinedPropertyValuesInfo;
import com.energyict.mdc.rest.impl.properties.PropertyInfo;
import com.energyict.mdc.rest.impl.properties.PropertySelectionMode;
import com.energyict.mdw.core.PluggableClassType;
import com.energyict.mdw.shadow.PluggableClassShadow;
import com.google.common.base.Optional;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

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
    public PropertyInfo[] propertyInfos;

    public DeviceCommunicationProtocolInfo() {
    }

    public DeviceCommunicationProtocolInfo(final UriInfo uriInfo, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
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
        List<PropertySpec> optionalProperties = deviceProtocolPluggableClass.getDeviceProtocol().getOptionalProperties();
        List<PropertySpec> requiredProperties = deviceProtocolPluggableClass.getDeviceProtocol().getRequiredProperties();
        TypedProperties properties = deviceProtocolPluggableClass.getProperties();
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        MdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, optionalProperties, properties, propertyInfoList);
        MdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, requiredProperties, properties, propertyInfoList);
        this.propertyInfos = propertyInfoList.toArray(new PropertyInfo[propertyInfoList.size()]);
    }

    public PluggableClassShadow asShadow() {
        PluggableClassShadow shadow = new PluggableClassShadow();
        shadow.setName(this.name);
        shadow.setJavaClassName(this.javaClassName);
        shadow.setPluggableType(PluggableClassType.DEVICEPROTOCOL);
        return shadow;
    }
}
