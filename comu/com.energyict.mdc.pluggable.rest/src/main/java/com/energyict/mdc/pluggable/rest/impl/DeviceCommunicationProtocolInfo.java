package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.pluggable.rest.impl.properties.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.impl.properties.PropertyInfo;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 11:21
 */
@XmlRootElement
public class DeviceCommunicationProtocolInfo {

    public long id;
    public String name;
    public LicensedProtocolInfo licensedProtocol;
    public String deviceProtocolVersion;
    public PropertyInfo[] propertyInfos;

    public DeviceCommunicationProtocolInfo() {
    }

    public DeviceCommunicationProtocolInfo(final UriInfo uriInfo, PropertySpecService propertySpecService, DeviceProtocolPluggableClass deviceProtocolPluggableClass, LicensedProtocol licensedProtocol, boolean embedProperties) {
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
            this.licensedProtocol = new LicensedProtocolInfo(licensedProtocol);
        }
        if (embedProperties) {
            List<PropertyInfo> propertyInfoList = createPropertyInfoList(uriInfo, propertySpecService, deviceProtocolPluggableClass);
            this.propertyInfos = propertyInfoList.toArray(new PropertyInfo[propertyInfoList.size()]);
        }
    }

    private List<PropertyInfo> createPropertyInfoList(UriInfo uriInfo, PropertySpecService propertySpecService, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        List<PropertySpec> propertySpecs = deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs();
        TypedProperties properties = deviceProtocolPluggableClass.getProperties(propertySpecs);
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        MdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecService, propertySpecs, properties, propertyInfoList);
        return propertyInfoList;
    }

    public void copyProperties(DeviceProtocolPluggableClass deviceProtocolPluggableClass) throws ParseException {
        List<PropertySpec> propertySpecs = deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs();
        for (PropertySpec propertySpec : propertySpecs) {
            Object value = MdcPropertyUtils.findPropertyValue(propertySpec, this.propertyInfos);
            if (value == null || value.equals("")) {
                deviceProtocolPluggableClass.removeProperty(propertySpec);
            } else {
                deviceProtocolPluggableClass.setProperty(propertySpec, value);
            }
        }
    }

}
