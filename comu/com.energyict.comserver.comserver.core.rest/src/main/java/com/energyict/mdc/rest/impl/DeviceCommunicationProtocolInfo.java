package com.energyict.mdc.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.PluggableClassType;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.pluggable.PluggableClassShadow;
import com.energyict.mdc.rest.impl.properties.MdcPropertyUtils;
import com.energyict.mdc.rest.impl.properties.MdcResourceProperty;
import com.energyict.mdc.rest.impl.properties.PropertyInfo;
import com.energyict.mdc.rest.impl.properties.PropertyValueInfo;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<PropertySpec> propertySpecs = deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs();
        TypedProperties properties = deviceProtocolPluggableClass.getProperties();
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        MdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, properties, propertyInfoList);
        return propertyInfoList;
    }

    public PluggableClassShadow asShadow() {
        PluggableClassShadow shadow = new PluggableClassShadow(PluggableClassType.DeviceProtocol);
        shadow.setName(this.name);
        shadow.setJavaClassName(this.licensedProtocol.protocolJavaClassName);
        shadow.setProperties(getTypedProperties());
        return shadow;
    }

    private TypedProperties getTypedProperties() {
        TypedProperties typedProperties = TypedProperties.empty();
        if (this.propertyInfos != null) {
            for (PropertyInfo propertyInfo : this.propertyInfos) {
                PropertyValueInfo propertyValueInfo = propertyInfo.getPropertyValueInfo();
                if (propertyValueInfo != null && propertyValueInfo.getValue() != null) {
                    Object value = propertyValueInfo.getValue();
                    if (Map.class.isAssignableFrom(value.getClass())) {
                        Object infoObject = propertyInfo.getPropertyTypeInfo().getSimplePropertyType().getInfoObject((Map<String, Object>) value);
                        if (MdcResourceProperty.class.isAssignableFrom(infoObject.getClass())) {
                            typedProperties.setProperty(propertyInfo.getKey(), ((MdcResourceProperty) infoObject).fromInfoObject());
                        }
                    } else {
                        typedProperties.setProperty(propertyInfo.getKey(), value);
                    }
                }
            }
        }
        return typedProperties;
    }
}
