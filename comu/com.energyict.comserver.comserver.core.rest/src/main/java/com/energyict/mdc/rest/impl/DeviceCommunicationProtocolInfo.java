package com.energyict.mdc.rest.impl;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.rest.impl.properties.MdcPropertyUtils;
import com.energyict.mdc.rest.impl.properties.PropertyInfo;
import com.energyict.mdc.rest.impl.properties.PropertyValueInfo;
import com.energyict.mdw.core.LicensedProtocol;
import com.energyict.mdw.core.PluggableClassType;
import com.energyict.mdw.shadow.PluggableClassShadow;

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
    public LicensedProtocolInfo licensedProtocol;
    public String deviceProtocolVersion;
    public PropertyInfo[] propertyInfos;

    public DeviceCommunicationProtocolInfo() {
    }

    public DeviceCommunicationProtocolInfo(final UriInfo uriInfo, DeviceProtocolPluggableClass deviceProtocolPluggableClass, LicensedProtocol licensedProtocol, boolean embedProperties) {
        this.name = deviceProtocolPluggableClass.getName();
        this.id = deviceProtocolPluggableClass.getId();
        try {
            this.deviceProtocolVersion = deviceProtocolPluggableClass.getDeviceProtocol().getVersion();
        } catch (Exception e) {
            //TODO, just logging this as we are working with a protocols OSGI bundle which doesn't contain all protocols yet!
            e.printStackTrace(System.err);
            this.deviceProtocolVersion = "*** PROTOCOL NOT SUPPORTED IN THE CURRENT OSGI BUNDLE YET ***";
        }
        if (licensedProtocol != null) {
            this.licensedProtocol = new LicensedProtocolInfo(licensedProtocol);
        }
        if (embedProperties) {
            List<PropertyInfo> propertyInfoList = createPropertyInfoList(uriInfo, deviceProtocolPluggableClass);
            this.propertyInfos = propertyInfoList.toArray(new PropertyInfo[propertyInfoList.size()]);
        }
    }

    private List<PropertyInfo> createPropertyInfoList(UriInfo uriInfo, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        List<PropertySpec> optionalProperties = deviceProtocolPluggableClass.getDeviceProtocol().getOptionalProperties();
        List<PropertySpec> requiredProperties = deviceProtocolPluggableClass.getDeviceProtocol().getRequiredProperties();
        TypedProperties properties = deviceProtocolPluggableClass.getProperties();
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        MdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, optionalProperties, properties, propertyInfoList);
        MdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, requiredProperties, properties, propertyInfoList);
        return propertyInfoList;
    }

    public PluggableClassShadow asShadow() {
        PluggableClassShadow shadow = new PluggableClassShadow();
        shadow.setName(this.name);
        shadow.setJavaClassName(this.licensedProtocol.protocolJavaClassName);
        shadow.setPluggableType(PluggableClassType.DEVICEPROTOCOL);
        shadow.setProperties(getTypedProperties());
        return shadow;
    }

    private TypedProperties getTypedProperties() {
        TypedProperties typedProperties = TypedProperties.empty();
        if (this.propertyInfos != null) {
            for (PropertyInfo propertyInfo : this.propertyInfos) {
                PropertyValueInfo propertyValueInfo = propertyInfo.getPropertyValueInfo();
                if (propertyValueInfo != null && propertyValueInfo.getValue() != null) {
                    typedProperties.setProperty(propertyInfo.getKey(), propertyValueInfo.getValue());
//                    Object value = propertyValueInfo.getValue();
//                    if (MdcResourceProperty.class.isAssignableFrom(value.getClass())) {
//                        typedProperties.setProperty(propertyInfo.getKey(), ((MdcResourceProperty) value).fromResourceObject());
//                    } else {
//                        typedProperties.setProperty(propertyInfo.getKey(), value);
//                    }
                }
            }
        }
        return typedProperties;
    }
}
