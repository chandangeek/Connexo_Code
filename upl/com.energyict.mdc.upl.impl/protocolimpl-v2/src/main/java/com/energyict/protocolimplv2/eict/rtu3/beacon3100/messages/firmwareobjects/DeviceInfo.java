package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects;

import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.SecurityProperty;

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
    private List<SecurityProperty> securityProperties;
    private long deviceID;

    public DeviceInfo(TypedProperties generalProperties, TypedProperties dialectProperties, List<SecurityProperty> securityProperties, long deviceID) {
        this.generalProperties = generalProperties;
        this.dialectProperties = dialectProperties;
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
    public List<SecurityProperty> getSecurityProperties() {
        return securityProperties;
    }
}