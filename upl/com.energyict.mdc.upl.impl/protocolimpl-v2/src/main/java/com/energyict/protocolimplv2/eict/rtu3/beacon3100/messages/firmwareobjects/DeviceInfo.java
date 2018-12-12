package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects;

import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.properties.TypedProperties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 13/08/2015 - 15:13
 */
@XmlRootElement
public class DeviceInfo {

    private TypedProperties generalProperties;
    private TypedProperties dialectProperties;
    private DeviceMasterDataExtractor.SecurityPropertySet securityPropertySet;
    private List<DeviceMasterDataExtractor.SecurityProperty> securityProperties;
    private long deviceID;

    public DeviceInfo(TypedProperties generalProperties, TypedProperties dialectProperties, DeviceMasterDataExtractor.SecurityPropertySet securityPropertySet, List<DeviceMasterDataExtractor.SecurityProperty> securityProperties, long deviceID) {
        this.generalProperties = generalProperties;
        this.dialectProperties = dialectProperties;
        this.securityPropertySet = securityPropertySet;
        this.securityProperties = securityProperties;
        this.deviceID = deviceID;
    }

    private DeviceInfo() {
        //JSon only
    }

    @XmlAttribute
    public long getDeviceID() {
        return deviceID;
    }

    @XmlAttribute
    public TypedProperties getGeneralProperties() {
        return generalProperties;
    }

    @XmlAttribute
    public TypedProperties getDialectProperties() {
        return dialectProperties;
    }

    @XmlAttribute
    public DeviceMasterDataExtractor.SecurityPropertySet getSecurityPropertySet() {
        return securityPropertySet;
    }

    @XmlAttribute
    public List<DeviceMasterDataExtractor.SecurityProperty> getSecurityProperties() {
        return securityProperties;
    }
}